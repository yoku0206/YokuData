package me.yoku.yokudata.database

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.yoku.yokudata.database.impl.UUIDProvider
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("MemberVisibilityCanBePrivate")
class MongoControl(plugin: JavaPlugin, uri: String, database: String, providers: MutableList<CodecProvider> = mutableListOf()) {

    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    val providers: MutableList<CodecProvider> = mutableListOf()

    private var isInit = false

    init {

        Logger.getLogger("org.mongodb.driver").level = Level.WARNING

        run {

            if (uri.isEmpty() || database.isEmpty()) {

                plugin.logger.warning("資料尚未設定 關閉插件!")

                Bukkit.getPluginManager().disablePlugin(plugin)

                return@run
            }

            this.client = MongoClient.create(uri)

            this.database = this.client.getDatabase(database)

            this.providers.addAll(providers)

            this.providers.add(UUIDProvider())

            this.isInit = true

            return@run

        }

    }

    fun close() { if (isInit) this.client.close() }

    inline fun <reified T : Any> getCollection(name: String, collection: (MongoCollection<T>) -> MongoCollection<T>) : MongoCollection<T> {

        val mongoCollection = this.database.getCollection(name, T::class.java)
            .withCodecRegistry(CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(this.providers),
                MongoClientSettings.getDefaultCodecRegistry()
            ))

        return collection.invoke(mongoCollection)
    }

    inline fun <reified T : Any> getCollection(name: String) : MongoCollection<T> { return getCollection<T>(name) { return it } }

    fun registerProvider(vararg providers: CodecProvider) { this.providers.addAll(providers) }

}