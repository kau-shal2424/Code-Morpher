parser grammar Python3Parser;

options { tokenVocab=Python3Lexer; }

file_input : (stmt | NEWLINE)* EOF ;

stmt
    : if_stmt
    | for_stmt
    | while_stmt
    | func_def
    | return_stmt NEWLINE?
    | simple_stmt NEWLINE?
    ;

func_def : DEF IDENTIFIER LPAREN parameters? RPAREN COLON NEWLINE INDENT stmt+ DEDENT ;

parameters : parameter (COMMA parameter)* ;

parameter : IDENTIFIER ;

return_stmt : RETURN expr? ;

simple_stmt : assignment | expr_stmt ;

assignment : IDENTIFIER ASSIGN expr ;

expr_stmt : expr ;

// For loop: for i in range(n) or for i in range(start, end)
for_stmt
    : FOR IDENTIFIER IN range_expr COLON NEWLINE INDENT stmt+ DEDENT
    ;

range_expr : RANGE LPAREN expr (COMMA expr)? RPAREN ;

// If statement with optional else
if_stmt : IF expr COLON NEWLINE INDENT stmt+ DEDENT
        (ELSE COLON NEWLINE INDENT stmt+ DEDENT)? ;

while_stmt : WHILE expr COLON NEWLINE INDENT stmt+ DEDENT ;

// Operator precedence: comparison > addition > multiplication > atom
expr : comparison ;

comparison
    : addition ((GT | LT | EQEQ) addition)*
    ;

addition
    : multiplication ((PLUS | MINUS) multiplication)*
    ;

multiplication
    : unary ((STAR | SLASH) unary)*
    ;

unary
    : atom
    | func_call
    | LPAREN expr RPAREN
    ;

atom : IDENTIFIER
     | INTEGER
     | STRING
     | LPAREN expr RPAREN
     ;

func_call : (IDENTIFIER | PRINT) LPAREN (expr (COMMA expr)*)? RPAREN ;
