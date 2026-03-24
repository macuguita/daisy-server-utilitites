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

package com.macuguta.daisy.daisy_home.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

data class Home(val name: String, val position: Vec3, val dimension: ResourceKey<Level>) {
	companion object {
		private val LEVEL_CODEC = ResourceLocation.CODEC.xmap(
			{ rl -> ResourceKey.create(Registries.DIMENSION, rl) },
			ResourceKey<Level>::location
		)

		val CODEC: Codec<Home> = RecordCodecBuilder.create { i ->
			i.group(
				Codec.STRING.fieldOf("name").forGetter { it.name },
				Vec3.CODEC.fieldOf("block_pos").forGetter { it.position },
				LEVEL_CODEC.optionalFieldOf("dimension", Level.OVERWORLD).forGetter { it.dimension },
			).apply(i, ::Home)
		}
	}
}
