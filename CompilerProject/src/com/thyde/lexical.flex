package com.thyde;

%%
%class Lexer // The class name of the generated Lexical Analyzer class
%unicode
%line
%column
%type Token // The class name of the class returned by the Lexical
			// Analyzer class when yylex() is called

// %debug // Uncomment to trouble shoot your definitions
%{
    private OpType GetOpType(String lexeme) {
        OpType ot;
            if (lexeme.equals("++")) {
                ot = OpType.INC;

            } else if (lexeme.equals("--")) {
                ot = OpType.DEC;

            } else if (lexeme.equals("==")) {
                ot = OpType.EQUAL;

            } else if (lexeme.equals("!=")) {
                ot = OpType.NOT_EQUAL;

            } else if (lexeme.equals("<")) {
                ot = OpType.LT;

            } else if (lexeme.equals(">")) {
                ot = OpType.GT;

            } else if (lexeme.equals("<=")) {
                ot = OpType.LTE;

            } else if (lexeme.equals(">=")) {
                ot = OpType.GTE;

            } else if (lexeme.equals("+")) {
                ot = OpType.PLUS;

            } else if (lexeme.equals("-")) {
                ot = OpType.MINUS;

            } else if (lexeme.equals("||")) {
                ot = OpType.OR;

            } else if (lexeme.equals("∗")) {
                ot = OpType.MULT;

            } else if (lexeme.equals("/")) {
                ot = OpType.DIV;

            } else if (lexeme.equals("%")) {
                ot = OpType.MOD;

            } else if (lexeme.equals("&&")) {
                ot = OpType.AND;

            } else if (lexeme.equals("=")) {
                ot = OpType.ASSIGN;

            } else {
                ot = OpType.NONE;
            }
            return ot;
        }
%}

%eofval{
    return new Token(TokenCode.EOF, DataType.NONE, OpType.NONE, null, yyline, yycolumn);
%eofval}

Whitespace = \r  |\n | \r\n | [ \t\f]
Comment = "/*" [^*] ~"*/" | "/*" "*"+ "/"

// Identifiers
Letter = [a-z] | [A-Z]
letter_ = {Letter} | [_]
digit = [0-9]
id = {letter_} ( {letter_} | {digit} )*

// Numbers
digits = {digit}+
optional_fraction = (\.{digits})?
optional_exponent = ( "E" (-|\+)? {digits})?
realNum = {digits} {optional_fraction} {optional_exponent}
intNum = {digits}

// Operators
incdecop = "++" | "--"
relop = "==" | "!=" | "<" | ">" | "<=" | ">="
addop = "+" | "-" | "||"
mulop = "*" | "/" | "%" | "&&"
assignop = "="

// Other
class = "class"
static = "static"
void = "void"
int = "int"
real = "real"
if = "if"
else = "else"
for = "for"
return = "return"
break = "break"
continue = "continue"
lparen = "("
rparen = ")"
lbrace = \{
rbrace = "}"
lbracket = "["
rbracket = "]"
not = "!"
comma = ","
semicolon = ";"




%%
{Whitespace} { /* ignore */ }

{Comment} { /* ignore */ }

{class} { return new Token(TokenCode.CLASS, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{static} { return new Token(TokenCode.STATIC, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{void} { return new Token(TokenCode.VOID, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{int} { return new Token(TokenCode.INT, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{real} { return new Token(TokenCode.REAL, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{if} { return new Token(TokenCode.IF, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{else} { return new Token(TokenCode.ELSE, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{for} { return new Token(TokenCode.FOR, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{return} { return new Token(TokenCode.RETURN, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{break} { return new Token(TokenCode.BREAK, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{continue} { return new Token(TokenCode.CONTINUE, DataType.KEYWORD, OpType.NONE, null, yyline, yycolumn); }
{lparen} { return new Token(TokenCode.LPAREN, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{rparen} { return new Token(TokenCode.RPAREN, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{lbrace} { return new Token(TokenCode.LBRACE, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{rbrace} { return new Token(TokenCode.RBRACE, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{lbracket} { return new Token(TokenCode.LBRACKET, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{rbracket} { return new Token(TokenCode.RBRACKET, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{not} { return new Token(TokenCode.NOT, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{comma} { return new Token(TokenCode.COMMA, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
{semicolon} { return new Token(TokenCode.SEMICOLON, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }

{id} { if(yytext().length() > 32) {
        return new Token(TokenCode.ERR_LONG_ID, DataType.NONE, OpType.NONE, null, yyline, yycolumn);
       }
       SymbolTableEntry stEntry = SymbolTable.AddEntry(yytext());
       return new Token(TokenCode.IDENTIFIER, DataType.ID, OpType.NONE, stEntry, yyline, yycolumn);
     }

{intNum} { SymbolTableEntry stEntry = SymbolTable.AddEntry(yytext());
           return new Token(TokenCode.NUMBER, DataType.INT, OpType.NONE, stEntry, yyline, yycolumn); }

{realNum} { SymbolTableEntry stEntry = SymbolTable.AddEntry(yytext());
            return new Token(TokenCode.NUMBER, DataType.REAL, OpType.NONE, stEntry, yyline, yycolumn); }

{incdecop} { return new Token(TokenCode.INCDECOP, DataType.OP, GetOpType(yytext()), null, yyline, yycolumn); }

{relop} { return new Token(TokenCode.RELOP, DataType.OP, GetOpType(yytext()), null, yyline, yycolumn); }

{addop} { return new Token(TokenCode.ADDOP, DataType.OP, GetOpType(yytext()), null, yyline, yycolumn); }

{mulop} { return new Token(TokenCode.MULOP, DataType.OP, GetOpType(yytext()), null, yyline, yycolumn); }

{assignop} { return new Token(TokenCode.ASSIGNOP, DataType.OP, OpType.ASSIGN, null, yyline, yycolumn); }



. { return new Token(TokenCode.ERR_ILL_CHAR, DataType.NONE, OpType.NONE, null, yyline, yycolumn); }
