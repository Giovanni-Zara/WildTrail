package com.wildtrail.app.util

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class AudioPlayerController {
    private var player: MediaPlayer? = null
    var playingPath: String? by mutableStateOf(null)
        private set

    fun toggle(path: String) {
        if (playingPath == path && player?.isPlaying == true) {
            player?.pause()
            playingPath = null
            return
        }
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener {
                this@AudioPlayerController.stop()
            }
            prepare()
            start()
        }
        playingPath = path
    }

    fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        playingPath = null
    }
}

@Composable
fun rememberAudioPlayerController(): AudioPlayerController {
    val controller = remember { AudioPlayerController() }
    DisposableEffect(controller) {
        onDispose { controller.stop() }
    }
    return controller
}
