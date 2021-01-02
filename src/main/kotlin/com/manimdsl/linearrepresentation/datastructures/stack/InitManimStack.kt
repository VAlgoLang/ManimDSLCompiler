package com.manimdsl.linearrepresentation.datastructures.stack

import com.manimdsl.frontend.DataStructureType
import com.manimdsl.linearrepresentation.*

data class InitManimStack(
    override val type: DataStructureType,
    override val ident: String,
    val position: Position,
    val alignment: Alignment,
    override var text: String,
    val color: String? = null,
    val textColor: String? = null,
    val showLabel: Boolean? = null,
    val creationStyle: String? = null,
    val creationTime: Double? = null,
    private var boundaries: List<Pair<Double, Double>> = emptyList(),
    private var maxSize: Int = -1,
    override val uid: String,
    override val runtime: Double = 1.0,
    override val render: Boolean
) : DataStructureMObject(type, ident, uid, text, boundaries) {
    override val classPath: String = "python/stack.py"
    override val className: String = "Stack"
    override val pythonVariablePrefix: String = ""

    init {
        color?.let { style.addStyleAttribute(Color(it)) }
        textColor?.let { style.addStyleAttribute(TextColor(it)) }
    }

    override fun getConstructor(): String {
        val coordinatesString = boundaries.joinToString(", ") { "[${it.first}, ${it.second}, 0]" }
        return "$ident = $className($coordinatesString, DOWN$style)"
    }

    override fun toPython(): List<String> {
        val creationString = if (creationStyle != null) ", creation_style=\"$creationStyle\"" else ""
        val runtimeString = if (creationTime != null) ", run_time=$creationTime" else ""
        val python =
            mutableListOf("# Constructing new $type \"${text}\"", getConstructor())
        val newIdent = if (showLabel == null || showLabel) "\"$text\"" else ""
        python.add(getInstructionString("$ident.create_init($newIdent$creationString)$runtimeString", true))
        return python
    }

    override fun setNewBoundary(corners: List<Pair<Double, Double>>, newMaxSize: Int) {
        maxSize = newMaxSize
        boundaries = corners
    }
}