package com.macuguita.daisy

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object DaisyServerUtilities : ModInitializer {
    private val MOD_ID = "daisy-server-utilities"
    private val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		LOGGER.info("Inititalized $MOD_ID")
	}
}