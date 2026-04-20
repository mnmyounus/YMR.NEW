package com.mnmyounus.ymr.ui.features
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.DocAdapter
import com.mnmyounus.ymr.adapter.MediaAdapter
import com.mnmyounus.ymr.databinding.FragmentListBinding
import com.mnmyounus.ymr.util.SaveUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MediaListFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!

    companion object {
        fun newInstance(type: String) = MediaListFragment().apply {
            arguments = Bundle().apply { putString("type", type) }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val type = arguments?.getString("type") ?: "photos"
        b.btnAction.visibility = View.GONE
        b.tvEmpty.text = "Loading..."
        b.layoutEmpty.visibility = View.VISIBLE
        b.recycler.visibility = View.GONE

        when (type) {
            "photos", "videos" -> {
                // 3 columns for grid
                b.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
                b.recycler.adapter = MediaAdapter { f -> SaveUtil.save(requireContext(), f, "YMR Media") }
            }
            else -> {
                b.recycler.layoutManager = LinearLayoutManager(requireContext())
                b.recycler.adapter = DocAdapter { f -> SaveUtil.save(requireContext(), f, "YMR Media") }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) { scan(type) }
            if (!isAdded) return@launch
            when (type) {
                "photos", "videos" -> (b.recycler.adapter as? MediaAdapter)?.submitList(files)
                else               -> (b.recycler.adapter as? DocAdapter)?.submitList(files)
            }
            b.layoutEmpty.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility    = if (files.isNotEmpty()) View.VISIBLE else View.GONE
            if (files.isEmpty()) b.tvEmpty.text = "No files found"
        }
    }

    private fun scan(type: String): List<File> {
        val sd = Environment.getExternalStorageDirectory()
        val dirs = when (type) {
            "photos"  -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Images"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"),
                File(sd, "WhatsApp Business/Media/WhatsApp Images"),
                File(sd, "Telegram/Telegram Images")
            )
            "videos"  -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Video"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"),
                File(sd, "Telegram/Telegram Video")
            )
            "audio"   -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Audio"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio"),
                File(sd, "WhatsApp/Media/WhatsApp Voice Notes")
            )
            else      -> listOf(
                File(sd, "WhatsApp/Media/WhatsApp Documents"),
                File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
        }
        val exts = when (type) {
            "photos"  -> setOf("jpg","jpeg","png","webp","gif")
            "videos"  -> setOf("mp4","3gp","mkv")
            "audio"   -> setOf("mp3","aac","ogg","opus","m4a","wav")
            else      -> setOf("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip")
        }
        return dirs
            .filter  { it.exists() && it.canRead() }
            .flatMap { dir ->
                dir.listFiles()
                   ?.filter { f -> f.isFile && f.exists() && !f.name.startsWith(".") && f.extension.lowercase() in exts && f.length() > 0 }
                   ?: emptyList()
            }
            .distinctBy { "${it.name}_${it.length()}" }
            .sortedByDescending { it.lastModified() }
            .take(50)   // hard limit: 50 per tab — fast, no memory pressure
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
