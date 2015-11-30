package com.thyde;

import java.util.*;

public class QuadrupleList {
    private ArrayList<Quadruple> qList;
    public int tacCodeMaxLength;
    public int param1MaxLength;
    public int param2MaxLength;
    public int resMaxLength;


    public QuadrupleList() {
        qList = new ArrayList<Quadruple>();
        tacCodeMaxLength = 0;
        param1MaxLength = 0;
        param2MaxLength = 0;
        resMaxLength = 0;
    }

    public void AddQuadruple(Quadruple q) {
        if (q.GetTacCode().toString().length() > tacCodeMaxLength) {
            tacCodeMaxLength = q.GetTacCode().toString().length();
        }
        if (q.GetParam1() != null && q.GetParam1().lexeme.length() > param1MaxLength) {
            param1MaxLength = q.GetParam1().lexeme.length();
        }
        if (q.GetParam2() != null && q.GetParam2().lexeme.length() > param2MaxLength) {
            param2MaxLength = q.GetParam2().lexeme.length();
        }
        if (q.GetResult() != null && q.GetResult().lexeme.length() > resMaxLength) {
            resMaxLength = q.GetResult().lexeme.length();
        }
        qList.add(q);
    }

    public void AddQuadrupleAt(Quadruple q, int index) {
        qList.add(index, q);
    }

    public int Size() {
        return qList.size();
    }

    public ListIterator<Quadruple> GetIterator() {
        return qList.listIterator();
    }
}
