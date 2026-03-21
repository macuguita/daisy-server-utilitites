package com.macuguta.daisy.daisy_home.commands

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguta.daisy.daisy_home.attachments.HomeData
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.data.AddHomeResult
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object SetHomeCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("sethome")
                .then(
                    Commands.argument("name", StringArgumentType.word())
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
                player.sendSystemMessage(Component.literal("Home '$name' set at your current position!"))
                CommandResult.SUCCESS.value
            }
            AddHomeResult.AT_CAPACITY -> {
                player.sendSystemMessage(
                    Component.literal("You have reached the maximum number of homes (${homeData.maxHomes}).")
                        .withStyle(ChatFormatting.RED)
                )
                CommandResult.FAILURE.value
            }
            AddHomeResult.DUPLICATE_NAME -> {
                player.sendSystemMessage(
                    Component.literal("A home named '$name' already exists.")
                        .withStyle(ChatFormatting.RED)
                )
                CommandResult.FAILURE.value
            }
        }
    }
}