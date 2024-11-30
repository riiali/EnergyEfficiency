package org.energyCons.Visitor;

interface Visitor {
    void visitNumberNode(NumberNode node);
    void visitAdditionNode(AdditionNode node);
}