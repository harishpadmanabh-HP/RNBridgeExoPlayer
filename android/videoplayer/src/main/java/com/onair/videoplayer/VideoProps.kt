package com.onair.videoplayer

import androidx.annotation.Keep


@Keep
data class VideoProps(
    val videoUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val artistName: String = "",
    val artworkUrl: String = "",
    val drmLicenseUrl: String = "",
)

@Keep
data class VideoInfo(
    val videoUrl: String,
    val title: String,
    val description: String = "",
    val artistName: String = "",
    val drmConfigs: DrmConfigs? = null,
    val isLive: Boolean,
    val shouldAutoPlay: Boolean
)

@Keep
data class DrmConfigs(
    val drmLicenseUrl: String,
    val hasDrm: Boolean
)
