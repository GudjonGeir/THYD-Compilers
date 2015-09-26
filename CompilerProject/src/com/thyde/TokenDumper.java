package com.thyde;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class TokenDumper {

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer(new FileReader(args[0]));

        while(true) {
            Token t = lexer.yylex();
            System.out.print(t.getTokenCode().toString());
            if (t.getTokenCode() == TokenCode.IDENTIFIER || t.getTokenCode() == TokenCode.NUMBER) {
                System.out.print("(" + t.getSymbolTableEntry().lexeme + ")");
            }

            if (t.getTokenCode() == TokenCode.EOF)
                break;
            else
                System.out.print(" ");
        }

        System.out.println();
        System.out.println();

        Iterator it = SymbolTable.GetTable();
        int index = 0;
        while (it.hasNext()) {
            Map.Entry<String, SymbolTableEntry> me = (Map.Entry)it.next();
            SymbolTableEntry entry = me.getValue();
            System.out.println(index + " " + entry.lexeme);
            index++;
        }
    }

}
