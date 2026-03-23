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

package com.macuguta.daisy.daisy_home.commands

import com.macuguta.daisy.daisy_home.attachments.HomeData
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.data.AddHomeResult
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult

object SetHomeCommand : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("sethome")
				.then(
					argument("name", StringArgumentType.word())
						.executes { ctx ->
							val player = ctx.source.playerOrException
							val name = StringArgumentType.getString(ctx, "name")
							return@executes addHome(player, name)
						}
				)
				.executes { ctx ->
					val player = ctx.source.playerOrException
					return@executes addHome(player, "home")
				}
		)
	}

	private fun addHome(player: ServerPlayer, name: String): Int {
		val homeData: HomeData = Homes.get(player)

		return when (homeData.addHome(name, player.blockPosition(), player.level().dimension())) {
			AddHomeResult.SUCCESS -> {
				player.sendSystemMessage(Component.translatable("daisy.command.sethome.success", name))
				CommandResult.SUCCESS.value
			}

			AddHomeResult.AT_CAPACITY -> {
				player.sendSystemMessage(
					Component.translatable("daisy.command.sethome.error.at_capacity", homeData.maxHomes)
						.withStyle(ChatFormatting.RED)
				)
				CommandResult.FAILURE.value
			}

			AddHomeResult.DUPLICATE_NAME -> {
				player.sendSystemMessage(
					Component.translatable("daisy.command.sethome.error.duplicate_name", name)
						.withStyle(ChatFormatting.RED)
				)
				CommandResult.FAILURE.value
			}
		}
	}
}
