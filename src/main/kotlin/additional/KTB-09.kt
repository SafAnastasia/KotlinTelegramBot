package org.example.additional

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word -> "${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return "${this.correctAnswer.original}\n$variants\n0 - выйти в меню"
}

fun main() {
    val learning = LearnWordsTrainer()
    val file = File("words.txt")
    file.appendText("house|дом|0\n")


    while (true) {
        println("Меню: 1 - Учить слова, 2 - Статистика, 3 - Выход")
        val input = readlnOrNull()?.trim() ?: ""

        when (input) {
            "1" -> {
                while (true) {
                    val question = learning.getNextQuestion()
                    if (question == null) {
                        println("Все слова выучены!")
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readlnOrNull()?.trim() ?: ""
                        if (userAnswerInput == "0") break

                        val chosenId = userAnswerInput.toIntOrNull()
                        if (chosenId == null || chosenId !in 1..question.variants.size) {
                            println("Введите число от 1 до ${question.variants.size} или 0")
                            continue
                        }
                        if (learning.checkAnswer(chosenId - 1)) {
                            println("Правильно!")
                        } else {
                            println("Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}.")
                        }
                        println()
                    }
                }
            }

            "2" -> {
                val statistics = learning.getStatistics()
                println("Выучено ${statistics.learnedWords.size} из ${statistics.totalCount} слов | ${statistics.percent} %")
            }

            "0" -> {
                println("Выход в меню")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
    }
}