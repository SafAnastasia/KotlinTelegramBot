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
        wordsFile.createNewFile()
        wordsFile.writeText("hello|привет|0\n")
        wordsFile.appendText("dog|собака|0\n")
        wordsFile.appendText("cat|кошка|0\n")

        val lines: List<String> = wordsFile.readLines()

        for (line in lines) {
            val line = line.split("|")
            val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toInt())
            dictionary.add(word)
        }
        println(dictionary)
    } catch (e: IOException) {
        println("Ошибка при работе с файлом: ${e.message}")
        e.printStackTrace()
    }
    return dictionary
}

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("1")
        println("2")
        println("0")
        val input = readlnOrNull()?.trim() ?: ""
        when (input) {
            "1" -> println("Учить слова")
            "2" -> {
                val totalCount = dictionary.size
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
                val percent = if (totalCount > 0) {
                    (learnedWords.size.toDouble() / totalCount * 100).toInt()
                } else 0

                println("Выучено $learnedWords из $totalCount слов | $percent%")
            }

            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}
