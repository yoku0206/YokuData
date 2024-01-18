package me.yoku.yokudata.block

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BlockVector
import java.util.*
import java.util.AbstractMap.SimpleEntry

class CustomBlockData(block: Block, plugin: JavaPlugin) : PersistentDataContainer {

    private val container: PersistentDataContainer

    private val chunk: Chunk = block.chunk

    private val key: NamespacedKey = getKey(plugin, block)

    private val entry: MutableMap.MutableEntry<UUID, BlockVector>

    private val plugin: JavaPlugin

    init {

        this.container = getPersistentDataContainer()
        this.entry = getBlockEntry(block)
        this.plugin = plugin

    }

    companion object {

        private val DIRTY_BLOCKS = mutableSetOf<Map.Entry<UUID, BlockVector>>()

        private val PRIMITIVE_DATA_TYPES = listOf(
            PersistentDataType.BYTE,
            PersistentDataType.LONG,
            PersistentDataType.DOUBLE,
            PersistentDataType.FLOAT,
            PersistentDataType.INTEGER,
            PersistentDataType.SHORT,
            PersistentDataType.BOOLEAN,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.STRING,
            PersistentDataType.TAG_CONTAINER,
            PersistentDataType.TAG_CONTAINER_ARRAY
        )

        private val PERSISTENCE_KEY = NamespacedKey.fromString("YokuData:BlockProtected")!!

        private val KEY_REGEX = Regex("^x(\\d+)y(-?\\d+)z(\\d+)$")

        private const val CHUNK_MIN_XZ = 0

        private const val CHUNK_MAX_XZ = (2 shl 3) - 1

        private val HAS_MIN_HEIGHT_METHOD: Boolean

        init {

            var tmp = false

            try {

                World::class.java.getMethod("getMinHeight")

                tmp = true

            } catch (_ : ReflectiveOperationException) {  }

            HAS_MIN_HEIGHT_METHOD = tmp

            val packageName = CustomBlockData::class.java.packageName

            val plugin = JavaPlugin.getProvidingPlugin(CustomBlockData::class.java)

            plugin.logger.warning("The Package of Class: $packageName")

        }

        fun getBlockEntry(block: Block) : MutableMap.MutableEntry<UUID, BlockVector> { return SimpleEntry(block.world.uid, BlockVector(block.x, block.y, block.z)) }

        fun isDirty(block: Block) : Boolean { return DIRTY_BLOCKS.contains(getBlockEntry(block)) }

        fun setDirty(plugin: JavaPlugin, entry: Map.Entry<UUID, BlockVector>) {

            if (!plugin.isEnabled) return

            DIRTY_BLOCKS.add(entry)

            Bukkit.getScheduler().runTask(plugin, Runnable { DIRTY_BLOCKS.remove(entry) })
        }

        private fun getKey(plugin: JavaPlugin, block: Block) : NamespacedKey { return NamespacedKey(plugin, getKey(block)) }

        fun getKey(block: Block) : String {

            val x = block.x and 0x000F
            val y = block.y
            val z = block.z and 0x000F

            return "x${x}y${y}z${z}"
        }

        fun getBlockFromKey(key: NamespacedKey, chunk: Chunk) : Block? {

            val matches = KEY_REGEX.matchEntire(key.key) ?: return null

            val x = matches.groups[1]!!.value.toInt()
            val y = matches.groups[2]!!.value.toInt()
            val z = matches.groups[3]!!.value.toInt()

            if ((x < CHUNK_MIN_XZ || x > CHUNK_MAX_XZ) || (z < CHUNK_MIN_XZ || z > CHUNK_MAX_XZ) ||
                (y < getWorldMinHeight(chunk.world) || y > chunk.world.maxHeight - 1)) return null

            return chunk.getBlock(x, y, z)

        }

        fun getWorldMinHeight(world: World) : Int { return if (HAS_MIN_HEIGHT_METHOD) world.minHeight else 0 }

        fun hasCustomBlockData(block: Block, plugin: JavaPlugin) : Boolean { return block.chunk.persistentDataContainer.has(getKey(plugin, block), PersistentDataType.TAG_CONTAINER) }

        fun isProtected(block: Block, plugin: JavaPlugin) : Boolean { return CustomBlockData(block, plugin).isProtected() }

        fun registerListener(plugin: JavaPlugin) { Bukkit.getPluginManager().registerEvents(BlockDataListener(plugin), plugin) }

        fun getBlockWithCustomData(plugin: JavaPlugin, chunk: Chunk) : MutableSet<Block> {

            return getBlockWithCustomData(chunk, NamespacedKey(plugin, "dummy"))

        }

        fun getBlockWithCustomData(chunk: Chunk, namespaced: NamespacedKey) : MutableSet<Block> {

            val container = chunk.persistentDataContainer

            return container.keys.filter { it.namespace == namespaced.namespace }
                .mapNotNull { getBlockFromKey(it, chunk) }
                .toMutableSet()
        }

        fun getDataType(container: PersistentDataContainer, key: NamespacedKey) : PersistentDataType<*, *>? {

            PRIMITIVE_DATA_TYPES.forEach { if (container.has(key, it)) return it }

            return null
        }

    }

    fun getBlock() : Block? {

        val world = Bukkit.getWorld(entry.key) ?: return null

        val vector = entry.value

        return world.getBlockAt(vector.blockX, vector.blockY, vector.blockZ)
    }

    private fun getPersistentDataContainer() : PersistentDataContainer {

        val chunkPDC = chunk.persistentDataContainer
        val blockPDC = chunkPDC.get(key, PersistentDataType.TAG_CONTAINER)

        if (blockPDC != null) return blockPDC

        return chunkPDC.adapterContext.newPersistentDataContainer()
    }

    fun isProtected() : Boolean { return has(PERSISTENCE_KEY, PersistentDataType.BOOLEAN) }

    fun setProtected(boolean: Boolean) { if (boolean) { set(PERSISTENCE_KEY, PersistentDataType.BOOLEAN, true) } else remove(PERSISTENCE_KEY) }

    fun clear() {

        this.container.keys.forEach { this.container.remove(it) }

        save()
    }

    private fun save() {

        setDirty(plugin, entry)

        if (this.container.isEmpty) chunk.persistentDataContainer.remove(key)
        else chunk.persistentDataContainer.set(key, PersistentDataType.TAG_CONTAINER, this.container)

    }

    fun copyTo(block: Block, plugin: JavaPlugin) {

        val data = CustomBlockData(block, plugin)

        keys.forEach { key ->

            val type: PersistentDataType<*, *> = getDataType(this, key) ?: return@forEach

            @Suppress("UNCHECKED_CAST")
            data.set(key, type as PersistentDataType<Any, Any>, get(key, type)!!)

        }
    }


    fun getDataType(key: NamespacedKey) : PersistentDataType<*, *>? { return Companion.getDataType(this, key) }

    override fun <T : Any, Z : Any> set(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z) {

        this.container.set(key, type, value)

        save()
    }

    override fun <T : Any, Z : Any> has(key: NamespacedKey, type: PersistentDataType<T, Z>) : Boolean { return this.container.has(key, type) }

    override fun <T : Any, Z : Any> get(key: NamespacedKey, type: PersistentDataType<T, Z>) : Z? { return this.container.get(key, type) }

    override fun <T : Any, Z : Any> getOrDefault(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z) : Z { return this.container.getOrDefault(key, type, value) }

    override fun getKeys() : MutableSet<NamespacedKey> { return this.container.keys }

    override fun remove(key: NamespacedKey) {

        this.container.remove(key)

        save()
    }

    override fun isEmpty() : Boolean { return this.container.isEmpty }

    override fun getAdapterContext() : PersistentDataAdapterContext { return this.container.adapterContext }
}