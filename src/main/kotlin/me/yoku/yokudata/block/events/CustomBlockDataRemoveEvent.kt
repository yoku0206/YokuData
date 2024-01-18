package me.yoku.yokudata.block.events

import me.yoku.yokudata.block.CustomBlockData
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

class CustomBlockDataRemoveEvent(plugin: JavaPlugin, block: Block, event: Event) : CustomBlockDataEvent(plugin, block, event) {
}