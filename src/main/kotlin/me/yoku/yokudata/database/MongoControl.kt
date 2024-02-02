package me.yoku.yokudata.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

@Suppress("MemberVisibilityCanBePrivate")
class MongoControl(plugin: JavaPlugin, uri: String, database: String) {

    lateinit var client: MongoClient
    lateinit var database: MongoDatabase

    private var isInit = false

    init {

        run {

            if (uri.isEmpty() || database.isEmpty()) {

                plugin.logger.warning("資料尚未設定 關閉插件!")

                Bukkit.getPluginManager().disablePlugin(plugin)

                return@run
            }

            this.client = MongoClients.create(uri)

            this.database = this.client.getDatabase(database)

            this.isInit = true

            return@run

        }

    }

    fun close() { if (isInit) this.client.close() }

    inline fun <reified T> getCollection(name: String, collection: (MongoCollection<T>) -> MongoCollection<T>): MongoCollection<T> {

        val mongoCollection = this.database.getCollection(name, T::class.java)

        return collection.invoke(mongoCollection)
    }

    inline fun <reified T> getCollection(name: String): MongoCollection<T> { return getCollection<T>(name) { return it } }
}