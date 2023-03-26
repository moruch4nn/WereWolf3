package dev.mr3n.werewolf3.discord.gateway

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TokenManager(val token: String, val client: HttpClient, val jsonParser: Json) {

    var limit = 10
    var remaining = 10
    var reset = System.currentTimeMillis()

    fun limited(): Boolean {
        if(reset < System.currentTimeMillis()) {
            this.remaining = this.limit
        }
        return remaining <= 0
    }

    suspend inline fun <reified T> patch(url: String, body: T) {
        if(limited()) { return }
        val response = client.patch(url) {
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Authorization, "Bot $token")
            setBody(jsonParser.encodeToString(body))
        }
        response.headers["x-ratelimit-limit"]?.toIntOrNull()?.let { this.limit = it }
        response.headers["x-ratelimit-remaining"]?.toIntOrNull()?.let { this.remaining = it }
        response.headers["x-ratelimit-reset"]?.toDoubleOrNull()?.let { this.reset = (it * 1000).toLong() }
    }
}