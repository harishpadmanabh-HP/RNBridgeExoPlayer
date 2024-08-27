package com.onair.videoplayer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.media3.common.Player

@Immutable
@Stable
data class PlayerWrapper(
    val exoPlayer: Player
)