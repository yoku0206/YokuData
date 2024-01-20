package me.yoku.yokudata.database.impl

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.util.*

class UUIDCodec : Codec<UUID> {

    override fun encode(writer: BsonWriter?, value: UUID?, encoderContext: EncoderContext?) {

        if (writer == null || value == null) return

        writer.writeString(value.toString())

    }

    override fun getEncoderClass() : Class<UUID> { return UUID::class.java }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?) : UUID? {

        if (reader == null) return null

        return UUID.fromString(reader.readString())
    }

}