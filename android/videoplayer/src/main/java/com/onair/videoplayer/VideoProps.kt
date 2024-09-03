package com.onair.videoplayer

import androidx.annotation.Keep


@Keep
data class VideoProps(
    val videoUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val artistName: String = "",
    val artworkUrl: String = "",
)