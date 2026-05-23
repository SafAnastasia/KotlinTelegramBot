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
        val updates = telegramBotService.getUpdates(offset)
        updates.forEach { update ->
            offset = update.updateId + 1

            if (update.chatId != null) {
                when (update.messageText) {
                    "/start" -> telegramBotService.sendMenu(update.chatId)
                    "Hello" -> telegramBotService.sendMessage(update.chatId, "Hello")
                }
            }

            if (update.callbackChatId != null) {
                when (update.callbackData) {
                    TelegramBotService.LEARN_WORDS_CLICKED ->
                        telegramBotService.sendMessage(update.callbackChatId, "Начинаем учить слова!")

                    TelegramBotService.STATISTICS_CLICKED -> {
                        val statistics = trainer.getStatistics()
                        val statisticsMessage =
                            "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%"
                        telegramBotService.sendMessage(update.callbackChatId, statisticsMessage)
                    }
                }
            }
        }
    }
}