package dev.mr3n.werewolf3.discord.gateway

import dev.mr3n.werewolf3.discord.gateway.OpCode.*
import dev.mr3n.werewolf3.discord.gateway.OpCode.Serializer
import dev.mr3n.werewolf3.discord.gateway.events.Heartbeat
import dev.mr3n.werewolf3.discord.gateway.events.HeartbeatAck
import dev.mr3n.werewolf3.discord.gateway.events.Hello
import dev.mr3n.werewolf3.discord.gateway.events.Reconnect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

interface Event {
    object DeserializationStrategy: kotlinx.serialization.DeserializationStrategy<Event?> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Event") {
            element("op", OpCode.serializer().descriptor)
            element("t", String.serializer().descriptor, isOptional = true)
            element("s", String.serializer().descriptor, isOptional = true)
            element("d", String.serializer().descriptor, isOptional = true)
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

                            }
                            HEARTBEAT -> {
                                data = structure.decodeSerializableElement(descriptor, index, Heartbeat.serializer())
                            }
                            INVALID_SESSION -> {

                            }
                            HELLO -> {
                                data = structure.decodeSerializableElement(descriptor, index, Hello.serializer())
                            }
                            else -> {}
                        }
                    }
                }
            }
            structure.endStructure(descriptor)
            return data
        }

        fun deserializeDispatchEvent(index: Int, decoder: CompositeDecoder, name: String?, sequence: Int?): Event? {
            when(name) {
                else -> {
                    return null
                }
            }
        }
    }
}