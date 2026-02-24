import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates"

    val client: HttpClient = HttpClient.newBuilder().build()
    val requestGetMe: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

    val response: HttpResponse<String?> = client.send(request, HttpResponse.BodyHandlers.ofString())
    val responseGetMe: HttpResponse<String?> = client.send(requestGetMe, HttpResponse.BodyHandlers.ofString())

    println(responseGetMe.body())
    println(response.body())

}