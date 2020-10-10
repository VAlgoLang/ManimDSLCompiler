package com.manimdsl

import antlr.ManimLexer
import antlr.ManimParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream

class ManimDSLParser(private val input: InputStream) {

    // Build ANTLR Parse Tree and if Syntax Errors found, throw them and exit
    fun parseFile(): ManimParser.ProgramContext {
        val input = CharStreams.fromStream(input)
        val lexer = ManimLexer(input)
        lexer.removeErrorListeners()
        val tokens = CommonTokenStream(lexer)
        val parser = ManimParser(tokens)
        parser.removeErrorListeners()
//        parser.addErrorListener(SyntaxErrorListener())
        val program = parser.program()
//        if (parser.numberOfSyntaxErrors > 0) {
//            exitProcess(SYNTAX_ERROR)
//        }
        return program
    }

}
