package com.macuguita.daisy.daisy_discord

import dev.yumi.mc.core.api.ModContainer
import dev.yumi.mc.core.api.YumiMods
import dev.yumi.mc.core.api.entrypoint.server.DedicatedServerModInitializer
import com.macuguita.daisy.daisy_discord.bot.BotManager
import folk.sisby.kaleido.api.WrappedConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.LoggerFactory
import net.minecraft.server.MinecraftServer
import com.macuguita.daisy.daisy_base.event.ServerStartedEvent

object DaisyDiscord : DedicatedServerModInitializer {

	private val MOD_ID = "daisy-discord"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)
	val CONFIG =
		WrappedConfig.createToml(YumiMods.get().configDirectory, "daisy", MOD_ID, DiscordConfig::class.java)
	lateinit var mcServer: MinecraftServer

	override fun onInitializeDedicatedServer(mod: ModContainer) {
		if (!CONFIG.isEnabled) return

		ServerStartedEvent.EVENT.register { mcServer = it }

		val modScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
		BotManager.start(modScope)
		DiscordEvents.register()
	}
}
