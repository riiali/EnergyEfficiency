package org.energyCons.Visitor;

class EvaluationVisitor implements Visitor {
    private int result;

    public int getResult() {
        return result;
    }

    @Override
    public void visitNumberNode(NumberNode node) {
        result = node.getValue();
    }

    @Override
    public void visitAdditionNode(AdditionNode node) {
        node.getLeft().accept(this);
        int leftResult = result;
        node.getRight().accept(this);
        int rightResult = result;
        result = leftResult + rightResult;
    }
}
