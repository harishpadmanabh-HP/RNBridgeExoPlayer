package com.onair.videoplayer.settingsOverlays

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.Format
import com.onair.videoplayer.R

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

@Composable
fun TrackSettingsDialogs(
    currentDialogType: TrackSettingsDialogType,
    subtitleTracksAvailable: List<Format>,
    selectedSubtitleTrack: Format?,
    modifier: Modifier = Modifier,
    onSettingsOptionChosen: (SettingsItem) -> Unit = {},
    onSubtitleTrackSelected: (Format?) -> Unit = {},
    audioTracksAvailable: List<Format>,
    onAudioTrackSelected: (Format?) -> Unit,
    selectedAudioTrack: Format?,
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
                    AudioTracksDialog(
                        isFromSettings = false,
                        audioTracksAvailable = audioTracksAvailable ,
                        selectedTrack = selectedAudioTrack ,
                        onAudioTrackSelected = onAudioTrackSelected
                    )
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
                        selectedSubtitleTrack = selectedSubtitleTrack,
                        onSubtitleTrackSelected = onSubtitleTrackSelected,
                        audioTracksAvailable = audioTracksAvailable,
                        onAudioTrackSelected = onAudioTrackSelected,
                        selectedAudioTrack = selectedAudioTrack
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
    selectedSubtitleTrack: Format?,
    onSubtitleTrackSelected: (Format?) -> Unit,
    audioTracksAvailable: List<Format>,
    onAudioTrackSelected: (Format?) -> Unit,
    selectedAudioTrack: Format?
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
                AudioTracksDialog(
                    audioTracksAvailable = audioTracksAvailable,
                    selectedTrack = selectedAudioTrack,
                    onAudioTrackSelected = onAudioTrackSelected,
                    isFromSettings = true
                )
            }

            SettingsItem.Subtitle -> {
                SubtitleTracksDialog(
                    subtitleTracksAvailable = subtitleTracksAvailable,
                    selectedTrack = selectedSubtitleTrack,
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



