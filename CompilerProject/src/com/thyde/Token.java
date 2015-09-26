package com.thyde;

public class Token {
    private TokenCode _tokenCode;
    private DataType _dataType;
    private OpType _opType;
    private SymbolTableEntry _symbolTableEntry;
    private int _lineNumber;
    private int _column;

    public Token(TokenCode tc, DataType dt, OpType ot, SymbolTableEntry ste, int lineNumber, int column)
    {
        _tokenCode        = tc;
        _dataType         = dt;
        _opType           = ot;
        _symbolTableEntry = ste;
        _lineNumber       = lineNumber;
        _column           = column;
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

    public int getLineNumber()
    {
        return _lineNumber;
    }

    public int getColumn(){
        return _column;
    }
}
