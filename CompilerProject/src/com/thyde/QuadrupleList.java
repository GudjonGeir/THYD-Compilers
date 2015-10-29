package com.thyde;

import java.util.*;

public class QuadrupleList {
    private static ArrayList<Quadruple> qList;

    public QuadrupleList() {
        qList = new ArrayList<Quadruple>();
    }

    public void AddQuadruple(Quadruple q) {
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
