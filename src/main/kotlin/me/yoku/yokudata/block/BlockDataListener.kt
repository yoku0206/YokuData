package me.yoku.yokudata.block

import me.yoku.yokudata.block.events.CustomBlockDataMoveEvent
import me.yoku.yokudata.block.events.CustomBlockDataRemoveEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.plugin.java.JavaPlugin

class BlockDataListener(private val plugin: JavaPlugin) : Listener {

    private val predicate: (Block) -> Boolean = { CustomBlockData.hasCustomBlockData(it, plugin) }

    private fun getBlockData(event: BlockEvent) : CustomBlockData { return getBlockData(event.block) }

    private fun getBlockData(block: Block) : CustomBlockData { return CustomBlockData(block, plugin) }

    private fun callAndRemove(event: BlockEvent) { if (callEvent(event)) getBlockData(event).clear() }

    private fun callEvent(event: BlockEvent) : Boolean { return callEvent(event.block, event) }

    private fun callEvent(block: Block, event: Event) : Boolean {

        if (!predicate.invoke(block) || CustomBlockData.isProtected(block, plugin)) return false

        val removeEvent = CustomBlockDataRemoveEvent(plugin, block, event)

        Bukkit.getPluginManager().callEvent(removeEvent)

        return !removeEvent.isCancelled
    }

    private fun callAndRemoveBlockStateList(states: List<BlockState>, event: Event) { states.map { it.block }.filter(predicate).forEach { callAndRemove(it, event) } }

    private fun callAndRemoveBlockList(blocks: List<Block>, event: Event) { blocks.filter(predicate).forEach { callAndRemove(it, event) } }

    private fun callAndRemove(block: Block, event: Event) { if (callEvent(block, event)) getBlockData(block).clear() }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) { callAndRemove(event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) { if (!CustomBlockData.isDirty(event.block)) callAndRemove(event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityChange(event: EntityChangeBlockEvent) { if (event.to != event.block.type) callAndRemove(event.block, event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExplode(event: BlockExplodeEvent) { callAndRemoveBlockList(event.blockList(), event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onExplode(event: EntityExplodeEvent) { callAndRemoveBlockList(event.blockList(), event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBurn(event: BlockBurnEvent) { callAndRemove(event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPiston(event: BlockPistonExtendEvent) { onPiston(event.blocks, event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPiston(event: BlockPistonRetractEvent) { onPiston(event.blocks, event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFade(event: BlockFadeEvent) {

        if (event.block.type == Material.FIRE) return

        if (event.newState.type != event.block.type) callAndRemove(event);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onStructure(event: StructureGrowEvent) { callAndRemoveBlockStateList(event.blocks, event) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onFertilize(event: BlockFertilizeEvent) { callAndRemoveBlockStateList(event.blocks, event) }

    private fun onPiston(blocks: List<Block>, event: BlockPistonEvent) {

        val map = mutableMapOf<Block, CustomBlockData>()
        val direction = event.direction

        blocks.filter(predicate).forEach {

            val data = CustomBlockData(it, plugin)

            if (data.isEmpty || data.isProtected()) return@forEach

            val relative = it.getRelative(direction)

            val moveEvent = CustomBlockDataMoveEvent(plugin, it, relative, event)

            Bukkit.getPluginManager().callEvent(moveEvent)

            if (moveEvent.isCancelled) return@forEach

            map[relative] = data
        }

        map.reverse().forEach {

            it.value.copyTo(it.key, plugin)

            it.value.clear()
        }

    }

    private fun <K, V> MutableMap<K, V>.reverse() : MutableMap<K, V> {

        val reversed = mutableMapOf<K, V>()

        this.keys.toList().reversed().forEach { reversed[it] = this[it]!! }

        return reversed
    }

}
