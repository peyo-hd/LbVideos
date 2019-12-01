package com.peyo.lbvideos

import android.net.Uri
import androidx.leanback.app.VideoSupportFragment
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class PlaybackFragment : VideoSupportFragment() {
    private val args: PlaybackFragmentArgs by navArgs()
    private lateinit var player: SimpleExoPlayer

    override fun onStart() {
        super.onStart()
        player = ExoPlayerFactory.newSimpleInstance(requireContext())
        player.setVideoSurface(surfaceView.holder.surface)

        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "LbVideos")
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(args.metadata.source))
        player.prepare(mediaSource, true, true)
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
