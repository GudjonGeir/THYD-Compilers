package com.thyde;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private static Lexer lexer;
    private static Token currentToken;
    private static Token prevToken;
    private static String[] codeLines;
    private static HashMap<String, List<TokenCode>> syncSets;
    private static int errorCounter;


    public static void main(String[] args) throws IOException {
        Parse(args[0]);
    }
    // TODO:
    // Comment helper functions

    public static void Parse(String filePath) throws IOException {
        lexer = new Lexer(new FileReader(filePath));
        currentToken = lexer.yylex();
        codeLines = MakeCodeLines(filePath);
        errorCounter = 0;

        syncSets = new HashMap<String, List<TokenCode>>();
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
        Match(TokenCode.EOF);

        if (errorCounter == 0) {
            System.out.println("No errors");
        }
        else {
            System.out.println("Number of errors: " + errorCounter);
        }
    }

    private static void Program() throws IOException {
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
        }
    }

    private static void Variable_declarations() throws IOException {
        // Peeking because Type starts with INT or REAL
        if (currentToken.getTokenCode() == TokenCode.INT || currentToken.getTokenCode() == TokenCode.REAL) {
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

    private static void Type() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.INT) {
            Match(TokenCode.INT);
        }
        else if (currentToken.getTokenCode() == TokenCode.REAL) {
            Match(TokenCode.REAL);
        }
        else {
            PrintError(null, "expected", "a type");
            Sync("type");
        }
    }

    private static void Variable_list() throws IOException {
        Variable();
        Variable_list_prime();
    }

    private static void Variable_list_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.COMMA) {
            if(!Match(TokenCode.COMMA)){
                Sync("variable_list'");
                return;
            }
            Variable();
            Variable_list_prime();
        }
        // epsilon
    }

    private static void Variable() throws IOException {
        if (!Match(TokenCode.IDENTIFIER)){
            Sync("variable");
            return;
        }
        Variable_prime();
    }

    private static void Variable_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.LBRACKET) {
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
            }
        }
        // epsilon
    }

    private static void Method_declarations() throws IOException {
        Method_declaration();
        More_method_declarations();
    }

    private static void More_method_declarations() throws IOException {
        //FIRST(Method_declaration() = {STATIC}
        if (currentToken.getTokenCode() == TokenCode.STATIC){
            Method_declaration();
            More_method_declarations();
        }
        // epsilon
    }

    private static void Method_declaration() throws IOException {
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
        }
    }

    private static void Method_return_type() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.VOID) {
            if(!Match(TokenCode.VOID)){
                Sync("method_return_type");
            }
        }
        else {
            Type();
        }
    }

    private static void  Parameters() throws IOException {
        // FIRST(Parameter_list()) = {INT, REAL}
        if (currentToken.getTokenCode() == TokenCode.INT || currentToken.getTokenCode() == TokenCode.REAL) {
            Parameter_list();
        }
        // epsilon
    }

    private static void Parameter_list() throws IOException {
        Type();
        if (!Match(TokenCode.IDENTIFIER)){
            Sync("parameter_list");
            return;
        }
        Parameter_list_prime();
    }

    private static void Parameter_list_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.COMMA) {
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

    private static void Statement_list() throws IOException {
        // FIRST(Statement()) = { IDENTIFIER, IF, FOR, RETURN, BREAK, CONTINUE, LBRACE }
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER
                || currentToken.getTokenCode() == TokenCode.IF
                || currentToken.getTokenCode() == TokenCode.FOR
                || currentToken.getTokenCode() == TokenCode.RETURN
                || currentToken.getTokenCode() == TokenCode.BREAK
                || currentToken.getTokenCode() == TokenCode.CONTINUE
                || currentToken.getTokenCode() == TokenCode.LBRACE) {
            Statement();
            Statement_list();
        }
        // epsilon
    }

    private static void Statement() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER) {
            if(!Match(TokenCode.IDENTIFIER)) {
                Sync("statement");
                return;
            }
            Statement_prime();
        }
        else if (currentToken.getTokenCode() == TokenCode.IF) {
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
        else if (currentToken.getTokenCode() == TokenCode.FOR) {
            if(!Match(TokenCode.FOR)) {
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.LPAREN)) {
                Sync("statement");
                return;
            }
            Variable_loc();
            if(!Match(TokenCode.ASSIGNOP)) {
                Sync("statement");
                return;
            }
            Expression();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
                return;
            }
            Expression();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
                return;
            }
            Incr_decr_var();
            if(!Match(TokenCode.RPAREN)) {
                Sync("statement");
                return;
            }
            Statement_block();
        }
        else if (currentToken.getTokenCode() == TokenCode.RETURN) {
            if(!Match(TokenCode.RETURN)) {
                Sync("statement");
                return;
            }
            Optional_expression();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.BREAK) {
            if(!Match(TokenCode.BREAK)) {
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.CONTINUE) {
            if(!Match(TokenCode.CONTINUE)) {
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
            }
        }
        else {
            Statement_block();
        }
    }

    private static void Statement_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.LPAREN) {
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
            }
        }
        else {
            Variable_loc_prime();
            Statement_prime_prime();
        }
    }

    private static void Statement_prime_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.ASSIGNOP) {
            if(!Match(TokenCode.ASSIGNOP)) {
                Sync("statement''");
                return;
            }
            Expression();
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement''");
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.INCDECOP) {
            if(!Match(TokenCode.INCDECOP)){
                Sync("statement''");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)){
                Sync("statement''");
            }
        }
        else {
            PrintError(null, "invalidStatement", null);
            Sync("statement''");
        }
    }

    private static void Optional_expression() throws IOException {
        // FIRST(Expression) = { IDENTIFIER, NUMBER, NOT, LPAREN, MINUS, PLUS }
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER
                || currentToken.getTokenCode() == TokenCode.NUMBER
                || currentToken.getTokenCode() == TokenCode.NOT
                || currentToken.getTokenCode() == TokenCode.LPAREN
                || (currentToken.getTokenCode() == TokenCode.ADDOP
                    && (currentToken.getOpType() == OpType.MINUS
                        || currentToken.getOpType() == OpType.PLUS))) {
            Expression();
        }
        // epsilon
    }

    private static void Statement_block() throws IOException {
        if(!Match(TokenCode.LBRACE)) {
            Sync("statement_block");
            return;
        }
        Statement_list();
        if(!Match(TokenCode.RBRACE)) {
            Sync("statement_block");
        }
    }

    private static void Incr_decr_var() throws IOException {
        Variable_loc();
        if(!Match(TokenCode.INCDECOP)){
            Sync("incr_decr_var");
        }
    }

    private static void Optional_else() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.ELSE) {
            if(!Match(TokenCode.ELSE)){
                    Sync("optional_else");
                    return;
            }
            Statement_block();
        }
        // epsilon
    }

    private static void Expression_list() throws IOException {
        // FIRST(Expression) = { IDENTIFIER, NUMBER, NOT, LPAREN, MINUS, PLUS }
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER
                || currentToken.getTokenCode() == TokenCode.NUMBER
                || currentToken.getTokenCode() == TokenCode.NOT
                || currentToken.getTokenCode() == TokenCode.LPAREN
                || (currentToken.getTokenCode() == TokenCode.ADDOP
                    && (currentToken.getOpType() == OpType.MINUS
                        || currentToken.getOpType() == OpType.PLUS))) {
            Expression();
            More_expressions();
        }
        // epsilon
    }

    private static void More_expressions() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.COMMA) {
            if(!Match(TokenCode.COMMA)) {
                Sync("more_expressions");
                return;
            }
            Expression();
            More_expressions();
        }
        // epsilon
    }

    private static void Expression() throws IOException {
        Simple_expression();
        Expression_prime();
    }

    private static void Expression_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.RELOP) {
            if (!Match(TokenCode.RELOP)) {
                Sync("expression'");
                return;
            }
            Simple_expression();
        }
        // epsilon
    }

    private static void Simple_expression() throws IOException {
        // FIRST(TERM) = { IDENTIFIER, NUMBER, NOT, LPAREN }
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER
                || currentToken.getTokenCode() == TokenCode.NUMBER
                || currentToken.getTokenCode() == TokenCode.NOT
                || currentToken.getTokenCode() == TokenCode.LPAREN) {
            Term();
            Simple_expression_prime();
        }
        // FIRST(Sign()) = { MINUS, PLUS }
        else if (currentToken.getTokenCode() == TokenCode.ADDOP
                && (currentToken.getOpType() == OpType.MINUS
                || currentToken.getOpType() == OpType.PLUS)) {
            Sign();
            Term();
            Simple_expression_prime();
        }
        else {
            PrintError(null, "invalidExpression", null);
            Sync("simple_expression");
        }
    }

    private static void Simple_expression_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.ADDOP) {
            if(!Match(TokenCode.ADDOP)){
                Sync("simple_expression'");
                return;
            }
            Term();
            Simple_expression_prime();
        }
        // epsilon
    }

    private static void Term() throws IOException {
        Factor();
        Term_prime();
    }

    private static void Term_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.MULOP) {
            if(!Match(TokenCode.MULOP)){
                Sync("term'");
                return;
            }
            Factor();
            Term_prime();
        }
        // epsilon
    }

    private static void Factor() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.IDENTIFIER) {
            if(!Match(TokenCode.IDENTIFIER)){
                if(!Match(TokenCode.LPAREN)){
                    Sync("factor");
                    return;
                }
            }
            Factor_prime();
        }
        else if (currentToken.getTokenCode() == TokenCode.NUMBER) {
            if(!Match(TokenCode.NUMBER)){
                Sync("factor");
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.LPAREN) {
            if(!Match(TokenCode.LPAREN)){
                Sync("factor");
                return;
            }
            Expression();
            if(!Match(TokenCode.RPAREN)){
                Sync("factor");
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.NOT) {
            if(!Match(TokenCode.NOT)){
                Sync("factor");
                return;
            }
            Factor();
        }
        else {
            // This will never happen
        }
    }

    private static void Factor_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.LPAREN) {
            if(!Match(TokenCode.LPAREN)){
                Sync("factor'");
                return;
            }
            Expression_list();
            if(!Match(TokenCode.RPAREN)){
                Sync("factor'");
            }
        }
        else {
            Variable_loc_prime();
        }
    }

    private static void Variable_loc() throws IOException {
        if(!Match(TokenCode.IDENTIFIER)){
            Sync("variable_loc");
            return;
        }
        Variable_loc_prime();
    }

    private static void Variable_loc_prime() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.LBRACKET) {
            if(!Match(TokenCode.LBRACKET)){
                Sync("variable_loc'");
                return;
            }
            Expression();
            if(!Match(TokenCode.RBRACKET)){
                Sync("variable_loc'");
            }
        }
        // epsilon
    }

    private static void Sign() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.ADDOP) {
            if (currentToken.getOpType() == OpType.PLUS || currentToken.getOpType() == OpType.MINUS) {
                if(!Match(TokenCode.ADDOP)) {
                    Sync("sign");
                }
            }
            else {
                // This never happens
            }
        }
        else {
            // This never happens
        }
    }

    private static boolean Match(TokenCode tc) throws IOException {
        if (currentToken.getTokenCode() != tc) {
            if (currentToken.getTokenCode() == TokenCode.ERR_LONG_ID) {
                PrintError(null, "longID", null);
                return false;
            }
            else if (currentToken.getTokenCode() == TokenCode.EOF) {
                PrintError(null, "unexpectedEOF", null);
                return false;
            }
            PrintError(tc, "expected", ConvertTokenCodeToString(tc));
            return false;
        }
        prevToken = currentToken;
        currentToken = lexer.yylex();
        if (currentToken.getTokenCode() == TokenCode.ERR_ILL_CHAR) {
            PrintError(null, "illegalChar", null);
            return false;
        }
        return true;
    }

    private static void PrintError(TokenCode expectedToken, String errorType, String expected) {

        // If the current token code is EOF and the error type is not unexpectedEOF we don't
        // handle any more errors because we know that they're all caused by the EOF that we've
        // already handled
        if (currentToken.getTokenCode() == TokenCode.EOF && !errorType.equals("unexpectedEOF")) {
            return;
        }
        errorCounter++;

        String errorMessage = "";
        int lineNr = currentToken.getLineNumber();
        int columnNr = currentToken.getColumn();


        if (errorType.equals("expected")) {
            if (expectedToken == TokenCode.SEMICOLON) {
                lineNr = prevToken.getLineNumber();
                columnNr = prevToken.getColumn() + prevToken.getTokenText().length();
            }
            errorMessage = "Expected " + expected;
        }
        else if (errorType.equals("invalidStatement")) {
            errorMessage = "Invalid statement";
        }
        else if (errorType.equals("invalidExpression")) {
            errorMessage = "Invalid expression";
        }
        else if (errorType.equals("illegalChar")) {
            errorMessage = "Illegal character";
        }
        else if (errorType.equals("longID")) {
            errorMessage = "Identifier too long";
        }
        else if (errorType.equals("endofclass")) {
            errorMessage = "End of class";
        }
        else if (errorType.equals("unexpectedEOF")) {
            lineNr = prevToken.getLineNumber();
            columnNr = prevToken.getColumn() + prevToken.getTokenText().length();
            errorMessage = "Unexpected end of file";
        }
        String errorLine = (lineNr + 1) + " : ";

        System.out.println(errorLine + codeLines[lineNr]);
        for (int i = 0; i < errorLine.length() + columnNr; i++) {
            System.out.print(" ");
        }
        System.out.println("^ " + errorMessage);
    }

    private static void Sync(String nonTerminal) throws IOException {
        while (currentToken.getTokenCode() != TokenCode.EOF) {
            if (syncSets.get(nonTerminal).contains(currentToken.getTokenCode())) {
                return;
            }
            currentToken = lexer.yylex();
        }
    }

    private static String[] MakeCodeLines(String filePath){

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

    private static String ConvertTokenCodeToString(TokenCode tc) {
        if (tc == TokenCode.SEMICOLON) {
            return "';'";
        }
        else if (tc == TokenCode.IDENTIFIER) {
            return "an identifier";
        }
        else if (tc == TokenCode.NUMBER) {
            return "a number";
        }
        else if (tc == TokenCode.ASSIGNOP) {
            return "'='";
        }
        else if (tc == TokenCode.CLASS) {
            return "'class'";
        }
        else if (tc == TokenCode.STATIC) {
            return "'static'";
        }
        else if (tc == TokenCode.VOID) {
            return "'void'";
        }
        else if (tc == TokenCode.LBRACE) {
            return "'{'";
        }
        else if (tc == TokenCode.RBRACE) {
            return "'}'";
        }
        else if (tc == TokenCode.LBRACKET) {
            return "'['";
        }
        else if (tc == TokenCode.RBRACKET) {
            return "']'";
        }
        else if (tc == TokenCode.LPAREN) {
            return "'('";
        }
        else if (tc == TokenCode.RPAREN) {
            return "')'";
        }
        else if (tc == TokenCode.INT || tc == TokenCode.REAL) {
            return "a type";
        }
        else if (tc == TokenCode.EOF) {
            return "end of file";
        }
        return "";
    }

}