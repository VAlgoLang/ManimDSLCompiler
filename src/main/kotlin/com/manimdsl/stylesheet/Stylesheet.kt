package com.manimdsl.stylesheet

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.manimdsl.errorhandling.ErrorHandler
import com.manimdsl.errorhandling.warnings.undeclaredVariableStyleWarning
import com.manimdsl.executor.ExecValue
import com.manimdsl.frontend.SymbolTableVisitor
import java.io.File
import java.lang.reflect.Type
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess

interface StylesheetProperty {
    val borderColor: String?
    val textColor: String?
}

data class AnimationProperties(override val borderColor: String? = null, override val textColor: String? = null) :
    StylesheetProperty

data class StyleProperties(
    override val borderColor: String? = null,
    override val textColor: String? = null,
    val animate: AnimationProperties? = null
) : StylesheetProperty


class Stylesheet(private val stylesheetPath: String?, private val symbolTableVisitor: SymbolTableVisitor) {

    private val stylesheet: Map<String, StyleProperties>
    private val dataStructureStrings = setOf("Stack")

    init {
        stylesheet = if (stylesheetPath != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<Map<String, StyleProperties>>() {}.type
            try {
                val stylesheetMap: Map<String, StyleProperties> = gson.fromJson(File(stylesheetPath).readText(), type)
                stylesheetMap.keys.forEach {
                    if (!(dataStructureStrings.contains(it) || symbolTableVisitor.getVariableNames().contains(it))) {
                        undeclaredVariableStyleWarning(it)
                    }
                }
                ErrorHandler.checkWarnings()
                stylesheetMap
            } catch (e: JsonSyntaxException) {
                print("Invalid JSON stylesheet: ")
                if (e.message.let { it != null && it.startsWith("duplicate key") }) {
                    println(e.message)
                } else {
                    println("Could not parse JSON")
                }
                exitProcess(1)
            }
        } else {
            emptyMap()
        }
    }

    fun getStyle(identifier: String, value: ExecValue): StylesheetProperty {
        val dataStructureStyle =
            stylesheet.getOrDefault(value.toString(), StyleProperties())
        val style = stylesheet.getOrDefault(identifier, dataStructureStyle)
        return style merge dataStructureStyle
    }

    fun getAnimatedStyle(identifier: String, value: ExecValue): AnimationProperties? {
        val dataStructureStyle =
            stylesheet.getOrDefault(value.toString(), StyleProperties())
        val style = stylesheet.getOrDefault(identifier, dataStructureStyle)
        return (style.animate ?: AnimationProperties()) merge (dataStructureStyle.animate ?: AnimationProperties())
    }
}

// Credit to https://stackoverflow.com/questions/44566607/combining-merging-data-classes-in-kotlin/44570679#44570679
inline infix fun <reified T : Any> T.merge(other: T): T {
    val propertiesByName = T::class.declaredMemberProperties.associateBy { it.name }
    val primaryConstructor = T::class.primaryConstructor
        ?: throw IllegalArgumentException("merge type must have a primary constructor")
    val args = primaryConstructor.parameters.associateWith { parameter ->
        val property = propertiesByName[parameter.name]
            ?: throw IllegalStateException("no declared member property found with name '${parameter.name}'")
        (property.get(this) ?: property.get(other))
    }
    return primaryConstructor.callBy(args)
}