package dev.mr3n.werewolf3.discord.gateway

import dev.mr3n.werewolf3.discord.gateway.OpCode.*
import dev.mr3n.werewolf3.discord.gateway.entities.Guild
import dev.mr3n.werewolf3.discord.gateway.entities.VoiceState
import dev.mr3n.werewolf3.discord.gateway.events.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

interface Event {

    interface DispatchEvent: Event {
        val sequence: Int
    }

    object DeserializationStrategy: kotlinx.serialization.DeserializationStrategy<Event?> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Event") {
            element("op", OpCode.serializer().descriptor)
            element("t", String.serializer().descriptor, isOptional = true)
            element("s", Int.serializer().descriptor, isOptional = true)
            element("d", JsonObject.serializer().descriptor, isOptional = true)
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): Event? {
            val structure = decoder.beginStructure(descriptor)
            var data: Event? = null
            var name: String? = null
            var sequence: Int? = null
            var op: OpCode? = null

            while(true) {
                when(val index = structure.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> { // op
                        op = structure.decodeSerializableElement(descriptor, index, OpCode.serializer())
                        when(op) {
                            HEARTBEAT_ACK -> data = HeartbeatAck()
                            RECONNECT -> data = Reconnect()
                            else -> {}
                        }
                    }
                    1 -> { // t
                        name = structure.decodeNullableSerializableElement(descriptor, index, String.serializer().nullable)
                    }
                    2 -> { // s
                        sequence = structure.decodeNullableSerializableElement(descriptor, index, Int.serializer().nullable)
                    }
                    3 -> { // d
                        when(op) {
                            DISPATCH -> {
                                when(name) {
                                    "VOICE_STATE_UPDATE" -> {
                                        val voiceState = structure.decodeSerializableElement(descriptor, index, VoiceState.serializer())
                                        data = VoiceStateUpdateEvent(voiceState, sequence!!)
                                    }
                                    "GUILD_CREATE" -> {
                                        val guild = structure.decodeSerializableElement(descriptor, index, Guild.serializer())
                                        data = GuildCreateEvent(guild, sequence!!)
                                    }
                                    else -> {
                                        structure.decodeNullableSerializableElement(descriptor, index, JsonElement.serializer().nullable)
                                    }
                                }
                            }
                            HEARTBEAT -> {
                                data = structure.decodeSerializableElement(descriptor, index, Heartbeat.serializer())
                            }
                            INVALID_SESSION -> {
                                structure.decodeNullableSerializableElement(descriptor, index, JsonElement.serializer().nullable)
                            }
                            HELLO -> {
                                data = structure.decodeSerializableElement(descriptor, index, Hello.serializer())
                            }
                            else -> { structure.decodeNullableSerializableElement(descriptor, index, JsonElement.serializer().nullable) }
                        }
                    }
                }
            }
            structure.endStructure(descriptor)
            return data
        }
    }
}