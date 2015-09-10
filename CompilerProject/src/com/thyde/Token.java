package com.thyde;

public class Token {
    public TokenCode tokenCode;
    public DataType dataType;
    public OpType opType;
    public SymbolTableEntry symbolTableEntry;

    public Token(TokenCode tc, DataType dt, OpType ot, SymbolTableEntry ste) {
        tokenCode = tc;
        dataType = dt;
        opType = ot;
        symbolTableEntry = ste;
    }

    public TokenCode getTokenCode() {
        return tokenCode;
    }
}
