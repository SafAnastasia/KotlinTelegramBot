package org.example.additional

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    private val client: HttpClient = HttpClient.newBuilder().build()

    companion object {
        const val STATISTICS_CLICKED = "statistics"
        const val LEARN_WORDS_CLICKED = "learn_words"
    }

    fun getUpdates(offset: Long, trainer: LearnWordsTrainer): Long {
        val url = "$baseUrl/getUpdates?offset=$offset"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody: String = response.body()

        val updateIdPattern = Regex(""""update_id":(\d+)""")
        val updateId = updateIdPattern.find(responseBody)
            ?.groupValues?.get(1)?.toLong() ?: offset

        val chatIdPattern = Regex(""""chat":\{"id":(\d+)""")
        val chatId = chatIdPattern.find(responseBody)
            ?.groupValues?.get(1)?.toLong()

        val textPattern = Regex(""""text":"(.*?)"""")
        val messageText = textPattern.find(responseBody)
            ?.groupValues?.get(1)

        val callbackDataPattern = Regex(""""callback_query":\{.*?"data":"(.*?)"""")
        val callbackData = callbackDataPattern.find(responseBody)
            ?.groupValues?.get(1)

        val callbackChatIdPattern = Regex(""""callback_query":\{.*?"from":\{"id":(\d+)""")
        val callbackChatId = callbackChatIdPattern.find(responseBody)
            ?.groupValues?.get(1)?.toLong()

        println("message: $messageText | callback: $callbackData | chatId: ${chatId ?: callbackChatId}")

        if (chatId != null) {
            when (messageText) {
                "/start" -> sendMenu(chatId)
                "Hello" -> sendMessage(chatId, "Hello")
            }
        }

        if (callbackChatId != null) {
            when (callbackData) {
                LEARN_WORDS_CLICKED -> sendMessage(callbackChatId, "Начинаем учить слова!")

                STATISTICS_CLICKED -> {
                    val statistics = trainer.getStatistics()
                    val statisticsMessage =
                        "Выучено ${statistics.learnedWords.size} из ${statistics.totalCount} слов | ${statistics.percent}%"
                    sendMessage(callbackChatId, statisticsMessage)
                }
            }
        }
        return updateId
    }

    fun sendMessage(chatId: Long, text: String) {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$baseUrl/sendMessage?chat_id=$chatId&text=$encodedText"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Сообщение отправлено: ${response.body()}")
    }

    fun sendMenu(chatId: Long) {
        val url = "$baseUrl/sendMessage"
        val requestBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [{"text": "Учить слова", "callback_data": "learn_words"}],
                        [{"text": "Статистика", "callback_data": "statistics"}]
                    ]
                }
            }
        """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Меню отправлено: ${response.body()}")
    }
}