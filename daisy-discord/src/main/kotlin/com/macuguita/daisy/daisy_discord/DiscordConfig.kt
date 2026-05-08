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

import folk.sisby.kaleido.api.WrappedConfig
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange

class DiscordConfig : WrappedConfig() {
	@Comment("Whether the module should be enabled.")
	var isEnabled: Boolean = false
	var botToken: String = "PLACE_TOKEN_HERE"
	var channelId: String = "PLACE_CHANNEL_ID_HERE"
	var avatarHeadsApi: String = "https://mc-heads.net/avatar/%uuid%"
	var playerJoinMessage: String = "%username% joined the server"
	var playerLeaveMessage: String = "%username% left the server"
	var serverStartedMessage: String = "Server started."
	var serverStoppingMessage: String = "Server stopping..."
	var discordMessageFormat: String = "<%username%> %messageContent%"
	@Comment("Format for reply indicators. Placeholders: %replyAuthor% (styled with replyAuthorColor), %replyContent% (styled with replyContentColor).")
	var replyFormat: String = "↪ %replyAuthor% %replyContent%"
	@Comment("Format for attachment links. Applied per attachment. Placeholders: %attachmentLabel% (Image/Video/File), %attachmentFilename%, %attachmentUrl%.")
	var attachmentFormat: String = "[%attachmentLabel%: %attachmentFilename%]"
	var colorUsernamesBasedOnRole: Boolean = true
	@IntegerRange(min = 0x000000, max = 0xFFFFFF)
	var defaultDiscordUsernameColor: Int = 0x99AAB5
	@Comment("Color of the reply indicator line (the ↪ @author part).")
	@IntegerRange(min = 0x000000, max = 0xFFFFFF)
	var replyAuthorColor: Int = 0x888888
	@Comment("Color of the reply content preview text.")
	@IntegerRange(min = 0x000000, max = 0xFFFFFF)
	var replyContentColor: Int = 0xAAAAAA
	@Comment("Color of attachment links.")
	@IntegerRange(min = 0x000000, max = 0xFFFFFF)
	var attachmentLinkColor: Int = 0x55AAFF
}
