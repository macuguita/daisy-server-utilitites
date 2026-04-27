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

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object BotManager {
	private lateinit var kord: Kord
	private lateinit var webhook: Webhook
	private lateinit var webhookToken: String
	private lateinit var scope: CoroutineScope
	private lateinit var loginJob: Job

	@Volatile
	private var isReady = false

	fun start(modScope: CoroutineScope) {
		scope = modScope
		loginJob = scope.launch {
			kord = Kord(DaisyDiscord.CONFIG.botToken)
			setupWebhook()
			listenToDiscord()
			isReady = true
			kord.login {
				@OptIn(PrivilegedIntent::class)
				intents += Intent.MessageContent
			}
		}
	}

	fun stop() {
		isReady = false
		runBlocking {
			kord.shutdown()
			loginJob.join()
			kord.resources.httpClient.close()
		}
		scope.cancel()
	}

	private suspend fun setupWebhook() {
		val channel = kord.getChannelOf<TextChannel>(Snowflake(DaisyDiscord.CONFIG.channelId))
			?: run {
				DaisyDiscord.LOGGER.error("Channel not found")
				return
			}

		val existing = channel.webhooks.firstOrNull { it.name == "Daisy MC Chat bridge" }
		if (existing != null) {
			webhook = existing
			webhookToken = existing.token
				?: run {
					DaisyDiscord.LOGGER.error("Webhook token null — is this webhook owned by the bot?")
					return
				}
		} else {
			val created = channel.createWebhook("Daisy MC Chat bridge") {
			}
			webhook = created
			webhookToken = created.token
				?: run {
					DaisyDiscord.LOGGER.error("Webhook token null after creation")
					return
				}
		}
	}

	private fun buildDiscordMessage(format: String, username: String, content: String, color: Int?): Component {
		val contentFormat = format.replace("%messageContent%", content)
		val parts = contentFormat.split("%username%")

		val root = Component.empty()
		parts.forEachIndexed { index, part ->
			if (part.isNotEmpty()) {
				root.append(Component.literal(part))
			}
			if (index < parts.size - 1) {
				val usernameComponent = Component.literal(username)
				if (color != null) usernameComponent.withStyle { it.withColor(color) }
				root.append(usernameComponent)
			}
		}
		return root
	}

	private fun listenToDiscord() {
		kord.on<MessageCreateEvent> {
			if (message.author?.isBot == true) return@on
			if (message.channelId != Snowflake(DaisyDiscord.CONFIG.channelId)) return@on

			val author = message.author ?: return@on
			val content = message.content

			val member = message.getGuildOrNull()?.getMemberOrNull(author.id)
			val username = member?.nickname ?: author.username
			val color = member?.roles
				?.toList()
				?.filter { it.color.rgb != 0 }
				?.maxByOrNull { it.rawPosition }
				?.color?.rgb
				?: 0x99AAB5

			DaisyDiscord.mcServer.playerList.broadcastSystemMessage(
				buildDiscordMessage(
					format = DaisyDiscord.CONFIG.discordMessageFormat,
					username = username,
					content = content,
					color = if (DaisyDiscord.CONFIG.colorUsernamesBasedOnRole) color else null
				),
				false
			)
		}
	}

	fun sendPlayerMessage(player: ServerPlayer, message: String) {
		if (!isReady) return
		scope.launch {
			webhook.execute(webhookToken) {
				username = player.name.string
				avatarUrl = "https://mc-heads.net/avatar/${player.stringUUID}/128"
				content = escapeChars(message)
				allowedMentions = AllowedMentionsBuilder()
			}
		}
	}

	fun sendSystemMessage(message: String) {
		if (!isReady) return
		scope.launch {
			kord.getChannelOf<TextChannel>(Snowflake(DaisyDiscord.CONFIG.channelId))
				?.createMessage(message)
		}
	}

	private fun escapeChars(text: String): String {
		val charsToEscape = setOf('\\', '*', '_', '~', '`', '>', '|', '[', ']', '(', ')', '#')
		return buildString {
			for (c in text) {
				if (c in charsToEscape) append('\\')
				append(c)
			}
		}
	}
}
