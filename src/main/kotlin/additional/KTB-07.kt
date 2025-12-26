package org.example.additional

import java.io.File
import java.io.IOException

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun loadDictionary(): List<Word> {
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
            val parts = line.split("|")
            if (parts.size < 3) continue

            val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = parts[2].toInt())
            dictionary.add(word)
        }
    } catch (e: IOException) {
        println("Ошибка при работе с файлом: ${e.message}")
        e.printStackTrace()
    }
    return dictionary
}

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        val input = readlnOrNull()?.trim() ?: ""
        when (input) {
            "1" -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }
                    if (notLearnedList.isEmpty()) {
                        println("Все слова выучены!")
                        break
                    }

                    val questionWords = dictionary.shuffled().take(minOf(4, dictionary.size))
                    val correctAnswer = questionWords.random()

                    println("Как переводится слово: ${correctAnswer.original}.")
                    questionWords.forEachIndexed { index, word ->
                        println("${index + 1} - ${word.translate}")
                    }
                    println("0 - Выйти в меню")

                    val answer = readlnOrNull()?.trim() ?: ""
                    if (answer == "0") break

                    val chosenIndex = answer.toIntOrNull()
                    if (chosenIndex == null || chosenIndex !in 1..questionWords.size) {
                        println("Введите число от 1 до ${questionWords.size} или 0")
                        continue
                    }

                    val chosenWord = questionWords[chosenIndex - 1]
                    if (chosenWord == correctAnswer) {
                        correctAnswer.correctAnswersCount++
                        println("Верно! (количество правильных ответов: ${correctAnswer.correctAnswersCount})")
                    } else {
                        println("Неверно. Правильный ответ: ${correctAnswer.translate}.")
                    }

                    println()
                }
            }

            "2" -> {
                val totalCount = dictionary.size
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
                val percent = if (totalCount > 0) {
                    (learnedWords.size.toDouble() / totalCount * 100).toInt()
                } else 0

                println("Выучено ${learnedWords.size} из $totalCount слов | $percent%")
            }

            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}