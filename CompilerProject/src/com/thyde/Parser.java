package com.thyde;

import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private static Lexer lexer;
    private static Token token;

    public static void main(String[] args) throws IOException {
        lexer = new Lexer(new FileReader(args[0]));
        Program();
    }

    public static void Program() throws IOException {
        token = lexer.yylex();
        if (token.getTokenCode() == TokenCode.CLASS) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.IDENTIFIER) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.LBRACE) {
                    token = lexer.yylex();
                    Variable_declarations();
                    token = lexer.yylex();
                    Method_declarations();

                    token = lexer.yylex();
                    if (token.getTokenCode() == TokenCode.RBRACE) {
                        token = lexer.yylex();
                        if (token.getTokenCode() == TokenCode.EOF) {

                        }
                        else {

                        }
                    }
                    else {

                    }
                }
                else {

                }
            }
            else {

            }
        }
        else {

        }
    }

    public static void Variable_declarations() throws IOException {
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Type();
            token = lexer.yylex();
            Variable_list();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
                Variable_declarations();
            }

        }
        // epsilon
    }

    public static void Type() {
        if (token.getTokenCode() == TokenCode.INT) {

        }
        else if (token.getTokenCode() == TokenCode.REAL) {

        }
        else {
            // TODO
        }
    }

    public static void Variable_list() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            Variable();

            token = lexer.yylex();
            Variable_list_prime();
        }
    }

    public static void Variable_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            token = lexer.yylex();

            Variable();
            Variable_list_prime();
        }
        // epsilon
    }

    public static void Variable() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();

            Variable_prime();
        }
        else {
            // TODO
        }
    }

    public static void Variable_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.NUMBER) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.RBRACE) {
                    token = lexer.yylex();
                }
                else {
                    // TODO
                }
            }
            else {
                // TODO
            }
        }
        // epsilon
    }

    public static void Method_declarations() throws IOException {
        Method_declaration();
        More_method_declarations();
    }

    public static void More_method_declarations() throws IOException {
        Method_declaration();
        More_method_declarations();
    }

    public static void Method_declaration() throws IOException {
        if (token.getTokenCode() == TokenCode.STATIC) {
            token = lexer.yylex();
            Method_return_type();
            if (token.getTokenCode() == TokenCode.IDENTIFIER) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.LPAREN) {
                    token = lexer.yylex();
                    Parameters();
                    if (token.getTokenCode() == TokenCode.RPAREN) {
                        token = lexer.yylex();
                        if (token.getTokenCode() == TokenCode.LBRACE) {
                            Variable_declarations();
                            token = lexer.yylex();
                            Statement_list();
                            if (token.getTokenCode() == TokenCode.RBRACE) {

                            }
                            else {
                                // TODO
                            }
                        }
                        else {
                            // TODO
                        }
                    }
                    else {
                        // TODO
                    }
                }
                else {
                    // TODO
                }
            }
            else {
                // TODO
            }
        }
        else {
            // TODO
        }
    }

    public static void Method_return_type() {
        if (token.getTokenCode() == TokenCode.VOID) {

        }
        else {
            Type();
        }
    }

    public static void  Parameters() throws IOException {
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Parameter_list();
        }
        // epsilon
    }

    public static void Parameter_list() throws IOException {
        Type();
        token = lexer.yylex();
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();
            Parameter_list_prime();
        }
        else {
            // TODO
        }
    }

    public static void Parameter_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            token = lexer.yylex();
            Type();
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.IDENTIFIER) {
                token = lexer.yylex();
                Parameter_list_prime();
            }
            else {
                // TODO
            }
        }
        // epsilon
    }

    public static void Statement_list() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER
                || token.getTokenCode() == TokenCode.IF
                || token.getTokenCode() == TokenCode.FOR
                || token.getTokenCode() == TokenCode.RETURN
                || token.getTokenCode() == TokenCode.BREAK
                || token.getTokenCode() == TokenCode.CONTINUE
                || token.getTokenCode() == TokenCode.RBRACE) {
            Statement();
            token = lexer.yylex();
            Statement_list();
        }
        // epsilon
    }

    public static void Statement() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            // TODO left factor
            // variable_loc
            // id ( expression_list)
            // incr_decr_var
        }
        else if (token.getTokenCode() == TokenCode.IF) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.LPAREN) {
                token = lexer.yylex();
                Expression();
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.RPAREN) {
                    token = lexer.yylex();
                    Statement_block();
                    token = lexer.yylex(); // TODO: maybe move this into Optional_else()
                    Optional_else();
                }
                else {
                    // TODO
                }
            }
            else {
                // TODO
            }
        }
        else if (token.getTokenCode() == TokenCode.FOR) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.LPAREN) {
                // TODO variable loc
            }

        }
        else if (token.getTokenCode() == TokenCode.RETURN) {
            token = lexer.yylex(); // TODO: maybe move this into Optional_expression
            Optional_expression();
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {

            }
            else {
                // TODO
            }
        }
        else if (token.getTokenCode() == TokenCode.BREAK) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {

            }
            else {
                // TODO
            }
        }
        else if (token.getTokenCode() == TokenCode.CONTINUE) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {

            }
            else {
                // TODO
            }
        }
        // TODO: left factor
        // incr_decr_var
        // statement_block
    }
}
