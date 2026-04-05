package org.example.additional

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService (private val botToken: String) {
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(offset: Long): Long {
        val url = "$baseUrl/getUpdates?offset=$offset"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody: String = response.body()

        val updateIdPattern = Regex("""update_id:(\d+)""")
        val updateId = updateIdPattern.find(responseBody)?.groupValues?.get(1)?.toLong()
            ?: offset
        val chatIdPattern = Regex("""chat":\{"id":(\d+)""")
        val chatId = chatIdPattern.find(responseBody)
            ?.groupValues?.get(1)?.toLong()

        val textPattern = Regex(""""text":"(.*?)"""")
        val textMatch = textPattern.find(responseBody)
        val messageText = textMatch?.groupValues?.get(1)

        println("Получить сообщение: $messageText от chatId: $chatId")
        if (messageText == "Hello" && chatId != null) {
            sendMessage(chatId, "Hello")
        }

        return updateId
    }

    fun sendMessage(chatId: Long, text: String) {
        val encodedText = URLEncoder.encode(text,"UTF-8")
        val url = "$baseUrl/sendMessage?chat_id=$chatId$text=$encodedText"
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> =
            client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Сообщение отправлено: ${response.body()}")
    }
}

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)

    var offset: Long = 0L

    while (true) {
        Thread.sleep(2000)
        val lastUpdateId = telegramBotService.getUpdates(offset)
        offset = lastUpdateId + 1
    }
}