package org.example.additional

import java.io.File
import java.io.IOException

data class Word(
    val original: String, //слово на английском
    val translate: String, // его перевод
    var correctAnswersCount: Int = 0 //количество правильных ответов
)

fun loadDictionary(): List<Word> {   // функция для загрузки словаря
    val wordsFile = File("words.txt") //объект файла в формате txt
    val dictionary = mutableListOf<Word>() //изменяемый список "словарь"

    try {
        if (!wordsFile.exists()) { //если файла нет создается пустой файл, функция exists() используется для проверки
            wordsFile.createNewFile() // существования файла, функция createNewFile() создает новый файл, если его нет
        }

        if (wordsFile.readText().isBlank()) { //readText() для чтения всего содержимого файла в виде строки
            wordsFile.writeText("hello|привет|0\n") //isBlank() проверяет пустая ли строка или состоит из пробелов
            wordsFile.appendText("dog|собака|0\n") //writeText() записывает текст в файле, полностью перезаписывает
            wordsFile.appendText("cat|кошка|0\n") //файл, appendText() добавляет текс в конец файла.
        }

        val lines: List<String> = wordsFile.readLines() // readLines() читает файл и возвращает список строк

        for (line in lines) { // перебираем строки с проверкой на пустые строки и пробелы
            if (line.isBlank()) continue //пропускаем, если пусто
            val parts = line.split("|")// делим строки на части функцией split()
            if (parts.size < 3) continue //если количество частей меньше 3х, пропускаем

            val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = parts[2].toInt())
            dictionary.add(word)//создан объект класса Word из "частей" строк, с преобразованием типа данных toInt()
        }//добавляем в словарь
    } catch (e: IOException) {//проверка на ошибку, если файл не записался выводим сообщение
        println("Ошибка при работе с файлом: ${e.message}")
        e.printStackTrace()
    }
    return dictionary // возвращаем собранный словарь
}

fun saveDictionary(dictionary: List<Word>) {//сохраняем текущий словарь, перезаписывая)
    val wordsFile = File("words.txt")

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

fun main() {
    val dictionary = loadDictionary() //загружаем словарь

    while (true) {//главное меню, работает бесконечно, пока не выберем 0.
        println("1 - Учить слова")
        println("2 - Статистика")
        println("0 - Выход")

        val input = readlnOrNull()?.trim() ?: "" // проверка на наличие строки, безопасный вызов, строка есть вызывается
        when (input) {// trim() - убирает пробелы по краям
            "1" -> {// Учить слова
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < 3 }//список с невыученными словами
                    if (notLearnedList.isEmpty()) {//создается новый список .filter(), с правильными ответами <3
                        println("Все слова выучены!")//isEmpty() - проверка на пустоту, если пусто значит все выучено
                        break //выходим
                    }

                    val questionWords = notLearnedList.shuffled().take(minOf(4, notLearnedList.size))
                    val correctAnswer =
                        questionWords.random()//варианты ответа (до 4х), перемешиваем список невыученных слов shuffled(),
                    //берем первые 4 элемента из списка или сколько есть, random()выбирает один случайный элемент
                    val correctAnswerId =
                        questionWords.indexOf(correctAnswer) + 1 //индекс правильного ответа для пользователя
                    println("Как переводится слово: ${correctAnswer.original}.")
                    questionWords.forEachIndexed { index, word -> //выводим варианты ответа, .forEachIndexed()
                        println("${index + 1} - ${word.translate}")//перебирает список (индекс, элемент слова)
                    }
                    println("0 - Меню")

                    val userAnswerInput = readlnOrNull()?.trim() ?: ""//проверка ввода ответа
                    if (userAnswerInput == "0") break

                    val chosenId = userAnswerInput.toIntOrNull()//выбранный вариант по числу, переводим ввод в число
                    if (chosenId == null || chosenId !in 1..questionWords.size) {
                        println("Введите число от 1 до ${questionWords.size} или 0")
                        continue//если число это число равно нулю и входит в диапазон 1..кол-во вариантов ответов
                        //то выводим сообщение
                    }

                    if (chosenId == correctAnswerId) {//сравниваем слово и правильный ответ
                        println("Правильно!")
                        correctAnswer.correctAnswersCount++//если совпадает, увеличиваем счетчик
                        saveDictionary(dictionary)//сохраняем изменения в словаре
                    } else {
                        println("Неправильно! ${correctAnswer.original} - это ${correctAnswer.translate}.")
                    }

                    println()
                }
            }

            "2" -> {
                val totalCount = dictionary.size//всего слов
                val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }//выученные слова
                val percent = if (totalCount > 0) {//процент выученных слов
                    (learnedWords.size.toDouble() / totalCount * 100).toInt()
                } else 0

                println("Выучено ${learnedWords.size} из $totalCount слов | $percent%")
            }

            "0" -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}