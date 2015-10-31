package com.thyde;
public enum TacCode {
    LABEL, UMINUS, ASSIGN, ADD, SUB, MULT, DIVIDE, DIV, MOD, OR, AND, NOT, LT,
    LE, GT, GE, EQ, NE, GOTO, CALL, APARAM, FPARAM, VAR, RETURN, NOOP;

    public static TacCode OpTypeToTacCode(OpType op) {
        switch (op) {
            case PLUS:
                return TacCode.ADD;
            case MINUS:
                return TacCode.SUB;
            case OR:
                return TacCode.OR;
            case MULT:
                return TacCode.MULT;
            case DIV:
                return TacCode.DIVIDE;
            case MOD:
                return TacCode.MOD;
            case LTE:
                return TacCode.LE;
            case GTE:
                return TacCode.GE;
            case EQUAL:
                return TacCode.EQ;
            case NOT_EQUAL:
                return TacCode.NE;
            case LT:
                return TacCode.LT;
            case GT:
                return TacCode.GT;
            case AND:
                return TacCode.AND;
            case ASSIGN:
                return TacCode.ASSIGN;
            default:
                return null;
        }
    }
}