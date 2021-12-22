This program calculates expressions like: 3 * (-3 + 2)

- Supports operators: +, -, *, / and ^.
- Also supports several entered operators following each other, like: 9 +++ 10 -- 8.
- The main logick of it, is in converting infix expression to postfix form using stack. For example:

Steps of calculation:
1) Parse input string to list using regex: 3 * (-3 + 2) -> [3, *, (, -3, +, 2, )];
2) Convert to postfix form: [3, *, (, -3, +, 2, )] -> 3 -3 2 + *
3) Calculating answer 
