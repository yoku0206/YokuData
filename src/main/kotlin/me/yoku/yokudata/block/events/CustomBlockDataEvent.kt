package me.yoku.yokudata.block.events

import me.yoku.yokudata.block.CustomBlockData
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

/**
 *
 */
open class CustomBlockDataEvent(plugin: JavaPlugin, private val block: Block, private val event: Event?) : Event(), Cancellable {

    private val data = CustomBlockData(block, plugin)

    private var isCancelled = false

    companion object {

        @JvmStatic
        val HANDLERS = HandlerList()

        fun getHandlerList() : HandlerList { return this.HANDLERS }

    }

    fun getBlock() : Block { return this.block }

    fun getEvent() : Event? { return this.event }

    fun getBlockData() : CustomBlockData { return this.data }

    fun getReason() : Reason {

        if (event == null) return Reason.UNKNOWN

        Reason.values().forEach {

            if (it == Reason.UNKNOWN) return@forEach

            if (it.getApplicableEvents().any { clazz -> clazz.java == event.javaClass }) return it
        }

        return Reason.UNKNOWN
    }

    override fun getHandlers(): HandlerList { return HANDLERS }

    override fun isCancelled() : Boolean { return this.isCancelled }

    override fun setCancelled(cancel: Boolean) { this.isCancelled = cancel }

    enum class Reason(vararg clazz: KClass<out Event>) {

        BLOCK_BREAK(BlockBreakEvent::class),
        BLOCK_PLACE(BlockPlaceEvent::class, BlockMultiPlaceEvent::class),
        EXPLOSION(EntityExplodeEvent::class, BlockExplodeEvent::class),
        PISTON(BlockPistonExtendEvent::class, BlockPistonRetractEvent::class),
        BURN(BlockBurnEvent::class),
        ENTITY_CHANGE_BLOCK(EntityChangeBlockEvent::class),
        FADE(BlockFadeEvent::class),
        STRUCTURE_GROW(StructureGrowEvent::class),
        FERTILIZE(BlockFertilizeEvent::class),

        UNKNOWN();

        private val events: List<KClass<out Event>> = if (clazz.isEmpty()) listOf() else clazz.toList()


        fun getApplicableEvents() : List<KClass<out Event>> { return this.events }

    }

}