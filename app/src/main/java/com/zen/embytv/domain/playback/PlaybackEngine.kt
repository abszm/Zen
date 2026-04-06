package com.zen.embytv.domain.playback

interface VideoPlayer {
    fun play(mediaUrl: String)
    fun pause()
    fun stop()
}

interface AudioTrackSelector {
    fun selectTrack(trackId: String)
}

interface QualitySelector {
    fun setAutoBitrate(enabled: Boolean)
}
