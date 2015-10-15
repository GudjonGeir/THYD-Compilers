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
    // Prettify error statements
    // Comment helper functions
    // Change public functions to private
    // use prevToken to point to missing semicolon
    // Unexpected eof

    public static void Parse(String filePath) throws IOException {
        lexer = new Lexer(new FileReader(filePath));
        currentToken = lexer.yylex();
        codeLines = makeCodeLines(filePath);
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
        if(!Match(TokenCode.EOF)){
            PrintError(null, "End of class");
        }

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
           return;
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
            PrintError(TokenCode.INT, "expected");
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
                return;
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
            return;
        }
    }

    private static void Method_return_type() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.VOID) {
            if(!Match(TokenCode.VOID)){
                Sync("method_return_type");
                return;
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
                return;
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.BREAK) {
            if(!Match(TokenCode.BREAK)) {
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
                return;
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.CONTINUE) {
            if(!Match(TokenCode.CONTINUE)) {
                Sync("statement");
                return;
            }
            if(!Match(TokenCode.SEMICOLON)) {
                Sync("statement");
                return;
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
                return;
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
                return;
            }
        }
        else if (currentToken.getTokenCode() == TokenCode.INCDECOP) {
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
            PrintError(null, "invalidStatement");
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
            return;
        }
    }

    private static void Incr_decr_var() throws IOException {
        Variable_loc();
        if(!Match(TokenCode.INCDECOP)){
            Sync("incr_decr_var");
            return;
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
            PrintError(null, "invalidExpression");
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
            //currentToken = lexer.yylex();
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
                return;
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
                return;
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
            System.out.println("Factor");
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
                return;
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
                return;
            }
        }
        // epsilon
    }

    private static void Sign() throws IOException {
        if (currentToken.getTokenCode() == TokenCode.ADDOP) {
            if (currentToken.getOpType() == OpType.PLUS || currentToken.getOpType() == OpType.MINUS) {
                if(!Match(TokenCode.ADDOP)) {
                    Sync("sign");
                    return;
                }
            }
            else {
                System.out.println("Sign - currentToken.getTokenCode() == TokenCode.ADDOP || currentToken.getOpType() == OpType.MINUS");
            }
        }
        else {
            System.out.println("Sign - currentToken.getTokenCode() == TokenCode.ADDOP");
        }
    }

    private static boolean Match(TokenCode tc) throws IOException {
        if (currentToken.getTokenCode() != tc) {
            PrintError(tc, "expected");
            return false;
        }
        prevToken = currentToken;
        currentToken = lexer.yylex();
        if (currentToken.getTokenCode() == TokenCode.ERR_ILL_CHAR) {
            PrintError(null, "illegalChar");
            return false;
        }
        else if (currentToken.getTokenCode() == TokenCode.ERR_LONG_ID) {
            PrintError(null, "longID");
            return false;
        }
//        else if (currentToken.getTokenCode() == TokenCode.EOF && tc != TokenCode.EOF) {
//            PrintError(null, "unexpectedEOF");
//        }
        return true;
    }

    private static void PrintError(TokenCode expectedToken, String errorType) {
        errorCounter++;

        String errorMessage = "";
        int lineNr = currentToken.getLineNumber();
        int columnNr = currentToken.getColumn();


        if (errorType.equals("expected")) {
            if (expectedToken == TokenCode.SEMICOLON) {
                lineNr = prevToken.getLineNumber();
                columnNr = prevToken.getColumn();
            }
             errorMessage = "Expected: " + expectedToken + ", got: " + currentToken.getTokenCode();
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
        else if (errorType.equals("unexpectedEOF")) {
            lineNr = prevToken.getLineNumber();
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

    private static String[] makeCodeLines(String filePath){

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