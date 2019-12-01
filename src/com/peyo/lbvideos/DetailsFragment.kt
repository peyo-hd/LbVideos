package com.peyo.lbvideos


import android.os.Bundle
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import java.io.IOException
import java.io.InputStream

class DetailsFragment : DetailsSupportFragment() {
    private var titleRes: Int = 0
    private var iconRes: Int = 0

    private val overview: DetailsOverviewRow
        get() {
            val adapter = ArrayObjectAdapter()
            adapter.add(Action(1, "OK"))
            adapter.add(Action(2, "Cancel"))

            val overview = DetailsOverviewRow("Menu Item Details")
            overview.actionsAdapter = adapter
            overview.imageDrawable = resources.getDrawable(iconRes, null)

            return overview
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = DetailsFragmentArgs.fromBundle(arguments!!)
        titleRes = args.titleResource
        iconRes = args.iconResource

        val selector = ClassPresenterSelector()
        selector.addClassPresenter(DetailsOverviewRow::class.java,
                FullWidthDetailsOverviewRowPresenter(
                        DetailsDescriptionPresenter()))
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())

        val rows = ArrayObjectAdapter(selector)
        rows.add(overview)
        adapter = rows

    }

    inner class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(
                vh: ViewHolder, item: Any) {
            vh.title.text = resources.getString(titleRes)
            vh.subtitle.text = "Menu Item Details"
            vh.body.text = ("Lorem ipsum dolor sit amet, consectetur "
                    + "adipisicing elit, sed do eiusmod tempor incididunt ut labore "
                    + " et dolore magna aliqua. Ut enim ad minim veniam, quis "
                    + "nostrud exercitation ullamco laboris nisi ut aliquip ex ea "
                    + "commodo consequat.")
        }
    }
}