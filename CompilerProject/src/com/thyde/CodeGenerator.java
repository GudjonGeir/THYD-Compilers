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

    // Used in case of continue or break
    private SymbolTableEntry currentStartOfLoop;
    private SymbolTableEntry currentEndOfLoop;

    // Used in optional return value
    private SymbolTableEntry currentMethod;

    private boolean mainFound;

    public CodeGenerator() {
        program = new QuadrupleList();
        globalTable = new SymbolTable();
        globalTable.AddEntry("writeln");
        globalTable.AddEntry("write");
        currentLocalTable = null;

        methodsStarted = false;

        currentStartOfLoop = null;
        currentEndOfLoop = null;

        currentMethod = null;

        mainFound = false;
    }

    public SymbolTableEntry generateVariable(String identifier) {
        SymbolTableEntry entry = null;
        if (methodsStarted) {
            entry = currentLocalTable.AddEntry(identifier);
        } else {
            entry = globalTable.AddEntry(identifier);
        }
        Quadruple q = new Quadruple(TacCode.VAR, null, null, entry);
        program.AddQuadruple(q);
        return entry;
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
        currentMethod = methodEntry;
        Quadruple methodQ = new Quadruple(TacCode.LABEL, null, null, methodEntry);
        program.AddQuadruple(methodQ);

        // If this is the main method we add the "goto main" quadruple at the correct
        // index
        if (identifier.equals("main")) {
            Quadruple gotoQ = new Quadruple(TacCode.GOTO, null, null, methodEntry);
            program.AddQuadrupleAt(gotoQ, gotoMainIndex);
            mainFound = true;
        }

        // Iterate over the parameters and add quadruples for them
        ListIterator<String> listIterator = paramList.listIterator();
        while (listIterator.hasNext()) {
            SymbolTableEntry paramEntry = currentLocalTable.AddEntry(listIterator.next());
            Quadruple paramQ = new Quadruple(TacCode.FPARAM, null, null, paramEntry);
            program.AddQuadruple(paramQ);
        }
    }

    public void generateMethodCall(SymbolTableEntry identifier, LinkedList<SymbolTableEntry> paramList) {
        // Iterate over the parameters and add quadruples for them
        ListIterator<SymbolTableEntry> listIterator = paramList.listIterator();
        while (listIterator.hasNext()) {
            Quadruple paramQ = new Quadruple(TacCode.APARAM, null, null, listIterator.next());
            program.AddQuadruple(paramQ);
        }
        Quadruple callQ = new Quadruple(TacCode.CALL, identifier, null, null);
        program.AddQuadruple(callQ);
    }

    public void generateStatement(TacCode tacCode, SymbolTableEntry p1, SymbolTableEntry p2, SymbolTableEntry res) {
        Quadruple q = new Quadruple(tacCode, p1, p2, res);
        program.AddQuadruple(q);
    }

    public void GenerateExpression(TacCode tc, SymbolTableEntry p1, SymbolTableEntry p2, SymbolTableEntry res) {
        Quadruple q = new Quadruple(tc, p1, p2, res);
        program.AddQuadruple(q);
    }


    public SymbolTableEntry CreateTableEntry(String lexeme) {
        if (currentLocalTable != null) {
            return currentLocalTable.AddEntry(lexeme);
        }
        return globalTable.AddEntry(lexeme);
    }

    public SymbolTableEntry TableLookup(String lexeme) {
        SymbolTableEntry entry = currentLocalTable.GetEntry(lexeme);
        if (entry == null) {
            entry = globalTable.GetEntry(lexeme);
        }
        return entry;
    }

    public SymbolTableEntry GetCurrentStartOfLoop() {
        return currentStartOfLoop;
    }

    public void SetCurrentStartOfLoop(SymbolTableEntry label) {
        currentStartOfLoop = label;
    }

    public SymbolTableEntry GetCurrentEndOfLoop() {
        return currentEndOfLoop;
    }

    public void SetCurrentEndOfLoop(SymbolTableEntry label) {
        currentEndOfLoop = label;
    }

    public SymbolTableEntry GetCurrentMethod() {
        return currentMethod;
    }

    public void PrintCode() {
        if (!mainFound) {
            System.out.println("Error: Main method not found");
            return;
        }
        ListIterator<Quadruple> iterator = program.GetIterator();
        int labelBuffer = 3;
        int tacCodeBuffer = 4;
        int param1Buffer = 8;
        int param2Buffer = 13;
        int resBuffer = 11;
        while (iterator.hasNext()) {
            Quadruple q = iterator.next();
            TacCode tc = q.GetTacCode();
            String param1 = q.GetParam1() != null ? q.GetParam1().lexeme : "";
            String param2 = q.GetParam2() != null ? q.GetParam2().lexeme : "";
            String result = q.GetResult() != null ? q.GetResult().lexeme : "";
            if (tc == TacCode.LABEL) {
                PrintSpaces(labelBuffer + program.resMaxLength - result.length());
                System.out.print(result + ":");
                q = iterator.next();
                tc = q.GetTacCode();
                param1 = q.GetParam1() != null ? q.GetParam1().lexeme : "";
                param2 = q.GetParam2() != null ? q.GetParam2().lexeme : "";
                result = q.GetResult() != null ? q.GetResult().lexeme : "";

            }
            else {
                PrintSpaces(labelBuffer + program.resMaxLength + 1);
            }
            PrintSpaces(tacCodeBuffer + program.tacCodeMaxLength - tc.toString().length());
            System.out.print(tc.toString());

            PrintSpaces(param1Buffer + program.param1MaxLength - param1.length());
            System.out.print(param1);

            PrintSpaces(param2Buffer + program.param2MaxLength - param2.length());
            System.out.print(param2);


            PrintSpaces(resBuffer + program.resMaxLength - result.length());
            System.out.println(result);
        }
    }

    public void PrintSpaces(int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(" ");
        }
    }

    public void PrintTables() {
        System.out.println("========Global table========");
        Iterator it = globalTable.GetTable();
        int index = 0;
        while (it.hasNext()) {
            Map.Entry<String, SymbolTableEntry> me = (Map.Entry)it.next();
            SymbolTableEntry entry = me.getValue();
            System.out.println(entry.lexeme);
            index++;
        }
        System.out.println("===========================");

        System.out.println("========Local table========");
        it = currentLocalTable.GetTable();
        index = 0;
        while (it.hasNext()) {
            Map.Entry<String, SymbolTableEntry> me = (Map.Entry)it.next();
            SymbolTableEntry entry = me.getValue();
            System.out.println(entry.lexeme);
            index++;
        }
        System.out.println("===========================");
    }
}
