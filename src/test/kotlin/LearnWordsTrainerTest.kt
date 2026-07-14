package org.example.additional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestionTest {
    private val hello = Word(original = "hello", translate = "привет")
    private val dog = Word(original = "dog", translate = "собака")
    private val cat = Word(original = "cat", translate = "кошка")
    private val sun = Word(original = "sun", translate = "солнце")

    private val question = Question(
        variants = listOf(hello, dog, cat, sun),
        correctAnswer = hello,
    )

    @Test
    fun `full format with four variants` () {
        val expected = "hello\n" +
                "1 - привет\n" +
                "2 - собака\n" +
                "3 - кошка\n" +
                "4 - солнце\n" +
                "0 - выйти в меню"

        assertEquals(expected, question.asConsoleString())
    }

    @Test
    fun `first line is original of correct answer`() {
        val firstLine = question.asConsoleString().lines().first()

        assertEquals("hello", firstLine)
    }

    @Test
    fun `variants are numbered starting from one`() {
        val lines = question.asConsoleString().lines()

        assertTrue(lines[1].startsWith("1 - "))
        assertTrue(lines[2].startsWith("2 - "))
        assertTrue(lines[3].startsWith("3 - "))
        assertTrue(lines[4].startsWith("4 - "))
    }

    @Test
    fun `variants show translations not originals`() {
        val result = question.asConsoleString()
        assertTrue(result.contains("собака"))
        assertTrue(result.contains("кошка"))
        assertTrue(!result.contains("2 - dog"))
        assertTrue(!result.contains("3 - cat"))
    }

    @Test
    fun `line count equals variants count plus two`() {
    val lines = question.asConsoleString().lines()

    assertEquals(question.variants.size + 2, lines.size)
}
    @Test
    fun `single variant question`() {
        val singleQuestion = Question(
            variants = listOf(dog),
            correctAnswer = dog,)
        val expected = "dog\n" +
                "1 - собака\n" +
                "0 - выйти в меню"

        assertEquals(expected, singleQuestion.asConsoleString())
    }

    @Test
    fun `correct answer word appears in variants line too`() {
        assertTrue(question.asConsoleString().contains("1 - привет"))
    }

    }