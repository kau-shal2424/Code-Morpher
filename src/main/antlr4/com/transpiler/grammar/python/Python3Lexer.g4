lexer grammar Python3Lexer;

// Virtual tokens injected by PythonIndentLexer (not matched by rules)
tokens { INDENT, DEDENT }

// Keywords (must appear before IDENTIFIER)
IF     : 'if' ;
PRINT  : 'print' ;
FOR    : 'for' ;
IN     : 'in' ;
RANGE  : 'range' ;
ELSE   : 'else' ;
WHILE  : 'while' ;
DEF    : 'def' ;
RETURN : 'return' ;

// Tokens
IDENTIFIER : [a-zA-Z_][a-zA-Z0-9_]* ;
INTEGER    : [0-9]+ ;

// Operators
ASSIGN : '=' ;
EQEQ   : '==' ;
GT     : '>' ;
LT     : '<' ;
PLUS   : '+' ;
MINUS  : '-' ;
STAR   : '*' ;
SLASH  : '/' ;

// Delimiters
LPAREN : '(' ;
RPAREN : ')' ;
COLON  : ':' ;
COMMA  : ',' ;

STRING
    : '"' (~["\\] | '\\' .)* '"'
    | '\'' (~['\\] | '\\' .)* '\''
    ;

// Whitespace and Newlines
NEWLINE : ( '\r' | '\n' | '\r\n' ) [ \t]* ;
WS      : [ \t]+ -> skip ;
