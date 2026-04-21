package org.example.additional

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Ошибка: передайте токен бота как аргумент!")
        return
    }

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    var offset: Long = 0L

    while (true) {
        Thread.sleep(2000)
        val lastUpdateId = telegramBotService.getUpdates(offset, trainer)
        offset = lastUpdateId + 1
    }
}