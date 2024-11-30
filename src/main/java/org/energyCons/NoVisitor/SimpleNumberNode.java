package org.energyCons.NoVisitor;

class SimpleNumberNode implements SimpleNode {
    private final int value;

    public SimpleNumberNode(int value) {
        this.value = value;
    }

    @Override
    public int evaluate() {
        return value;
    }
}