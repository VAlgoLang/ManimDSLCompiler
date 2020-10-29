package com.manimdsl
import com.manimdsl.linearrepresentation.*
import com.manimdsl.runtime.VirtualMachine
import com.manimdsl.stylesheet.Stylesheet
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test


class ASTExecutorTests {


//    @Test
//    fun checkBasicFunction() {
//        val program =
//                "fun f(x : number): number{\n" +
//                "    return x * 3;\n" +
//                "}\n" +
//                "let ans = f(3);\n"
//
//        val (_, abstractSyntaxTree, symbolTable, lineNodeMap) = buildAST(program)
//
//        val expected = listOf(
//            PartitionBlock(
//                    scaleLeft = "1/3",
//                    scaleRight = "2/3"
//            ),
//            VariableBlock(
//                    variables = listOf(""),
//                    ident = "variable_block",
//                    variableGroupName = "variable_vg",
//                    variableFrame = "variable_frame",
//                    textColor = null
//            ),
//            CodeBlock(
//                lines = listOf("fun f(x : number): number{", "    return x * 3;", "}", "let ans = f(3);", ""),
//                ident = "code_block",
//                codeTextName = "code_text",
//                pointerName = "pointer",
//                textColor = null
//            ),
//            UpdateVariableState(variables= listOf(), ident="variable_block", textColor=null),
//            MoveToLine(lineNumber = 4, pointerName = "pointer", codeBlockName = "code_block", codeTextVariable = "code_text"),
//            UpdateVariableState(variables= listOf("\"x = 3.0\""), ident="variable_block", textColor=null),
//            MoveToLine(lineNumber = 1, pointerName = "pointer", codeBlockName = "code_block", codeTextVariable = "code_text"),
//            UpdateVariableState(variables= listOf("\"x = 3.0\""), ident="variable_block", textColor=null),
//            MoveToLine(lineNumber = 2, pointerName = "pointer", codeBlockName = "code_block", codeTextVariable = "code_text"),
//            MoveToLine(lineNumber = 4, pointerName = "pointer", codeBlockName = "code_block", codeTextVariable = "code_text"),
//            UpdateVariableState(variables= listOf("\"ans = 9.0\""), ident="variable_block", textColor=null),
//            UpdateVariableState(variables= listOf("\"ans = 9.0\""), ident="variable_block", textColor=null),
//            Sleep(length=1.0)
//        )
//        val actual = VirtualMachine(abstractSyntaxTree, symbolTable, lineNodeMap, program.split("\n"), Stylesheet(null, symbolTable)).runProgram().second
//
//        assertEquals(expected, actual)
//
//    }


    // Assumes syntactically correct program
    private fun buildAST(program: String): ParserResult {
        val parser = ManimDSLParser(program.byteInputStream())
        return parser.convertToAst(parser.parseFile().second)
    }
}