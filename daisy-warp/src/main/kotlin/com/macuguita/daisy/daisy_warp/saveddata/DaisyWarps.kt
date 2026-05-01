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

package com.macuguita.daisy.daisy_warp.saveddata

import java.util.*
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import com.macuguita.daisy.daisy_warp.data.AddWarpResult
import com.macuguita.daisy.daisy_warp.data.RemoveWarpResult
import com.macuguita.daisy.daisy_warp.data.Warp
import com.macuguita.daisy.daisy_warp.shim.Shim

class DaisyWarps private constructor(
	private val warpsInternal: MutableList<Warp> = mutableListOf(),
) : SavedData() {

	constructor() : this(mutableListOf())

	fun all(): List<Warp> = Collections.unmodifiableList(ArrayList(warpsInternal))

	fun find(name: String): Warp? = warpsInternal.find { it.name == name.lowercase() }

	fun add(name: String, pos: Vec3, dimension: ResourceKey<Level>): AddWarpResult {
		val n = name.lowercase()
		if (warpsInternal.any { it.name == n }) return AddWarpResult.DUPLICATE_NAME
		warpsInternal.add(Warp(n, pos, dimension))
		setDirty()
		return AddWarpResult.SUCCESS
	}

	fun remove(name: String): RemoveWarpResult {
		val n = name.lowercase()
		if (warpsInternal.none { it.name == n }) return RemoveWarpResult.NOT_FOUND
		warpsInternal.removeIf { it.name == n }
		setDirty()
		return RemoveWarpResult.SUCCESS
	}

	override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
		val ops = provider.createSerializationContext(NbtOps.INSTANCE)
		Warp.CODEC.listOf().encodeStart(ops, warpsInternal.toList())
			.resultOrPartial { error -> throw IllegalStateException("Failed to save warps: $error") }
			.ifPresent { tag.put("warps", it) }
		return tag
	}

	companion object {
		const val DATA_NAME = "daisy_warps"

		val FACTORY = Shim.makeFactory(
			::DaisyWarps,
			{ tag, provider -> load(tag, provider) },
			null
		)

		private fun load(tag: CompoundTag, provider: HolderLookup.Provider): DaisyWarps {
			val instance = DaisyWarps()
			if (!tag.contains("warps")) return instance

			val ops = provider.createSerializationContext(NbtOps.INSTANCE)
			Warp.CODEC.listOf().parse(ops, tag.get("warps"))
				.resultOrPartial { error -> throw IllegalStateException("Failed to load warps: $error") }
				.ifPresent { instance.warpsInternal.addAll(it) }

			return instance
		}

		fun get(server: MinecraftServer): DaisyWarps =
			server.overworld().dataStorage.computeIfAbsent(FACTORY, DATA_NAME)
	}
}
