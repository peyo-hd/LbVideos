package com.peyo.lbvideos

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import androidx.navigation.fragment.navArgs
import java.lang.Long.max
import java.lang.Long.min

class PlaybackFragment : VideoSupportFragment() {
    private val args: PlaybackFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MediaPlayerGlue(requireContext(), MediaPlayerAdapter(activity)).apply {
            setHost(VideoSupportFragmentGlueHost(this@PlaybackFragment))
            addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue) {
                    if (glue.isPrepared) {
                        //playerGlue.seekProvider = MySeekProvider()
                        play()
                    }
                }
            })
            title = args.metadata.title
            subtitle = args.metadata.studio
            playerAdapter.setDataSource(Uri.parse(args.metadata.source))
        }
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
                    max(0, playerAdapter.currentPosition - 5000))
            fastForwardAction -> playerAdapter.seekTo(
                    min(playerAdapter.duration, playerAdapter.currentPosition + 5000))
            else -> super.onActionClicked(action)
        }
    }

}
