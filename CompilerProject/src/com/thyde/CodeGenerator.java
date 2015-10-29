package com.thyde;

import java.util.*;

public class CodeGenerator {
    private static QuadrupleList program;
    protected SymbolTable globalTable;
    private SymbolTable currentLocalTable;

    // Variables to contain where "goto main" belongs in
    // the tac code
    private int gotoMainIndex;
    private boolean methodsStarted;

    public CodeGenerator() {
        program = new QuadrupleList();
        globalTable = new SymbolTable();

        methodsStarted = false;

    }

    public void generateVariable(String identifier) {
        SymbolTableEntry entry = globalTable.AddEntry(identifier);
        Quadruple q = new Quadruple(TacCode.VAR, null, null, entry);
        program.AddQuadruple(q);
    }

    public void generateMethod(String identifier, LinkedList<String> paramList) {
        // If this is the first time a method is generated we save the number
        // of global variables declared before so we know where to add the
        // "goto main" quadruple
        if (!methodsStarted) {
            gotoMainIndex = program.Size();
            methodsStarted = true;
        }
        currentLocalTable = new SymbolTable();

        SymbolTableEntry methodEntry = globalTable.AddEntry(identifier);
        Quadruple methodQ = new Quadruple(TacCode.LABEL, null, null, methodEntry);
        program.AddQuadruple(methodQ);

        // If this is the main method we add the "goto main" quadruple at the correct
        // index
        if (identifier.equals("main")) {
            Quadruple gotoQ = new Quadruple(TacCode.GOTO, null, null, methodEntry);
            program.AddQuadrupleAt(gotoQ, gotoMainIndex);
        }

        // Iterate over the parameters and add quadruples for them
        ListIterator<String> listIterator = paramList.listIterator();
        while (listIterator.hasNext()) {
            SymbolTableEntry paramEntry = currentLocalTable.AddEntry(listIterator.next());
            Quadruple paramQ = new Quadruple(TacCode.FPARAM, null, null, paramEntry);
            program.AddQuadruple(paramQ);
        }
    }

    public void generateStatement(TacCode tacCode, SymbolTableEntry p1, SymbolTableEntry p2, SymbolTableEntry res) {
        Quadruple q = new Quadruple(tacCode, p1, p2, res);
        program.AddQuadruple(q);
    }

    public SymbolTableEntry TableLookup(String lexeme) {
        SymbolTableEntry entry = currentLocalTable.GetEntry(lexeme);
        if (entry == null) {
            entry = globalTable.GetEntry(lexeme);
        }
        return entry;
    }

    public void PrintCode() {
        ListIterator<Quadruple> iterator = program.GetIterator();
        while (iterator.hasNext()) {
            Quadruple q = iterator.next();
            TacCode tc = q.GetTacCode();
            String param1 = q.GetParam1() != null ? q.GetParam1().lexeme : "";
            String param2 = q.GetParam2() != null ? q.GetParam2().lexeme : "";
            String result = q.GetResult() != null ? q.GetResult().lexeme : "";
            System.out.println("TacCode: " + tc + ", param1: " + param1 + ", param2: " + param2 + ", result: " + result);
        }
    }
}
