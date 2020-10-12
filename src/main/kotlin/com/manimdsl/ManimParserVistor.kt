package com.manimdsl

import antlr.ManimParser
import antlr.ManimParserBaseVisitor
import com.manimdsl.frontend.*

class ManimParserVisitor: ManimParserBaseVisitor<ASTNode>() {
    private val currentSymbolTable = SymbolTableNode()
    private val semanticAnalyser = SemanticAnalysis()
    override fun visitProgram(ctx: ManimParser.ProgramContext): ProgramNode {
        return ProgramNode(ctx.stat().map { visit(it) as StatementNode })
    }

    override fun visitSleepStatement(ctx: ManimParser.SleepStatementContext): SleepNode {
        return SleepNode(visit(ctx.expr()) as ExpressionNode)
    }

    override fun visitDeclarationStatement(ctx: ManimParser.DeclarationStatementContext): DeclarationNode {
        val identifier = ctx.IDENT().symbol.text
        // Type work done in Symbol Table - we can keep it entirely external or can reference from the AST Node here
        if (!semanticAnalyser.failIfRedeclaredVariable(currentSymbolTable, identifier)) {
            println("Redeclared!!")
        }
        val expression = visit(ctx.expr()) as ExpressionNode

        val type = if (ctx.type() != null) {
            visit(ctx.type()) as Type
        } else {
            semanticAnalyser.inferType(currentSymbolTable, expression)
        }

        currentSymbolTable.addVariable(identifier, type)
        return DeclarationNode(ctx.start.line, identifier, expression)
    }

    override fun visitAssignmentStatement(ctx: ManimParser.AssignmentStatementContext): AssignmentNode {
        val expression = visit(ctx.expr()) as ExpressionNode
        val identifier = ctx.IDENT().symbol.text
        val rhsType = semanticAnalyser.inferType(currentSymbolTable, expression)
        val identifierType = currentSymbolTable.getTypeOf(identifier)

        if (semanticAnalyser.undeclaredAssignment(currentSymbolTable, identifier)) {
            println("Undeclared Assignment!!")
        }

        if (semanticAnalyser.failIfIncompatibleTypes(identifierType, rhsType)) {
            println("Incompatible Types!!")
        }
        return AssignmentNode(ctx.start.line, identifier, expression)
    }

    override fun visitMethodCallStatement(ctx: ManimParser.MethodCallStatementContext): MethodCallNode {
        // This one ignores return value/is a command returning void
        return visitMethodCall(ctx.method_call() as ManimParser.MethodCallContext)
    }

    override fun visitMethodCallExpression(ctx: ManimParser.MethodCallExpressionContext): MethodCallNode {
        return visitMethodCall(ctx.method_call() as ManimParser.MethodCallContext)
    }

    override fun visitArgumentList(ctx: ManimParser.ArgumentListContext?): ArgumentNode {
        return ArgumentNode((ctx?.expr()
                ?: listOf<ManimParser.ExprContext>()).map { visit(it) as ExpressionNode })
    }

    override fun visitMethodCall(ctx: ManimParser.MethodCallContext): MethodCallNode {
        // Type signature of methods to be determined by symbol table
        val arguments = visitArgumentList(ctx.arg_list() as ManimParser.ArgumentListContext?).arguments
        return MethodCallNode(ctx.start.line, ctx.IDENT(0).symbol.text, ctx.IDENT(1).symbol.text, arguments)
    }

    override fun visitStackCreate(ctx: ManimParser.StackCreateContext): ConstructorNode {
        return ConstructorNode(ctx.start.line, StackType, listOf())
    }

    override fun visitIdentifier(ctx: ManimParser.IdentifierContext): IdentifierNode {
        return IdentifierNode(ctx.start.line, ctx.text)
    }

    override fun visitBinaryExpression(ctx: ManimParser.BinaryExpressionContext): BinaryExpression {
        val expr1 = visit(ctx.expr(0)) as ExpressionNode
        val expr2 = visit(ctx.expr(1)) as ExpressionNode
        return when (ctx.binary_operator.type) {
            ManimParser.ADD -> AddExpression(ctx.start.line, expr1, expr2)
            ManimParser.MINUS -> SubtractExpression(ctx.start.line, expr1, expr2)
            else -> MultiplyExpression(ctx.start.line, expr1, expr2)
        }
    }

    override fun visitUnaryOperator(ctx: ManimParser.UnaryOperatorContext): UnaryExpression {
        val expr = visit(ctx.expr()) as ExpressionNode
        return when (ctx.unary_operator.type) {
            ManimParser.ADD -> PlusExpression(ctx.start.line, expr)
            else -> MinusExpression(ctx.start.line, expr)
        }
    }

    override fun visitCommentStatement(ctx: ManimParser.CommentStatementContext): CommentNode {
        // Command command given for render purposes
        return CommentNode(ctx.STRING().text)
    }
    override fun visitNumberLiteral(ctx: ManimParser.NumberLiteralContext): NumberNode {
        return NumberNode(ctx.start.line, ctx.NUMBER().symbol.text.toDouble())
    }

    override fun visitNumberType(ctx: ManimParser.NumberTypeContext): NumberType {
        return NumberType
    }

    override fun visitStackType(ctx: ManimParser.StackTypeContext): StackType {
        return StackType
    }
}