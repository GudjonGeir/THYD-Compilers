package com.thyde;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class SymbolTable {
    private LinkedHashMap<String, SymbolTableEntry> table;

    public SymbolTable(){
        table = new LinkedHashMap();
    }

    public void AddEntry(SymbolTableEntry ste) {
        table.put(ste.lexeme, ste);
    }

    public SymbolTableEntry GetEntry(String lexeme) {
        return table.get(lexeme);
    }

    public Iterator<SymbolTableEntry> GetTable() {
        Set entrySet = table.entrySet();

        Iterator it = entrySet.iterator();
        return it;
    }
}
