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

package com.macuguita.daisy.daisy_tpa.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_tpa.TpaConfig
import com.macuguita.daisy.daisy_tpa.data.TpaManager
import com.macuguita.daisy.daisy_tpa.data.TpaRequest
import com.macuguita.daisy.daisy_tpa.data.TpaType

object TpaAcceptCommand : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("tpaaccept")
				.executes { ctx ->
					val player = ctx.source.playerOrException
					val req = TpaManager.popMostRecent(
						player.uuid,
						TpaConfig.INSTANCE.requestExpiryMs
					) ?: run {
						ctx.source.sendFailure(Component.translatable("daisy.command.tpaacept.error.no_requests"))
						return@executes CommandResult.FAILURE.value
					}

					executeTeleport(player, req)
					CommandResult.SUCCESS.value
				}
				.then(
					argument("player", EntityArgument.player())
						.executes { ctx ->
							val player = ctx.source.playerOrException
							val requester = EntityArgument.getPlayer(ctx, "player")

							val req = TpaManager.popFromRequester(
								player.uuid, requester.uuid,
								TpaConfig.INSTANCE.requestExpiryMs
							) ?: run {
								ctx.source.sendFailure(
									Component.translatable(
										"daisy.command.tpaacept.error.no_requests_from_player",
										requester.name.string
									)
								)
								return@executes CommandResult.FAILURE.value
							}

							executeTeleport(player, req)
							CommandResult.SUCCESS.value
						}
				)
		)
	}

	private fun executeTeleport(target: ServerPlayer, req: TpaRequest) {
		val server = target.server ?: return
		val requester = server.playerList.getPlayer(req.requester) ?: return

		val targetName = target.gameProfile.name
		val requesterName = requester.gameProfile.name

		when (req.type) {
			TpaType.TO -> {
				requester.stopRiding()
				requester.teleportTo(
					target.serverLevel(),
					target.x,
					target.y,
					target.z,
					target.yRot,
					target.xRot
				)

				target.sendSystemMessage(
					Component.translatable(
						"daisy.command.tpaacept.feedback.target",
						requesterName
					)
				)
				requester.sendSystemMessage(
					Component.translatable(
						"daisy.command.tpaacept.feedback.requester",
						targetName
					)
				)
			}

			TpaType.HERE -> {
				target.stopRiding()
				target.teleportTo(
					requester.serverLevel(),
					requester.x,
					requester.y,
					requester.z,
					requester.yRot,
					requester.xRot
				)

				target.sendSystemMessage(Component.translatable("daisy.command.tpaacept.feedback.target", target))
				requester.sendSystemMessage(
					Component.translatable(
						"daisy.command.tpaacept.feedback.requester",
						requester
					)
				)
			}
		}
	}
}
