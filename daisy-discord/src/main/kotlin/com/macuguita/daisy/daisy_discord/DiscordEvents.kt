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
import com.macuguita.daisy.daisy_base.event.OnChatMessageEvent
import com.macuguita.daisy.daisy_base.event.PlayerJoinEvent
import com.macuguita.daisy.daisy_base.event.PlayerLeaveEvent
import com.macuguita.daisy.daisy_base.event.ServerStartedEvent
import com.macuguita.daisy.daisy_base.event.ServerStoppedEvent
import com.macuguita.daisy.daisy_base.event.ServerStoppingEvent

object DiscordEvents {
	fun register() {
		PlayerJoinEvent.EVENT.register { _, player, _ ->
			BotManager.sendSystemMessage(
				DaisyDiscord.CONFIG.playerJoinMessage.replace(
					"%username%",
					player.name.string
				)
			)
		}

		PlayerLeaveEvent.EVENT.register { player ->
			BotManager.sendSystemMessage(
				DaisyDiscord.CONFIG.playerLeaveMessage.replace(
					"%username%",
					player.name.string
				)
			)
		}

		OnChatMessageEvent.EVENT.register { chat, sender, _ ->
			BotManager.sendPlayerMessage(sender, chat.signedContent().trim())
		}

		ServerStartedEvent.EVENT.register {
			BotManager.sendSystemMessage(DaisyDiscord.CONFIG.serverStartedMessage)
		}

		ServerStoppingEvent.EVENT.register {
			BotManager.sendSystemMessage(DaisyDiscord.CONFIG.serverStoppingMessage)
		}

		ServerStoppedEvent.EVENT.register {
			BotManager.stop()
		}
	}
}
