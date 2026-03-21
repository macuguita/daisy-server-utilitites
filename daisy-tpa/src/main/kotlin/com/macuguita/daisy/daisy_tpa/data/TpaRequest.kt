package com.macuguita.daisy.daisy_tpa.data

import java.util.UUID

data class TpaRequest(
    val requester: UUID,
    val target: UUID,
    val type: TpaType,
    val timestamp: Long
)
