package com.macuguta.daisy.daisy_home.commands

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguta.daisy.daisy_home.DaisyHome
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.data.RemoveHomeResult
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DelHomeCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("delhome")
                .then(
                    Commands.argument("name", StringArgumentType.word())
                        .suggests { context, builder -> DaisyHome.suggestHomes(context, builder) }
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            val name = StringArgumentType.getString(ctx, "name")
                            deleteHome(player, name)
                        }
                )
        )
    }

    private fun deleteHome(player: ServerPlayer, name: String): Int {
        val homeData = Homes.get(player)

        return when (homeData.removeHome(name)) {
            RemoveHomeResult.SUCCESS -> {
                player.sendSystemMessage(Component.literal("Home '$name' has been removed."))
                CommandResult.SUCCESS.value
            }
            RemoveHomeResult.NOT_FOUND -> {
                player.sendSystemMessage(
                    Component.literal("Home '$name' does not exist.")
                        .withStyle(ChatFormatting.RED)
                )
                CommandResult.FAILURE.value
            }
        }
    }
}