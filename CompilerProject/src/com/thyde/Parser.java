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
                    //token = lexer.yylex();
                    Method_declarations();

                    //token = lexer.yylex();
                    if (token.getTokenCode() == TokenCode.RBRACE) {
                        token = lexer.yylex();
                        if (token.getTokenCode() == TokenCode.EOF) {
                            System.out.println("No errors");
                        }
                        else {
                            // TODO
                            System.out.println("Program - token.getTokenCode() == TokenCode.EOF");
                        }
                    }
                    else {
                        // TODO
                        System.out.println("Program - token.getTokenCode() == TokenCode.RBRACE");
                    }
                }
                else {
                    // TODO
                    System.out.println("Program - token.getTokenCode() == TokenCode.LBRACE");
                }
            }
            else {
                // TODO
                System.out.println("Program - token.getTokenCode() == TokenCode.IDENTIFIER");
            }
        }
        else {
            // TODO
            System.out.println("Program - token.getTokenCode() == TokenCode.CLASS");
        }
    }

    public static void Variable_declarations() throws IOException {
        // Peeking because Type starts with INT or REAL
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Type();
            //token = lexer.yylex();
            Variable_list();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
                Variable_declarations();
            }

        }
        // epsilon
    }

    public static void Type() throws IOException {
        if (token.getTokenCode() == TokenCode.INT) {
            token = lexer.yylex();
        }
        else if (token.getTokenCode() == TokenCode.REAL) {
            token = lexer.yylex();
        }
        else {
            // TODO
            System.out.println("Type");
        }
    }

    public static void Variable_list() throws IOException {
        Variable();
        //token = lexer.yylex();
        Variable_list_prime();
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
            System.out.println("Variable");
        }
    }

    public static void Variable_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.NUMBER) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.RBRACKET) {
                    token = lexer.yylex();
                }
                else {
                    // TODO
                    System.out.println("Variable_prime - token.getTokenCode() == TokenCode.RBACKET");
                }
            }
            else {
                // TODO
                System.out.println("Variable_prime - token.getTokenCode() == TokenCode.NUMBER");
            }
        }
        // epsilon
    }

    public static void Method_declarations() throws IOException {
        Method_declaration();
        More_method_declarations();
    }

    public static void More_method_declarations() throws IOException {
        //FIRST(Method_declaration() = {STATIC}
        if (token.getTokenCode() == TokenCode.STATIC){
            Method_declaration();
            More_method_declarations();
        }
        // epsilon
    }

    public static void Method_declaration() throws IOException {
        if (token.getTokenCode() == TokenCode.STATIC) {
            token = lexer.yylex();
            Method_return_type();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.IDENTIFIER) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.LPAREN) {
                    token = lexer.yylex();
                    Parameters();
                    if (token.getTokenCode() == TokenCode.RPAREN) {
                        token = lexer.yylex();
                        if (token.getTokenCode() == TokenCode.LBRACE) {
                            token = lexer.yylex();
                            Variable_declarations();
                            //token = lexer.yylex();
                            Statement_list();
                            if (token.getTokenCode() == TokenCode.RBRACE) {
                                token = lexer.yylex();
                            }
                            else {
                                // TODO
                                System.out.println("Method_declaration - token.getTokenCode() == TokenCode.RBRACE");
                            }
                        }
                        else {
                            // TODO
                            System.out.println("Method_declaration - token.getTokenCode() == TokenCode.LBRACE");
                        }
                    }
                    else {
                        // TODO
                        System.out.println("Method_declaration - token.getTokenCode() == TokenCode.RPAREN");
                    }
                }
                else {
                    // TODO
                    System.out.println("Method_declaration - token.getTokenCode() == TokenCode.LPAREN");
                }
            }
            else {
                // TODO
                System.out.println("Method_declaration - token.getTokenCode() == TokenCode.IDENTIFIER");
            }
        }
        else {
            // TODO
            System.out.println("Method_declaration - token.getTokenCode() == TokenCode.STATIC");
            System.out.println("\tTokenCode: " + token.getTokenCode());
        }
    }

    public static void Method_return_type() throws IOException {
        if (token.getTokenCode() == TokenCode.VOID) {
            token = lexer.yylex();
        }
        else {
            Type();
        }
    }

    public static void  Parameters() throws IOException {
        // FIRST(Parameter_list()) = {INT, REAL}
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Parameter_list();
        }
        // epsilon
    }

    public static void Parameter_list() throws IOException {
        Type();
        //token = lexer.yylex();
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();
            Parameter_list_prime();
        }
        else {
            // TODO
            System.out.println("Parameter_list");
        }
    }

    public static void Parameter_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            token = lexer.yylex();
            Type();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.IDENTIFIER) {
                token = lexer.yylex();
                Parameter_list_prime();
            }
            else {
                // TODO
                System.out.println("Parameter_list_prime");
            }
        }
        // epsilon
    }

    public static void Statement_list() throws IOException {
        // FIRST(Statement()) = { IDENTIFIER, IF, FOR, RETURN, BREAK, CONTINUE, LBRACE }
        if (token.getTokenCode() == TokenCode.IDENTIFIER
                || token.getTokenCode() == TokenCode.IF
                || token.getTokenCode() == TokenCode.FOR
                || token.getTokenCode() == TokenCode.RETURN
                || token.getTokenCode() == TokenCode.BREAK
                || token.getTokenCode() == TokenCode.CONTINUE
                || token.getTokenCode() == TokenCode.LBRACE) {
            Statement();
            //token = lexer.yylex();
            Statement_list();
        }
        // epsilon
    }

    public static void Statement() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();
            Statement_prime();
        }
        else if (token.getTokenCode() == TokenCode.IF) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.LPAREN) {
                token = lexer.yylex();
                Expression();
                //token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.RPAREN) {
                    token = lexer.yylex();
                    Statement_block();
                    //token = lexer.yylex(); // TODO: maybe move this into Optional_else()
                    Optional_else();
                }
                else {
                    // TODO
                    System.out.println("Statement - token.getTokenCode() == TokenCode.RPAREN");
                }
            }
            else {
                // TODO
                System.out.println("Statement - token.getTokenCode() == TokenCode.LPAREN");
            }
        }
        else if (token.getTokenCode() == TokenCode.FOR) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.LPAREN) {
                token = lexer.yylex();
                Variable_loc();
                //token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.ASSIGNOP) {
                    token = lexer.yylex();
                    Expression();
                    //token = lexer.yylex();
                    if (token.getTokenCode() == TokenCode.SEMICOLON) {
                        token = lexer.yylex();
                        Expression();
                        //token = lexer.yylex();
                        if (token.getTokenCode() == TokenCode.SEMICOLON) {
                            token = lexer.yylex();
                            Incr_decr_var();
                            //token = lexer.yylex();
                            if (token.getTokenCode() == TokenCode.RPAREN) {
                                token = lexer.yylex();
                                Statement_block();
                            }
                            else {
                                // TODO
                                System.out.println("Statement - token.getTokenCode() == TokenCode.RPAREN");
                            }
                        }
                        else {
                            // TODO
                            System.out.println("Statement - token.getTokenCode() == TokenCode.SEMICOLON1");
                        }
                    }
                    else {
                        // TODO
                        System.out.println("Statement - token.getTokenCode() == TokenCode.SEMICOLON2");
                    }
                }
                else {
                    // TODO
                    System.out.println("Statement - token.getTokenCode() == TokenCode.ASSIGNOP");

                }
            }
            else {
                // TODO
                System.out.println("Statement - token.getTokenCode() == TokenCode.LPAREN");
            }

        }
        else if (token.getTokenCode() == TokenCode.RETURN) {
            token = lexer.yylex(); // TODO: maybe move this into Optional_expression
            Optional_expression();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
            }
            else {
                // TODO
                System.out.println("Statement - token.getTokenCode() == TokenCode.SEMICOLON3");

            }
        }
        else if (token.getTokenCode() == TokenCode.BREAK) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
            }
            else {
                // TODO
                System.out.println("Statement - token.getTokenCode() == TokenCode.SEMICOLON4");
            }
        }
        else if (token.getTokenCode() == TokenCode.CONTINUE) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
            }
            else {
                // TODO
                System.out.println("Statement - token.getTokenCode() == TokenCode.SEMICOLON5");
            }
        }
        else {
            Statement_block();
        }
    }

    public static void Statement_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LPAREN) {
            token = lexer.yylex();
            Expression_list();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.RPAREN) {
                token = lexer.yylex();
                if (token.getTokenCode() == TokenCode.SEMICOLON) {
                    token = lexer.yylex();
                }
            }
            else {
                System.out.println("Statement_prime");
                // TODO
            }
        }
        else {
            Variable_loc_prime();
            //token = lexer.yylex();
            Statement_prime_prime();
        }
    }

    public static void Statement_prime_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.ASSIGNOP) {
            token = lexer.yylex();
            Expression();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Statement_prime_prime - token.getTokenCode() == TokenCode.SEMICOLON1");
                token = lexer.yylex();
                token = lexer.yylex();
                token = lexer.yylex();
                token = lexer.yylex();
                token = lexer.yylex();

                // TODO
            }
        }
        else if (token.getTokenCode() == TokenCode.INCDECOP) {
            token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.SEMICOLON) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Statement_prime_prime - token.getTokenCode() == TokenCode.SEMICOLON2");
            }
        }
        else {
            System.out.println("Statement_prime_prime - token.getTokenCode() == TokenCode.INCDECOP || token.getTokenCode() == TokenCode.ASSIGNOP");

            // TODO
        }
    }

    public static void Optional_expression() throws IOException {
        // FIRST(Expression) = { IDENTIFIER, NUMBER, NOT, LPAREN, MINUS, PLUS }
        if (token.getTokenCode() == TokenCode.IDENTIFIER
                || token.getTokenCode() == TokenCode.NUMBER
                || token.getTokenCode() == TokenCode.NOT
                || token.getTokenCode() == TokenCode.LPAREN
                || (token.getTokenCode() == TokenCode.ADDOP
                    && (token.getOpType() == OpType.MINUS
                        || token.getOpType() == OpType.PLUS))) {
            Expression();
        }
        // epsilon
    }

    public static void Statement_block() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACE) {
            token = lexer.yylex();
            Statement_list();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.RBRACE) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Statement_block - token.getTokenCode() == TokenCode.RBRACE");
                // TODO
            }
        }
        else {
            System.out.println("Statement_block - token.getTokenCode() == TokenCode.LBRACE");

            // TODO
        }
    }

    public static void Incr_decr_var() throws IOException {
        Variable_loc();
        //token = lexer.yylex();
        if (token.getTokenCode() == TokenCode.INCDECOP) {
            token = lexer.yylex();
        }
        else {
            System.out.println("Incr_decr_var");
            // TODO
        }
    }

    public static void Optional_else() throws IOException {
        if (token.getTokenCode() == TokenCode.ELSE) {
            token = lexer.yylex();
            Statement_block();
        }
        // epsilon
    }

    public static void Expression_list() throws IOException {
        // FIRST(Expression) = { IDENTIFIER, NUMBER, NOT, LPAREN, MINUS, PLUS }
        if (token.getTokenCode() == TokenCode.IDENTIFIER
                || token.getTokenCode() == TokenCode.NUMBER
                || token.getTokenCode() == TokenCode.NOT
                || token.getTokenCode() == TokenCode.LPAREN
                || (token.getTokenCode() == TokenCode.ADDOP
                    && (token.getOpType() == OpType.MINUS
                        || token.getOpType() == OpType.PLUS))) {
            Expression();
            //token = lexer.yylex();
            More_expressions();
        }
        // epsilon
    }

    public static void More_expressions() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            token = lexer.yylex();
            Expression();
            //token = lexer.yylex();
            More_expressions();
        }
        // epsilon
    }

    public static void Expression() throws IOException {
        Simple_expression();
        //token = lexer.yylex();
        Expression_prime();
    }

    public static void Expression_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.RELOP) {
            token = lexer.yylex();
            Simple_expression();
        }
        // epsilon
    }

    public static void Simple_expression() throws IOException {
        // FIRST(TERM) = { IDENTIFIER, NUMBER, NOT, LPAREN }
        if (token.getTokenCode() == TokenCode.IDENTIFIER
                || token.getTokenCode() == TokenCode.NUMBER
                || token.getTokenCode() == TokenCode.NOT
                || token.getTokenCode() == TokenCode.LPAREN) {
            Term();
            //token = lexer.yylex();
            Simple_expression_prime();
        }
        // FIRST(Sign()) = { MINUS, PLUS }
        else if (token.getTokenCode() == TokenCode.ADDOP
                && (token.getOpType() == OpType.MINUS
                || token.getOpType() == OpType.PLUS)) {
            Sign();
            //token = lexer.yylex();
            Term();
            //token = lexer.yylex();
            Simple_expression_prime();
        }
        else {
            System.out.println("Simple_expression");

            // TODO
        }
    }

    public static void Simple_expression_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.ADDOP) {
            token = lexer.yylex();
            Term();
            //token = lexer.yylex();
            Simple_expression_prime();
        }
        // epsilon
    }

    public static void Term() throws IOException {
        Factor();
        //token = lexer.yylex();
        Term_prime();
    }

    public static void Term_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.MULOP) {
            token = lexer.yylex();
            Factor();
            //token = lexer.yylex();
            Term_prime();
        }
        // epsilon
    }

    public static void Factor() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();
            Factor_prime();
        }
        else if (token.getTokenCode() == TokenCode.NUMBER) {
            token = lexer.yylex();
        }
        else if (token.getTokenCode() == TokenCode.LPAREN) {
            token = lexer.yylex();
            Expression();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.RPAREN) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Factor");

                // TODO
            }
        }
        else if (token.getTokenCode() == TokenCode.NOT) {
            token = lexer.yylex();
            Factor();
        }
        else {
            System.out.println("Factor");
        }
    }

    public static void Factor_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LPAREN) {
            token = lexer.yylex();
            Expression_list();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.RPAREN) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Factor_prime");
                // TODO
            }
        }
        else {
            Variable_loc_prime();
        }
    }

    public static void Variable_loc() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            token = lexer.yylex();
            Variable_loc_prime();
        }
        else {
            System.out.println("Variable_loc");

            // TODO
        }
    }

    public static void Variable_loc_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            token = lexer.yylex();
            Expression();
            //token = lexer.yylex();
            if (token.getTokenCode() == TokenCode.RBRACKET) {
                token = lexer.yylex();
            }
            else {
                System.out.println("Variable_loc_prime - token.getTokenCode() == TokenCode.RBRACKET");

                // TODO
            }
        }
        // epsilon
    }

    public static void Sign() throws IOException {
        if (token.getTokenCode() == TokenCode.ADDOP) {
            if (token.getOpType() == OpType.PLUS) {
                token = lexer.yylex();
            }
            else if (token.getOpType() == OpType.MINUS) {
                token = lexer.yylex();
            }
            else {

                System.out.println("Sign - token.getTokenCode() == TokenCode.ADDOP || token.getOpType() == OpType.MINUS");
                // TODO
            }
        }
        else {
            System.out.println("Sign - token.getTokenCode() == TokenCode.ADDOP");

            // TODO
        }
    }
}
