package org.example.additional

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json

class TelegramBotService(private val botToken: String) {
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    private val client: HttpClient = HttpClient.newBuilder().build()

    private val json = Json {ignoreUnknownKeys = true}

    companion object {
        const val STATISTICS_CLICKED = "statistics"
        const val LEARN_WORDS_CLICKED = "learn_words"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    }

    fun getUpdates(offset: Long): List<Update> {
        val url = "$baseUrl/getUpdates?offset=$offset"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val telegramResponse = json.decodeFromString<Response>(response.body())
        return telegramResponse.result
    }

    fun sendMessage(chatId: Long, text: String) {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$baseUrl/sendMessage?chat_id=$chatId&text=$encodedText"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Сообщение отправлено: ${response.body()}")
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val keyboard = question.variants.mapIndexed { index, word ->
            """[{"text":"${word.translate}", "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$index"}]"""
        }.joinToString(separator = ",\n")

        val requestBody = """
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        $keyboard
                    ]
                }
            }
        """.trimIndent()


        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/sendMessage"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Вопрос отправлен: ${response.body()}")
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
        val request: HttpRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Меню отправлено: ${response.body()}")
    }
}
