package me.yoku.yokudata.database.impl

import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import java.util.*

@Suppress("UNCHECKED_CAST")
class UUIDProvider : CodecProvider {

    override fun <T : Any?> get(clazz: Class<T>?, registry: CodecRegistry?) : Codec<T>? {

        if (clazz == UUID::class.java) { return UUIDCodec() as Codec<T>
        }

        return null

    }
}