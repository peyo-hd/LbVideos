package com.peyo.lbvideos

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import coil.api.load
import com.peyo.lbvideos.database.VideoMetadata

class CardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(ImageCardView(parent.context))

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val metadata = item as VideoMetadata
        val card = viewHolder.view as ImageCardView
        val res = card.resources

        card.setMainImageDimensions(res.getDimensionPixelSize(R.dimen.card_width),
                res.getDimensionPixelSize(R.dimen.card_height))
        card.mainImageView.load(metadata.card)
        card.titleText = metadata.title
        card.contentText = metadata.description
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) = Unit
}