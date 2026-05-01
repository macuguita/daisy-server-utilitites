package com.macuguita.daisy.daisy_home.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_base.commands.command
import com.macuguita.daisy.daisy_base.commands.string
import com.macuguita.daisy.daisy_home.attachments.HomeData
import com.macuguita.daisy.daisy_home.attachments.Homes
import com.macuguita.daisy.daisy_home.data.AddHomeResult

object SetHomeCommand : CommandRegistrator {

	override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {

		dispatcher.command("sethome") {

			executes {
				addHome(source.playerOrException, "home")
			}

			argument("name", StringArgumentType.word()) {
				executes {
					addHome(source.playerOrException, string("name"))
				}
			}
		}
	}

	private fun addHome(player: ServerPlayer, name: String): CommandResult {
		val homeData: HomeData = Homes.get(player)

		return when (homeData.addHome(name, player.position(), player.level().dimension())) {
			AddHomeResult.SUCCESS -> {
				player.sendSystemMessage(
					Component.translatable("daisy.command.sethome.success", name)
				)
				CommandResult.SUCCESS
			}

			AddHomeResult.AT_CAPACITY -> {
				player.sendSystemMessage(
					Component.translatable("daisy.command.sethome.error.at_capacity", homeData.maxHomes)
						.withStyle(ChatFormatting.RED)
				)
				CommandResult.FAILURE
			}

			AddHomeResult.DUPLICATE_NAME -> {
				player.sendSystemMessage(
					Component.translatable("daisy.command.sethome.error.duplicate_name", name)
						.withStyle(ChatFormatting.RED)
				)
				CommandResult.FAILURE
			}
		}
	}
}
