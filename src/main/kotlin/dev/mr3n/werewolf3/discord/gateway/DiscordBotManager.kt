package dev.mr3n.werewolf3.discord.gateway

import dev.mr3n.werewolf3.discord.gateway.entities.Identify
import dev.mr3n.werewolf3.discord.gateway.entities.VoiceState
import dev.mr3n.werewolf3.discord.gateway.events.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.thread

class DiscordBotManager(private val tokens: List<String>) {
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    private val listeners = mutableListOf<DiscordEventListener>()

    private val timer = Timer()

    val voiceStates = mutableMapOf<String, VoiceState>()

    private var lastSequenceNumber: Int? = null

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val jsonParserIgnoreNul = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = true
    }

    val tokenManagers = tokens.map { TokenManager(it, client, jsonParserIgnoreNul) }

    fun startHeartbeat(milliSec: Long) {
        timer.scheduleAtFixedRate(0L, milliSec) {
            runBlocking { session.sendSerialized(GatewayEvent(OpCode.HEARTBEAT, lastSequenceNumber)) }
        }
    }

    val session = runBlocking { client.webSocketSession("wss://gateway.discord.gg/?v=10&encoding=json") }

    fun addListener(listener: DiscordEventListener) = this.listeners.add(listener)

    init {
        thread {
            runBlocking {
                while(true) {
                    val json = String(session.incoming.receive().data, Charsets.UTF_8)
                    val event = jsonParser.decodeFromString(Event.DeserializationStrategy,json)
                    if(event is Hello) { this@DiscordBotManager.startHeartbeat(event.heartbeatInterval.toLong() - 1000) }
                    if(event is Event.DispatchEvent) {
                        this@DiscordBotManager.lastSequenceNumber = event.sequence
                        if(event is VoiceStateUpdateEvent) {
                            if(voiceStates[event.voiceState.userId]?.requestToSpeakTimestamp != event.voiceState.requestToSpeakTimestamp) {
                                listeners.forEach { it.onStageSpeakRequest(StageSpeakRequestEvent(event.voiceState)) }
                            }
                            if(voiceStates[event.voiceState.userId]?.channelId != event.voiceState.channelId) {
                                listeners.forEach { it.onAudioChannelJoin(AudioChannelJoinEvent(event.voiceState)) }
                            }
                            voiceStates[event.voiceState.userId] = event.voiceState
                            listeners.forEach { it.onVoiceStateUpdate(event) }
                        }
                        if(event is GuildCreateEvent) {
                            voiceStates.putAll(event.guild.voiceStates.associateBy { it.userId })
                            listeners.forEach { it.onGuildCreate(event) }
                        }
                    }
                }
            }
        }
    }

    inline fun <reified T> patch(url: String, body: T): Boolean {
        val tokenManager = tokenManagers.filterNot { it.limited() }.randomOrNull()?:return false
        runBlocking { tokenManager.patch(url, body) }
        return true
    }

    init {
        runBlocking {
             session.sendSerialized(GatewayEvent(OpCode.IDENTIFY,Identify(tokens[0], Intent.GUILDS, Intent.GUILD_VOICE_STATES)))
        }
    }
}

fun main() {
    DiscordBotManager(listOf(System.getenv("DISCORD_BOT_TOKEN"))).addListener(object: DiscordEventListener() {
        override fun onGuildCreate(event: GuildCreateEvent) {
            event.guild.voiceStates
        }

        override fun onVoiceStateUpdate(event: VoiceStateUpdateEvent) {
            println(event.voiceState)
        }
    })
}