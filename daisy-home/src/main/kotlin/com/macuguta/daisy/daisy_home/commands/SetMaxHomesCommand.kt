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

import com.macuguta.daisy.daisy_home.attachments.Homes
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult

object SetMaxHomesCommand : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("setmaxhomes")
				.requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
				.then(
					argument("player", EntityArgument.player())
						.then(
							argument("amount", IntegerArgumentType.integer(1))
								.executes { ctx ->
									val player = EntityArgument.getPlayer(ctx, "player")
									val amount = IntegerArgumentType.getInteger(ctx, "amount")
									Homes.get(player).maxHomes = amount
									player.sendSystemMessage(
										Component.translatable("daisy.command.setmaxhomes.feedback.target", amount)
									)
									ctx.source.sendSuccess(
										{
											Component.translatable(
												"daisy.command.setmaxhomes.feedback.user",
												player.name.string,
												amount
											)
										},
										true
									)
									CommandResult.SUCCESS.value
								}
						)
				)
		)
	}
}
