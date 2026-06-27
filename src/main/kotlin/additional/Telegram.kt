package org.example.additional

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Ошибка: передайте токен бота как аргумент!")
        return
    }

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    val trainer = mutableMapOf<Long, LearnWordsTrainer>()

    var offset: Long = 0L

    while (true) {
        Thread.sleep(2000)
        try {
            val updates = telegramBotService.getUpdates(offset)
            updates.forEach { update ->
                offset = update.updateId + 1

                val chatId = update.message?.chat?.id
                val messageText = update.message?.text
                val callbackChatId = update.callbackQuery?.from?.id
                val callbackData = update.callbackQuery?.data

                val userId = chatId ?: callbackChatId ?: return@forEach

                val trainer = trainer.getOrPut(userId) { LearnWordsTrainer("$userId.txt") }

                if (chatId != null) {
                    when (messageText) {
                        "/start" -> telegramBotService.sendMenu(chatId)
                        "Hello" -> telegramBotService.sendMessage(chatId, "Hello")
                    }
                }

                if (callbackChatId != null) {
                    when {
                        callbackData == TelegramBotService.LEARN_WORDS_CLICKED -> {
                            checkNextQuestionAndSend(trainer, telegramBotService, callbackChatId)
                        }

                        callbackData == TelegramBotService.STATISTICS_CLICKED -> {
                            val statistics = trainer.getStatistics()
                            telegramBotService.sendMessage(
                                callbackChatId,
                                "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%"
                            )
                        }

                        callbackData != null && callbackData.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) -> {
                            val userAnswerIndex = callbackData
                                .substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX)
                                .toInt()
                            if (trainer.checkAnswer(userAnswerIndex)) {
                                telegramBotService.sendMessage(callbackChatId, "Правильно!")
                            } else {
                                val correctAnswer = trainer.question?.correctAnswer
                                telegramBotService.sendMessage(
                                    callbackChatId,
                                    "Неправильно! ${correctAnswer?.original} - это ${correctAnswer?.translate}"
                                )
                            }

                            checkNextQuestionAndSend(trainer, telegramBotService, callbackChatId)
                        }

                        callbackData == TelegramBotService.RESET_CLICKED -> {
                            trainer.resetProgress()
                            telegramBotService.sendMessage(
                                callbackChatId,
                                "Прогресс сброшен! Начинаем сначала."
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка сети: ${e.message}")
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