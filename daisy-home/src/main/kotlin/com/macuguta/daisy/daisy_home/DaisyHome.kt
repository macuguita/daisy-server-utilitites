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

package com.macuguta.daisy.daisy_home

import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.commands.DelHomeCommand
import com.macuguta.daisy.daisy_home.commands.HomeCommand
import com.macuguta.daisy.daisy_home.commands.ListHomesCommands
import com.macuguta.daisy.daisy_home.commands.SetHomeCommand
import com.macuguta.daisy.daisy_home.commands.SetMaxHomesCommand
import java.util.concurrent.CompletableFuture
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object DaisyHome : ModInitializer {
	private val MOD_ID = "daisy-home"

	override fun onInitialize() {
		if (!HomeConfig.INSTANCE.isEnabled) return
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			DelHomeCommand.register(dispatcher)
			HomeCommand.register(dispatcher)
			ListHomesCommands.register(dispatcher)
			SetHomeCommand.register(dispatcher)
			SetMaxHomesCommand.register(dispatcher)
		}
	}

	fun String.id(): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, this)

	fun suggestHomes(
		context: CommandContext<CommandSourceStack>,
		builder: SuggestionsBuilder,
	): CompletableFuture<Suggestions> {
		val player = context.source.playerOrException
		val homeNames = Homes.get(player).homes.map { it.name }
		homeNames.forEach { builder.suggest(it) }
		return builder.buildFuture()
	}
}
