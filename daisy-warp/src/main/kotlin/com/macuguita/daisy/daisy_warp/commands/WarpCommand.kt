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

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_warp.saveddata.DaisyWarps
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object WarpCommand : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal("warp")
                .then(
                    argument("name", StringArgumentType.word())
                        .suggests { ctx, builder ->
                            DaisyWarps.get(ctx.source.server).all()
                                .forEach { builder.suggest(it.name) }
                            builder.buildFuture()
                        }
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            val name = StringArgumentType.getString(ctx, "name")
                            warp(player, ctx.source.server, name)
                        }
                )
        )
    }

    private fun warp(player: ServerPlayer, server: MinecraftServer, name: String): Int {
        val warps = DaisyWarps.get(server)

        val warp = warps.find(name)
            ?: return CommandResult.FAILURE.value.also {
                player.sendSystemMessage(
                    Component.literal("Warp '$name' does not exist.")
                        .withStyle(ChatFormatting.RED)
                )
            }

        val level = server.getLevel(warp.dimension)
            ?: return CommandResult.FAILURE.value.also {
                player.sendSystemMessage(
                    Component.literal("Could not find dimension '${warp.dimension.location()}'.")
                        .withStyle(ChatFormatting.RED)
                )
            }

        player.teleportTo(
            level,
            warp.position.x + 0.5,
            warp.position.y.toDouble(),
            warp.position.z + 0.5,
            player.yRot,
            player.xRot
        )
        player.sendSystemMessage(Component.literal("Teleported to warp '$name'."))
        return CommandResult.SUCCESS.value
    }
}