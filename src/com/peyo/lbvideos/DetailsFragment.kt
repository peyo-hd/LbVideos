package com.peyo.lbvideos

import android.os.Bundle
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.Coil
import coil.api.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsFragment : DetailsSupportFragment() {
    private val args: DetailsFragmentArgs by navArgs()

    private val overview: DetailsOverviewRow
        get() {
            val adapter = ArrayObjectAdapter()
            adapter.add(Action(1, "OK"))
            adapter.add(Action(2, "Cancel"))

            val overview = DetailsOverviewRow("Menu Item Details")
            overview.actionsAdapter = adapter

            lifecycleScope.launch(Dispatchers.IO) {
                overview.imageDrawable = Coil.get(args.metadata.card)
            }
            return overview
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selector = ClassPresenterSelector()
        selector.addClassPresenter(DetailsOverviewRow::class.java,
                FullWidthDetailsOverviewRowPresenter(
                        DetailsDescriptionPresenter()))
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())

        val rows = ArrayObjectAdapter(selector)
        rows.add(overview)
        adapter = rows

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Action) {
                if (item.id == 1L) {
                    findNavController().navigate(DetailsFragmentDirections
                            .actionDetailsToPlayback(args.metadata))
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    inner class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(
                vh: ViewHolder, item: Any) {
            vh.title.text = args.metadata.title
            vh.subtitle.text = args.metadata.studio
            vh.body.text = args.metadata.description
        }
    }
}