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

package com.macuguita.daisy.daisy_tpa

import com.macuguita.daisy.daisy_base.commands.CommandResult
import com.macuguita.daisy.daisy_tpa.commands.TpaAcceptCommand
import com.macuguita.daisy.daisy_tpa.commands.TpaCommand
import com.macuguita.daisy.daisy_tpa.commands.TpaHereCommand
import com.macuguita.daisy.daisy_tpa.data.TpaManager
import com.macuguita.daisy.daisy_tpa.data.TpaRequest
import com.macuguita.daisy.daisy_tpa.data.TpaType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.level.ServerPlayer
import org.slf4j.LoggerFactory

object DaisyTpa : ModInitializer {
    private val MOD_ID = "daisy-tpa"
    private val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        if (!TpaConfig.INSTANCE.isEnabled) return
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            TpaCommand.register(dispatcher)
            TpaHereCommand.register(dispatcher)
            TpaAcceptCommand.register(dispatcher)
        }
    }

    fun handle(
        ctx: CommandContext<CommandSourceStack>,
        type: TpaType
    ): Int {
        val sender = ctx.source.player ?: return CommandResult.FAILURE.value
        val target = EntityArgument.getPlayer(ctx, "player") ?: return CommandResult.FAILURE.value

        if (sender.uuid == target.uuid) {
            ctx.source.sendFailure(Component.literal("You cannot request a teleport to yourself"))
            return CommandResult.FAILURE.value
        }

        val success = TpaManager.sendRequest(
            TpaRequest(
                requester = sender.uuid,
                target = target.uuid,
                type = type,
                timestamp = System.currentTimeMillis()
            ),
            TpaConfig.INSTANCE.requestExpiryMs
        )

        if (!success) {
            ctx.source.sendFailure(Component.literal("You already have a pending request to this player"))
            return CommandResult.FAILURE.value
        }

        sendTpaFeedback(sender, target, type)
        return CommandResult.SUCCESS.value
    }

    private fun sendTpaFeedback(sender: ServerPlayer, target: ServerPlayer, type: TpaType) {
        val senderName = sender.name.string
        val targetName = target.name.string

        val senderMessage = when (type) {
            TpaType.TO ->
                "Sent teleport request to $targetName"

            TpaType.HERE ->
                "Requested $targetName to teleport to you"
        }

        val targetMessage = when (type) {
            TpaType.TO ->
                "$senderName wants to teleport to you. "

            TpaType.HERE ->
                "$senderName wants you to teleport to them. "
        }

        sender.sendSystemMessage(Component.literal(senderMessage))

        target.sendSystemMessage(
            Component.literal(targetMessage)
                .append(acceptButton(senderName))
        )
    }

    private fun acceptButton(senderName: String): Component =
        Component.literal("[Click to accept]").withStyle {
            it.withClickEvent(
                ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/tpaaccept $senderName"
                )
            )
                .withColor(ChatFormatting.GREEN)
                .withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.literal("Click to accept teleport request")
                    )
                )
        }
}
