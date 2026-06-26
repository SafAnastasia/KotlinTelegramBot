package org.example.additional

import java.io.File
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Response(
    val result: List<Update> = emptyList()
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Message(
    val chat: Chat,
    val text: String? = null
)

@Serializable
data class Chat(
    val id: Long
)

@Serializable
data class CallbackQuery(
    val id: String,
    val from: From,
    val data: String? = null
)

@Serializable
data class From(
    val id: Long
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboardButton>>
)

@Serializable
data class InlineKeyboardButton(
    val text: String,
    @SerialName("callback_data")
    val callbackData: String
)

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word -> "${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return "${this.correctAnswer.original}\n$variants\n0 - выйти в меню"
}

class LearnWordsTrainer(private val fileName: String = "words.txt") {
    companion object {
        private const val SEPARATOR = "|"
        private const val PART = 3
        private const val ANSWER_OPTIONS = 4
        private const val CORRECT_ANSWERS = 3
    }

    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS }
        if (notLearnedList.isEmpty()) return null

        val questionWords = notLearnedList.shuffled()
            .take(minOf(ANSWER_OPTIONS, notLearnedList.size))
            .toMutableList()
        if (questionWords.size < ANSWER_OPTIONS) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWERS }
            val need = ANSWER_OPTIONS - questionWords.size

            questionWords.addAll(
                learnedList.shuffled().take(minOf(need, learnedList.size))
            )
        }

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
            } else false
        } ?: false
    }

    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= CORRECT_ANSWERS }
        val percent = if (totalCount > 0) {
            (learnedCount.toDouble() / totalCount * 100).toInt()
        } else 0
        return Statistics(totalCount, learnedCount, percent)
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile = File(fileName)
        val dictionary = mutableListOf<Word>()

        try {
            if (!wordsFile.exists()) wordsFile.createNewFile()
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

                val word = Word(
                    original = parts[0],
                    translate = parts[1],
                    correctAnswersCount = parts[2].toInt()
                )
                dictionary.add(word)
            }
        } catch (e: IOException) {
            println("Ошибка при работе с файлом: ${e.message}")
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File(fileName)

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

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary(dictionary)
        question = null
    }
}

