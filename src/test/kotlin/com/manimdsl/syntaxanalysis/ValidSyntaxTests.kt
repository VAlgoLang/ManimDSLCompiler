package com.manimdsl.syntaxanalysis

import com.manimdsl.ExitStatus
import com.manimdsl.ManimDSLParser
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream

class ValidSyntaxTests {
    companion object {
        @JvmStatic
        fun data(): Stream<Arguments> {
            return File("src/test/testFiles/valid/").walk().filter { it.isFile }.map { Arguments.of(it.path) }
                .asStream()
        }

        @JvmStatic
        @BeforeAll
        internal fun initialiseBinFolder() {
            File("src/test/testFiles/bin").mkdir()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            File("src/test/testFiles/bin").deleteRecursively()
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    fun testFileIsSyntacticallyValid(fileName: String) {
        val inputFile = File(fileName)
        val parser = ManimDSLParser(inputFile.inputStream())
        val (syntaxErrorStatus, _) = parser.parseFile()
        assertEquals(ExitStatus.EXIT_SUCCESS, syntaxErrorStatus)
    }
}
