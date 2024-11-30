package org.energyCons.Visitor;

import java.util.List;

public class VisitorPattern {
    /*public static void main(String[] args) {
        //not getting args for now

        Node tree = new AdditionNode(
                new NumberNode(5),
                new AdditionNode(new NumberNode(3), new NumberNode(2))
        );

        EvaluationVisitor visitor = new EvaluationVisitor();
        tree.accept(visitor);
        System.out.println("Risultato Visitor Pattern: " + visitor.getResult());
    }*/
    public static void sum(List<Integer> numbers) {
        Node tree = new NumberNode(numbers.get(0));
        for (int i = 1; i < numbers.size(); i++) {
            tree = new AdditionNode(tree, new NumberNode(numbers.get(i)));
        }
        EvaluationVisitor visitor = new EvaluationVisitor();
        tree.accept(visitor);
        System.out.println("Risultato Visitor Pattern: " + visitor.getResult());
    }
}