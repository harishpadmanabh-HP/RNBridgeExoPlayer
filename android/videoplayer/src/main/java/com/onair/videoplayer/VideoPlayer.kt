package com.onair.videoplayer

import android.app.Activity
import android.app.Dialog
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
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
import com.onair.videoplayer.settingsOverlays.SettingsItem
import com.onair.videoplayer.settingsOverlays.TrackSettingsDialogType
import com.onair.videoplayer.settingsOverlays.TrackSettingsDialogs
import kotlinx.coroutines.launch


val LogTag = "NativeVideoPlayer"

@RequiresApi(Build.VERSION_CODES.O)
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
    onFullScreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
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
    val pipParamsBuilder = remember {
        PictureInPictureParams.Builder()
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
            }

            override fun onPlayerError(error: PlaybackException) {
                onPlayerError(error.errorCode)
                val cause = error.cause
                Log.e(LogTag, "PlaybackException Occurred: ${error.message} caused by $cause")
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
    var isControllerVisible by remember {
        mutableStateOf(false)
    }

    var currentDialogType by remember {
        mutableStateOf(TrackSettingsDialogType.None)
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

    val windowInsetsController =
        reactActivity.window?.decorView?.let {
            WindowCompat.getInsetsController(reactActivity.window!!, it)
        }
    windowInsetsController?.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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
                isFullscreen = !isFullscreen
                onFullScreenChanged(isFullscreen)
            }
            rewindButton.setOnClickListener {
                exoPlayer.seekBack()
            }
            forwardButton.setOnClickListener {
                exoPlayer.seekForward()
            }
            settingsButton.setOnClickListener {
                currentDialogType = TrackSettingsDialogType.Settings
            }
        }
        subtitleButton.setOnClickListener {
            if (currentDialogType == TrackSettingsDialogType.Subtitle)
                currentDialogType = TrackSettingsDialogType.None
            else
                currentDialogType = TrackSettingsDialogType.Subtitle
        }
        audioTrackButton.setOnClickListener {
            if (currentDialogType == TrackSettingsDialogType.Audio)
                currentDialogType = TrackSettingsDialogType.None
            else
                currentDialogType = TrackSettingsDialogType.Audio
        }
        pipButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (reactActivity.isInPictureInPictureMode.not()) {
                    pipParamsBuilder.setAspectRatio(Rational(16, 9))
                    reactActivity.enterPictureInPictureMode(pipParamsBuilder.build())
                }
            }
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
                setFixedTextSize(TEXT_SIZE_TYPE_ABSOLUTE, if (isFullscreen) 18f else 13f);

            }
        }
    }


    //Handle control button visibility for portrait and landscape
    LaunchedEffect(isFullscreen) {
        if (DeviceType.isTv(context)) {
            audioTrackButton.isVisible(true)
            subtitleButton.isVisible(true)
            pipButton.isVisible(true)
        } else {
            if (isFullscreen) {
                settingsButton.isVisible(false)
                rewindButton.isVisible(true)
                forwardButton.isVisible(true)
                audioTrackButton.isVisible(true)
                subtitleButton.isVisible(true)
                pipButton.isVisible(true)
            } else {
                settingsButton.isVisible(true)
                rewindButton.isVisible(false)
                forwardButton.isVisible(false)
                audioTrackButton.isVisible(false)
                subtitleButton.isVisible(false)
                pipButton.isVisible(false)
            }
        }
        if (!DeviceType.isTv(context))
            if (isFullscreen) {
                windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, androidx.media3.ui.R.drawable.exo_styled_controls_fullscreen_exit
                    )
                )
            } else {
                windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        context, R.drawable.custom_controls_full_screen_closed
                    )
                )
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
        }
    }


    val subtitleTracksAvailable = remember {
        mutableStateListOf<Format>()
    }
    var subtitleTrackSelected by remember {
        mutableStateOf<Format?>(null)
    }
    val audioTracksAvailable = remember {
        mutableStateListOf<Format>()
    }
    var audioTrackSelected by remember {
        mutableStateOf<Format?>(null)
    }

    BackHandler(isFullscreen) {
        isFullscreen = !isFullscreen
        onFullScreenChanged(isFullscreen)
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
                    isControllerVisible = it == View.VISIBLE
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
                .zIndex(1f)
                .focusable(
                    enabled = true,
                    interactionSource = interactionSource
                )
                .onKeyEvent {
                    if (isControllerVisible)
                        playerView.dispatchKeyEvent(it.nativeKeyEvent)
                    else {
                        playerView.showController()
                        playerView.dispatchKeyEvent(it.nativeKeyEvent)
                    }
                }
                .focusRequester(focusRequester),
        )
        TrackSettingsDialogs(
            currentDialogType = currentDialogType,
            subtitleTracksAvailable = subtitleTracksAvailable,
            selectedSubtitleTrack = subtitleTrackSelected,
            audioTracksAvailable = audioTracksAvailable,
            selectedAudioTrack = audioTrackSelected,
            onSettingsOptionChosen = { settingsItem ->
                when (settingsItem) {
                    SettingsItem.Audio -> {
                        currentDialogType = TrackSettingsDialogType.Audio
                    }

                    SettingsItem.Subtitle -> {
                        currentDialogType = TrackSettingsDialogType.Subtitle
                    }
                }
            },
            onSubtitleTrackSelected = { format ->
                applySelectedSubtitleTrack(
                    player = exoPlayer,
                    language = format?.language
                )
                subtitleTrackSelected = format
                currentDialogType = TrackSettingsDialogType.None
                subtitleButton.requestFocus()

            },
            onAudioTrackSelected = {format->
                applySelectedAudioTrack(
                    player = exoPlayer,
                    language = format?.language,
                    mimeType = format?.sampleMimeType
                )
                audioTrackSelected=format
                currentDialogType = TrackSettingsDialogType.None
                audioTrackButton.requestFocus()
            },
            onDismissAllDialogs = {
                currentDialogType = TrackSettingsDialogType.None
                playPauseButton.requestFocus()

            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
        )

    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(isplayerReady) {
        launch {
            subtitleTracksAvailable.clear()
            subtitleTracksAvailable.addAll(getTrackOfType(exoPlayer, context, C.TRACK_TYPE_TEXT))
        }
        launch {
            audioTracksAvailable.clear()
            audioTracksAvailable.addAll(getTrackOfType(exoPlayer, context, C.TRACK_TYPE_AUDIO))
        }
    }

}















