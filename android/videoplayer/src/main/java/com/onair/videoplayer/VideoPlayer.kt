package com.onair.videoplayer

import android.app.Activity
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue.TEXT_SIZE_TYPE_ABSOLUTE
import androidx.media3.common.text.CueGroup
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
    reactActivity: Activity,
    parentFrame: FrameLayout,
    modifier: Modifier = Modifier,
    title: String = "Venus Tour",
    description: String = "",
    artistName: String = "Zara Larrason",
    artworkUrl: String = "",
    playWhenReady: Boolean = true,
    seekBackSeconds: Int = 5,
    seekForwardSeconds: Int = 10,
    isInListItem: Boolean = false,
    isLive: Boolean = false,
    resizeMode: Int = VideoResizeKeys.RESIZE_MODE_FIT,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onIsPlayingChanged: (Boolean) -> Unit = {},
    onPlayerError: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = reactActivity
    val focusRequester = remember { FocusRequester() }
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableStateOf(configuration.orientation) }

    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var fullScreenDialog by remember {
        mutableStateOf<Dialog?>(null)
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


    val playerView = remember {
        PlayerView(context)
    }
    val subTitleView = remember {
        playerView.findViewById<TextView>(R.id.customSubtitle)
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
            override fun onCues(cueGroup: CueGroup) {
                super.onCues(cueGroup)
                Log.d(LogTag, "SUBTITLE $cueGroup")
                subTitleView.text = cueGroup.cues.firstOrNull()?.text ?: ""
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
                        DefaultMediaSourceFactory(context).setLiveTargetOffsetMs(5000)
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


    val interactionSource = remember { MutableInteractionSource() }
    var isControllerViible by remember {
        mutableStateOf(false)
    }

    val fullScreenButton = remember {
        playerView.findViewById<ImageView>(R.id.exo_fullscreen_img)
    }
    val titleView = remember {
        playerView.findViewById<TextView>(R.id.exo_title)
    }
    val artistView = remember {
        playerView.findViewById<TextView>(R.id.exo_artist)
    }
    val liveIndicatorLayout=remember {
        playerView.findViewById<LinearLayout>(R.id.live_indicator)
    }

    val closeFullScreenDialog = {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        (playerView.parent as ViewGroup).removeView(playerView)
        parentFrame.addView(playerView)
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                context, androidx.media3.ui.R.drawable.exo_styled_controls_fullscreen_enter
            )
        )
        isFullscreen = false
        fullScreenDialog?.dismiss()
    }

    val initFullScreenDialog = {
        fullScreenDialog =
            object :
                Dialog(reactActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
                @Deprecated("Deprecated in Java")
                override fun onBackPressed() {
                    if (isFullscreen) closeFullScreenDialog()
                    super.onBackPressed()
                }

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    val windowInsetsController =
                        window?.decorView?.let {
                            WindowCompat.getInsetsController(window!!, it)
                        }
                    windowInsetsController?.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    Log.i(LogTag,"windowInsetsController = $windowInsetsController")
                    windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())

                    ViewCompat.setOnApplyWindowInsetsListener(window!!.decorView) { v, windowInsets ->
                        Log.i(LogTag,"setOnApplyWindowInsetsListener = $windowInsets")
                        if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())){
                            windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                        }


                        ViewCompat.onApplyWindowInsets(v, windowInsets)
                    }
                }
            }
    }

    val openFullScreenDialog = {
        initFullScreenDialog()
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                androidx.media3.ui.R.drawable.exo_styled_controls_fullscreen_exit
            )
        )
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        (playerView.parent as ViewGroup).removeView(playerView)
        fullScreenDialog?.addContentView(
            playerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        isFullscreen = true
        fullScreenDialog?.show()
    }

    val setDetails = {
        titleView.text = title
        artistView.text = artistName
    }

    val setLiveIndicators={
        liveIndicatorLayout.visibility=if(isLive) View.VISIBLE else View.GONE
    }



    val initFullScreenButton = {
        fullScreenButton.setOnClickListener {
            if (isFullscreen) closeFullScreenDialog()
            else openFullScreenDialog()
        }
    }

    val hideDefaultSubtitleView ={
        playerView.subtitleView?.let{subtitleView ->
            subtitleView.apply {
                setFixedTextSize(TEXT_SIZE_TYPE_ABSOLUTE, 0F);
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                initFullScreenButton()
                setDetails()
                setLiveIndicators()
            } else if (event == Lifecycle.Event.ON_START) {
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

    LaunchedEffect(configuration.orientation) {
        orientation = configuration.orientation
        Log.i(LogTag, "Orientation changed: $orientation isFullscreen: $isFullscreen")
        if (orientation == ORIENTATION_LANDSCAPE && !isFullscreen && exoPlayer.isPlaying) {
            openFullScreenDialog()
        }
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
                it.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                it.setControllerAnimationEnabled(false)
                it.setShowFastForwardButton(true)
                it.setShowRewindButton(true)
                it.setShowNextButton(false)
                it.setShowPreviousButton(false)
                it.showController()
                it.setResizeMode(resizeMode.asAspectRatioFrameLayoutResizeMode())
                it.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                    isControllerViible = it == View.VISIBLE
                })
                it.artworkDisplayMode = ARTWORK_DISPLAY_MODE_FILL
                it.setShowSubtitleButton(true)


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

                hideDefaultSubtitleView()

            },
            modifier = Modifier
                //  .fillMaxSize()
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





