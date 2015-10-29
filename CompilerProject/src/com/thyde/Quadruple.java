package com.thyde;

public class Quadruple {
    private TacCode tacCode;
    private SymbolTableEntry param1;
    private SymbolTableEntry param2;
    private SymbolTableEntry result;

    public Quadruple(TacCode tacCode, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
        this.tacCode = tacCode;
        this.param1 = param1;
        this.param2 = param2;
        this.result = result;
    }

    public TacCode GetTacCode() {
        return tacCode;
    }

    public SymbolTableEntry GetParam1() {
        return param1;
    }

    public SymbolTableEntry GetParam2() {
        return param2;
    }

    public SymbolTableEntry GetResult() {
        return result;
    }
}
