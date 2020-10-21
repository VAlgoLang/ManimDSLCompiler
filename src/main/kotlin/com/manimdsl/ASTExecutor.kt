package com.manimdsl

import com.manimdsl.frontend.*
import com.manimdsl.linearrepresentation.*
import com.manimdsl.shapes.Rectangle
import java.util.*

// Wrapper classes for values of variables while executing code
sealed class ExecValue

data class DoubleValue(val value: Double, val manimObject: MObject? = null) : ExecValue()

data class StackValue(val initObject: MObject, val stack: Stack<Pair<Double, MObject>>) : ExecValue()

object EmptyValue : ExecValue()

class ASTExecutor(
        private val program: ProgramNode,
        private val symbolTableVisitor: SymbolTableVisitor,
        private val fileLines: List<String>
) {

    private val linearRepresentation = mutableListOf<ManimInstr>()
    private val variableNameGenerator = VariableNameGenerator(symbolTableVisitor)

    private val codeBlockVariable: String
    private val codeTextVariable: String
    private val pointerVariable: String

    init {
        pointerVariable = variableNameGenerator.generateNameFromPrefix("pointer")
        codeBlockVariable = variableNameGenerator.generateNameFromPrefix("code_block")
        codeTextVariable = variableNameGenerator.generateNameFromPrefix("code_text")
    }

    // Map of all the variables and there current values
    private val variables = mutableMapOf<String, ExecValue>()

    // Keep track of next instruction to execute
    private var programCounter = 0

    private val finalDSLCode = mutableListOf<String>()

    private fun executeExpression(node: ExpressionNode, insideMethodCall: Boolean = false): ExecValue {
        return when (node) {
            is IdentifierNode -> variables[node.identifier]!!
            is NumberNode -> DoubleValue(node.double)
            is MethodCallNode -> executeMethodCall(node, insideMethodCall)
            is AddExpression -> executeBinaryOp(node) { x, y -> x + y }
            is SubtractExpression -> executeBinaryOp(node) { x, y -> x - y }
            is MultiplyExpression -> executeBinaryOp(node) { x, y -> x * y }
            is PlusExpression -> executeUnaryOp(node) { x -> x }
            is MinusExpression -> executeUnaryOp(node) { x -> -x }
            else -> EmptyValue
        }
    }

    private fun executeMethodCall(node: MethodCallNode, insideMethodCall: Boolean): ExecValue {
        return when (val ds = variables[node.instanceIdentifier]) {
            is StackValue -> {
                return when (node.dataStructureMethod) {
                    is StackType.PushMethod -> {
                        val doubleValue = executeExpression(node.arguments[0], true) as DoubleValue
                        val secondObject = if (ds.stack.empty()) ds.initObject else ds.stack.peek().second

                        val hasOldShape = doubleValue.manimObject != null

                        val rectangleShape = Rectangle(doubleValue.value.toString())
                        val oldShape = doubleValue.manimObject ?: EmptyMObject
                        val rectangle = if (hasOldShape) oldShape else NewMObject(
                            rectangleShape,
                            variableNameGenerator.generateShapeName(rectangleShape),
                            codeTextVariable
                        )

                        val instructions =
                            mutableListOf<ManimInstr>(MoveObject(rectangle.ident, secondObject.ident, ObjectSide.ABOVE))
                        if (!hasOldShape) {
                            instructions.add(0, rectangle)
                        }

                        linearRepresentation.addAll(instructions)
                        ds.stack.push(Pair(doubleValue.value, rectangle))
                        doubleValue
                    }
                    is StackType.PopMethod -> {
                        val poppedValue = ds.stack.pop()
                        val secondObject = if (ds.stack.empty()) ds.initObject else ds.stack.peek().second

                        val topOfStack = poppedValue.second
                        val instructions = listOf(
                            MoveObject(
                                topOfStack.ident,
                                secondObject.ident,
                                ObjectSide.ABOVE,
                                20,
                                !insideMethodCall
                            ),
                        )

                        linearRepresentation.addAll(instructions)
                        DoubleValue(poppedValue.first, poppedValue.second)
                    }
                    else -> EmptyValue
                }
            }
            else -> EmptyValue
        }
    }

    private fun executeConstructor(node: ConstructorNode, identifier: String): ExecValue {
        return when (node.type) {
            is StackType -> {
                val numStack = variables.values.filterIsInstance(StackValue::class.java).lastOrNull()
                val (instructions, newObject) = if (numStack == null) {
                    val stackInit = InitStructure(
                        2,
                        -1,
                        Alignment.HORIZONTAL,
                        variableNameGenerator.generateNameFromPrefix("empty"),
                        identifier
                    )
                    // Add to stack of objects to keep track of identifier
                    Pair(listOf(stackInit), stackInit)
                } else {
                    val stackInit = InitStructureRelative(
                        Alignment.HORIZONTAL,
                        variableNameGenerator.generateNameFromPrefix("empty"),
                        identifier,
                        numStack.initObject.ident
                    )
                    Pair(listOf(stackInit), stackInit)
                }
                linearRepresentation.addAll(instructions)
                StackValue(newObject, Stack())
            }
        }
    }

    private fun executeUnaryOp(node: UnaryExpression, op: (first: Double) -> Double): ExecValue {
        return DoubleValue(op((executeExpression(node.expr) as DoubleValue).value))
    }

    private fun executeBinaryOp(node: BinaryExpression, op: (first: Double, seconds: Double) -> Double): ExecValue {
        return DoubleValue(
            op(
                (executeExpression(node.expr1) as DoubleValue).value,
                (executeExpression(node.expr2) as DoubleValue).value
            )
        )
    }

    // Returns whether the program is complete and the state of all the variables after executing the statement
    fun executeNextStatement(): Pair<Boolean, List<ManimInstr>> {
        val node = program.statements[programCounter]
        programCounter++

        if (node is CodeNode) {
            finalDSLCode.add(fileLines[node.lineNumber - 1])
            linearRepresentation.add(MoveToLine(finalDSLCode.size, pointerVariable, codeBlockVariable))
        }

        when (node) {
            is DeclarationOrAssignment -> visitAssignmentOrDeclaration(node)
            is ExpressionNode -> executeExpression(node)
            is SleepNode -> linearRepresentation.add(Sleep((executeExpression(node.sleepTime) as DoubleValue).value))
            is CommentNode -> TODO()
            is DeclarationNode -> TODO()
            is AssignmentNode -> TODO()
            is ReturnNode -> TODO()
        }

        val endOfProgram = program.statements.size == programCounter
        if (endOfProgram) {
            linearRepresentation.add(0, CodeBlock(finalDSLCode, codeBlockVariable, codeTextVariable, pointerVariable))
        }

        return Pair(endOfProgram, linearRepresentation)
    }

    private fun visitAssignmentOrDeclaration(node: DeclarationOrAssignment) {
        variables[node.identifier] =
            when (node.expression) {
                is ConstructorNode -> executeConstructor(node.expression as ConstructorNode, node.identifier)
                else -> executeExpression(node.expression)
            }
    }

    fun getValue(identifier: String): ExecValue {
        return this.variables.getOrDefault(identifier, EmptyValue)
    }
}