package com.thyde;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class TokenDumper {

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(new FileReader(args[0]));
        SymbolTable symbolTable = new SymbolTable();

        while(true) {
            Token t = lexer.yylex();
            System.out.print(t.getTokenCode().toString());
            if (t.getTokenCode() == TokenCode.IDENTIFIER || t.getTokenCode() == TokenCode.NUMBER) {
                System.out.print("(" + t.symbolTableEntry.lexeme + ")");
                symbolTable.AddEntry(t.symbolTableEntry);
            }

            System.out.print(" ");

            if (t.getTokenCode() == TokenCode.EOF)
                break;
        }

        System.out.println();
        System.out.println();

        Iterator it = symbolTable.GetTable();
        int index = 0;
        while (it.hasNext()) {
            Map.Entry<String, SymbolTableEntry> me = (Map.Entry)it.next();
            SymbolTableEntry entry = me.getValue();
            System.out.println(index + " " + entry.lexeme);
            index++;
        }
    }

}
