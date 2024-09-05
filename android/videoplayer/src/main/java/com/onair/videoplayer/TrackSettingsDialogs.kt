package com.onair.videoplayer

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

@Composable
fun TrackSettingsDialogs(
    currentDialogType: TrackSettingsDialogType,
    subtitleTracksAvailable: List<Format>,
    selectedSubtitleTrack: Format?,
    modifier: Modifier = Modifier,
    onSettingsOptionChosen: (SettingsItem) -> Unit = {},
    onSubtitleTrackSelected: (Format?) -> Unit = {}
) {


    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f),
        contentAlignment = Alignment.BottomEnd
    ) {
        AnimatedContent(
            targetState = currentDialogType,
            label = "Video Options",
            modifier = Modifier
                .background(Color.Transparent)
                .padding(
                    end = if (currentDialogType == TrackSettingsDialogType.Settings) 60.dp else 0.dp,
                    bottom = 0.dp
                )
        ) { type ->
            when (type) {
                TrackSettingsDialogType.None -> {}
                TrackSettingsDialogType.Audio -> {
                }

                TrackSettingsDialogType.Subtitle -> {
                    SubtitleTracksDialog(
                        subtitleTracksAvailable = subtitleTracksAvailable,
                        selectedTrack = selectedSubtitleTrack,
                        onSubtitleTrackSelected = onSubtitleTrackSelected
                    )
                }

                TrackSettingsDialogType.Settings -> {
                    SettingsDialog(onSettingsChosen = onSettingsOptionChosen)
                }
            }
        }
    }

}

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    onSettingsChosen: (SettingsItem) -> Unit
) {
    Column(
        modifier = modifier
            .wrapContentSize()
            .background(colorResource(id = R.color.black_dialog_bg))
    ) {
        SettingsItemRow(
            item = SettingsItem.Subtitle,
            modifier = Modifier.padding(12.dp),
            onClicked = onSettingsChosen
        )
        SettingsItemRow(
            item = SettingsItem.Audio,
            modifier = Modifier.padding(12.dp),
            onClicked = onSettingsChosen
        )
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
    subtitleTracksAvailable: List<Format>,
    selectedTrack: Format?,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit
) {
    val trackNames = remember {
        mutableStateListOf<String>()
    }
    LaunchedEffect(subtitleTracksAvailable) {
        subtitleTracksAvailable.forEach {
            trackNames.add(it.language ?: UNKNOWN_LANGUAGE)
        }
    }

    Column(
        modifier = modifier
            .background(colorResource(id = R.color.black_dialog_bg))
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
    val context = LocalContext.current
    Row(
        modifier = modifier
            .wrapContentWidth()
            .height(45.dp)
            .padding(12.dp)
            .clickable {
                if (!isSelected)
                    onSubtitleTrackSelected(track)
            }
            .focusable(true),
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
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun SubtitleItemOff(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit,
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .height(45.dp)
            .padding(12.dp)
            .clickable {
                if (!isSelected)
                    onSubtitleTrackSelected(null)
            }
            .focusable(true),
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
            color = colorResource(id = R.color.white),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}