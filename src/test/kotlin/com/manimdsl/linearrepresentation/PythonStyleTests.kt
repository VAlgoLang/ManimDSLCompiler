package com.manimdsl.linearrepresentation

import com.manimdsl.shapes.Rectangle
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test


class PythonStyleTests {

    @Test
    fun stylesAreMappedToPythonCorrectly() {

        val color = "white"
        val textColor = "black"
        val rectangle = Rectangle("x", "rectangle", "stack1", color = color, textColor = textColor, hidden = false)

        val mobject = NewMObject(rectangle, "codeBlock")

        val expected =
            "x = Rectangle_block(\"rectangle\", stack1, color=${color.toUpperCase()}, text_color=${textColor.toUpperCase()})"
        assertEquals(expected, mobject.toPython()[1])
    }

    @Test
    fun hexadecimalColorsAreMappedCorrectly() {

        val color = "#ffffff"
        val textColor = "#ffffff"
        val rectangle = Rectangle("x", "rectangle", "stack1", color = color, textColor = textColor, hidden = false)

        val mobject = NewMObject(rectangle, "codeBlock")

        val expected =
            "x = Rectangle_block(\"rectangle\", stack1, color=\"$color\", text_color=\"$textColor\")"
        assertEquals(expected, mobject.toPython()[1])
    }

    @Test
    fun invalidTextWeightsAreReturnedToDefault() {

        val rectangle = Rectangle("x", "rectangle", "stack1", hidden = false)

        val mobject = NewMObject(rectangle, "codeBlock")

        val expected =
            "x = Rectangle_block(\"rectangle\", stack1)"
        assertEquals(expected, mobject.toPython()[1])
    }
}