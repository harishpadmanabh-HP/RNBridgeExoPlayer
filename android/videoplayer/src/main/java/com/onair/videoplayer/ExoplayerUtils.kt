package com.onair.videoplayer

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTrackNameProvider


@OptIn(UnstableApi::class)
fun getTrackOfType(player: ExoPlayer, context: Context, trackType:Int): List<Pair<String, String>> {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return emptyList()
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return emptyList()

    val subtitles = mutableListOf<Pair<String, String>>()

    // Iterate over each renderer, find text tracks (subtitle tracks)
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == trackType) {
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
                    Log.i(LogTag, "Tacks Available: ${name.getTrackName(format)}")
                }
            }
        }
    }
    Log.i(LogTag, "Subtitles: $subtitles")
    return subtitles
}

@OptIn(UnstableApi::class)
fun applySelectedSubtitleTrack(player: ExoPlayer, language: String?) {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return

    val parametersBuilder = trackSelector.buildUponParameters()

    // Disable subtitles if no language is selected
    if (language.isNullOrEmpty()) {
        parametersBuilder.setRendererDisabled(C.TRACK_TYPE_TEXT, true)  // Disable text tracks
    } else {
        // Set the preferred subtitle language
        parametersBuilder.setPreferredTextLanguage(language)
        parametersBuilder.setRendererDisabled(C.TRACK_TYPE_TEXT, false)  // Enable text tracks
    }

    // Apply the updated track selection parameters
    trackSelector.setParameters(parametersBuilder)
}


fun Modifier.onNotVisible(onNotVisible: () -> Unit): Modifier = composed {
    val view = LocalView.current
    var isVisible: Boolean? by remember { mutableStateOf(null) }

    if (isVisible == true) {
        LaunchedEffect(isVisible) {
            onNotVisible()
        }
    }

    onGloballyPositioned { coordinates ->
        isVisible = coordinates.isCompletelyVisible(view)
    }
}

fun LayoutCoordinates.isCompletelyVisible(view: View): Boolean {
    if (!isAttached) return false
    // Window relative bounds of our compose root view that are visible on the screen
    val globalRootRect = android.graphics.Rect()
    if (!view.getGlobalVisibleRect(globalRootRect)) {
        //Log.i(LogTag, "View is not visible at all")
        // we aren't visible at all.
        return false
    }
    val bounds = boundsInWindow()
    // Make sure we are completely in bounds.
    return bounds.top >= globalRootRect.top &&
            bounds.left >= globalRootRect.left &&
            bounds.right <= globalRootRect.right &&
            bounds.bottom <= globalRootRect.bottom
}

fun List<View>.setFocusedBackground() {
    forEach { view ->
        view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                v.setBackgroundResource(R.drawable.focus_indicator)
            else
                v.setBackgroundResource(0)
        }
    }
}

@OptIn(UnstableApi::class)
fun Int.asAspectRatioFrameLayoutResizeMode() = when (this) {
    VideoResizeKeys.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
    VideoResizeKeys.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    VideoResizeKeys.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    VideoResizeKeys.RESIZE_MODE_FIXED_WIDTH -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    VideoResizeKeys.RESIZE_MODE_FIXED_HEIGHT -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    else -> {
        AspectRatioFrameLayout.RESIZE_MODE_FIT
    }
}