package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Heartbeat.Serializer::class)
data class Heartbeat(val lastSequenceNumber: Long): Event {
    object Serializer: KSerializer<Heartbeat> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("Heartbeat", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): Heartbeat {
            return Heartbeat(decoder.decodeLong())
        }

        override fun serialize(encoder: Encoder, value: Heartbeat) {
            encoder.encodeLong(value.lastSequenceNumber)
        }

    }
}