package com.peyo.lbvideos

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.peyo.lbvideos.database.VideoCollection
import com.peyo.lbvideos.database.VideoDatabase
import com.peyo.lbvideos.database.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainFragment : BrowseSupportFragment() {
    private lateinit var database: VideoDatabase
    private lateinit var synchronizeJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        headersState = HEADERS_HIDDEN

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            //findNavController().navigate(MainFragmentDirections.actionMainToDetails(
            //        R.string.about_preference, R.drawable.ic_settings_about))

            val metadata = item as VideoMetadata
            findNavController().navigate(MainFragmentDirections.actionMainToPlayback(metadata))
        }

        database = VideoDatabase.getInstance(requireContext())

        adapter = ArrayObjectAdapter(ListRowPresenter())

        synchronizeJob = lifecycleScope.launch(Dispatchers.IO) {
            populateAdapter(adapter as ArrayObjectAdapter)
            synchronize()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post { lifecycleScope. launch(Dispatchers.IO) {
            synchronizeJob.join()
            
            val collectionAdapter = adapter as ArrayObjectAdapter
            if (collectionAdapter.size() <=1) populateAdapter(collectionAdapter)
        } }
    }

    private fun populateAdapter(adapter: ArrayObjectAdapter) {
        val collections = database.collections().findAll()
        val collectionRows = collections.mapIndexed { idx, collection ->
            val header = HeaderItem(idx.toLong(), collection.category)
            val listRowAdapter = ArrayObjectAdapter(CardPresenter()).apply {
                setItems(database.metadata().findByCategory(collection.category), null)
            }
            ListRow(header, listRowAdapter)
        }
        adapter.setItems(collectionRows, listRowDiffCallback)
    }

    private val listRowDiffCallback = object : DiffCallback<ListRow>() {
        override fun areContentsTheSame(
                oldItem: ListRow, newItem: ListRow) = oldItem.hashCode() == newItem.hashCode()
        override fun areItemsTheSame(
                oldItem: ListRow, newItem: ListRow): Boolean  =
                oldItem.headerItem == newItem.headerItem &&
                        oldItem.adapter.size() == newItem.adapter.size() &&
                        (0 until oldItem.adapter.size()).all { idx ->
                            oldItem.adapter.get(idx) == oldItem.adapter.get(idx)
                        }
    }

    private fun synchronize() {
        val database = VideoDatabase.getInstance(requireContext())
        val feed = parseMediaFeed()

        database.metadata().insert(*feed.metadata.toTypedArray())
        database.collections().insert(*feed.collections.toTypedArray())
    }

    private data class FeedParseResult(
            val metadata: List<VideoMetadata>,
            val collections: List<VideoCollection>
    )

    private fun parseMediaFeed() : FeedParseResult {
        //val stream = context!!.resources.openRawResource(R.raw.videos)
        //val data = JSONObject(String(stream.readBytes(), StandardCharsets.UTF_8))

        val data = fetchJSON(requireContext().resources.getString(R.string.videos_url))

        val metadatas: MutableList<VideoMetadata> = mutableListOf()

        val feed = data.getJSONArray("googlevideos")
        val collections = feed.mapObject { obj ->
            val collection = VideoCollection(
                    category = obj.getString("category"))
            val subItemsMetadata = obj.getJSONArray("videos").mapObject { subItem ->
                VideoMetadata(
                        category = collection.category,
                        title = subItem.getString("title"),
                        description = subItem.getString("description"),
                        card = subItem.getString("card"),
                        background = subItem.getString("background"),
                        studio = subItem.getString("studio"),
                        source = subItem.getJSONArray("sources").get(0).toString())
            }
            metadatas.addAll(subItemsMetadata)
            collection
        }
        return FeedParseResult(metadatas, collections)
    }

    private fun fetchJSON(urlString: String): JSONObject {
        var reader: BufferedReader? = null
        val url = URL(urlString)
        val urlConnection = url.openConnection() as HttpsURLConnection
        return try {
            reader = BufferedReader(InputStreamReader(urlConnection.inputStream,
                    "utf-8"))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            val json = sb.toString()
            JSONObject(json)
        } finally {
            urlConnection.disconnect()
            if (null != reader) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}

private fun <T> JSONArray.mapObject(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }