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

package com.macuguita.daisy.daisy_discord.bot

import com.macuguita.daisy.daisy_discord.DaisyDiscord
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.behavior.execute
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
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
import net.minecraft.server.level.ServerPlayer

object BotManager {

	private lateinit var kord: Kord
	private lateinit var webhook: Webhook
	private lateinit var webhookToken: String
	private lateinit var channel: TextChannel

	private lateinit var scope: CoroutineScope
	private lateinit var loginJob: Job

	@Volatile
	private var started = false
	@Volatile
	private var isReady = false

	fun start(modScope: CoroutineScope) {
		synchronized(this) {
			if (started) {
				DaisyDiscord.LOGGER.error("BotManager already started!")
				return
			}

			started = true
		}

		scope = modScope

		loginJob = scope.launch {
			try {
				kord = Kord(DaisyDiscord.CONFIG.botToken)

				setupChannel()
				setupWebhook()
				registerSlashCommands()
				listenToDiscord()

				kord.on<ReadyEvent> {
					isReady = true
					DaisyDiscord.LOGGER.info("Discord bot connected and ready")
				}

				kord.login {
					@OptIn(PrivilegedIntent::class)
					intents += Intent.MessageContent
				}

			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error("Failed to start Discord bot", e)
				isReady = false
				started = false
			}
		}
	}

	fun stop() {
		synchronized(this) {
			if (!started) return

			started = false
			isReady = false
		}

		runBlocking {
			try {
				kord.shutdown()
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error("Error during Kord shutdown", e)
			}

			try {
				loginJob.cancel()
				loginJob.join()
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error("Error while waiting for login job", e)
			}

			try {
				kord.resources.httpClient.close()
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error("Error closing HTTP client", e)
			}
		}

		scope.cancel()
	}

	private suspend fun setupChannel() {
		channel = kord.getChannelOf<TextChannel>(
			Snowflake(DaisyDiscord.CONFIG.channelId)
		) ?: error("Discord channel not found")
	}

	private suspend fun setupWebhook() {
		val existing = channel.webhooks.firstOrNull {
			it.name == "Daisy MC Chat bridge"
		}

		if (existing != null) {
			webhook = existing

			webhookToken = existing.token
				?: error("Webhook token is null")

			return
		}

		val created = channel.createWebhook("Daisy MC Chat bridge") {}
		webhook = created
		webhookToken = created.token
			?: error("Webhook token is null after creation")
	}

	private suspend fun registerSlashCommands() {
		val playerListCommand = "playerlist"
		kord.createGlobalChatInputCommand(
			playerListCommand,
			"Returns the player list of the Minecraft server"
		) {
		}
		kord.on<ChatInputCommandInteractionCreateEvent> {
			when (interaction.command.rootName) {
				playerListCommand -> {
					val mc = DaisyDiscord.mcServer
					val players = if (mc.playerList.playerCount == 0) {
						"Nobody is online"
					} else {
						mc.playerList.players.joinToString("\n") { it.name.string }
					}
					interaction.respondEphemeral {
						content = """
							Players online (${mc.playerList.playerCount}/${mc.playerList.maxPlayers}):
							$players
						""".trimIndent()
					}
				}
			}
		}
	}

	private fun listenToDiscord() {

		kord.on<MessageCreateEvent> {
			if (message.author?.isBot == true) return@on
			if (message.channelId != channel.id) return@on

			try {
				val author = message.author ?: return@on
				val member = message.getGuildOrNull()?.getMemberOrNull(author.id)

				val displayName =
					member?.nickname
						?: author.globalName
						?: author.username

				val color = member?.roles
					?.toList()
					?.filter { it.color.rgb != 0 }
					?.maxByOrNull { it.rawPosition }
					?.color?.rgb
					?: DaisyDiscord.CONFIG.defaultDiscordUsernameColor

				val mcMessage = MessageFormatter.buildDiscordMessage(
					format = DaisyDiscord.CONFIG.discordMessageFormat,
					displayName = displayName,
					username = author.username,
					content = message.content,
					color = if (DaisyDiscord.CONFIG.colorUsernamesBasedOnRole) color else null,
					replyComponent = MessageFormatter.buildReplyComponent(message.referencedMessage),
					attachmentComponent = MessageFormatter.buildAttachmentComponent(message.attachments)
				)

				DaisyDiscord.mcServer.playerList.broadcastSystemMessage(
					mcMessage,
					false
				)
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error(
					"Failed to process Discord message",
					e
				)
			}
		}
	}

	fun sendPlayerMessage(player: ServerPlayer, message: String) {
		if (!isReady) return

		scope.launch {
			try {
				webhook.execute(webhookToken) {
					username = player.name.string

					avatarUrl = DaisyDiscord.CONFIG.avatarHeadsApi
						.replace("%uuid%", player.stringUUID)

					content = MessageFormatter.escapeMarkdown(message)

					allowedMentions = AllowedMentionsBuilder()
				}
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error(
					"Failed to send player message to Discord",
					e
				)
			}
		}
	}

	fun sendSystemMessage(message: String) {
		if (!isReady) return

		scope.launch {
			try {
				channel.createMessage(message)
			} catch (e: Exception) {
				DaisyDiscord.LOGGER.error(
					"Failed to send system message to Discord: {}",
					message,
					e
				)
			}
		}
	}
}
