package org.example.additional

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"

    val client: HttpClient = HttpClient.newBuilder().build()
    val requestGetMe: HttpRequest =
        HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()

    val responseGetMe: HttpResponse<String?> =
        client.send(requestGetMe, HttpResponse.BodyHandlers.ofString())

    println(responseGetMe.body())

    var offset: Long = 0L

    while (true) {
        Thread.sleep(2000)
        val lastUpdateId = getUpdates(botToken, offset)
        offset = lastUpdateId + 1
    }

}

fun getUpdates(botToken: String, offset: Long): Long {
    val offsetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$offset"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(offsetUpdates)).build()
    val response: HttpResponse<String> =
        client.send(request,HttpResponse.BodyHandlers.ofString())
    val responseBody: String = response.body()

    val updateIdPattern = Regex("""update_id:(\d+)""")
    val updateMatch = updateIdPattern.find(responseBody)
    val updateId = updateMatch?.groupValues?.get(1)?.toLong()?: offset

    val textPattern = Regex("""text":"(.*?)""")
    val textMatch = textPattern.find(responseBody)
    val messageText = textMatch?.groupValues?.get(1)

    if (messageText != null) {
        println(messageText)
    }

    return updateId
}