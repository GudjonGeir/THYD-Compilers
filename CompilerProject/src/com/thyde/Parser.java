package com.thyde;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private static Lexer lexer;
    private static Token token;
    private static String[] codeLines;
    private static HashMap<String, List<TokenCode>> syncSets;


    public static void main(String[] args) throws IOException {
        Parse(args[0]);
    }

    public static void Parse(String filePath) throws IOException {
        lexer = new Lexer(new FileReader(filePath));
        token = lexer.yylex();
        codeLines = makeCodeLines(filePath);


        syncSets.put("program", Arrays.asList(TokenCode.EOF));
        syncSets.put("variable_declarations", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.STATIC));
        syncSets.put("type", Arrays.asList(TokenCode.IDENTIFIER));
        syncSets.put("variable_list", Arrays.asList(TokenCode.SEMICOLON));
        syncSets.put("variable_list'", Arrays.asList(TokenCode.SEMICOLON));
        syncSets.put("variable", Arrays.asList(TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("variable'", Arrays.asList(TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("method_declarations", Arrays.asList(TokenCode.RBRACE));
        syncSets.put("more_method_declarations", Arrays.asList(TokenCode.RBRACE));
        syncSets.put("method_declaration", Arrays.asList(TokenCode.STATIC, TokenCode.RBRACE));
        syncSets.put("method_return_type", Arrays.asList(TokenCode.IDENTIFIER));
        syncSets.put("parameters", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("parameter_list", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("parameter_list'", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("statement_list", Arrays.asList(TokenCode.RBRACE));
        syncSets.put("statement", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.RBRACE));
        syncSets.put("statement'", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.RBRACE));
        syncSets.put("statement''", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.RBRACE));
        syncSets.put("optional_expression", Arrays.asList(TokenCode.SEMICOLON));
        syncSets.put("statement_block", Arrays.asList(TokenCode.ELSE, TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.RBRACE));
        syncSets.put("incr_decr_var", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("optional_else", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.IF, TokenCode.FOR, TokenCode.RETURN, TokenCode.BREAK, TokenCode.CONTINUE, TokenCode.LBRACE, TokenCode.RBRACE));
        syncSets.put("expression_list", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("more_expressions", Arrays.asList(TokenCode.RPAREN));
        syncSets.put("expression", Arrays.asList(TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("expression'", Arrays.asList(TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("simple_expression", Arrays.asList(TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("simple_expression'", Arrays.asList(TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("term", Arrays.asList(TokenCode.ADDOP, TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("term'", Arrays.asList(TokenCode.ADDOP, TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("factor", Arrays.asList(TokenCode.MULOP, TokenCode.ADDOP, TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("factor'", Arrays.asList(TokenCode.MULOP, TokenCode.ADDOP, TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("variable_loc", Arrays.asList(TokenCode.INCDECOP, TokenCode.ASSIGNOP));
        syncSets.put("variable_loc'", Arrays.asList(TokenCode.ASSIGNOP, TokenCode.INCDECOP, TokenCode.MULOP, TokenCode.ADDOP, TokenCode.RELOP, TokenCode.RBRACKET, TokenCode.RPAREN, TokenCode.COMMA, TokenCode.SEMICOLON));
        syncSets.put("sign", Arrays.asList(TokenCode.IDENTIFIER, TokenCode.NUMBER, TokenCode.LPAREN, TokenCode.NOT));

        Program();
        if(!Match(TokenCode.EOF)){
            //TODO
            return;
        }
        System.out.println("No errors");
    }

    public static void Program() throws IOException {
        if(!Match(TokenCode.CLASS)) {
            Sync("program");
            return;
        }
        if(!Match(TokenCode.IDENTIFIER)){
            Sync("program");
            return;
        }
        if(!Match(TokenCode.LBRACE)){
            Sync("program");
            return;
        }
        Variable_declarations();
        Method_declarations();
       if(!Match(TokenCode.RBRACE)){
           Sync("program");
           return;
       }
    }

    public static void Variable_declarations() throws IOException {
        // Peeking because Type starts with INT or REAL
        if (token.getTokenCode() == TokenCode.INT || token.getTokenCode() == TokenCode.REAL) {
            Type();
            Variable_list();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("variable_declarations");
                return;
            }
            Variable_declarations();
        }
        // epsilon
    }

    public static void Type() throws IOException {
        if (token.getTokenCode() == TokenCode.INT) {
            if(!Match(TokenCode.INT)){
                Sync("type");
                return;
            }
        }
        else if (token.getTokenCode() == TokenCode.REAL) {
            if(!Match(TokenCode.REAL)){
                Sync("type");
                return;
            }
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
            if(!Match(TokenCode.COMMA)){
                Sync("variable_list'");
                return;
            }
            Variable();
            Variable_list_prime();
        }
        // epsilon
    }

    public static void Variable() throws IOException {
        if (!Match(TokenCode.IDENTIFIER)){
            Sync("variable");
            return;
        }
        Variable_prime();
    }

    public static void Variable_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            if(!Match(TokenCode.LBRACKET)){
                Sync("variable'");
                return;
            }
            if(!Match(TokenCode.NUMBER)){
                Sync("variable'");
                return;
            }
            if(!Match(TokenCode.RBRACKET)){
                Sync("variable'");
                return;
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
        if(!Match(TokenCode.STATIC)){
            Sync("method_declaration");
            return;
        }
        Method_return_type();
        if(!Match(TokenCode.IDENTIFIER)){
            Sync("method_declaration");
            return;
        }
        if(!Match(TokenCode.LPAREN)){
            Sync("method_declaration");
            return;
        }
        Parameters();
        if(!Match(TokenCode.RPAREN)){
            Sync("method_declaration");
            return;
        }
        if(!Match(TokenCode.LBRACE)){
            Sync("method_declaration");
            return;
        }
        Variable_declarations();
        Statement_list();
        if(!Match(TokenCode.RBRACE)){
            Sync("method_declaration");
            return;
        }
    }

    public static void Method_return_type() throws IOException {
        if (token.getTokenCode() == TokenCode.VOID) {
            if(!Match(TokenCode.VOID)){
                Sync("method_return_type");
                return;
            }
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
        if (!Match(TokenCode.IDENTIFIER)){
            Sync("parameter_list");
            return;
        }
        Parameter_list_prime();
    }

    public static void Parameter_list_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.COMMA) {
            if(!Match(TokenCode.COMMA)){
                Sync("parameter_list'");
                return;
            }
            Type();
            if (!Match(TokenCode.IDENTIFIER)){
                Sync("parameter_list'");
                return;
            }
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
            if(!Match(TokenCode.IDENTIFIER)) {
                Sync("statement");
                return;
            }
            Statement_prime();
        }
        else if (token.getTokenCode() == TokenCode.IF) {
            if(!Match(TokenCode.IF)){
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.LPAREN)){
                Sync("statement");
                return;
            }
            Expression();
            if(!Match(TokenCode.RPAREN)){
                Sync("statement");
                return;
            }
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
            if(!Match(TokenCode.LPAREN)) {
                Sync("statement'");
                return;
            }
            Expression_list();
            if(!Match(TokenCode.RPAREN)) {
                Sync("statement'");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement'");
                return;
            }
        }
        else {
            Variable_loc_prime();
            Statement_prime_prime();
        }
    }

    public static void Statement_prime_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.ASSIGNOP) {
            if(!Match(TokenCode.ASSIGNOP)) {
                Sync("statement''");
                return;
            }
            Expression();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement''");
                return;
            }
        }
        else if (token.getTokenCode() == TokenCode.INCDECOP) {
            if(!Match(TokenCode.INCDECOP)){
                Sync("statement''");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)){
                Sync("statement''");
                return;
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
        if(!Match(TokenCode.LBRACE)) {
            Sync("statement_block");
            return;
        }
        Statement_list();
        if(!Match(TokenCode.RBRACE)) {
            Sync("statement_block");
            return;
        }
    }

    public static void Incr_decr_var() throws IOException {
        Variable_loc();
        if(!Match(TokenCode.INCDECOP)){
            Sync("incr_decr_var");
            return;
        }
    }

    public static void Optional_else() throws IOException {
        if (token.getTokenCode() == TokenCode.ELSE) {
            if(!Match(TokenCode.ELSE)){
                    Sync("optional_else");
                    return;
            }
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
            if(!Match(TokenCode.ADDOP)){
                Sync("simple_expression'");
                return;
            }
            Term();
            Simple_expression_prime();
        }
        // epsilon
    }

    public static void Term() throws IOException {
        Factor();
        Term_prime();
    }

    public static void Term_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.MULOP) {
            if(Match(TokenCode.MULOP)){
                Sync("term'");
                return;
            }
            Factor();
            //token = lexer.yylex();
            Term_prime();
        }
        // epsilon
    }

    public static void Factor() throws IOException {
        if (token.getTokenCode() == TokenCode.IDENTIFIER) {
            if(!Match(TokenCode.IDENTIFIER)){
                if(!Match(TokenCode.LPAREN)){
                    Sync("factor");
                    return;
                }
            }
            Factor_prime();
        }
        else if (token.getTokenCode() == TokenCode.NUMBER) {
            if(!Match(TokenCode.NUMBER)){
                Sync("factor");
                return;
            }
        }
        else if (token.getTokenCode() == TokenCode.LPAREN) {
            if(!Match(TokenCode.LPAREN)){
                Sync("factor");
                return;
            }
            Expression();
            if(!Match(TokenCode.RPAREN)){
                Sync("factor");
                return;
            }
        }
        else if (token.getTokenCode() == TokenCode.NOT) {
            if(!Match(TokenCode.NOT)){
                Sync("factor");
                return;
            }
            Factor();
        }
        else {
            System.out.println("Factor");
        }
    }

    public static void Factor_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LPAREN) {
            if(!Match(TokenCode.LPAREN)){
                Sync("factor'");
                return;
            }
            Expression_list();
            if(!Match(TokenCode.RPAREN)){
                Sync("factor'");
                return;
            }
        }
        else {
            Variable_loc_prime();
        }
    }

    public static void Variable_loc() throws IOException {
        if(!Match(TokenCode.IDENTIFIER)){
            Sync("variable_loc");
            return;
        }
        Variable_loc_prime();
    }

    public static void Variable_loc_prime() throws IOException {
        if (token.getTokenCode() == TokenCode.LBRACKET) {
            if(!Match(TokenCode.LBRACKET)){
                Sync("variable_loc'");
                return;
            }
            Expression();
            if(!Match(TokenCode.RBRACKET)){
                Sync("variable_loc'");
                return;
            }
        }
        // epsilon
    }

    public static void Sign() throws IOException {
        if (token.getTokenCode() == TokenCode.ADDOP) {
            if (token.getOpType() == OpType.PLUS || token.getOpType() == OpType.MINUS) {
                if(!Match(TokenCode.ADDOP)) {
                    Sync("sign");
                    return;
                }
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

    private static void Sync(String nonTerminal) throws IOException {
        while (token.getTokenCode() != TokenCode.EOF) {
            token = lexer.yylex();
            if (syncSets.get(nonTerminal).contains(token.getTokenCode())) {
                return;
            }
        }
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
