package calculator
import java.math.BigInteger

fun main() {
    val calc = SmartCalculator()
    println("Bye!")
}
class SmartCalculator() {
    private val variables = mutableMapOf<String, String>()
    var mainLoop = true

    inner class Stack() {
        private val stack = mutableListOf<String>()
        fun push(element: String) = stack.add(element)
        fun pop() = stack.removeLast()
        fun peek(): String = stack.last()
        fun isEmpty(): Boolean = stack.isEmpty()
        fun size(): Int = stack.size
        fun contains(element: String): Boolean = stack.contains(element)
        fun print() = println(stack)
    }
    init {
        while (mainLoop) {
            println("Input expression for calculation or type /help or /exit:")
            val input = readLine()!!.replace("\\s+".toRegex(), "")
            if (input.isEmpty()) continue else decisioner(input)
        }
    }
    fun decisioner(input: String) {
        when {
            input.contains("\\B\\/[a-zA-Z]+".toRegex()) -> commander(input)
            input.contains("=") -> checkAssignmentAndAssign(input)
            else -> calculate(input)
        }
    }
    fun commander(input: String) {
        val help = """
            This program calculates things. (Not supports floating point, but! Huge numbers allowed =) )
            Allowed operations: *, /, +, - or ^
            Example of supported expression: (3 + 5) * 2
        """.trimIndent()
        when (input) {
            "/help" -> println(help)
            "/exit" -> mainLoop = false
            else -> println("Unknown command")
        }
    }
    fun checkAssignmentAndAssign(input: String) {
        val list = input.split("=")
        if (!list[0].matches("[a-zA-Z]+".toRegex())) {
            println("Invalid identifier")
            return
        }
        if (list.size != 2 || !list[1].matches("([a-zA-Z]+|-?\\d+)".toRegex())) {
            println("Invalid assignment")
            return
        }
        when {
            list[1].matches("-?\\d+".toRegex()) -> variables[list[0]] = list[1]
            variables.containsKey(list[1]) -> variables[list[0]] = variables[list[1]]!!
            else -> println("Unknown variable")
        }
    }
    fun getVariable(i: String): String {
        if (variables.containsKey(i)) return variables[i]!!
        else throw Exception("Unknown variable")
    }
    fun calculate(input: String) {
        val preparse = preParse(input)
        if(preparse.contains("[error]")) {
            println("Invalid expression")
            return
        }

        val parsedString = parseString(preparse)
        try {
            val postfix = toPostfix(parsedString)
            toAnswer(postfix)
        } catch (e:Exception) {
            println(e.message)
            return
        }
    }
    fun preParse(input: String): String {
        var preParse = input.replace("\\++".toRegex(), "+")
        preParse = preParse.replace("(\\*\\*+|\\/\\/+|\\^\\^+)".toRegex(), "[error]")

        // converting minuses into "+" or "-" ("---" -> "-")
        val minusRegex = """\-\-+""".toRegex()
        while (preParse.contains(minusRegex)) {
            val finded = minusRegex.find(preParse)!!
            val replacer = if(finded.value.length % 2 == 0) "+" else "-"
            preParse = preParse.replace(finded.value, replacer)
        }
        return preParse
    }
    fun parseString(input: String): MutableList<String> {
        val list = mutableListOf<String>()
        val regex = """(?<=[\(\+\*\/\^])-\d+|(?<!(\)|\w))-\d+|\w+|([\+\-\*\/\^])|([\)\(])""".toRegex()
        val find = regex.findAll(input).forEach { list.add(it.value) }
        return list
    }
    fun hasHigherPrec(stackPeek: String, currentOp: String): Boolean {
        fun precedence(operand: String): Int {
            return when(operand) {
                "+","-" -> 1
                "*","/" -> 2
                "^" -> 3
                "(", ")" -> 4
                else -> -1
            }
        }
        return precedence(stackPeek) >= precedence(currentOp)
    }
    fun toPostfix(list: MutableList<String>): MutableList<String> {
        val stack = Stack()
        val output = mutableListOf<String>()
        for (i in list) {
            if(i.matches("-?\\w+".toRegex())) {
                output.add(i)
            } else {
                if (!stack.isEmpty() && hasHigherPrec(stack.peek(), i)) {
                    while (!stack.isEmpty() && stack.peek() != "(") {
                        output.add(stack.peek())
                        stack.pop()
                    }
                }
                if (i == ")") {
                    if (!stack.contains("(")) throw Exception("Invalid expression")
                    while (stack.peek() != "(") {
                        output.add(stack.peek())
                        stack.pop()
                    }
                    stack.pop()
                    continue
                }
                stack.push(i)
            }
        }
        if(stack.contains("(")) throw Exception("Invalid expression")
        while (!stack.isEmpty()) {
            output.add(stack.peek())
            stack.pop()
        }
        return output
    }
    fun toAnswer(list: MutableList<String>) {
        val digit = """-?\d+""".toRegex()
        val letter = """[a-zA-Z]+""".toRegex()
        val stack = Stack()
        for (i in list) {
            when {
                i.matches(letter) -> stack.push(getVariable(i))
                i.matches(digit) -> stack.push(i)
                else -> {
                    if (stack.size() < 2) throw Exception("Only one operand in stack while trying to calculate")
                    val second = BigInteger(stack.peek())
                    stack.pop()
                    val first = BigInteger(stack.peek())
                    stack.pop()
                    when (i) {
                        "+" -> stack.push((first + second).toString())
                        "-" -> stack.push((first - second).toString())
                        "*" -> stack.push((first * second).toString())
                        "/" -> stack.push((first / second).toString())
                        "^" -> stack.push((first.pow(second.toInt())).toString())
                    }
                }
            }
        }
        println(stack.peek())
    }
}