package org.energyCons.Visitor;

class AdditionNode implements Node {
    private final Node left;
    private final Node right;

    public AdditionNode(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAdditionNode(this);
    }
}