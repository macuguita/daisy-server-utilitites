package com.macuguta.daisy.daisy_home.commands

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component

object SetMaxHomesCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("setmaxhomes")
                .requires { it.hasPermission(2) }
                .then(
                    Commands.argument("player", EntityArgument.player())
                        .then(
                            Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes { ctx ->
                                    val player = EntityArgument.getPlayer(ctx, "player")
                                    val amount = IntegerArgumentType.getInteger(ctx, "amount")
                                    Homes.get(player).maxHomes = amount
                                    player.sendSystemMessage(
                                        Component.literal("Max homes set to $amount.")
                                    )
                                    ctx.source.sendSuccess(
                                        { Component.literal("Set max homes for ${player.name.string} to $amount.") },
                                        true
                                    )
                                    CommandResult.SUCCESS.value
                                }
                        )
                )
        )
    }
}