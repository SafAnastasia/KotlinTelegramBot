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
    private val dictionary = loadDictionary() //загружаем словарь

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
            if (correctAnswerId == userAnswerIndex) {//сравниваем слово и правильный ответ
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
        val learnedWords = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWERS }//выученные слова
        val percent = if (totalCount > 0) {//процент выученных слов
            (learnedWords.size.toDouble() / totalCount * 100).toInt()
        } else 0
        return Statistics(totalCount, learnedWords, percent)
    }

    private fun loadDictionary(): List<Word> {   // функция для загрузки словаря
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
                val parts = line.split(SEPARATOR)// делим строки на части функцией split()
                if (parts.size < PART) continue //если количество частей меньше 3х, пропускаем

                val word = Word(original = parts[0], translate = parts[1], correctAnswersCount = parts[2].toInt())
                dictionary.add(word)//создан объект класса Word из "частей" строк, с преобразованием типа данных toInt()
            }//добавляем в словарь
        } catch (e: IOException) {//проверка на ошибку, если файл не записался выводим сообщение
            println("Ошибка при работе с файлом: ${e.message}")
            e.printStackTrace()
        }
        return dictionary // возвращаем собранный словарь
    }

    private fun saveDictionary(dictionary: List<Word>) {//сохраняем текущий словарь, перезаписывая)
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
