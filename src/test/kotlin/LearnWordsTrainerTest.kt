package org.example.additional

import java.io.File
import kotlin.test.AfterTest
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
            correctAnswer = dog,
        )
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

class LearnWordsTrainerTest {

    private lateinit var dictionaryFile: File

    @AfterTest
    fun tearDown() {
        if (::dictionaryFile.isInitialized) dictionaryFile.delete()
    }

    @Test
    fun `statistics with 4 words of 7`() {
        dictionaryFile = File.createTempFile("test_words", ".txt")
        dictionaryFile.writeText(
            "hello|привет|3\n" +
                    "dog|собака|3\n" +
                    "cat|кошка|3\n" +
                    "sun|солнце|3\n" +
                    "moon|луна|1\n" +
                    "star|звезда|0\n" +
                    "sky|небо|2\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)

        val statistics = trainer.getStatistics()

        assertEquals(7, statistics.totalCount)
        assertEquals(4, statistics.learnedCount)
        assertEquals(57, statistics.percent)
    }

    @Test
    fun `statistics with corrupted file`() {
        dictionaryFile = File.createTempFile("test_words_corrupted", ".txt")
        dictionaryFile.writeText(
            "hello|привет|3\n" +
                    "dog|собака\n" +
                    "sun|солнце|1\n"
        )

        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)
        val statistics = trainer.getStatistics()

        assertEquals(2, statistics.totalCount)
    }

    @Test
    fun `getNextQuestion with 5 unlearned words`() {
        dictionaryFile = File.createTempFile("test_words_5unlearned", ".txt")
        dictionaryFile.writeText(
            "hello|привет|0\n" +
                    "dog|собака|0\n" +
                    "cat|кошка|1\n" +
                    "sun|солнце|2\n" +
                    "moon|луна|0\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)

        val question = trainer.getNextQuestion()

        assertTrue(question != null)
        assertTrue(question!!.variants.contains(question.correctAnswer))
        assertTrue(question.variants.size <= 4)
    }

    @Test
    fun `getNextQuestion with 1 unlearned word`() {
        dictionaryFile = File.createTempFile("test_words_1unlearned", ".txt")
        dictionaryFile.writeText(
            "hello|привет|3\n" +
                    "dog|собака|4\n" +
                    "cat|кошка|0\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)

        val question = trainer.getNextQuestion()

        assertTrue(question != null)
        assertEquals(1, question!!.variants.size)
        assertEquals("cat", question.correctAnswer.original)
    }

    @Test
    fun `getNextQuestion with all words learned`() {
        dictionaryFile = File.createTempFile("test_words_alllearned", ".txt")
        dictionaryFile.writeText(
            "hello|привет|3\n" +
                    "dog|собака|4\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)

        val question = trainer.getNextQuestion()

        assertTrue(question == null)
    }

    @Test
    fun `checkAnswer returns true and increments count for correct index`() {
        dictionaryFile = File.createTempFile("test_words_checktrue", ".txt")
        dictionaryFile.writeText(
            "hello|привет|0\n" +
                    "dog|собака|1\n" +
                    "cat|кошка|3\n" +
                    "sun|солнце|4\n" +
                    "moon|луна|0\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)
        val question = trainer.getNextQuestion()!!
        val correctIndex = question.variants.indexOf(question.correctAnswer)
        val countBefore = question.correctAnswer.correctAnswersCount

        val result = trainer.checkAnswer(correctIndex)

        assertTrue(result)
        assertEquals(countBefore + 1, question.correctAnswer.correctAnswersCount)
    }

    @Test
    fun `checkAnswer returns false and does not change count for incorrect index`() {
        dictionaryFile = File.createTempFile("test_words_checkfalse", ".txt")
        dictionaryFile.writeText(
            "hello|привет|0\n" +
                    "dog|собака|1\n" +
                    "cat|кошка|3\n" +
                    "sun|солнце|4\n" +
                    "moon|луна|0\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)
        val question = trainer.getNextQuestion()!!
        val correctIndex = question.variants.indexOf(question.correctAnswer)
        val wrongIndex = (correctIndex + 1) % question.variants.size
        val countBefore = question.correctAnswer.correctAnswersCount

        val result = trainer.checkAnswer(wrongIndex)

        assertTrue(!result)
        assertEquals(countBefore, question.correctAnswer.correctAnswersCount)
    }

    @Test
    fun `resetProgress with 2 words in dictionary`() {
        dictionaryFile = File.createTempFile("test_words_2reset", ".txt")
        dictionaryFile.writeText(
            "hello|привет|3\n" +
                    "dog|собака|4\n"
        )
        val trainer = LearnWordsTrainer(dictionaryFile.absolutePath)
        trainer.getNextQuestion()

        trainer.resetProgress()
        val statistics = trainer.getStatistics()

        assertEquals(2, statistics.totalCount)
        assertEquals(0, statistics.learnedCount)
        assertTrue(trainer.question == null)
    }
}