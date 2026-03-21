package com.macuguita.daisy.daisy_base.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack

interface CommandRegistrator {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
}