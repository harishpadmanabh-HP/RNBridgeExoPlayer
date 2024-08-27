package com.onair.videoplayer

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.OptIn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ARTWORK_DISPLAY_MODE_FILL

val LogTag = "NativeVideoPlayer"

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    startPosition: Long = -1L,
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    artistName: String = "",
    artworkUrl: String = "",
    playWhenReady: Boolean = true,
    seekBackSeconds: Int = 5,
    seekForwardSeconds: Int = 10,
    isInListItem: Boolean = false,
    isLive: Boolean = false,
    resizeMode: Int = VideoResizeKeys.RESIZE_MODE_FIT,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onFullScreenToggle: (Boolean, Long) -> Unit = { _, _ -> },
    onIsPlayingChanged: (Boolean) -> Unit = {},
    onPlayerError: (Int) -> Unit = {},
    onKeyEvent: (NativeKeyEvent) -> Unit = {},
    onProgressChange: (Long) -> Unit = {},
    onPlayerAttached: (ExoPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableStateOf(configuration.orientation) }


    LaunchedEffect(configuration.orientation) {
        orientation = configuration.orientation
        Log.i(LogTag, "Orientation changed: $orientation")
    }


    val mediaItem = remember {
        mutableStateOf(
            VideoProps(
                videoUrl = videoUrl,
                description = description,
                artistName = artistName,
                artworkUrl = artworkUrl
            ).toMediaItem(isLive)
        )
    }

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
    }

    val exoPlayerListener = remember {
        object : Player.Listener {


            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(isPlaying)
                Log.d(LogTag, "onIsPlayingChanged: $isPlaying")
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error.errorCode)
                val cause = error.cause
                Log.e(LogTag, "PlaybackException Occurred: ${error.message} caused by $cause")
            }

            override fun onMetadata(metadata: Metadata) {
                super.onMetadata(metadata)
                Log.d(LogTag, "onMetadata: ${metadata}")
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                Log.d(LogTag, "onMediaMetadataChanged: ${mediaMetadata}")
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                Log.d(LogTag, "onTracksChanged: ${tracks.groups}")
            }

        }

    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(seekBackSeconds * 1000L)
            .setSeekForwardIncrementMs(seekForwardSeconds * 1000L)
            .setTrackSelector(trackSelector)
            .also {
                if (isLive) {
                    it.setMediaSourceFactory(
                        DefaultMediaSourceFactory(context).setLiveTargetOffsetMs(
                            5000
                        )
                    )
                    it.setLivePlaybackSpeedControl(
                        DefaultLivePlaybackSpeedControl.Builder().setFallbackMaxPlaybackSpeed(1.04f)
                            .build()
                    )
                }
            }
            .build().apply {
                this.setMediaItem(mediaItem.value)
                this.prepare()
                this.playWhenReady = playWhenReady
                this.addListener(exoPlayerListener)

            }
    }

    val releasePlayer = {
        exoPlayer.removeListener(exoPlayerListener)
        exoPlayer.release()
        Log.i(LogTag, "Player released")
    }

    val playerView = remember {
        PlayerView(context)
    }

    val openFullScreen = {
        val intent = Intent(context, FullScreenVideoPlayerActivity::class.java).also {
            it.putExtra(FullScreenVideoPlayerActivity.VIDEO_URL, videoUrl)
            it.putExtra(
                FullScreenVideoPlayerActivity.VIDEO_START_POSITION,
                exoPlayer.currentPosition
            )
        }
        context.startActivity(intent)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (exoPlayer.isPlaying.not()) {
                    exoPlayer.play()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                releasePlayer()
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

    LaunchedEffect(startPosition) {
        Log.i("FullScreenVideoActivity", "start position changed $startPosition")
        if (startPosition > -1L)
            exoPlayer.seekTo(startPosition)
    }

    Box(
        modifier = modifier.onNotVisible {
            Log.i(LogTag, "onNotVisible on screen called")
            if (isInListItem) {
                releasePlayer()
            }
        }
    ) {
        AndroidView(
            factory = {
                playerView.also {
                    it.requestFocus()
                }
            },
            update = {
                it.player = exoPlayer
                exoPlayer?.let {
                    onPlayerAttached(exoPlayer )
                }
                it.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                it.setControllerAnimationEnabled(true)
                it.setShowFastForwardButton(true)
                it.setShowRewindButton(true)
                it.setShowNextButton(false)
                it.setShowPreviousButton(false)
                it.showController()
                it.setFullscreenButtonClickListener { fullScreen ->
                    onFullScreenToggle(fullScreen, exoPlayer.currentPosition)
                    Log.i(LogTag, "FullScreenChanged: $fullScreen")
                }
                it.setResizeMode(resizeMode.asAspectRatioFrameLayoutResizeMode())
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
        Log.i(LogTag, "View is not visible at all")
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



