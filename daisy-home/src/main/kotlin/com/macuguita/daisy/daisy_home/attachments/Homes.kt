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

@file:Suppress("UnstableApiUsage")

package com.macuguita.daisy.daisy_home.attachments

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.macuguita.daisy.daisy_home.DaisyHome
import com.macuguita.daisy.daisy_home.DaisyHome.id
import com.macuguita.daisy.daisy_home.data.AddHomeResult
import com.macuguita.daisy.daisy_home.data.Home
import com.macuguita.daisy.daisy_home.data.RemoveHomeResult

object Homes {

	val ATTACHMENT: AttachmentType<HomeAttachedData> =
		AttachmentRegistry.create(
			"homes".id
		) { builder ->
			builder
				.initializer { HomeAttachedData(emptyList(), DaisyHome.CONFIG.maxDefaultHomes) }
				.persistent(HomeAttachedData.CODEC)
				.copyOnDeath()
		}

	fun get(target: AttachmentTarget): HomeData = HomeData(target)
}

data class HomeAttachedData(
	val homes: List<Home>,
	val maxHomes: Int = DaisyHome.CONFIG.maxDefaultHomes,
) {
	companion object {
		val CODEC: Codec<HomeAttachedData> =
			RecordCodecBuilder.create { i ->
				i.group(
					Home.CODEC.listOf()
						.optionalFieldOf("homes", emptyList())
						.forGetter { it.homes },
					Codec.INT
						.optionalFieldOf("max_homes", DaisyHome.CONFIG.maxDefaultHomes)
						.forGetter { it.maxHomes }
				).apply(i, ::HomeAttachedData)
			}
	}

	fun addHome(name: String, pos: Vec3, dimension: ResourceKey<Level>): Pair<HomeAttachedData, AddHomeResult> {
		val n = name.lowercase()
		if (homes.size >= maxHomes) return this to AddHomeResult.AT_CAPACITY
		if (homes.any { it.name == n }) return this to AddHomeResult.DUPLICATE_NAME
		return copy(homes = homes + Home(n, pos, dimension)) to AddHomeResult.SUCCESS
	}

	fun removeHome(name: String): Pair<HomeAttachedData, RemoveHomeResult> {
		val n = name.lowercase()
		if (homes.none { it.name == n }) return this to RemoveHomeResult.NOT_FOUND
		return copy(homes = homes.filterNot { it.name == n }) to RemoveHomeResult.SUCCESS
	}
}

data class HomeData(private val target: AttachmentTarget) {

	private fun current(): HomeAttachedData =
		target.getAttachedOrElse(Homes.ATTACHMENT, HomeAttachedData(emptyList(), DaisyHome.CONFIG.maxDefaultHomes))

	val homes: List<Home> get() = current().homes

	var maxHomes: Int
		get() = current().maxHomes
		set(value) {
			target.setAttached(
				Homes.ATTACHMENT,
				current().copy(maxHomes = value)
			)
		}

	fun addHome(name: String, pos: Vec3, dimension: ResourceKey<Level>): AddHomeResult {
		val current = current()
		val (updated, result) = current.addHome(name, pos, dimension)
		if (result == AddHomeResult.SUCCESS) {
			target.setAttached(Homes.ATTACHMENT, updated)
		}
		return result
	}

	fun removeHome(name: String): RemoveHomeResult {
		val current = current()
		val (updated, result) = current.removeHome(name)
		if (result == RemoveHomeResult.SUCCESS) {
			target.setAttached(Homes.ATTACHMENT, updated)
		}
		return result
	}
}
