package me.yoku.yokudata.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

@Suppress("MemberVisibilityCanBePrivate")
class MongoControl {

    companion object {

        private lateinit var client: MongoClient
        lateinit var database: MongoDatabase

        private var isInit = false

        fun setup(plugin: JavaPlugin, uri: String, database: String): Companion {

            if (uri.isEmpty() || database.isEmpty()) {

                plugin.logger.warning("資料尚未設定 關閉插件!")

                Bukkit.getPluginManager().disablePlugin(plugin)

                return this
            }

            this.client = MongoClients.create(uri)

            this.database = this.client.getDatabase(database)

            this.isInit = true

            return this
        }

//        fun getCollection(name: String, collection: MongoCollection<Document>.() -> Unit): MongoCollection<Document> { return getCollection<Document>(name, collection) }
//
//        fun getCollection(name: String): MongoCollection<Document> { return getCollection<Document>(name) { } }

        inline fun <reified T> getCollection(name: String, collection: MongoCollection<T>.() -> Unit): MongoCollection<T> { return this.database.getCollection(name, T::class.java).apply(collection) }

        inline fun <reified T> getCollection(name: String): MongoCollection<T> { return getCollection<T>(name) { } }

    }
}