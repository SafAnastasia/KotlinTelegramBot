package org.example.additional

import java.io.File
import java.io.IOException

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

data class Statistics(
    val totalCount: Int,
    val learnedWords: List<Word>,
    val percent: Int,
)

class LearnWordsTrainer {
    companion object {
        private const val SEPARATOR = "|"
        private const val PART = 3
        private const val ANSWER_OPTIONS = 4
        private const val CORRECT_ANSWERS = 3
        private const val WORDS_FILE = "words.txt"
    }

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS }
        if (notLearnedList.isEmpty()) return null

        val questionWords = notLearnedList.shuffled().take(minOf(ANSWER_OPTIONS, notLearnedList.size))
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size//всего слов
        val learnedWords = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWERS }
        val percent = if (totalCount > 0) {
            (learnedWords.size.toDouble() / totalCount * 100).toInt()
        } else 0
        return Statistics(totalCount, learnedWords, percent)
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile = File("words.txt")
        val dictionary = mutableListOf<Word>()

        try {
            if (!wordsFile.exists()) {
                wordsFile.createNewFile()
            }

            if (wordsFile.readText().isBlank()) {
                wordsFile.writeText("hello|привет|0\n")
                wordsFile.appendText("dog|собака|0\n")
                wordsFile.appendText("cat|кошка|0\n")
            }

            val lines: List<String> = wordsFile.readLines()

            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split(SEPARATOR)
                if (parts.size < PART) continue

                val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = parts[2].toInt())
                dictionary.add(word)
            }
        } catch (e: IOException) {
            println("Ошибка при работе с файлом: ${e.message}")
            e.printStackTrace()
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File(WORDS_FILE)

        try {
            val content = buildString {
                for (word in dictionary) {
                    append("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
                }
            }

            wordsFile.writeText(content)
        } catch (e: IOException) {
            println("Ошибка при сохранении словаря: ${e.message}")
        }
    }
}
