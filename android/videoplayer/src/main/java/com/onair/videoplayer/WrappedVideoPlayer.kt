package com.onair.videoplayer

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ARTWORK_DISPLAY_MODE_FILL

@Composable
fun FragmentWrappedVideoPlayer(
    videoProps: VideoProps,
    startPosition: Long = -1L,
    modifier: Modifier = Modifier,
    resizeMode: Int = VideoResizeKeys.RESIZE_MODE_FIT,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onFullScreenToggle: (Boolean) -> Unit = {},
    onIsPlayingChanged: (Boolean) -> Unit = {},
    onPlayerError: (Int) -> Unit = {},
    onKeyEvent: (NativeKeyEvent) -> Unit = {},
    onRelease: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }


    val playerView = remember {
        PlayerView(context)
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (videoProps.exoPlayer?.isPlaying?.not() == true) {
                    videoProps.exoPlayer?.play()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                videoProps.exoPlayer?.pause()
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                onRelease()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.i(LogTag, "Dispose release lifecycle observer")
        }
    }


    val interactionSource = remember { MutableInteractionSource() }
    var isControllerViible by remember {
        mutableStateOf(false)
    }



    Box(
        modifier = modifier
    ) {
        AndroidView(
            factory = {
                playerView.also {
                    it.requestFocus()
                }
            },
            update = { it ->
                it.player = videoProps.exoPlayer
                it.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                it.setControllerAnimationEnabled(true)
                it.setShowFastForwardButton(true)
                it.setShowRewindButton(true)
                it.setShowNextButton(false)
                it.setShowPreviousButton(false)
                it.showController()
                it.setFullscreenButtonClickListener {isFullScreen ->
                    onFullScreenToggle(isFullScreen)
                }
                it.setResizeMode(resizeMode.asAspectRatioFrameLayoutResizeMode())
                it.setAspectRatioListener { targetAspectRatio, naturalAspectRatio, aspectRatioMismatch ->
                    Log.i(
                        LogTag,
                        "onAspectRatioUpdated: $targetAspectRatio, $naturalAspectRatio, $aspectRatioMismatch"
                    )
                }
                it.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                    isControllerViible = it == View.VISIBLE
                })
                it.artworkDisplayMode = ARTWORK_DISPLAY_MODE_FILL


                if (DeviceType.isTv(context)) {
                    val playPauseButton =
                        it.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_play_pause)
                    val rewindButton =
                        it.findViewById<Button>(androidx.media3.ui.R.id.exo_rew_with_amount)
                    val forwardButton =
                        it.findViewById<Button>(androidx.media3.ui.R.id.exo_ffwd_with_amount)
                    val settingsButton =
                        it.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_settings)

                    listOf(
                        playPauseButton,
                        rewindButton,
                        forwardButton,
                        settingsButton
                    ).setFocusedBackground()
                }


            },
            modifier = Modifier
                .fillMaxSize()
                .focusable(
                    enabled = true,
                    interactionSource = interactionSource
                )
                .onFocusChanged {
                    Log.i(LogTag, "onFocusChanged: ${it.hasFocus}")
                }
                .onKeyEvent {
                    if (isControllerViible)
                        playerView.dispatchKeyEvent(it.nativeKeyEvent)
                    else {
                        playerView.showController()
                        playerView.dispatchKeyEvent(it.nativeKeyEvent)
                    }
                }
                .focusRequester(focusRequester),
        )

    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}