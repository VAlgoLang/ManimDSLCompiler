package com.manimdsl.linearrepresentation

import com.manimdsl.frontend.DataStructureType
import com.manimdsl.runtime.ExecValue
import com.manimdsl.shapes.*

/** Objects **/

interface MObject : ManimInstr {
    val shape: Shape
}

/** Positioning **/
interface Position
object RelativeToMoveIdent : Position

data class Coord(val x: Double, val y: Double) : Position {
    override fun toString(): String {
        return "$x, $y"
    }
}

enum class ObjectSide(var coord: Coord) {
    ABOVE(Coord(0.0, 0.25)),
    BELOW(Coord(0.0, -0.25)),
    LEFT(Coord(-0.25, 0.0)),
    RIGHT(Coord(0.25, 0.0));

    fun addOffset(offset: Int): Coord {
        if (this == ABOVE) {
            return Coord(this.coord.x, this.coord.y + offset)
        }
        return coord
    }

    override fun toString(): String {
        return coord.toString()
    }
}

/** MObjects **/
data class CodeBlock(
    val lines: List<List<String>>,
    val ident: String,
    val codeTextName: String,
    val pointerName: String,
    val textColor: String? = null,
) : MObject {
    override val shape: Shape = CodeBlockShape(ident, textColor)

    override fun toPython(): List<String> {
        val codeLines = StringBuilder()
        codeLines.append("[")
        (lines.indices).forEach {
            codeLines.append("[")
            codeLines.append("\"${lines[it].joinToString("\",\"")}\"")
            if (it == lines.size - 1) {
                codeLines.append("]")
            } else {
                codeLines.append("], ")
            }
        }
        codeLines.append("]")

        return listOf(
            "# Building code visualisation pane",
            "code_lines = $codeLines",
            shape.getConstructor(),
            "$codeTextName = $ident.build()",
            "$codeTextName.set_width(4.2)",
            "$codeTextName.next_to(variable_frame, DOWN, buff=0.9)",
            "$codeTextName.to_edge(buff=MED_LARGE_BUFF)",
            "self.code_end = len(code_lines) if self.code_end > len(code_lines) else self.code_end",
            "self.play(FadeIn($codeTextName[self.code_start:self.code_end]))",
            "# Constructing current line pointer",
            "$pointerName = ArrowTip(color=YELLOW).scale(0.7).flip(TOP)"
        )
    }
}

data class PartitionBlock(
    val scaleLeft: String,
    val scaleRight: String,
) : MObject {
    override val shape: Shape = NullShape
    override fun toPython(): List<String> {
        return listOf(
            "# Building partition of scene",
            "width = FRAME_WIDTH",
            "height = FRAME_HEIGHT",
            "lhs_width = width * $scaleLeft",
            "rhs_width = width * $scaleRight",
            "variable_height = (height - SMALL_BUFF) * $scaleLeft",
            "code_height = (height - SMALL_BUFF) * $scaleRight",
            "variable_frame = Rectangle(height=variable_height, width=lhs_width, color=BLACK)",
            "variable_frame.to_corner(UL, buff=0)",
            "code_frame = Rectangle(height=code_height, width=lhs_width, color=BLACK)",
            "code_frame.next_to(variable_frame, DOWN, buff=0) \n"
        )
    }
}

data class VariableBlock(
    val variables: List<String>,
    val ident: String,
    val variableGroupName: String,
    val variableFrame: String,
    val textColor: String? = null,
) : MObject {
    override val shape: Shape = VariableBlockShape(ident, variables, variableFrame, textColor)

    override fun toPython(): List<String> {
        return listOf(
            "# Building variable visualisation pane",
            shape.getConstructor(),
            "$variableGroupName = $ident.build()",
            "$variableGroupName.move_to($variableFrame)",
            "self.play(FadeIn($variableGroupName))"
        )
    }
}

sealed class DataStructureMObject(
    open val type: DataStructureType,
    open val ident: String,
    private var boundaries: List<Pair<Double, Double>> = emptyList()
) : MObject {

    abstract fun setNewBoundary(corners: List<Pair<Double, Double>>, newMaxSize: Int)

}

data class InitManimStack(
    override val type: DataStructureType,
    override val ident: String,
    val position: Position,
    val alignment: Alignment,
    val text: String,
    val moveToShape: Shape? = null,
    val color: String? = null,
    val textColor: String? = null,
    private var boundary: List<Pair<Double, Double>> = emptyList(),
    private var maxSize: Int = -1
) : DataStructureMObject(type, ident, boundary) {
    override var shape: Shape = NullShape

    override fun toPython(): List<String> {
        val python =
            mutableListOf("# Constructing new ${type} \"${text}\"", shape.getConstructor())
        python.add("self.play($ident.create_init(\"$text\"))")
        return python
    }

    override fun setNewBoundary(corners: List<Pair<Double, Double>>, newMaxSize: Int) {
        maxSize = newMaxSize
        boundary = corners
        shape = InitManimStackShape(ident, text, boundary, alignment, color, textColor)
    }
}

data class ArrayStructure(
    override val type: DataStructureType,
    override val ident: String,
    val text: String,
    val values: Array<ExecValue>,
    val color: String? = null,
    val textColor: String? = null,
    var maxSize: Int = -1,
    private var boundaries: List<Pair<Double, Double>> = emptyList()
) : DataStructureMObject(type, ident, boundaries) {
    override var shape: Shape = NullShape

    override fun toPython(): List<String> {
        return listOf(
            "# Constructing new $type \"$text\"",
            shape.getConstructor(),
            "self.play(ShowCreation($ident.title))",
            "self.play(*[ShowCreation(array_elem.all) for array_elem in $ident.array_elements])"
        )
    }

    override fun setNewBoundary(corners: List<Pair<Double, Double>>, newMaxSize: Int) {
        maxSize = newMaxSize
        boundaries = corners
        shape = ArrayShape(ident, values, text, boundaries, color, textColor)
    }
}

data class NewMObject(override val shape: Shape, val codeBlockVariable: String) : MObject {
    override fun toPython(): List<String> {
        return listOf(
            "# Constructs a new ${shape.className} with value ${shape.text}",
            shape.getConstructor(),
        )
    }
}

object EmptyMObject : MObject {
    override val shape: Shape = NullShape
    override fun toPython(): List<String> = emptyList()
}