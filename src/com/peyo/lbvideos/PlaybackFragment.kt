package com.peyo.lbvideos

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import java.lang.Long.max
import java.lang.Long.min

class PlaybackFragment : VideoSupportFragment() {
    private val args: PlaybackFragmentArgs by navArgs()

    private lateinit var player : SimpleExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = SimpleExoPlayer.Builder(requireContext()).build()
        mediaSession = MediaSessionCompat(requireContext(), "LbVideos")
        mediaSessionConnector = MediaSessionConnector(mediaSession)

        val adapter = LeanbackPlayerAdapter(requireContext(), player, 200)

        MediaPlayerGlue(requireContext(), adapter).apply {
            host = VideoSupportFragmentGlueHost(this@PlaybackFragment)

            title = args.metadata.title
            subtitle = args.metadata.studio

            val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "LbVideos")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(args.metadata.source))
            player.prepare(mediaSource, true, true)
            playWhenPrepared()
        }
    }

    override fun onResume() {
        super.onResume()
        mediaSessionConnector.setPlayer(player)
        mediaSession.isActive = true
    }

    override fun onPause() {
        super.onPause()
        mediaSession.isActive = false
        mediaSessionConnector.setPlayer(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }

    private inner class MediaPlayerGlue(context: Context, adapter: LeanbackPlayerAdapter) :
            PlaybackTransportControlGlue<LeanbackPlayerAdapter>(context, adapter) {
        private val rewindAction = PlaybackControlsRow.RewindAction(context)
        private val fastForwardAction = PlaybackControlsRow.FastForwardAction(context)

        override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter?) {
            super.onCreatePrimaryActions(primaryActionsAdapter)
            primaryActionsAdapter?.apply {
                add(0, rewindAction)
                add(fastForwardAction)
            }
        }

        override fun onActionClicked(action: Action) = when(action){
            rewindAction -> playerAdapter.seekTo(
                    max(0, playerAdapter.currentPosition - 10000))
            fastForwardAction -> playerAdapter.seekTo(
                    min(playerAdapter.duration, playerAdapter.currentPosition + 10000))
            else -> super.onActionClicked(action)
        }
    }

}
