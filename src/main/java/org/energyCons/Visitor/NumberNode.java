package org.energyCons.Visitor;

class NumberNode implements Node {
    private final int value;

    public NumberNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNumberNode(this);
    }
}