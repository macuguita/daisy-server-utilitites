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

import com.macuguta.daisy.daisy_home.DaisyHome
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult

object HomeCommand : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("home")
				.then(
					argument("name", StringArgumentType.word())
						.suggests { context, builder -> DaisyHome.suggestHomes(context, builder) }
						.executes { ctx ->
							val player = ctx.source.playerOrException
							val name = StringArgumentType.getString(ctx, "name")
							teleportToHome(player, ctx.source.server, name)
						}
				)
				.executes { ctx ->
					val player = ctx.source.playerOrException
					teleportToHome(player, ctx.source.server, "home")
				}
		)
	}

	private fun teleportToHome(player: ServerPlayer, server: MinecraftServer, name: String): Int {
		val homeData = Homes.get(player)
		val home = homeData.homes.find { it.name == name.lowercase() }
			?: return CommandResult.FAILURE.value.also {
				player.sendSystemMessage(
					Component.translatable("daisy.command.home.error.not_found", name)
						.withStyle(ChatFormatting.RED)
				)
			}

		val level = server.getLevel(home.dimension)
			?: return CommandResult.FAILURE.value.also {
				player.sendSystemMessage(
					Component.translatable("daisy.command.home.error.level_not_found", home.dimension.location())
						.withStyle(ChatFormatting.RED)
				)
			}

		player.teleportTo(
			level,
			home.position.x,
			home.position.y,
			home.position.z,
			player.yRot,
			player.xRot
		)
		player.sendSystemMessage(Component.translatable("daisy.command.home.success", name))
		return CommandResult.SUCCESS.value
	}
}
