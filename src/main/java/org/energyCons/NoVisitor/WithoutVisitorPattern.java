package org.energyCons.NoVisitor;

import java.util.List;

public class WithoutVisitorPattern {
    public static void sum(List<Integer> numbers) {
        int result = 0;
        for(int number : numbers) {
            result += number;
        }
        System.out.println("Risultato Senza Visitor Pattern: " + result);
    }
}