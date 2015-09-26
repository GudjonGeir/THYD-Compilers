package com.thyde;

public class Token {
    private TokenCode _tokenCode;
    private DataType _dataType;
    private OpType _opType;
    private SymbolTableEntry _symbolTableEntry;

    public Token(TokenCode tc, DataType dt, OpType ot, SymbolTableEntry ste)
    {
        _tokenCode = tc;
        _dataType = dt;
        _opType = ot;
        _symbolTableEntry = ste;
    }

    public TokenCode getTokenCode()
    {
        return _tokenCode;
    }

    public DataType getDataType()
    {
        return _dataType;
    }

    public OpType getOpType()
    {
        return _opType;
    }

    public SymbolTableEntry getSymbolTableEntry()
    {
        return _symbolTableEntry;
    }
}
