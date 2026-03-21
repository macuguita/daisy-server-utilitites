package com.macuguta.daisy.daisy_home.commands

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguta.daisy.daisy_home.DaisyHome
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object HomeCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("home")
                .then(
                    Commands.argument("name", StringArgumentType.word())
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
                    Component.literal("Home '$name' does not exist.")
                        .withStyle(ChatFormatting.RED)
                )
            }

        val level = server.getLevel(home.dimension)
            ?: return CommandResult.FAILURE.value.also {
                player.sendSystemMessage(
                    Component.literal("Could not find dimension '${home.dimension.location()}'.")
                        .withStyle(ChatFormatting.RED)
                )
            }

        player.teleportTo(
            level,
            home.position.x + 0.5,
            home.position.y.toDouble(),
            home.position.z + 0.5,
            player.yRot,
            player.xRot
        )
        player.sendSystemMessage(Component.literal("Teleported to home '$name'."))
        return CommandResult.SUCCESS.value
    }
}