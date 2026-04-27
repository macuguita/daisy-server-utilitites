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

package com.macuguita.daisy.daisy_base.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.NameAndId

fun CommandContext<CommandSourceStack>.string(name: String): String =
	StringArgumentType.getString(this, name)

fun CommandContext<CommandSourceStack>.playerArg(name: String): ServerPlayer =
	EntityArgument.getPlayer(this, name)

fun CommandContext<CommandSourceStack>.int(name: String): Int =
	IntegerArgumentType.getInteger(this, name)

fun CommandContext<CommandSourceStack>.gameProfile(name: String): Collection<NameAndId> =
	GameProfileArgument.getGameProfiles(this, name)

fun CommandDispatcher<CommandSourceStack>.command(
	name: String,
	block: CommandBuilder<CommandSourceStack>.() -> Unit,
) {
	val root = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
	val builder = CommandBuilder(root)
	builder.block()
	this.register(root)
}
