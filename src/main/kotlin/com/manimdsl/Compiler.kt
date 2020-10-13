package com.manimdsl

import java.io.File
import kotlin.system.exitProcess

private fun compile(filename: String) {
    val file = File(filename)
    if (!file.isFile) {
        // File argument was not valid
        println("Please enter a valid file className: ${file.name} not found")
        exitProcess(1)
    }

    println("Compiling...")
    val parser = ManimDSLParser(file.inputStream())
    val (exitStatus, program) = parser.parseFile()

    // Error handling
    if (exitStatus != ExitStatus.EXIT_SUCCESS) {
        exitProcess(exitStatus.code)
    }

    val abstractSyntaxTree = parser.convertToAst(program)
    // Error handling
    if (exitStatus != ExitStatus.EXIT_SUCCESS) {
        exitProcess(exitStatus.code)
    }
    println(abstractSyntaxTree)
}

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        // No argument passed in
        println("Please enter a file name")
        return
    }

    compile(args.first())

}

