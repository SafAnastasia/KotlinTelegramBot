package org.example.additional

import java.net.URI
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

    fun getUpdates(offset: Long): List<Update> {
        val url = "$baseUrl/getUpdates?offset=$offset"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody: String = response.body()

        val updateIdPattern = Regex(""""update_id":(\d+)""")
        val chatIdPattern = Regex(""""chat":\{"id":(\d+)""")
        val textPattern = Regex(""""text":"(.*?)"""")
        val callbackDataPattern = Regex(""""callback_query":\{.*?"data":"(.*?)"""")
        val callbackChatIdPattern = Regex(""""callback_query":\{.*?"from":\{"id":(\d+)""")

        val updateId = updateIdPattern.find(responseBody)
            ?.groupValues?.get(1)?.toLong() ?: return emptyList()

        val update = Update(
            updateId = updateId,
            chatId = chatIdPattern.find(responseBody)?.groupValues?.get(1)?.toLong(),
            messageText = textPattern.find(responseBody)?.groupValues?.get(1),
            callbackData = callbackDataPattern.find(responseBody)?.groupValues?.get(1),
            callbackChatId = callbackChatIdPattern.find(responseBody)?.groupValues?.get(1)?.toLong()
        )

        return listOf(update)
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
                        [{"text": "Учить слова", "callback_data": "$LEARN_WORDS_CLICKED"}],
                        [{"text": "Статистика", "callback_data": "$STATISTICS_CLICKED"}]
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
