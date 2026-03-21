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

package com.macuguita.daisy.daisy_base.config

import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import java.util.UUID

abstract class ModuleConfig(
    private val fileName: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val properties = Properties()
    private val path: Path = FabricLoader.getInstance().configDir.resolve(fileName)

    fun load() {
        Files.createDirectories(path.parent)

        if (Files.exists(path)) {
            Files.newBufferedReader(path).use { properties.load(it) }
        }

        configure()

        Files.newBufferedWriter(path).use { properties.store(it, "Daisy Config - $fileName") }
    }

    // Each subclass defines its own properties here
    protected abstract fun configure()

    protected fun string(key: String, default: String): String {
        if (!properties.containsKey(key)) properties.setProperty(key, default)
        return properties.getProperty(key)
    }

    protected fun int(key: String, default: Int): Int =
        string(key, "$default").toIntOrNull() ?: default.also {
            logger.warn("Couldn't load '$key' in $fileName, using default: $default")
        }

    protected fun boolean(key: String, default: Boolean): Boolean =
        string(key, "$default").toBooleanStrictOrNull() ?: default.also {
            logger.warn("Couldn't load '$key' in $fileName, using default: $default")
        }

    protected fun long(key: String, default: Long): Long =
        string(key, "$default").toLongOrNull() ?: default.also {
            logger.warn("Couldn't load '$key' in $fileName, using default: $default")
        }

    protected fun uuids(key: String, default: Set<UUID> = emptySet()): Set<UUID> =
        string(key, default.joinToString(","))
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull {
                try { UUID.fromString(it) } catch (e: IllegalArgumentException) {
                    logger.warn("Invalid UUID '$it' in $fileName")
                    null
                }
            }
            .toSet()
}