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
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.phys.Vec3
import com.macuguita.daisy.daisy_warp.DaisyWarp.id
import com.macuguita.daisy.daisy_warp.data.AddWarpResult
import com.macuguita.daisy.daisy_warp.data.RemoveWarpResult
import com.macuguita.daisy.daisy_warp.data.Warp
import com.macuguita.daisy.daisy_warp.shim.Shim

class DaisyWarps() : SavedData() {

	var _warps: MutableList<Warp> = mutableListOf()

	init {
		this.setDirty()
	}

	private constructor(warps: MutableList<Warp>) : this() {
		this._warps = mutableListOf()

		warps.forEach { _warps.add(it) }
	}

	fun all(): List<Warp> = Collections.unmodifiableList(ArrayList(_warps))

	fun find(name: String): Warp? = _warps.find { it.name == name.lowercase() }

	fun add(name: String, pos: Vec3, dimension: ResourceKey<Level>): AddWarpResult {
		val n = name.lowercase()
		if (_warps.any { it.name == n }) return AddWarpResult.DUPLICATE_NAME
		_warps.add(Warp(n, pos, dimension))
		setDirty()
		return AddWarpResult.SUCCESS
	}

	fun remove(name: String): RemoveWarpResult {
		val n = name.lowercase()
		if (_warps.none { it.name == n }) return RemoveWarpResult.NOT_FOUND
		_warps.removeIf { it.name == n }
		setDirty()
		return RemoveWarpResult.SUCCESS
	}

	companion object {
		val DATA_NAME = "warps".id
		val CODEC: Codec<DaisyWarps> = RecordCodecBuilder.create { i ->
			i.group(
				Warp.CODEC.listOf().fieldOf("warps").forGetter { w -> w._warps }
			).apply(i, ::DaisyWarps)
		}
		val TYPE = Shim.makeFactory(
			DATA_NAME,
			::DaisyWarps,
			CODEC,
			null
		)

		fun get(server: MinecraftServer): DaisyWarps =
			server.dataStorage.computeIfAbsent(TYPE);
	}
}
