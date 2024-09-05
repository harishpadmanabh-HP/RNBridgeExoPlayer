package com.onair.videoplayer

import android.util.Log
import android.view.KeyEvent.KEYCODE_BACK
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTrackNameProvider

enum class TrackSettingsDialogType {
    None, Audio, Subtitle, Settings
}

enum class SettingsItem(
    val title: String,
    val icon: Int
) {
    Audio("Audio", R.drawable.custom_controls_audio),
    Subtitle("Subtitles", R.drawable.custom_controls_caption)
}

const val UNKNOWN_LANGUAGE = "Unknown Language"

@kotlin.OptIn(ExperimentalAnimationApi::class)
@Composable
fun TrackSettingsDialogs(
    currentDialogType: TrackSettingsDialogType,
    subtitleTracksAvailable: List<Format>,
    selectedSubtitleTrack: Format?,
    modifier: Modifier = Modifier,
    onSettingsOptionChosen: (SettingsItem) -> Unit = {},
    onSubtitleTrackSelected: (Format?) -> Unit = {},
    onDismissAllDialogs: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f)
            .then(
                if (currentDialogType != TrackSettingsDialogType.None)
                    Modifier.clickable {
                        onDismissAllDialogs()
                    }
                else
                    Modifier
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        AnimatedContent(
            targetState = currentDialogType,
            label = "Video Options",
            modifier = Modifier
                .background(Color.Transparent)
        ) { type ->
            when (type) {
                TrackSettingsDialogType.None -> {}
                TrackSettingsDialogType.Audio -> {
                }

                TrackSettingsDialogType.Subtitle -> {
                    SubtitleTracksDialog(
                        subtitleTracksAvailable = subtitleTracksAvailable,
                        selectedTrack = selectedSubtitleTrack,
                        onSubtitleTrackSelected = onSubtitleTrackSelected,
                        isFromSettings = false,
                        dismissDialog = onDismissAllDialogs
                    )
                }

                TrackSettingsDialogType.Settings -> {
                    SettingsDialog(
                        onSettingsChosen = onSettingsOptionChosen,
                        subtitleTracksAvailable = subtitleTracksAvailable,
                        selectedTrack = selectedSubtitleTrack,
                        onSubtitleTrackSelected = onSubtitleTrackSelected
                    )
                }
            }
        }
    }

}

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    onSettingsChosen: (SettingsItem) -> Unit,
    subtitleTracksAvailable: List<Format>,
    selectedTrack: Format?,
    onSubtitleTrackSelected: (Format?) -> Unit
) {
    var chosenSettings by remember {
        mutableStateOf<SettingsItem?>(null)
    }

    BackHandler(chosenSettings != null) {
        chosenSettings = null
    }



    AnimatedContent(
        targetState = chosenSettings,
        label = "Choose Settings",
        modifier = Modifier
            .padding(end = if (chosenSettings == null) 60.dp else 0.dp)
            .background(Color.Transparent)
    ) { item ->
        when (item) {
            SettingsItem.Audio -> {

            }

            SettingsItem.Subtitle -> {
                SubtitleTracksDialog(
                    subtitleTracksAvailable = subtitleTracksAvailable,
                    selectedTrack = selectedTrack,
                    onSubtitleTrackSelected = onSubtitleTrackSelected,
                    isFromSettings = true
                )
            }

            null -> {
                Column(
                    modifier = modifier
                        .wrapContentSize()
                        .background(colorResource(id = R.color.black_dialog_bg))
                ) {
                    SettingsItemRow(
                        item = SettingsItem.Subtitle,
                        modifier = Modifier.padding(12.dp),
                        onClicked = {
                            chosenSettings = it
                        }
                    )
                    SettingsItemRow(
                        item = SettingsItem.Audio,
                        modifier = Modifier.padding(12.dp),
                        onClicked = {
                            chosenSettings = it
                        }
                    )
                }
            }
        }

    }

    Log.i(LogTag, "Settings shown")
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    modifier: Modifier = Modifier,
    onClicked: (SettingsItem) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(.45f)
            .clickable {
                onClicked(item)
            }
            .focusable(true),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(painter = painterResource(id = item.icon), contentDescription = item.title)
        Text(
            text = item.title,
            fontFamily = FontFamily(listOf(Font(R.font.dm_sans_light))),
            fontSize = 14.sp,
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = item.title
        )
    }
}

@Composable
fun SubtitleTracksDialog(
    isFromSettings:Boolean,
    subtitleTracksAvailable: List<Format>,
    selectedTrack: Format?,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit,
    dismissDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val trackNames = remember {
        mutableStateListOf<String>()
    }
    LaunchedEffect(subtitleTracksAvailable) {
        subtitleTracksAvailable.forEach {
            trackNames.add(it.language ?: UNKNOWN_LANGUAGE)
        }
        focusRequester.requestFocus()
    }

    BackHandler(!isFromSettings) {
        dismissDialog()
    }

    Column(
        modifier = modifier
            .then(
                if (DeviceType.isTv(context))
                    Modifier.fillMaxWidth(.2f)
                else
                    Modifier.fillMaxWidth(.4f)
            )
            .focusRequester(focusRequester)
            .focusGroup()
            .background(colorResource(id = R.color.black_dialog_bg))
            .onKeyEvent {
              Log.i(LogTag,"onKeyEvent ${it.nativeKeyEvent}")
                if (it.nativeKeyEvent.keyCode == KEYCODE_BACK){
                    dismissDialog()
                }
                false
            }
    ) {
        SubtitleItemOff(
            isSelected = selectedTrack == null,
            onSubtitleTrackSelected = onSubtitleTrackSelected
        )
        subtitleTracksAvailable.forEach {
            SubtitleTrackItem(
                track = it,
                isSelected = selectedTrack == it,
                onSubtitleTrackSelected = onSubtitleTrackSelected
            )
        }

    }
}

@OptIn(UnstableApi::class)
@Composable
fun SubtitleTrackItem(
    track: Format,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit
) {
    val highlightIndication = remember { MyHighlightIndication() }
    var interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()


    val context = LocalContext.current


    Box(modifier = modifier
        .fillMaxWidth()
        .height(45.dp)
        .clickable(
            interactionSource = interactionSource,
            indication = highlightIndication,
            enabled = true,
            onClick = {
                if (!isSelected)
                    onSubtitleTrackSelected(track)
            }
        )) {
        Row(
            modifier = modifier
                .wrapContentWidth()
                .height(45.dp)
                .padding(12.dp),
        ) {
            if (isSelected)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorResource(id = R.color.red))
                )

            Text(
                text = DefaultTrackNameProvider(context.resources).getTrackName(track),
                fontFamily = FontFamily(
                    listOf(if (isSelected) Font(R.font.dm_sans_bold) else Font(R.font.dm_sans_light))
                ),
                fontSize = 14.sp,
                color = colorResource(id = if (isFocused) R.color.red else R.color.white),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }

}

@Composable
fun SubtitleItemOff(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit,
) {
    val highlightIndication = remember { MyHighlightIndication() }
    var interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Log.i(LogTag, "isFocused OFF $isFocused")

    Box(modifier = modifier
        .fillMaxWidth()
        .height(45.dp)
        .clickable(
            interactionSource = interactionSource,
            indication = highlightIndication,
            enabled = true,
            onClick = {
                if (!isSelected)
                    onSubtitleTrackSelected(null)
            }
        )) {
        Row(
            modifier = modifier
                .wrapContentWidth()
                .height(45.dp)
                .padding(12.dp),
        ) {
            if (isSelected)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorResource(id = R.color.red))
                )

            Text(
                text = "Off",
                fontFamily = FontFamily(
                    listOf(if (isSelected) Font(R.font.dm_sans_bold) else Font(R.font.dm_sans_light))
                ),
                fontSize = 14.sp,
                color = colorResource(id = if (isFocused) R.color.red else R.color.white),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }


}

private class MyHighlightIndicationInstance(isEnabledState: State<Boolean>) :
    IndicationInstance {
    private val isEnabled by isEnabledState
    override fun ContentDrawScope.drawIndication() {
        if (isEnabled) {
            drawRoundRect(
                size = size,
                color = Color.White,
                cornerRadius = CornerRadius(x = 24.dp.toPx(), y = 24.dp.toPx()),
                alpha = 1f
            )
        }
        drawContent()

    }
}

class MyHighlightIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource):
            IndicationInstance {
        val isFocusedState = interactionSource.collectIsFocusedAsState()
        return remember(interactionSource) {
            MyHighlightIndicationInstance(isEnabledState = isFocusedState)
        }
    }
}