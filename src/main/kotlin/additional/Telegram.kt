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
                val callbackData = update.callbackData
                when {
                    callbackData == TelegramBotService.LEARN_WORDS_CLICKED -> {
                        checkNextQuestionAndSend(trainer, telegramBotService, update.callbackChatId)
                    }

                    callbackData == TelegramBotService.STATISTICS_CLICKED -> {
                        val statistics = trainer.getStatistics()
                        val statisticsMessage =
                            "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%"
                        telegramBotService.sendMessage(update.callbackChatId, statisticsMessage)
                    }

                    callbackData != null && callbackData.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) -> {
                        val userAnswerIndex = callbackData
                            .substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX)
                            .toInt()
                        if (trainer.checkAnswer(userAnswerIndex)) {
                            telegramBotService.sendMessage(update.callbackChatId, "Правильно!")
                        } else {
                            val correctAnswer = trainer.question?.correctAnswer
                            telegramBotService.sendMessage(
                                update.callbackChatId,
                                "Неправильно! ${correctAnswer?.original} - это ${correctAnswer?.translate}"
                            )
                        }

                        checkNextQuestionAndSend(trainer, telegramBotService, update.callbackChatId)
                    }
                }
            }
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        telegramBotService.sendQuestion(chatId, question)
    }
}