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

package com.macuguita.daisy.daisy_discord

import com.macuguita.daisy.daisy_discord.bot.BotManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

object MinecraftEvents {
	fun register() {
		ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
			BotManager.sendSystemMessage(
				DaisyDiscord.CONFIG.playerJoinMessage.replace(
					"%username%",
					handler.player.name.string
				)
			)
		}

		ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
			BotManager.sendSystemMessage(
				DaisyDiscord.CONFIG.playerLeaveMessage.replace(
					"%username%",
					handler.player.name.string
				)
			)
		}

		ServerMessageEvents.CHAT_MESSAGE.register { chat, sender, _ ->
			BotManager.sendPlayerMessage(sender, chat.signedContent().trim())
		}

		ServerLifecycleEvents.SERVER_STARTED.register {
			BotManager.sendSystemMessage(DaisyDiscord.CONFIG.serverStartedMessage)
		}

		ServerLifecycleEvents.SERVER_STOPPING.register {
			BotManager.sendSystemMessage(DaisyDiscord.CONFIG.serverStoppingMessage)
		}

		ServerLifecycleEvents.SERVER_STOPPED.register {
			BotManager.stop()
		}
	}
}
