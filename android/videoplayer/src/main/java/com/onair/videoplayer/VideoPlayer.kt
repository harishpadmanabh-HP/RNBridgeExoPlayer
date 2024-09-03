package com.onair.videoplayer

import android.app.Activity
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
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
import androidx.media3.ui.CaptionStyleCompat
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
    var caption by rememberSaveable {
        mutableStateOf("")
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

    var isplayerReady by remember {
        mutableStateOf(false)
    }


    val playerView = remember {
        PlayerView(context)
    }

    val exoPlayerListener = remember {
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(isPlaying)
                //Log.d(LogTag, "onIsPlayingChanged: $isPlaying")
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error.errorCode)
                val cause = error.cause
                // Log.e(LogTag, "PlaybackException Occurred: ${error.message} caused by $cause")
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                //  Log.d(LogTag, "onTracksChanged: ${tracks.groups}")
            }

            override fun onCues(cueGroup: CueGroup) {
                super.onCues(cueGroup)
                //Log.d(LogTag, "SUBTITLE $cueGroup")
                //caption = (cueGroup.cues.firstOrNull()?.text ?: "").toString()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    // ExoPlayer is ready; you can access track information now
                    isplayerReady = true

                }
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
        //  Log.i(LogTag, "Player released")
    }


    val interactionSource = remember { MutableInteractionSource() }
    var isControllerViible by remember {
        mutableStateOf(false)
    }

    val fullScreenButton = remember {
        playerView.findViewById<ImageView>(R.id.exo_fullscreen_img)
    }
    val settingsButton = remember {
        playerView.findViewById<ImageView>(R.id.exo_settings_custom)
    }
    val subtitleButton = remember {
        playerView.findViewById<ImageView>(R.id.exo_subtitle_custom)
    }
    val audioTrackButton = remember {
        playerView.findViewById<ImageView>(R.id.exo_audio_custom)
    }
    val titleView = remember {
        playerView.findViewById<TextView>(R.id.exo_title)
    }
    val artistView = remember {
        playerView.findViewById<TextView>(R.id.exo_artist)
    }
    val liveIndicatorLayout = remember {
        playerView.findViewById<LinearLayout>(R.id.live_indicator)
    }
    val playPauseButton = remember {
        playerView.findViewById<ImageButton>(R.id.exo_play_pause)
    }
    val pipButton = remember {
        playerView.findViewById<ImageButton>(R.id.exo_pip_custom)
    }
    val rewindButton = remember {
        playerView.findViewById<Button>(R.id.exo_rew_custom)
    }
    val forwardButton = remember {
        playerView.findViewById<Button>(R.id.exo_ffwd_custom)
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
                    //Log.i(LogTag, "windowInsetsController = $windowInsetsController")
                    windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())

                    ViewCompat.setOnApplyWindowInsetsListener(window!!.decorView) { v, windowInsets ->
                        // Log.i(LogTag, "setOnApplyWindowInsetsListener = $windowInsets")
                        if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
                        ) {
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

    val setLiveIndicators = {
        liveIndicatorLayout.visibility = if (isLive) View.VISIBLE else View.GONE
    }


    val configureControlButtons = {
        if (!DeviceType.isTv(context)) {
            fullScreenButton.setOnClickListener {
                if (isFullscreen) closeFullScreenDialog()
                else openFullScreenDialog()
            }
            rewindButton.setOnClickListener {
                exoPlayer.seekBack()
            }
            forwardButton.setOnClickListener {
                exoPlayer.seekForward()
            }

        }

        if (!DeviceType.isTv(context))
            settingsButton.setOnClickListener {

            }

        subtitleButton.setOnClickListener {

        }
        audioTrackButton.setOnClickListener {

        }
        pipButton.setOnClickListener {

        }


    }

    val configureDefaultSubtitleView = {
        playerView.subtitleView?.let { subtitleView ->
            subtitleView.apply {
                val style = CaptionStyleCompat(
                    Color.White.toArgb(),
                    Color.Transparent.toArgb(),
                    Color.Transparent.toArgb(),
                    CaptionStyleCompat.EDGE_TYPE_RAISED,
                    Color.Transparent.toArgb(),
                    ResourcesCompat.getFont(reactActivity, R.font.dm_sans_light)
                )
                //get the subtitleView reference from the StyledPlayerView
                setApplyEmbeddedFontSizes(false)
                setApplyEmbeddedStyles(false)
                setBottomPaddingFraction(2F) // padding from the bottom
                setStyle(style)
                setFixedTextSize(TEXT_SIZE_TYPE_ABSOLUTE, if (isFullscreen) 20f else 13f);

            }
        }
    }



    LaunchedEffect(configuration.orientation) {
        orientation = configuration.orientation
        //Log.i(LogTag, "Orientation changed: $orientation isFullscreen: $isFullscreen")
        if (orientation == ORIENTATION_LANDSCAPE && !isFullscreen && exoPlayer.isPlaying) {
            openFullScreenDialog()
        }
    }

    //Handle control button visibility for portrait and landscape
    LaunchedEffect(isFullscreen) {
        if(DeviceType.isTv(context)){
            audioTrackButton.isVisible(true)
            subtitleButton.isVisible(true)
            pipButton.isVisible(true)
        }else{
            if(isFullscreen){
                settingsButton.isVisible(false)
                rewindButton.isVisible(true)
                forwardButton.isVisible(true)
                audioTrackButton.isVisible(true)
                subtitleButton.isVisible(true)
                pipButton.isVisible(true)
            }else{
                settingsButton.isVisible(true)
                rewindButton.isVisible(false)
                forwardButton.isVisible(false)
                audioTrackButton.isVisible(false)
                subtitleButton.isVisible(false)
                pipButton.isVisible(false)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                configureControlButtons()
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
            // Log.i(LogTag, "Dispose release lifecycle observer")
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
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
                // it.setResizeMode(resizeMode.asAspectRatioFrameLayoutResizeMode())
                it.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
                it.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                    isControllerViible = it == View.VISIBLE
                })
                it.artworkDisplayMode = ARTWORK_DISPLAY_MODE_FILL
                it.setShowSubtitleButton(true)

                val tvRewind = it.findViewById<Button>(R.id.exo_rew_with_amount)
                val tvForward = it.findViewById<Button>(R.id.exo_ffwd_with_amount)

                if (DeviceType.isTv(context)) {
                    listOf(
                        playPauseButton,
                        tvRewind,
                        tvForward,
                        subtitleButton,
                        pipButton,
                        audioTrackButton
                    ).setFocusedBackground()
                }

                configureDefaultSubtitleView()

            },
            modifier = Modifier
                //  .fillMaxSize()
                .focusable(
                    enabled = true,
                    interactionSource = interactionSource
                )
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

        Text(
            text = caption,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = Color.White,
            fontFamily = FontFamily(listOf(Font(R.font.dm_sans_light))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .zIndex(3f)
        )


    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(isplayerReady) {
        getTrackOfType(exoPlayer, context, C.TRACK_TYPE_AUDIO)
        getTrackOfType(exoPlayer, context, C.TRACK_TYPE_TEXT)
        getTrackOfType(exoPlayer, context, C.TRACK_TYPE_VIDEO)
        applySelectedSubtitleTrack(exoPlayer, "en") // For English subtitles
    }
}




