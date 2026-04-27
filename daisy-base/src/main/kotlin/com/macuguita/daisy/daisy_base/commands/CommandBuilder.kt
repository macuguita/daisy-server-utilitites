package com.macuguita.daisy.daisy_base.commands

import java.util.function.Predicate
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider

class CommandBuilder<S>(
	private val node: ArgumentBuilder<S, *>,
) {

	fun requires(predicate: Predicate<S>) {
		node.requires(predicate)
	}

	fun suggests(provider: SuggestionProvider<S>) {
		if (node is RequiredArgumentBuilder<S, *>) {
			node.suggests(provider)
		} else {
			error("Suggestions can only be applied to argument nodes")
		}
	}

	fun executes(block: CommandContext<S>.() -> CommandResult) {
		node.executes { ctx -> block(ctx).value }
	}

	fun literal(name: String, block: CommandBuilder<S>.() -> Unit) {
		val literalNode = LiteralArgumentBuilder.literal<S>(name)
		val builder = CommandBuilder<S>(literalNode)
		builder.block()
		node.then(literalNode)
	}

	fun <T> argument(
		name: String,
		type: ArgumentType<T>,
		block: CommandBuilder<S>.() -> Unit,
	) {
		val argNode = RequiredArgumentBuilder.argument<S, T>(name, type)
		val builder = CommandBuilder<S>(argNode)
		builder.block()
		node.then(argNode)
	}
}
