package com.thyde;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static Lexer lexer;
    private static Token token;
    private static String[] codeLines;


    public static void main(String[] args) throws IOException {
        Parse(args[0]);
    }

    public static void Parse(String filePath) throws IOException {
        lexer = new Lexer(new FileReader(filePath));
        token = lexer.yylex();
        codeLines = makeCodeLines(filePath);
        Program();
        Match(TokenCode.EOF);
        System.out.println("No errors");
    }

    public static void Program() throws IOException {
        Match(TokenCode.CLASS);
        Match(TokenCode.IDENTIFIER);
        Match(TokenCode.LBRACE);
        Variable_declarations();
        Method_declarations();
        Match(TokenCode.RBRACE);
    }

    public static void Variable_declarations() throws IOException {
        // Peeking because Type starts with INT or REAL
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Type();
            Variable_list();
            Match(TokenCode.SEMICOLON);
            Variable_declarations();
        }
        // epsilon
    }

    public static void Type() throws IOException {
        if (token.getTokenCode() == TokenCode.INT) {
            Match(TokenCode.INT);
        }
        else if (token.getTokenCode() == TokenCode.REAL) {
            Match(TokenCode.REAL);
        }
        else {
            // TODO
            System.out.println("Type");
        }
    }

    public static void Variable_list() throws IOException {
        Variable();
        Variable_list_prime();
    }

    public static void Variable_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            Match(TokenCode.COMMA);
            Variable();
            Variable_list_prime();
        }
        // epsilon
    }

    public static void Variable() throws IOException {
        Match(TokenCode.IDENTIFIER);
        Variable_prime();
    }

    public static void Variable_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            Match(TokenCode.LBRACKET);
            Match(TokenCode.NUMBER);
            Match(TokenCode.RBRACKET);
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
        Match(TokenCode.STATIC);
        Method_return_type();
        Match(TokenCode.IDENTIFIER);
        Match(TokenCode.LPAREN);
        Parameters();
        Match(TokenCode.RPAREN);
        Match(TokenCode.LBRACE);
        Variable_declarations();
        Statement_list();
        Match(TokenCode.RBRACE);
    }

    public static void Method_return_type() throws IOException {
        if (token.getTokenCode() == TokenCode.VOID) {
            Match(TokenCode.VOID);
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
        Match(TokenCode.IDENTIFIER);
        Parameter_list_prime();
    }

    public static void Parameter_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            Match(TokenCode.COMMA);
            Type();
            Match(TokenCode.IDENTIFIER);
            Parameter_list_prime();
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
            Statement_list();
        }
        // epsilon
    }

    public static void Statement() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            Match(TokenCode.IDENTIFIER);
            Statement_prime();
        }
        else if (token.getTokenCode() == TokenCode.IF) {
            Match(TokenCode.IF);
            Match(TokenCode.LPAREN);
            Expression();
            Match(TokenCode.RPAREN);
            Statement_block();
            Optional_else();
        }
        else if (token.getTokenCode() == TokenCode.FOR) {
            Match(TokenCode.FOR);
            Match(TokenCode.LPAREN);
            Variable_loc();
            Match(TokenCode.ASSIGNOP);
            Expression();
            Match(TokenCode.SEMICOLON);
            Expression();
            Match(TokenCode.SEMICOLON);
            Incr_decr_var();
            Match(TokenCode.RPAREN);
            Statement_block();
        }
        else if (token.getTokenCode() == TokenCode.RETURN) {
            Match(TokenCode.RETURN);
            Optional_expression();
            Match(TokenCode.SEMICOLON);
        }
        else if (token.getTokenCode() == TokenCode.BREAK) {
            Match(TokenCode.BREAK);
            Match(TokenCode.SEMICOLON);
        }
        else if (token.getTokenCode() == TokenCode.CONTINUE) {
            Match(TokenCode.CONTINUE);
            Match(TokenCode.SEMICOLON);
        }
        else {
            Statement_block();
        }
    }

    public static void Statement_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LPAREN) {
            Match(TokenCode.LPAREN);
            Expression_list();
            Match(TokenCode.RPAREN);
            Match(TokenCode.SEMICOLON);
        }
        else {
            Variable_loc_prime();
            Statement_prime_prime();
        }
    }

    public static void Statement_prime_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.ASSIGNOP) {
            Match(TokenCode.ASSIGNOP);
            Expression();
            Match(TokenCode.SEMICOLON);
        }
        else if (token.getTokenCode() == TokenCode.INCDECOP) {
            Match(TokenCode.INCDECOP);
            Match(TokenCode.SEMICOLON);
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
        Match(TokenCode.LBRACE);
        Statement_list();
        Match(TokenCode.RBRACE);
    }

    public static void Incr_decr_var() throws IOException {
        Variable_loc();
        Match(TokenCode.INCDECOP);
    }

    public static void Optional_else() throws IOException {
        if (token.getTokenCode() == TokenCode.ELSE) {
            Match(TokenCode.ELSE);
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
            if (token.getOpType() == OpType.PLUS || token.getOpType() == OpType.MINUS) {
                Match(TokenCode.ADDOP);
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

    private static boolean Match(TokenCode tc) throws IOException {
        if (token.getTokenCode() != tc) {
            String lineNumber = (token.getLineNumber() + 1) + " : ";

            System.out.println(lineNumber + codeLines[token.getLineNumber()]);
            for (int i = 0; i < lineNumber.length() + token.getColumn(); i++) {
                System.out.print(" ");
            }
            System.out.println("^ Expected: " + tc + ", got: " + token.getTokenCode());
//            System.out.println("Error: line number " + token.getLineNumber() + ", column " + token.getColumn());
//            System.out.println("Expected: " + tc + ", got: " + token.getTokenCode());
            return false;
        }
        else if (token.getTokenCode() == TokenCode.ERR_ILL_CHAR) {

        }
        else if (token.getTokenCode() == TokenCode.ERR_LONG_ID) {

        }
        token = lexer.yylex();
        return true;
    }

    public static String[] makeCodeLines(String filePath){

        String line;

        Scanner code = null;
        try {
            code = new Scanner(new File(filePath)).useDelimiter("\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        List<String> codeLinesTemp = new ArrayList<String>();

        while (code.hasNext()) {
            line = code.next();
            codeLinesTemp.add(line);
        }
        code.close();

        return codeLinesTemp.toArray(new String[codeLinesTemp.size()]);
    }

}
