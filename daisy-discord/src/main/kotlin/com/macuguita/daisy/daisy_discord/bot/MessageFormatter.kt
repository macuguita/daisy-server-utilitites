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

import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Message
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import com.macuguita.daisy.daisy_discord.DaisyDiscord

object MessageFormatter {

	fun buildDiscordMessage(
		format: String,
		displayName: String,
		username: String,
		content: String,
		color: Int?,
		replyComponent: Component?,
		attachmentComponent: Component?
	): Component {
		val contentFormat = format.replace("%messageContent%", content)
		val parts = contentFormat.split("%username%")

		val root = Component.empty()

		if (replyComponent != null) {
			root.append(replyComponent)
			root.append(Component.literal("\n"))
		}

		parts.forEachIndexed { index, part ->
			if (part.isNotEmpty()) {
				root.append(Component.literal(part))
			}

			if (index < parts.size - 1) {
				root.append(buildUsernameComponent(displayName, username, color))
			}
		}

		if (attachmentComponent != null) {
			if (content.isNotBlank()) {
				root.append(Component.literal(" "))
			}
			root.append(attachmentComponent)
		}

		return root
	}

	private fun buildUsernameComponent(
		displayName: String,
		username: String,
		color: Int?
	): MutableComponent =
		Component.literal(displayName).withStyle { style ->
			style
				.let { s -> if (color != null) s.withColor(color) else s }
				.let { s ->
					if (displayName != username) {
						s.withHoverEvent(
							HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								Component.literal("@$username")
							)
						)
					} else s
				}
		}

	fun buildReplyComponent(referencedMessage: Message?): MutableComponent? {
		referencedMessage ?: return null

		val config = DaisyDiscord.CONFIG
		val replyAuthor = referencedMessage.author?.username
			?: referencedMessage.webhookId?.let { referencedMessage.data.author.username }
			?: "Unknown"
		val replyContent = referencedMessage.content.ifBlank { "[attachment/embed]" }
		val preview = if (replyContent.length > 120) replyContent.take(120) + "..." else replyContent

		val authorComponent = Component.literal(replyAuthor).withStyle {
			it.withColor(config.replyAuthorColor)
				.withItalic(true)
				.withHoverEvent(
					HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						Component.literal("<@$replyAuthor> $replyContent")
					)
				)
		}
		val contentComponent = Component.literal(preview).withStyle {
			it.withColor(config.replyContentColor).withItalic(true)
		}

		val parts = config.replyFormat.split("%replyAuthor%", "%replyContent%")
		val placeholders = Regex("%replyAuthor%|%replyContent%").findAll(config.replyFormat)
			.map { it.value }.toList()

		val root = Component.empty()
		parts.forEachIndexed { index, part ->
			if (part.isNotEmpty()) root.append(Component.literal(part).withStyle { it.withItalic(true) })
			if (index < placeholders.size) {
				root.append(when (placeholders[index]) {
					"%replyAuthor%" -> authorComponent
					"%replyContent%" -> contentComponent
					else -> Component.empty()
				})
			}
		}
		return root
	}

	fun buildAttachmentComponent(attachments: Collection<Attachment>): MutableComponent? {
		if (attachments.isEmpty()) return null

		val config = DaisyDiscord.CONFIG
		val component = Component.empty()

		attachments.forEachIndexed { index, attachment ->
			if (index > 0) component.append(Component.literal(" "))

			val label = when {
				attachment.contentType?.startsWith("image/") == true -> "Image"
				attachment.contentType?.startsWith("video/") == true -> "Video"
				else -> "File"
			}

			val text = config.attachmentFormat
				.replace("%attachmentLabel%", label)
				.replace("%attachmentFilename%", attachment.filename)
				.replace("%attachmentUrl%", attachment.url)

			component.append(
				Component.literal(text).withStyle {
					it.withColor(config.attachmentLinkColor)
						.withUnderlined(true)
						.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, attachment.url))
						.withHoverEvent(
							HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								Component.literal(attachment.url)
							)
						)
				}
			)
		}

		return component
	}

	fun escapeMarkdown(text: String): String {
		val charsToEscape = setOf('\\', '*', '_', '~', '`', '>', '|', '[', ']', '(', ')', '#')
		return buildString {
			for (c in text) {
				if (c in charsToEscape) append('\\')
				append(c)
			}
		}
	}
}
