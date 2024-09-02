package com.onair.videoplayer

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.DefaultTrackNameProvider


@OptIn(UnstableApi::class)
fun getSubtitleTracks(player: ExoPlayer, context: Context): List<Pair<String, String>> {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return emptyList()
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return emptyList()

    val subtitles = mutableListOf<Pair<String, String>>()

    // Iterate over each renderer, find text tracks (subtitle tracks)
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
            val trackGroups: TrackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
            for (groupIndex in 0 until trackGroups.length) {
                val trackGroup = trackGroups[groupIndex]
                for (trackIndex in 0 until trackGroup.length) {
                    // Get the format details for the subtitle track
                    val format = trackGroup.getFormat(trackIndex)
                    val language = format.language ?: "Unknown Language"
                    val mimeType = format.sampleMimeType ?: "Unknown Format"
                    subtitles.add(Pair(language, mimeType))
                    val name = DefaultTrackNameProvider(context.resources)
                    Log.i(LogTag, "Subtitles: ${name.getTrackName(format)}")
                }
            }
        }
    }
    Log.i(LogTag, "Subtitles: $subtitles")
    return subtitles
}
