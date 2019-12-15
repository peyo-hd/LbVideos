package com.peyo.lbvideos

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import java.lang.Long.max
import java.lang.Long.min

class PlaybackFragment : VideoSupportFragment() {
    private val args: PlaybackFragmentArgs by navArgs()

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var glue : MediaPlayerGlue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaSession = MediaSessionCompat(requireContext(), "LbVideos").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(MediaSessionCallback())
        }

        MediaControllerCompat(requireContext(), mediaSession).also { mediaController ->
            MediaControllerCompat.setMediaController(requireActivity(), mediaController)
        }

        glue = MediaPlayerGlue(requireContext(), MediaPlayerAdapter(activity)).apply {
            host = VideoSupportFragmentGlueHost(this@PlaybackFragment)
            addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue) {
                    if (glue.isPrepared) {
                        play()
                        mediaSession.isActive = true
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, 0)
                    }
                }
            })
            title = args.metadata.title
            subtitle = args.metadata.studio
            playerAdapter.setDataSource(Uri.parse(args.metadata.source))
        }
    }

    val AVAILABLE_MEDIA_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
            or PlaybackStateCompat.ACTION_PAUSE
            or PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_SEEK_TO)

    private fun updatePlaybackState(
            @PlaybackStateCompat.State state: Int,
            position: Long) {
        val builder = PlaybackStateCompat.Builder()
                .setActions(AVAILABLE_MEDIA_ACTIONS)
                .setState(state, position, 1.0f)
        mediaSession.setPlaybackState(builder.build())
    }

    private inner class MediaPlayerGlue(context: Context, adapter: MediaPlayerAdapter) :
            PlaybackTransportControlGlue<MediaPlayerAdapter>(context, adapter) {
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

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onSeekTo(pos: Long) {
            glue.seekTo(pos)
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, pos)
        }

        override fun onPause() {
            glue.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, glue.currentPosition)
        }

        override fun onPlay() {
            glue.play()
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING, glue.currentPosition)
        }

        override fun onStop() {
            findNavController().popBackStack()
        }
    }

}
