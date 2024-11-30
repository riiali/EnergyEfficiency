package org.energyCons.NoVisitor;

class SimpleAdditionNode implements SimpleNode {
    private final SimpleNode left;
    private final SimpleNode right;

    public SimpleAdditionNode(SimpleNode left, SimpleNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int evaluate() {
        return left.evaluate() + right.evaluate();
    }
}