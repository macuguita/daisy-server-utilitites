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

package com.macuguita.daisy.daisy_player_management.commands

import java.util.*
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.phys.Vec3
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_base.toCommandString
import com.macuguita.daisy.daisy_base.toShortString

object PlayerPosCommand : CommandRegistrator {

	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("playerpos")
				.requires { it.hasPermission(2) }
				.then(
					argument("player", GameProfileArgument.gameProfile())
						.executes { ctx ->
							val profiles = GameProfileArgument.getGameProfiles(ctx, "player")
							if (profiles.size != 1) {
								ctx.source.sendFailure(
									Component.translatable("daisy.command.playerpos.error.one_player")
										.withStyle(ChatFormatting.RED)
								)
								return@executes CommandResult.FAILURE.value
							}

							val profile = profiles.first()
							val server = ctx.source.server
							val onlinePlayer = server.playerList.getPlayer(profile.id)
							val playerPos = if (onlinePlayer != null) {
								getOnlinePlayerPos(server, profile.id)
							} else {
								getOfflinePlayerPos(server, profile.id)
							}
							val dim = if (onlinePlayer != null) {
								getOnlinePlayerLevel(server, profile.id)
							} else {
								getOfflinePlayerLevel(server, profile.id)
							}

							ctx.source.sendSuccess({
								Component.translatable("daisy.command.playerpos.header", profile.name)
									.append(
										Component.literal(" $dim ${playerPos.toShortString()}")
											.withStyle { style ->
												style.withColor(ChatFormatting.GREEN)
													.withClickEvent(
														ClickEvent(
															ClickEvent.Action.RUN_COMMAND,
															"/execute in $dim run tp @s ${playerPos.toCommandString()}"
														)
													)
													.withHoverEvent(
														HoverEvent(
															HoverEvent.Action.SHOW_TEXT,
															Component.translatable("daisy.tooltip.teleport")
														)
													)
											})
							}, false)

							CommandResult.SUCCESS.value
						}
				)
		)
	}

	fun getOnlinePlayerPos(server: MinecraftServer, uuid: UUID): Vec3 {
		val player = server.playerList.getPlayer(uuid)
		return player?.position() ?: Vec3.ZERO
	}

	fun getOfflinePlayerPos(server: MinecraftServer, uuid: UUID): Vec3 {
		val nbt = server.playerDataStorage.`daisy$getNbt`(uuid)

		val pos = nbt.getList("Pos", Tag.TAG_DOUBLE.toInt())
		val x = pos.getDouble(0)
		val y = pos.getDouble(1)
		val z = pos.getDouble(2)

		return Vec3(x, y, z)
	}

	fun getOnlinePlayerLevel(server: MinecraftServer, uuid: UUID): String {
		val player = server.playerList.getPlayer(uuid)
		return if (player == null) getOfflinePlayerLevel(server, uuid) else player.level().dimension().location()
			.toString()
	}

	fun getOfflinePlayerLevel(server: MinecraftServer, uuid: UUID): String {
		val nbt = server.playerDataStorage.`daisy$getNbt`(uuid)

		return nbt.getString("Dimension")
	}
}
