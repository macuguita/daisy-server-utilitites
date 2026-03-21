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

import com.macuguita.daisy.daisy_base.commands.CommandRegistrator
import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_home.mixin.MinecraftServerAccessor
import com.macuguita.daisy.daisy_home.mixin.PlayerDataStorageAccessor
import com.macuguta.daisy.daisy_home.attachments.HomeAttachedData
import com.macuguta.daisy.daisy_home.attachments.Homes
import com.macuguta.daisy.daisy_home.data.Home
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import java.io.File

object ListHomesCommands : CommandRegistrator {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {

        dispatcher.register(
            literal("homes")
                .executes { ctx ->
                    val player = ctx.source.playerOrException
                    val homes = Homes.get(player).homes
                    sendHomeList(ctx.source, player.name.string, homes, useHomeCommand = true)
                }
        )

        dispatcher.register(
            literal("playerhomes")
                .requires { it.hasPermission(2) }
                .then(
                    argument("player", GameProfileArgument.gameProfile())
                        .executes { ctx ->
                            val profiles = GameProfileArgument.getGameProfiles(ctx, "player")
                            if (profiles.size != 1) {
                                ctx.source.sendFailure(
                                    Component.literal("Please specify exactly one player.")
                                        .withStyle(ChatFormatting.RED)
                                )
                                return@executes CommandResult.FAILURE.value
                            }

                            val profile = profiles.first()
                            val server = ctx.source.server
                            val onlinePlayer = server.playerList.getPlayer(profile.id)
                            val homes = if (onlinePlayer != null) {
                                Homes.get(onlinePlayer).homes
                            } else {
                                getOfflineHomes(server, profile.id)
                            }

                            if (homes == null) {
                                ctx.source.sendFailure(
                                    Component.literal("Could not find homes for '${profile.name}'.")
                                        .withStyle(ChatFormatting.RED)
                                )
                                return@executes CommandResult.FAILURE.value
                            }

                            sendHomeList(ctx.source, profile.name, homes, useHomeCommand = false)
                        }
                )
        )
    }

    private fun getOfflineHomes(server: MinecraftServer, uuid: java.util.UUID): List<Home>? {
        val playerDataStorageAccessor =
            (server as MinecraftServerAccessor).`daisy_home$getPlayerDataStorage`() as PlayerDataStorageAccessor
        val playerDir = playerDataStorageAccessor.`daisy_home$getPlayerDir`()
        val file = File(playerDir, "$uuid.dat")

        if (!file.exists() || !file.isFile) return null

        val nbt = try {
            net.minecraft.nbt.NbtIo.readCompressed(
                file.toPath(),
                net.minecraft.nbt.NbtAccounter.unlimitedHeap()
            )
        } catch (e: Exception) {
            return null
        }

        val attachments = nbt.getCompound("fabric:attachments")
        if (!attachments.contains("daisy-home:homes")) return null

        return HomeAttachedData.CODEC
            .parse(net.minecraft.nbt.NbtOps.INSTANCE, attachments.getCompound("daisy-home:homes"))
            .resultOrPartial { }
            .map { it.homes }
            .orElse(null)
    }

    private fun sendHomeList(
        source: CommandSourceStack,
        playerName: String,
        homes: List<Home>,
        useHomeCommand: Boolean
    ): Int {
        if (homes.isEmpty()) {
            source.sendFailure(
                Component.literal("$playerName has no homes set.")
                    .withStyle(ChatFormatting.RED)
            )
            return CommandResult.FAILURE.value
        }

        val text: MutableComponent = Component.literal("$playerName's Homes:")

        homes.forEach { home ->
            val pos = home.position
            val dim = home.dimension.location()

            val clickCommand = if (useHomeCommand) {
                "/home ${home.name}"
            } else {
                "/execute in $dim run tp @s ${pos.x} ${pos.y} ${pos.z}"
            }

            val locationText = Component.literal(
                "\n${home.name}: "
            ).append(
                Component.literal("$dim [${pos.x}, ${pos.y}, ${pos.z}]")
                    .withStyle { style ->
                        style
                            .withColor(ChatFormatting.GREEN)
                            .withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand))
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Click to teleport")
                                )
                            )
                    }
            )

            text.append(locationText)
        }

        source.sendSuccess({ text }, false)
        return CommandResult.SUCCESS.value
    }
}