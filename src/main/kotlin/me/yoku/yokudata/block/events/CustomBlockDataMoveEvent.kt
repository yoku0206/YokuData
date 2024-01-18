package me.yoku.yokudata.block.events

import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

class CustomBlockDataMoveEvent(plugin: JavaPlugin, from: Block, private val to: Block, event: Event) : CustomBlockDataEvent(plugin, from, event) {

    fun getBlockTo() : Block { return this.to }

}