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

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.logging.LogUtils
import com.mojang.serialization.Dynamic
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.ProblemReporter
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Items
import net.minecraft.world.level.dimension.DimensionType
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_base.commands.command
import com.macuguita.daisy.daisy_base.commands.gameProfile
import com.macuguita.daisy.daisy_player_management.menu.SavingPlayerDataMenu
import com.macuguita.daisy.daisy_player_management.mixin.EntityAccessor


object ViewCommand : CommandRegistrator {

	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
		dispatcher.command("inview") {
			requires { it.hasPermission(Commands.LEVEL_ADMINS) }

			argument("player", GameProfileArgument.gameProfile()) {
				executes {
					val target = resolveTarget() ?: return@executes CommandResult.FAILURE
					val viewer = source.player ?: return@executes CommandResult.FAILURE

					openInventoryGui(viewer, target)
					CommandResult.SUCCESS
				}
			}
		}

		dispatcher.command("echestview") {
			requires { it.hasPermission(Commands.LEVEL_ADMINS) }

			argument("player", GameProfileArgument.gameProfile()) {
				executes {
					val target = resolveTarget() ?: return@executes CommandResult.FAILURE
					val viewer = source.player ?: return@executes CommandResult.FAILURE

					openEnderChestGui(viewer, target)
					CommandResult.SUCCESS
				}
			}
		}
	}

	private fun CommandContext<CommandSourceStack>.resolveTarget(): ServerPlayer? {
		val profiles = gameProfile("player")

		if (profiles.size != 1) {
			source.sendFailure(
				Component.translatable("daisy.command.playerpos.error.one_player")
					.withStyle(ChatFormatting.RED)
			)
			return null
		}

		return getPlayer(profiles.first(), source)
	}

	private fun openInventoryGui(viewer: ServerPlayer, target: ServerPlayer) {
		val gui = SavingPlayerDataMenu(MenuType.GENERIC_9x5, viewer, target)
		gui.title = target.name
		addBackground(gui)

		for (i in 0 until target.inventory.containerSize) {
			gui.setSlotRedirect(i, Slot(target.inventory, i, 0, 0))
		}

		gui.open()
	}

	private fun openEnderChestGui(viewer: ServerPlayer, target: ServerPlayer) {
		val echest = target.enderChestInventory

		val type = when (echest.containerSize) {
			9 -> MenuType.GENERIC_9x1
			18 -> MenuType.GENERIC_9x2
			36 -> MenuType.GENERIC_9x4
			45 -> MenuType.GENERIC_9x5
			54 -> MenuType.GENERIC_9x6
			else -> MenuType.GENERIC_9x3
		}

		val gui = SavingPlayerDataMenu(type, viewer, target)
		gui.setTitle(target.name)
		addBackground(gui)

		for (i in 0 until echest.containerSize) {
			gui.setSlotRedirect(i, Slot(echest, i, 0, 0))
		}

		gui.open()
	}

	private fun getPlayer(profile: GameProfile, source: CommandSourceStack): ServerPlayer? {
		var requestedPlayer =
			source.server.playerList.getPlayer(profile.id)

		if (requestedPlayer == null) {
			requestedPlayer =
				source.server.playerList.getPlayerForLogin(profile, ClientInformation.createDefault())
			val compoundOpt = source.server.playerList.load(requestedPlayer)
			if (compoundOpt.isPresent) {
				val compound = compoundOpt.get()
				if (compound.contains("Dimension")) {
					val world = source.server.getLevel(
						DimensionType.parseLegacy(Dynamic(NbtOps.INSTANCE, compound.get("Dimension")))
							.result().get()
					)

					if (world != null) {
						(requestedPlayer as EntityAccessor).`daisy$setLevel`(world)
					}
				}
			}
		}

		return requestedPlayer
	}

//	private fun getPlayer(profile: GameProfile, source: CommandSourceStack): ServerPlayer? {
//		var requestedPlayer = source.server.playerList.getPlayer(profile.id)
//
//		if (requestedPlayer == null) {
//			requestedPlayer = ServerPlayer(
//				source.server,
//				source.server.overworld(),
//				GameProfile(profile.id, profile.name),
//				ClientInformation.createDefault()
//			)
//			val readViewOpt = source.server.playerList
//				.loadPlayerData(profile).map({ playerData ->
//					TagValueInput.create(
//						ProblemReporter.ScopedCollector(LogUtils.getLogger()),
//						source.server.registryAccess(),
//						playerData
//					)
//				})
//			readViewOpt.ifPresent({ input -> requestedPlayer.load(input) })
//
//			if (readViewOpt.isPresent) {
//				val readView = readViewOpt.get()
//				val dimension = readView.getString("Dimension")
//
//				if (dimension.isPresent) {
//					val world = source.server.getLevel(
//						ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(dimension.get()) ?: return null)
//					)
//
//					if (world != null) {
//						(requestedPlayer as EntityAccessor).`daisy$setLevel`(world)
//					}
//				}
//			}
//		}
//		return requestedPlayer
//	}

	private fun addBackground(gui: SimpleGui) {
		for (i in 0..<gui.getSize()) {
			gui.setSlot(i, GuiElementBuilder(Items.BARRIER).setName(Component.literal("")).build())
		}
	}
}
