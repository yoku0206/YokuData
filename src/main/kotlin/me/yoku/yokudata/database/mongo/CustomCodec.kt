package me.yoku.yokudata.database.mongo

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import kotlin.reflect.KClass

interface CustomCodec<T : Any> : Codec<T> {

    fun encoder(writer : BsonWriter, value: T)

    fun decoder(reader: BsonReader) : T?

    fun getClass() : KClass<T>

}