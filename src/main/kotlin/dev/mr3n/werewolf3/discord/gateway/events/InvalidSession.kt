package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = InvalidSession.Serializer::class)
data class InvalidSession(val resumable: Boolean): Event {
    object Serializer: KSerializer<InvalidSession> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("Heartbeat", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): InvalidSession {
            return InvalidSession(decoder.decodeBoolean())
        }

        override fun serialize(encoder: Encoder, value: InvalidSession) {
            encoder.encodeBoolean(value.resumable)
        }

    }
}