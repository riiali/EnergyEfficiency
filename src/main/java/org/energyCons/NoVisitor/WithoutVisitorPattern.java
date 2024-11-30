package org.energyCons.NoVisitor;

import java.util.List;

public class WithoutVisitorPattern {
    /*public static void main(String[] args) {
        // not getting args for now
        SimpleNode tree = new SimpleAdditionNode(
                new SimpleNumberNode(5),
                new SimpleAdditionNode(new SimpleNumberNode(3), new SimpleNumberNode(2))
        );

        System.out.println("Risultato Senza Visitor Pattern: " + tree.evaluate());
    }*/
    public static void sum(List<Integer> numbers) {
        SimpleNode tree = new SimpleNumberNode(numbers.get(0));
        for (int i = 1; i < numbers.size(); i++) {
            tree = new SimpleAdditionNode(tree, new SimpleNumberNode(numbers.get(i)));
        }

        System.out.println("Risultato Senza Visitor Pattern: " + tree.evaluate());
    }
}