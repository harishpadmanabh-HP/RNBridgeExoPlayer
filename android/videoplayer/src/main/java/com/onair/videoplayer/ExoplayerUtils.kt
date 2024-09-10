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
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTrackNameProvider

fun View.isVisible(visible: Boolean) {
    if (visible)
        this.visibility = View.VISIBLE
    else
        this.visibility = View.GONE
}

@OptIn(UnstableApi::class)
fun getTrackOfType(player: ExoPlayer, context: Context, trackType: Int): List<Format> {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return emptyList()
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return emptyList()

    val subtitles = mutableListOf<Format>()

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
                    subtitles.add(format)
                    val name = DefaultTrackNameProvider(context.resources)
                    Log.i(LogTag, "Tacks Available: ${name.getTrackName(format)}")
                }
            }
        }
    }
    return subtitles
}

@OptIn(UnstableApi::class)
fun applySelectedSubtitleTrack(player: ExoPlayer, language: String?) {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return

    val parametersBuilder = trackSelector.buildUponParameters()

    // Disable subtitles if no language is selected
    if (language.isNullOrEmpty()) {
        parametersBuilder
            .setRendererDisabled(C.TRACK_TYPE_TEXT, true)  // Disable text tracks
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED)
            .setPreferredTextLanguage(null)

    } else {
        // Set the preferred subtitle language
        parametersBuilder.setPreferredTextLanguage(language)
        parametersBuilder.setRendererDisabled(C.TRACK_TYPE_TEXT, false)  // Enable text tracks
    }
    Log.i(LogTag,"Applied TEXT TRACK $language")

    // Apply the updated track selection parameters
    trackSelector.setParameters(parametersBuilder.build())
}

@OptIn(UnstableApi::class)
fun applySelectedAudioTrack(player: ExoPlayer, language: String?, mimeType: String?) {
    val trackSelector = player.trackSelector as? DefaultTrackSelector ?: return

    val parametersBuilder = trackSelector.buildUponParameters()
    if (language.isNullOrEmpty() && mimeType.isNullOrEmpty()) {
        // Clear all overrides for audio track
        parametersBuilder
            .clearSelectionOverrides(C.TRACK_TYPE_AUDIO)
            .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
    } else {
        // Apply specific audio preferences if provided
        if (!language.isNullOrEmpty()) {
            parametersBuilder
                .setPreferredAudioLanguage(language)
                .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
        }

        // Set or clear audio channel preference
        if (!mimeType.isNullOrEmpty()) {
            parametersBuilder.setPreferredAudioMimeType(mimeType)
        }
    }

    Log.i(LogTag,"Applied AUDIO TRACK $language,$mimeType")

    // Apply the updated track selection parameters
    trackSelector.setParameters(parametersBuilder.build())
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