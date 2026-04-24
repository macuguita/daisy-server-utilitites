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

import com.macuguta.daisy.daisy_home.attachments.HomeAttachedData
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.data.Home
import java.util.*
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_base.commands.command
import com.macuguita.daisy.daisy_base.commands.gameProfile
import com.macuguita.daisy.daisy_base.toCommandString
import com.macuguita.daisy.daisy_base.toShortString

object ListHomesCommands : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {

		dispatcher.command("homes") {
			executes {
				val player = source.playerOrException
				sendHomeList(source, player.name.string, Homes.get(player).homes, useHomeCommand = true)
			}
		}

		dispatcher.command("playerhomes") {
			requires(Commands.hasPermission(Commands.LEVEL_ADMINS))

			argument("player", GameProfileArgument.gameProfile()) {
				executes {
					val profiles = gameProfile("player")

					if (profiles.size != 1) {
						source.sendFailure(
							Component.translatable("daisy.command.playerhomes.error.one_player")
								.withStyle(ChatFormatting.RED)
						)
						return@executes CommandResult.FAILURE
					}

					val profile = profiles.first()
					val server = source.server
					val onlinePlayer = server.playerList.getPlayer(profile.id)
					val homes = if (onlinePlayer != null) {
						Homes.get(onlinePlayer).homes
					} else {
						getOfflineHomes(server, profile.id)
					}

					if (homes == null) {
						source.sendFailure(
							Component.translatable("daisy.command.playerhomes.error.no_homes_found", profile.name)
								.withStyle(ChatFormatting.RED)
						)
						return@executes CommandResult.FAILURE
					}

					sendHomeList(source, profile.name, homes, useHomeCommand = false)
				}
			}
		}
	}

	private fun getOfflineHomes(server: MinecraftServer, uuid: UUID): List<Home>? {
		val nbt = server.playerDataStorage.`daisy$getNbt`(uuid)

		val attachments = nbt.getCompound("fabric:attachments")
			.flatMap { it.getCompound("daisy-home:homes") }
		if (attachments.isEmpty()) return null

		return HomeAttachedData.CODEC
			.parse(NbtOps.INSTANCE, attachments.get())
			.resultOrPartial { }
			.map { it.homes }
			.orElse(null)
	}

	private fun sendHomeList(
		source: CommandSourceStack,
		playerName: String,
		homes: List<Home>,
		useHomeCommand: Boolean,
	): CommandResult {
		if (homes.isEmpty()) {
			source.sendFailure(
				Component.translatable("daisy.command.homes.error.no_homes", playerName)
					.withStyle(ChatFormatting.RED)
			)
			return CommandResult.FAILURE
		}

		val text: MutableComponent = Component.translatable("daisy.command.homes.feedback.1", playerName)

		homes.forEach { home ->
			val pos = home.position
			val dim = home.dimension.identifier()

			val clickCommand = if (useHomeCommand) {
				"/home ${home.name}"
			} else {
				"/execute in $dim run tp @s ${pos.toCommandString()}"
			}

			val locationText = Component.literal(
				"\n${home.name}: "
			).append(
				Component.literal("$dim ${pos.toShortString()}")
					.withStyle { style ->
						style
							.withColor(ChatFormatting.GREEN)
							.withClickEvent(ClickEvent.RunCommand(clickCommand))
							.withHoverEvent(
								HoverEvent.ShowText(
									Component.translatable("daisy.tooltip.teleport")
								)
							)
					}
			)

			text.append(locationText)
		}

		source.sendSuccess({ text }, false)
		return CommandResult.SUCCESS
	}
}
