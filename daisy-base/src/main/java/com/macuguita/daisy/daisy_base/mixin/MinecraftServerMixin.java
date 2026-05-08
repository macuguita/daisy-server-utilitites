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

package com.macuguita.daisy.daisy_base.mixin;

import com.macuguita.daisy.daisy_base.event.ServerStartedEvent;

import com.macuguita.daisy.daisy_base.event.ServerStoppedEvent;
import com.macuguita.daisy.daisy_base.event.ServerStoppingEvent;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0), method = "runServer")
	private void daisy$afterSetupServer(CallbackInfo info) {
		ServerStartedEvent.getEVENT().invoker().serverStarted((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("HEAD"), method = "stopServer")
	private void daisy$beforeShutdownServer(CallbackInfo info) {
		ServerStoppingEvent.getEVENT().invoker().serverStarted((MinecraftServer) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "stopServer")
	private void daisy$afterShutdownServer(CallbackInfo info) {
		ServerStoppedEvent.getEVENT().invoker().serverStarted((MinecraftServer) (Object) this);
	}
}
