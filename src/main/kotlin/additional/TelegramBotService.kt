package org.example.additional

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class TelegramBotService(private val botToken: String) {
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    private val client: HttpClient = HttpClient.newBuilder().build()

    private val json = Json { ignoreUnknownKeys = true }

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

    private fun sendRequest(requestBody: String) {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/sendMessage"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Ответ: ${response.body()}")
    }

    fun sendMessage(chatId: Long, text: String) {
        val requestBody = json.encodeToString(
            SendMessageRequest(
                chatId = chatId,
                text = text
            )
        )
        sendRequest(requestBody)
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val keyboard = question.variants.mapIndexed { index, word ->
            listOf(
                InlineKeyboardButton(
                    text = word.translate,
                    callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                )
            )
        }

        val requestBody = json.encodeToString(
            SendMessageRequest(
                chatId = chatId,
                text = question.correctAnswer.original,
                replyMarkup = ReplyMarkup(inlineKeyboard = keyboard)
            )
        )

        sendRequest(requestBody)
    }

    fun sendMenu(chatId: Long) {
        val requestBody = json.encodeToString(
            SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                inlineKeyboard = listOf(
                    listOf(InlineKeyboardButton(
                            text = "Учить слова",
                            callbackData = LEARN_WORDS_CLICKED
                        )),
                    listOf(InlineKeyboardButton(
                            text = "Статистика",
                            callbackData = STATISTICS_CLICKED
                        ))
                    )
                )
            )
        )
        sendRequest(requestBody)
    }
}

