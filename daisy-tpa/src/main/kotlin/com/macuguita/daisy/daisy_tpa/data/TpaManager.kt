package com.macuguita.daisy.daisy_tpa.data

import java.util.UUID
import kotlin.collections.maxByOrNull

object TpaManager {
    private val requests = mutableMapOf<UUID, MutableList<TpaRequest>>()

    fun sendRequest(request: TpaRequest, expiryMs: Int?): Boolean {
        val list = requests.getOrPut(request.target) { mutableListOf() }

        if (list.any { it.requester == request.requester && !isExpired(it, expiryMs) }) {
            return false
        }

        list.add(request)
        return true
    }

    fun popMostRecent(target: UUID, expiryMs: Int?): TpaRequest? {
        cleanup(target, expiryMs)
        return requests[target]?.maxByOrNull { it.timestamp }
            ?.also { requests[target]?.remove(it) }
    }

    fun popFromRequester(target: UUID, requester: UUID, expiryMs: Int?): TpaRequest? {
        cleanup(target, expiryMs)
        val list = requests[target] ?: return null
        val req = list.find { it.requester == requester }
        req?.let { list.remove(it) }
        return req
    }

    private fun cleanup(target: UUID, expiryMs: Int?) {
        requests[target]?.removeIf { isExpired(it, expiryMs) }
        if (requests[target]?.isEmpty() == true) {
            requests.remove(target)
        }
    }

    private fun isExpired(req: TpaRequest, expiryMs: Int?): Boolean {
        return System.currentTimeMillis() - req.timestamp > (expiryMs ?: 60_000)
    }
}