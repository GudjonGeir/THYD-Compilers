package com.thyde;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class Parser {
    private static Lexer lexer;
    private static Token currentToken;
    private static Token prevToken;
    private static ErrorHandler errorHandler;
    protected static final boolean TRACE = false;

    private static CodeGenerator codeGenerator;
    private static int tempNameCounter;
    private static int lableCounter;



    public static void main(String[] args) throws IOException{
        Parse(args[0]);
    }


    public static void Parse(String filePath) throws IOException{
        lexer = new Lexer(new FileReader(filePath));
        errorHandler = new ErrorHandler(lexer, filePath);
        currentToken = lexer.yylex();

        codeGenerator = new CodeGenerator();
        tempNameCounter = 1;
        lableCounter = 1;


        Program();
        match(TokenCode.EOF);

        if (errorHandler.m_errorCount == 0) {
            codeGenerator.PrintCode();
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
        String identifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);
        codeGenerator.generateVariable(identifier);
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
        String methodIdentifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);
        match(TokenCode.LPAREN);

        LinkedList<String> paramList = Parameters();

        codeGenerator.generateMethod(methodIdentifier, paramList);

        match(TokenCode.RPAREN);
        match(TokenCode.LBRACE);
        Variable_declarations();
        Statement_list();
        match(TokenCode.RBRACE);
        codeGenerator.generateStatement(TacCode.RETURN, null, null, null);
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

    private static LinkedList<String>  Parameters() {
        errorHandler.startNonT(NonT.PARAMETERS);
        // FIRST(Parameter_list()) = {INT, REAL}
        if (lookaheadIn(NonT.firstOf(NonT.PARAMETER_LIST))) {
            LinkedList<String> paramList = Parameter_list();
            errorHandler.stopNonT();
            return paramList;
        }
        // epsilon
        errorHandler.stopNonT();
        return new LinkedList<String>();
    }

    private static LinkedList<String> Parameter_list() {
        errorHandler.startNonT(NonT.PARAMETER_LIST);
        Type();
        String identifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);

        LinkedList<String> paramList = Parameter_list_prime();
        paramList.addFirst(identifier);
        errorHandler.stopNonT();
        return paramList;
    }

    private static LinkedList<String> Parameter_list_prime() {
        errorHandler.startNonT(NonT.PARAMETER_LIST2);
        if (lookaheadIs(TokenCode.COMMA) && !errorHandler.inRecovery()) {
            match(TokenCode.COMMA);
            Type();
            String identifier = getCurrentLexeme();
            match(TokenCode.IDENTIFIER);

            LinkedList<String> paramList  = Parameter_list_prime();
            paramList.addFirst(identifier);

            errorHandler.stopNonT();
            return paramList;
        }
        // epsilon
        errorHandler.stopNonT();
        return new LinkedList<String>();
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
            SymbolTableEntry lab1 = NewLabel();
            SymbolTableEntry lab2 = NewLabel();

            SymbolTableEntry tempVar = Expression();
            SymbolTableEntry zero = codeGenerator.TableLookup("0");
            codeGenerator.generateStatement(TacCode.EQ, tempVar, zero, lab2);
            match(TokenCode.RPAREN);
            Statement_block();
//            codeGenerator.generateStatement(TacCode.GOTO, null, null, lab1);
//            codeGenerator.generateStatement(TacCode.LABEL, null, null, lab2);
            Optional_else(lab1, lab2);
//            codeGenerator.generateStatement(TacCode.LABEL, null, null, lab1);
        }
        else if (lookaheadIs(TokenCode.FOR)) {
            trace("for");
            match(TokenCode.FOR);
            match(TokenCode.LPAREN);

            SymbolTableEntry varloc1Entry = Variable_loc();
            match(TokenCode.ASSIGNOP);
            SymbolTableEntry expr1Entry = Expression();
            match(TokenCode.SEMICOLON);
            codeGenerator.generateStatement(TacCode.ASSIGN, expr1Entry, null, varloc1Entry);

            SymbolTableEntry lab1 = NewLabel(); // Start of loop
            SymbolTableEntry lab2 = NewLabel(); // End of loop
            codeGenerator.SetCurrentEndOfLoop(lab2);
            codeGenerator.generateStatement(TacCode.LABEL, null, null, lab1);

            SymbolTableEntry expr2Entry = Expression();

            SymbolTableEntry zero = codeGenerator.TableLookup("0");
            SymbolTableEntry one = codeGenerator.TableLookup("1");
            codeGenerator.generateStatement(TacCode.EQ, expr2Entry, zero, lab2);

            SymbolTableEntry lab5 = NewLabel(); // iteration action
            codeGenerator.SetCurrentStartOfLoop(lab5);


            match(TokenCode.SEMICOLON);
            SymbolTableEntry varloc2Entry = Variable_loc();
            OpType incdecop = currentToken.getOpType();
            TacCode incdecTac = incdecop == OpType.INC ? TacCode.ADD : TacCode.SUB;
            match(TokenCode.INCDECOP);
            match(TokenCode.RPAREN);


            Statement_block();

            codeGenerator.generateStatement(TacCode.LABEL, null, null, lab5);
            codeGenerator.generateStatement(incdecTac, varloc2Entry, one, varloc2Entry);
            codeGenerator.generateStatement(TacCode.GOTO, null, null, lab1);
            codeGenerator.generateStatement(TacCode.LABEL, null, null, lab2);
        }
        else if (lookaheadIs(TokenCode.RETURN)) {
            trace("return");
            match(TokenCode.RETURN);
            Optional_expression();
//            codeGenerator.generateStatement(TacCode.RETURN, null, null, null);
            match(TokenCode.SEMICOLON);
        }
        else if (lookaheadIs(TokenCode.BREAK)) {
            trace("break");
            match(TokenCode.BREAK);
            match(TokenCode.SEMICOLON);
            codeGenerator.generateStatement(TacCode.GOTO, null, null, codeGenerator.GetCurrentEndOfLoop());
        }
        else if (lookaheadIs(TokenCode.CONTINUE)) {
            trace("continue");
            match(TokenCode.CONTINUE);
            match(TokenCode.SEMICOLON);
            codeGenerator.generateStatement(TacCode.GOTO, null, null, codeGenerator.GetCurrentStartOfLoop());
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
        String identifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);
        Statement_prime_prime(identifier);
        match(TokenCode.SEMICOLON);
        errorHandler.stopNonT();
    }

    private static void Statement_prime_prime(String identifier) {
        errorHandler.startNonT(NonT.STATEMENT3);
        if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            SymbolTableEntry idEntry = codeGenerator.TableLookup(identifier);
            LinkedList exprList = Expression_list();
            codeGenerator.generateMethodCall(idEntry, exprList);
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.INCDECOP)) {
            OpType op = currentToken.getOpType();
            match(TokenCode.INCDECOP);
            SymbolTableEntry entry = codeGenerator.TableLookup(identifier);
            SymbolTableEntry one =  codeGenerator.TableLookup("1");
            TacCode tc = null;
            if(op == OpType.INC) {
                tc = TacCode.ADD;
            }
            else {
                tc = TacCode.SUB;
            }

            codeGenerator.generateStatement(tc, entry, one, entry);
        }
        else if (lookaheadIs(TokenCode.ASSIGNOP)) {
            match(TokenCode.ASSIGNOP);
            SymbolTableEntry exprEntry = Expression();
            SymbolTableEntry idEntry = codeGenerator.TableLookup(identifier);
            codeGenerator.generateStatement(TacCode.ASSIGN, exprEntry, null, idEntry);
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
            SymbolTableEntry exprEntry = Expression();
            if (exprEntry != null) {
                codeGenerator.generateStatement(TacCode.ASSIGN, exprEntry, null, codeGenerator.GetCurrentMethod());
            }
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

    private static void Optional_else(SymbolTableEntry endOfIf, SymbolTableEntry endOfElse) {
        errorHandler.startNonT(NonT.OPTIONAL_ELSE);
        if (lookaheadIs(TokenCode.ELSE)) {
            match(TokenCode.ELSE);
            codeGenerator.generateStatement(TacCode.GOTO, null, null, endOfIf);
            codeGenerator.generateStatement(TacCode.LABEL, null, null, endOfElse);
            Statement_block();
            codeGenerator.generateStatement(TacCode.LABEL, null, null, endOfIf);
        }
        else {
            // Prevent double labels
            codeGenerator.generateStatement(TacCode.LABEL, null, null, endOfElse);
        }
        // epsilon
        errorHandler.stopNonT();
    }

    private static LinkedList<SymbolTableEntry> Expression_list() {
        errorHandler.startNonT(NonT.EXPRESSION_LIST);
        LinkedList<SymbolTableEntry> exprList = new LinkedList<SymbolTableEntry>();
        if (lookaheadIsFirstOfExpression()) {
            SymbolTableEntry exprEntry = Expression();
            exprList = More_expressions();
            exprList.addFirst(exprEntry);
        }
        // epsilon
        errorHandler.stopNonT();
        return exprList;
    }

    private static LinkedList<SymbolTableEntry> More_expressions() {
        errorHandler.startNonT(NonT.MORE_EXPRESSIONS);
        LinkedList<SymbolTableEntry> exprList = new LinkedList<SymbolTableEntry>();
        if (lookaheadIs(TokenCode.COMMA) && !errorHandler.inRecovery()) {
            match(TokenCode.COMMA);
            SymbolTableEntry exprEntry = Expression();
            exprList = More_expressions();
            exprList.addFirst(exprEntry);
        }
        // epsilon
        errorHandler.stopNonT();
        return exprList;
    }

    private static SymbolTableEntry Expression() {
        errorHandler.startNonT(NonT.EXPRESSION);
        SymbolTableEntry simpleExprEntry = Simple_expression();
        SymbolTableEntry exprPrimeEntry = Expression_prime(simpleExprEntry);
        errorHandler.stopNonT();
        if (exprPrimeEntry == null) {
            return simpleExprEntry;
        }
        return exprPrimeEntry;
    }

    private static SymbolTableEntry Expression_prime(SymbolTableEntry parentEntry) {
        errorHandler.startNonT(NonT.EXPRESSION2);
        SymbolTableEntry entry = null;
        if (lookaheadIs(TokenCode.RELOP)) {
            TacCode op = TacCode.OpTypeToTacCode(currentToken.getOpType());
            match(TokenCode.RELOP);
            SymbolTableEntry simpleExprEntry = Simple_expression();

            SymbolTableEntry label1 = NewLabel();
            SymbolTableEntry label2 = NewLabel();
            SymbolTableEntry zero = codeGenerator.TableLookup("0");
            SymbolTableEntry one = codeGenerator.TableLookup("1");
            SymbolTableEntry tempVar = NewTemp();

            codeGenerator.generateStatement(op, parentEntry, simpleExprEntry, label1);
            codeGenerator.generateStatement(TacCode.ASSIGN, zero, null, tempVar);
            codeGenerator.generateStatement(TacCode.GOTO, null, null, label2);
            codeGenerator.generateStatement(TacCode.LABEL, null, null, label1);
            codeGenerator.generateStatement(TacCode.ASSIGN, one, null, tempVar);
            codeGenerator.generateStatement(TacCode.LABEL, null, null, label2);
            entry = tempVar;
        }
        // epsilon
        errorHandler.stopNonT();
        return entry;
    }

    private static SymbolTableEntry Simple_expression() {
        errorHandler.startNonT(NonT.SIMPLE_EXPRESSION);
        OpType uminus = null;
        if (lookaheadIn(NonT.firstOf(NonT.SIGN))) {
            uminus = Sign();
        }
        SymbolTableEntry termEntry = Term();

        if (uminus == OpType.MINUS) {
            SymbolTableEntry tempEntry = NewTemp();
            codeGenerator.generateStatement(TacCode.UMINUS, termEntry, null, tempEntry);
            termEntry = tempEntry;
        }

        SymbolTableEntry simpleExprPrimeEntry = Simple_expression_prime(termEntry);
        errorHandler.stopNonT();
        // Simple_expression_prime can result in epsilon.
        if(simpleExprPrimeEntry == null) {
            return termEntry;
        }
        return simpleExprPrimeEntry;
    }

    // Receives a STE from parent in case an ADDOP is present, then it creates a temp var and
    // generates tac code for the expression and returns the entry for the temp var.
    // If addop is not present it returns a null entry (epsilon)
    private static SymbolTableEntry Simple_expression_prime(SymbolTableEntry parentEntry) {
        errorHandler.startNonT(NonT.SIMPLE_EXPRESSION2);
        SymbolTableEntry entry = null;
        if (lookaheadIs(TokenCode.ADDOP)) {
            OpType op = currentToken.getOpType();
            match(TokenCode.ADDOP);

            SymbolTableEntry termEntry = Term();
            SymbolTableEntry tempEntry = NewTemp();
            TacCode opType = TacCode.OpTypeToTacCode(op);
            codeGenerator.GenerateExpression(opType, parentEntry, termEntry, tempEntry);
            SymbolTableEntry sExprPrimeEntry = Simple_expression_prime(tempEntry);
            if (sExprPrimeEntry != null) {
                entry = sExprPrimeEntry;
            }
            else {
                entry = tempEntry;
            }
        }
        // epsilon
        errorHandler.stopNonT();
        return entry;
    }

    private static SymbolTableEntry Term() {
        errorHandler.startNonT(NonT.TERM);
        SymbolTableEntry factorEntry = Factor();
        SymbolTableEntry termPrimeEntry = Term_prime(factorEntry);
        errorHandler.stopNonT();
        if (termPrimeEntry == null) {
            return factorEntry;
        }
        return termPrimeEntry;
    }

    // Receives a STE from parent in case an MULOP is present, then it creates a temp var and
    // generates tac code for the term and returns the entry for the temp var.
    // If MULOP is not present it returns a null entry (epsilon)
    private static SymbolTableEntry Term_prime(SymbolTableEntry parentEntry) {
        errorHandler.startNonT(NonT.TERM2);
        SymbolTableEntry entry = null;
        if (lookaheadIs(TokenCode.MULOP)) {
            OpType op = currentToken.getOpType();
            match(TokenCode.MULOP);
            SymbolTableEntry factorEntry = Factor();
            SymbolTableEntry tempEntry = NewTemp();
            TacCode opType = TacCode.OpTypeToTacCode(op);
            codeGenerator.GenerateExpression(opType, parentEntry, factorEntry, tempEntry);
            SymbolTableEntry termPrimeEntry = Term_prime(tempEntry);
            if (termPrimeEntry != null) {
                entry = termPrimeEntry;
            }
            else{
                entry = tempEntry;
            }
        }
        // epsilon
        errorHandler.stopNonT();
        return entry;
    }

    private static SymbolTableEntry Factor() {
        errorHandler.startNonT(NonT.FACTOR);
        SymbolTableEntry entry = null;
        if (lookaheadIs(TokenCode.IDENTIFIER)) {
            entry = Factor_prime();
        }
        else if (lookaheadIs(TokenCode.NUMBER)) {
            String num = getCurrentLexeme();
            match(TokenCode.NUMBER);
            entry = codeGenerator.CreateTableEntry(num);
        }
        else if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            entry = Expression();
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.NOT)) {
            match(TokenCode.NOT);
            entry = Factor();
        }
        else { // TODO: Add error context, i.e. factor
            noMatch();
        }
        errorHandler.stopNonT();
        return entry;
    }

    private static SymbolTableEntry Factor_prime() {
        errorHandler.startNonT(NonT.FACTOR2);
        String identifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);
        SymbolTableEntry entry = codeGenerator.TableLookup(identifier);

        Factor_prime_prime(entry);
        errorHandler.stopNonT();
        return entry;
    }

    private static void Factor_prime_prime(SymbolTableEntry parentEntry) {
        errorHandler.startNonT(NonT.FACTOR3);
        if (lookaheadIs(TokenCode.LPAREN)) {
            match(TokenCode.LPAREN);
            LinkedList exprList = Expression_list();
            codeGenerator.generateMethodCall(parentEntry, exprList);
            match(TokenCode.RPAREN);
        }
        else if (lookaheadIs(TokenCode.LBRACKET)) {
            match(TokenCode.LBRACKET);
            Expression();
            match(TokenCode.RBRACKET);
        }
        errorHandler.stopNonT();
    }

    private static SymbolTableEntry Variable_loc() {
        errorHandler.startNonT(NonT.VARIABLE_LOC);
        String identifier = getCurrentLexeme();
        match(TokenCode.IDENTIFIER);
        SymbolTableEntry idEntry = codeGenerator.TableLookup(identifier);
        Variable_loc_prime();
        errorHandler.stopNonT();
        return idEntry;
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

    private static OpType Sign() {
        errorHandler.startNonT(NonT.SIGN);
        OpType opType = null;
        if (lookaheadIsFirstOfSign()) {
            opType = currentToken.getOpType();
            match(TokenCode.ADDOP);
        }
        else { // TODO: Add error context, i.e. sign
            noMatch();
        }
        errorHandler.stopNonT();
        return opType;
    }

    /***********************************
     * Start of parser helper functions
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

    /************************************
     * Start of code generation functions
     ************************************/

    private static String getCurrentLexeme() {
        return currentToken.getTokenLexeme();
    }
    private static SymbolTableEntry NewTemp() {
        while(true) {
            String name = "t" + tempNameCounter;
            SymbolTableEntry lookup = codeGenerator.TableLookup(name);
            if (lookup != null) {
                continue;
            }
            SymbolTableEntry entry = codeGenerator.generateVariable(name);
            tempNameCounter++;
            return entry;
        }
    }

    private static SymbolTableEntry NewLabel() {
        while (true) {
            String label = "lab" + lableCounter;
            SymbolTableEntry lookup = codeGenerator.TableLookup(label);
            if (lookup != null) {
                continue;
            }
            SymbolTableEntry entry = codeGenerator.CreateTableEntry(label);
            lableCounter++;
            return entry;
        }

    }
}