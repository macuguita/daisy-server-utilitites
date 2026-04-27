/*
 * Copyright (c) 2026 macuguita
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.macuguita.daisy.daisy_player_management

import folk.sisby.kaleido.api.WrappedConfig
import java.nio.file.Files
import com.mojang.logging.LogUtils
import net.minecraft.nbt.NbtIo
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.ProblemReporter
import net.minecraft.util.Util
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.storage.TagValueOutput
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import com.macuguita.daisy.daisy_player_management.commands.OfflineTpCommand
import com.macuguita.daisy.daisy_player_management.commands.PlayerPosCommand
import com.macuguita.daisy.daisy_player_management.commands.ViewCommand
import com.macuguita.daisy.daisy_player_management.mixin.ServerPlayerAccessor


object DaisyPlayerManagement : ModInitializer {

	private val MOD_ID = "daisy-player-management"
	val CONFIG = WrappedConfig.createToml(
		FabricLoader.getInstance().configDir,
		"daisy",
		MOD_ID,
		PlayerManagementConfig::class.java
	)

	override fun onInitialize() {
		if (!CONFIG.isEnabled) return
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			OfflineTpCommand.register(dispatcher)
			PlayerPosCommand.register(dispatcher)
			ViewCommand.register(dispatcher)
		}
	}

	fun savePlayerData(player: ServerPlayer) {
		val playerDataDir =
			(player as ServerPlayerAccessor).`daisy$getServer`().getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile()
		try {
			ProblemReporter.ScopedCollector(player.problemPath(), LogUtils.getLogger()).use { logging ->
				val nbtWriteView = TagValueOutput.createWithContext(logging, player.registryAccess())
				player.saveWithoutId(nbtWriteView)
				val path = playerDataDir.toPath()
				val path2 = Files.createTempFile(path, player.getStringUUID() + "-", ".dat")
				val nbtCompound = nbtWriteView.buildResult()
				NbtIo.writeCompressed(nbtCompound, path2)
				val path3 = path.resolve(player.getStringUUID() + ".dat")
				val path4 = path.resolve(player.getStringUUID() + ".dat_old")
				Util.safeReplaceFile(path3, path2, path4)
			}
		} catch (_: Exception) {
			LogUtils.getLogger().warn("Failed to save player data for {}", player.name.string)
		}
	}
}
