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

package com.macuguita.daisy.daisy_warp

import folk.sisby.kaleido.api.WrappedConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import com.macuguita.daisy.daisy_warp.commands.AddWarpCommand
import com.macuguita.daisy.daisy_warp.commands.DelWarpCommand
import com.macuguita.daisy.daisy_warp.commands.SpawnCommand
import com.macuguita.daisy.daisy_warp.commands.WarpCommand
import com.macuguita.daisy.daisy_warp.commands.WarpsCommand

object DaisyWarp : ModInitializer {

	private val MOD_ID = "daisy-warp"
	val CONFIG = WrappedConfig.createToml(FabricLoader.getInstance().configDir, "daisy", MOD_ID, WarpConfig::class.java)

	override fun onInitialize() {
		if (!CONFIG.isEnabled) return
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			AddWarpCommand.register(dispatcher)
			DelWarpCommand.register(dispatcher)
			if (CONFIG.allowSpawnCommand) {
				SpawnCommand.register(dispatcher)
			}
			WarpCommand.register(dispatcher)
			WarpsCommand.register(dispatcher)
		}
	}
}
