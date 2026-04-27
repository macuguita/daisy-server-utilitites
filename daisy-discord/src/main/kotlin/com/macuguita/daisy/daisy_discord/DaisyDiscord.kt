package com.macuguita.daisy.daisy_discord

import folk.sisby.kaleido.api.WrappedConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import net.minecraft.server.MinecraftServer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader

object DaisyDiscord : DedicatedServerModInitializer {

	private val MOD_ID = "daisy-discord"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)
	val CONFIG =
		WrappedConfig.createToml(FabricLoader.getInstance().configDir, "daisy", MOD_ID, DiscordConfig::class.java)
	lateinit var mcServer: MinecraftServer

	override fun onInitializeServer() {
		if (!CONFIG.isEnabled) return

		ServerLifecycleEvents.SERVER_STARTED.register { mcServer = it }

		val modScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		BotManager.start(modScope)
		MinecraftEvents.register()
	}
}
