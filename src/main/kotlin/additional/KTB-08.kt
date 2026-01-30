package org.example.additional

import java.io.File

data class Word(
    val original: String, //слово на английском
    val translate: String, // его перевод
    var correctAnswersCount: Int = 0 //количество правильных ответов
)

fun Question.asConsoleString(): String{
    val variants = this.variants
        .mapIndexed { index: Int, word -> "${index + 1} - ${word.translate}"}
        .joinToString (separator = "/n" )
    return "${this.correctAnswer.original}/n$variants/n0 - выйти в меню"
}

fun main() {
    val learning = LearnWordsTrainer()
    val file = File("words.txt")
    file.appendText("house|дом|0\n")


    while (true) {//главное меню, работает бесконечно, пока не выберем 0.
        println("1 - Учить слова, 2 - Статистика, 0 - Выход")
        val input = readlnOrNull()?.trim() ?: "" // проверка на наличие строки, безопасный вызов, строка есть вызывается

        when (input) {// trim() - убирает пробелы по краям
            "1" -> {// Учить слова
                while (true) {
                    val question =  learning.getNextQuestion()
                    if (question == null) {//создается новый список .filter(), с правильными ответами <3
                        println("Все слова выучены!")//isEmpty() - проверка на пустоту, если пусто значит все выучено
                        break //выходим
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readlnOrNull()?.trim() ?: ""//проверка ввода ответа
                        if (userAnswerInput == "0") break

                        val chosenId = userAnswerInput.toIntOrNull()//выбранный вариант по числу, переводим ввод в число
                        if (chosenId == null || chosenId !in 1..question.variants.size) {
                            println("Введите число от 1 до ${question.variants.size} или 0")
                            continue//если число это число равно нулю и входит в диапазон 1..кол-во вариантов ответов
                        }    //то выводим сообщение
                        if (learning.checkAnswer(userAnswerInput?.minus(1))) {//сравниваем слово и правильный ответ
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
                println("Выучено ${statistics.learnedWords.size} из $statistics.totalCount слов | $statistics.percent%")
            }

            "0" -> {
                println("Выход в меню")
                    break
                }
            else -> println("Введите число 1, 2 или 0")
        }
    }
}