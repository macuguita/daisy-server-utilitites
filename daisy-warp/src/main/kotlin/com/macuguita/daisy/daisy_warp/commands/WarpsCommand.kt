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

package com.macuguita.daisy.daisy_warp.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_warp.data.Warp
import com.macuguita.daisy.daisy_warp.saveddata.DaisyWarps

object WarpsCommand : CommandRegistrator {
	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.register(
			literal("warps")
				.executes { ctx ->
					listWarps(ctx.source)
				}
		)
	}

	private fun listWarps(source: CommandSourceStack): Int {
		val warps = DaisyWarps.get(source.server).all()

		if (warps.isEmpty()) {
			source.sendFailure(
				Component.translatable("daisy.command.warps.error.no_warps")
					.withStyle(ChatFormatting.RED)
			)
			return CommandResult.FAILURE.value
		}

		val text: MutableComponent = Component.translatable("daisy.command.warps.header")

		warps.forEach { warp ->
			text.append(buildWarpEntry(warp))
		}

		source.sendSuccess({ text }, false)
		return CommandResult.SUCCESS.value
	}

	private fun buildWarpEntry(warp: Warp): MutableComponent {
		val pos = warp.position
		val dim = warp.dimension.identifier()

		return Component.literal("\n${warp.name}: ").append(
			Component.literal("$dim [${pos.x}, ${pos.y}, ${pos.z}]")
				.withStyle { style ->
					style
						.withColor(ChatFormatting.GREEN)
						.withClickEvent(
							ClickEvent.RunCommand("/warp ${warp.name}")
						)
						.withHoverEvent(
							HoverEvent.ShowText(
								Component.translatable("daisy.tooltip.teleport", warp.name)
							)
						)
				}
		)
	}
}
