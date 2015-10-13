package com.thyde;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class SymbolTable {
    private static LinkedHashMap<String, SymbolTableEntry> table = new LinkedHashMap<String, SymbolTableEntry>();

    public static SymbolTableEntry AddEntry(String lexeme)
    {
        SymbolTableEntry entry = new SymbolTableEntry(lexeme);
        table.put(lexeme, entry);
        return entry;
    }

    public static SymbolTableEntry GetEntry(String lexeme)
    {
        return table.get(lexeme);
    }

    public static Iterator<SymbolTableEntry> GetTable() {
        Set entrySet = table.entrySet();

        Iterator it = entrySet.iterator();
        return it;
    }
}
