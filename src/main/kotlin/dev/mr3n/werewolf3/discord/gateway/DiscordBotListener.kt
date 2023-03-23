package dev.mr3n.werewolf3.discord.gateway

import dev.mr3n.werewolf3.discord.gateway.entities.Identify
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

object DiscordBotListener {
    val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val session = runBlocking { client.webSocketSession("wss://gateway.discord.gg/?v=10&encoding=json") }

    val thread = thread {
        runBlocking {
            while(true) {
                val frame = session.incoming.receive()
                println(frame.data.decodeToString())
            }
        }
    }

    init {
        runBlocking {
             session.sendSerialized(Identify("MTA1MTIwMDk1OTMwNjU0NzI3MA.GLw3hJ.8UiHqxwwClEQlEiajMP5zH8RU-PolF_rY7IEek", Intent.GUILDS, Intent.GUILD_VOICE_STATES))
        }
    }
}

fun main() {
    DiscordBotListener
}