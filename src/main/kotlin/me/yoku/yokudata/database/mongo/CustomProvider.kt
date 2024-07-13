package me.yoku.yokudata.database.mongo

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

@Suppress("UNCHECKED_CAST")
abstract class CustomProvider<T : Any> : CodecProvider, CustomCodec<T> {

    override fun <T : Any?> get(clazz: Class<T>?, registry: CodecRegistry?) : Codec<T>? {

        if (clazz == this::class.java) return this as Codec<T>

        return null
    }

    override fun encode(writer: BsonWriter?, value: T, ctx: EncoderContext?) {

        if (writer == null) return

        this.encoder(writer, value)

    }

    override fun getEncoderClass() : Class<T> { return this.getClass().java }

    override fun decode(reader: BsonReader?, ctx: DecoderContext?) : T? {

        if (reader == null) return null

        return this.decoder(reader)
    }

}