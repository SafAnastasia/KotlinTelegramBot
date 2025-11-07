package org.example.additional

import java.io.File
import java.io.IOException

fun main() {
    val wordsFile: File = File("words.txt")

    try {
        wordsFile.createNewFile()
        wordsFile.writeText("hello привет\n")
        wordsFile.appendText("dog собака\n")
        wordsFile.appendText("cat кошка\n")
        val lines = wordsFile.readLines()

        for (line in lines) {
            println(line)
        }
    } catch (e: IOException) {
        println("Ошибка при работе с файлом: ${e.message}")
        e.printStackTrace()
    }
}