package com.thyde;

import javax.swing.plaf.nimbus.State;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
    private static Lexer lexer;
    private static Token currentToken;
    private static Token prevToken;
    private static ErrorHandler errorHandler;
    protected static final boolean TRACE = false;



    public static void main(String[] args) throws IOException{
        Parse(args[0]);
    }


    public static void Parse(String filePath) throws IOException{
        lexer = new Lexer(new FileReader(filePath));
        errorHandler = new ErrorHandler(lexer, filePath);
        currentToken = lexer.yylex();

        Program();
        match(TokenCode.EOF);

        if (errorHandler.m_errorCount == 0) {
            System.out.println("No errors");
        }
        else {
            System.out.println("Number of errors: " + errorHandler.m_errorCount);
        }
    }

    private static void Program() {
        errorHandler.startNonT(NonT.PROGRAM);
        match(TokenCode.CLASS);
        match(TokenCode.IDENTIFIER);
        match(TokenCode.LBRACE);
        Variable_declarations();
        Method_declarations();
        match(TokenCode.RBRACE);
        errorHandler.stopNonT();
    }

    private static void Variable_declarations() {
        errorHandler.startNonT(NonT.VARIABLE_DECLARATIONS);
        // Peeking because Type starts with INT or REAL
        if (lookaheadIn(NonT.firstOf(NonT.TYPE))) {
            Type();
            Variable_list();
            match(TokenCode.SEMICOLON);
            Variable_declarations();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Type() {
        errorHandler.startNonT(NonT.TYPE);
        if (currentToken.getTokenCode() == TokenCode.INT) {
            match(TokenCode.INT);
        }
        else if (currentToken.getTokenCode() == TokenCode.REAL) {
            match(TokenCode.REAL);
        }
        else {
            noMatch(); // TODO: expected a type
        }
        errorHandler.stopNonT();
    }

    private static void Variable_list() {
        errorHandler.startNonT(NonT.VARIABLE_LIST);
        Variable();
        Variable_list_prime();
        errorHandler.stopNonT();
    }

    private static void Variable_list_prime() {
        errorHandler.startNonT(NonT.VARIABLE_LIST_2);
        if (lookaheadIs(TokenCode.COMMA)) {
            match(TokenCode.COMMA);
            Variable();
            Variable_list_prime();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Variable() {
        errorHandler.startNonT(NonT.VARIABLE);
        match(TokenCode.IDENTIFIER);
        if (lookaheadIs(TokenCode.LBRACKET)) {
            match(TokenCode.LBRACKET);
            match(TokenCode.NUMBER);
            match(TokenCode.RBRACKET);
        }
        errorHandler.stopNonT();
    }


    private static void Method_declarations() {
        errorHandler.startNonT(NonT.METHOD_DECLARATIONS);
        Method_declaration();
        More_method_declarations();
        errorHandler.stopNonT();
    }

    private static void More_method_declarations() {
        //FIRST(Method_declaration() = {STATIC}
        errorHandler.startNonT(NonT.MORE_METHOD_DECLARATIONS);
        if (lookaheadIn(NonT.firstOf(NonT.METHOD_DECLARATION))) {
            Method_declaration();
            More_method_declarations();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Method_declaration() {
        errorHandler.startNonT(NonT.METHOD_DECLARATION);
        match(TokenCode.STATIC);
        Method_return_type();
        match(TokenCode.IDENTIFIER);
        match(TokenCode.LPAREN);
        Parameters();
        match(TokenCode.RPAREN);
        match(TokenCode.LBRACE);
        Variable_declarations();
        Statement_list();
        match(TokenCode.RBRACE);
        errorHandler.stopNonT();
    }

    private static void Method_return_type() {
        errorHandler.startNonT(NonT.METHOD_RETURN_TYPE);
        if (lookaheadIs(TokenCode.VOID)) {
            match(TokenCode.VOID);
        }
        else {
            Type();
        }
        errorHandler.stopNonT();
    }

    private static void  Parameters() {
        errorHandler.startNonT(NonT.PARAMETERS);
        // FIRST(Parameter_list()) = {INT, REAL}
        if (lookaheadIn(NonT.firstOf(NonT.PARAMETER_LIST))) {
            Parameter_list();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Parameter_list() {
        errorHandler.startNonT(NonT.PARAMETER_LIST);
        Type();
        match(TokenCode.IDENTIFIER);
        Parameter_list_prime();
        errorHandler.stopNonT();
    }

    private static void Parameter_list_prime() {
        errorHandler.startNonT(NonT.PARAMETER_LIST2);
        if (lookaheadIs(TokenCode.COMMA) && !errorHandler.inRecovery()) {
            match(TokenCode.COMMA);
            Type();
            match(TokenCode.IDENTIFIER);
            Parameter_list_prime();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Statement_list() {
        errorHandler.startNonT(NonT.STATEMENT_LIST);
        // FIRST(Statement()) = { IDENTIFIER, IF, FOR, RETURN, BREAK, CONTINUE, LBRACE }
        if (lookaheadIn(NonT.firstOf(NonT.STATEMENT))	&& !errorHandler.inRecovery()) {
            Statement();
            Statement_list();
        }
        errorHandler.stopNonT();
        // epsilon
    }

    private static void Statement() {
        boolean noMatch = false;
        errorHandler.startNonT(NonT.STATEMENT);
        if (lookaheadIs(TokenCode.IDENTIFIER))
        {
            trace("idStartingStmt");
            Statement_prime();
        }
        else if (lookaheadIs(TokenCode.IF)) {
            trace("if");
            match(TokenCode.IF);
            match(TokenCode.LPAREN);
            Expression();
            match(TokenCode.RPAREN);
            Statement_block();
            Optional_else();
        }
        else if (lookaheadIs(TokenCode.FOR)) {
            trace("for");
            match(TokenCode.FOR);
            match(TokenCode.LPAREN);
            Variable_loc();
            match(TokenCode.ASSIGNOP);
            Expression();
            match(TokenCode.SEMICOLON);
            Expression();
            match(TokenCode.SEMICOLON);
            Variable_loc();
            match(TokenCode.INCDECOP);
            match(TokenCode.RPAREN);
            Statement_block();
        }
        else if (lookaheadIs(TokenCode.RETURN)) {
            trace("return");
            match(TokenCode.RETURN);
            Optional_expression();
            match(TokenCode.SEMICOLON);
        }
        else if (lookaheadIs(TokenCode.BREAK)) {
            trace("break");
            match(TokenCode.BREAK);
            match(TokenCode.SEMICOLON);
        }
        else if (lookaheadIs(TokenCode.CONTINUE)) {
            trace("continue");
            match(TokenCode.CONTINUE);
            match(TokenCode.SEMICOLON);
        }
        else if (lookaheadIs(TokenCode.RBRACE)) {
            trace("block");
            Statement_block();
        }
        else {
            trace("noMatch");
            noMatch = true;
            errorHandler.stopNonT();
            noMatch(); // TODO: invalid statement
        }
        if (!noMatch) {
            errorHandler.stopNonT();
        }
    }

    private static void Statement_prime() {
        errorHandler.startNonT(NonT.STATEMENT2);
        match(TokenCode.IDENTIFIER);
        Statement_prime_prime();
        match(TokenCode.SEMICOLON);
        errorHandler.stopNonT();
    }

    private static void Statement_prime_prime() {
        errorHandler.startNonT(NonT.STATEMENT3);
        if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            Expression_list();
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.INCDECOP)) {
            match(TokenCode.INCDECOP);
        }
        else if (lookaheadIs(TokenCode.ASSIGNOP)) {
            match(TokenCode.ASSIGNOP);
            Expression();
        }
        else if (lookaheadIs(TokenCode.LBRACKET)) {
            match(TokenCode.LBRACKET);
            Expression();
            match(TokenCode.RBRACKET);
            match(TokenCode.ASSIGNOP);
            Expression();
        }
        else // TODO: Add error context, i.e. idStartingStatement
            noMatch();
        errorHandler.stopNonT();
    }

    private static void Optional_expression() {
        errorHandler.startNonT(NonT.OPTIONAL_EXPRESSION);
        // FIRST(Expression) = { IDENTIFIER, NUMBER, NOT, LPAREN, MINUS, PLUS }
        if (lookaheadIsFirstOfExpression()) {
            Expression();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Statement_block() {
        errorHandler.startNonT(NonT.STATEMENT_BLOCK);
        match(TokenCode.LBRACE);
        Statement_list();
        match(TokenCode.RBRACE);
        errorHandler.stopNonT();
    }

    private static void Optional_else() {
        errorHandler.startNonT(NonT.OPTIONAL_ELSE);
        if (lookaheadIs(TokenCode.ELSE)) {
            match(TokenCode.ELSE);
            Statement_block();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Expression_list() {
        errorHandler.startNonT(NonT.EXPRESSION_LIST);
        if (lookaheadIsFirstOfExpression()) {
            Expression();
            More_expressions();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void More_expressions() {
        errorHandler.startNonT(NonT.MORE_EXPRESSIONS);
        if (lookaheadIs(TokenCode.COMMA) && !errorHandler.inRecovery()) {
            match(TokenCode.COMMA);
            Expression();
            More_expressions();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Expression() {
        errorHandler.startNonT(NonT.EXPRESSION);
        Simple_expression();
        Expression_prime();
        errorHandler.stopNonT();
    }

    private static void Expression_prime() {
        errorHandler.startNonT(NonT.EXPRESSION2);
        if (lookaheadIs(TokenCode.RELOP)) {
            match(TokenCode.RELOP);
            Simple_expression();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Simple_expression() {
        errorHandler.startNonT(NonT.SIMPLE_EXPRESSION);
        if (lookaheadIn(NonT.firstOf(NonT.SIGN)))
            Sign();
        Term();
        Simple_expression_prime();
        errorHandler.stopNonT();
    }

    private static void Simple_expression_prime() {
        errorHandler.startNonT(NonT.SIMPLE_EXPRESSION2);
        if (lookaheadIs(TokenCode.ADDOP)) {
            match(TokenCode.ADDOP);
            Term();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Term() {
        errorHandler.startNonT(NonT.TERM);
        Factor();
        Term_prime();
        errorHandler.stopNonT();
    }

    private static void Term_prime() {
        errorHandler.startNonT(NonT.TERM2);
        if (lookaheadIs(TokenCode.MULOP)) {
            match(TokenCode.MULOP);
            Factor();
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Factor() {
        errorHandler.startNonT(NonT.FACTOR);
        if (lookaheadIs(TokenCode.IDENTIFIER))
            Factor_prime();
        else if (lookaheadIs(TokenCode.NUMBER))
            match(TokenCode.NUMBER);
        else if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            Expression();
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.NOT)) {
            match(TokenCode.NOT);
            Factor();
        }
        else { // TODO: Add error context, i.e. factor
            noMatch();
        }
        errorHandler.stopNonT();
    }

    private static void Factor_prime() {
        errorHandler.startNonT(NonT.FACTOR2);
        match(TokenCode.IDENTIFIER);
        Factor_prime_prime();
        errorHandler.stopNonT();
    }

    private static void Factor_prime_prime() {
        errorHandler.startNonT(NonT.FACTOR3);
        if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            Expression_list();
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.LBRACKET)) {
            match(TokenCode.LBRACKET);
            Expression();
            match(TokenCode.RBRACKET);
        }
        errorHandler.stopNonT();
    }

    private static void Variable_loc() {
        errorHandler.startNonT(NonT.VARIABLE_LOC);
        match(TokenCode.IDENTIFIER);
        Variable_loc_prime();
        errorHandler.stopNonT();
    }

    private static void Variable_loc_prime() {
        errorHandler.startNonT(NonT.VARIABLE_LOC2);
        if (lookaheadIs(TokenCode.LBRACKET)) {
            match(TokenCode.LBRACKET);
            Expression();
            match(TokenCode.RBRACKET);
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static void Sign() {
        errorHandler.startNonT(NonT.SIGN);
        if (lookaheadIsFirstOfSign()) {
            match(TokenCode.ADDOP);
        }
        else { // TODO: Add error context, i.e. sign
            noMatch();
        }
        errorHandler.stopNonT();
    }

    /***********************************
     * Start of helper functions
     ***********************************/

    // Reads the next token.
    // If the compiler is in error recovery we do not actually read a new token, we just pretend we do.
    // We will get match failures which the ErrorHandler will supress. When we leave the procedure with
    // the offending non-terminal, the ErrorHandler will go out of recovery mode and start reading tokens again.
    private static void readNextToken() {
        try {
            // If the Error handler is in recovery mode, we don't read new tokens!
            // We simply use current tokens until the Error handler exits the recovery mode
            if (!errorHandler.inRecovery()) {
                prevToken = currentToken;
                currentToken = lexer.yylex();
                trace("++ Next token read: " + currentToken.getTokenCode());
                if (TRACE)
                    if (prevToken != null && prevToken.getLineNumber() != currentToken.getLineNumber())
                        System.out.println("Line " + currentToken.getLineNumber());
            }
            else
                trace("++ Next token skipped because of recovery: Still: " + currentToken.getTokenCode());
            // System.out.println(m_current.getTokenCode() + String.valueOf(m_current.getLineNum()) + ", col: " + String.valueOf(m_current.getColumnNum()));
        }
        catch(IOException e) {
            System.out.println("IOException reading next token");
            System.exit(1);
        }
    }

    // Returns the next token of the input, without actually reading it
    private static Token lookahead() {
        return currentToken;
    }

    // Returns true if the lookahead token has the given tokencode
    private static boolean lookaheadIs(TokenCode tokenCode) {
        return currentToken.getTokenCode() == tokenCode;
    }

    // Returns true if the lookahed token is included in the given array of token codes
    private static boolean lookaheadIn(TokenCode[] tokenCodes) {
        for(int n=0;n<tokenCodes.length;n++)
            if (tokenCodes[n] == currentToken.getTokenCode())
                return true;
        return false;
    }

    // Returns true if the lookahed token is in the FIRST of EXPRESSION.
    // Need to specially check if the token is ADDOP to make sure the token is +/-
    // (by checking the OpType of the token)
    private static boolean lookaheadIsFirstOfExpression() {
        if (!lookaheadIn(NonT.firstOf(NonT.EXPRESSION)))
            return false;
        if (lookaheadIs(TokenCode.ADDOP) && lookahead().getOpType() != OpType.PLUS && lookahead().getOpType() != OpType.MINUS)
            return false;
        else
            return true;
    }


    // Return true if the lookahed is the first of sign (actually if the lexeme for the token was '+' or '-')
    private static boolean lookaheadIsFirstOfSign() {
        return (lookaheadIs(TokenCode.ADDOP) && (lookahead().getOpType() == OpType.PLUS || lookahead().getOpType() == OpType.MINUS));
    }

    // Match the the token and read next token if match is successful.
    // If the match is unsuccessfull we let the ErrorHandler report the error and supply us with the next token to use.
    // This next token will then not be used until we leave the parsing method where the mismatch occured.
    // If the ErrorHandler is in the recovery state, it will suppress the error (not report it).
    private static void match(TokenCode tokenCode) {
        if (currentToken.getTokenCode() != tokenCode)
        {
            Token[] tokens = errorHandler.tokenMismatch(tokenCode, currentToken, prevToken);
            currentToken = tokens[0];
            prevToken = tokens[1];
            trace("	failed match for " + tokenCode + ". current: " + currentToken.getTokenCode() + ", prev: " + currentToken.getTokenCode());
        }
        else {
            trace("	Matched " + tokenCode);
            readNextToken();
        }
    }

    // Called when none the next token is none of the possible tokens for some given part of the non-terminal.
    // Behaviour is the same as match except that we have no specific token to match against.
    private static void noMatch() {
        Token[] tokens = errorHandler.noMatch(currentToken, prevToken);
        currentToken = tokens[0];
        prevToken = tokens[1];
    }


    private static void trace(String msg) {
        if (TRACE) {
            System.out.println(msg);
        }
    }

}