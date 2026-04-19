package com.mnmyounus.ymr.ui.features
import android.Manifest; import android.app.Dialog; import android.content.pm.PackageManager
import android.os.Build; import android.os.Bundle; import android.os.Environment; import android.view.*
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope; import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.mnmyounus.ymr.R; import com.mnmyounus.ymr.adapter.StatusAdapter
import com.mnmyounus.ymr.databinding.FragmentStatusBinding
import com.mnmyounus.ymr.util.SaveUtil
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext
import java.io.File
class StatusFragment : Fragment() {
    private var _b: FragmentStatusBinding? = null; private val b get() = _b!!
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { load() }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentStatusBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val adapter = StatusAdapter(onClick={f->preview(f)}, onSave={f->SaveUtil.save(requireContext(),f,"YMR Statuses")})
        b.recycler.layoutManager = GridLayoutManager(requireContext(),2); b.recycler.adapter = adapter
        b.fabScan.setOnClickListener{checkAndLoad()}; b.btnScan.setOnClickListener{checkAndLoad()}
        checkAndLoad()
    }
    private fun checkAndLoad() {
        val perm = if (Build.VERSION.SDK_INT>=33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(),perm)!=PackageManager.PERMISSION_GRANTED)
            permLauncher.launch(arrayOf(perm,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        else load()
    }
    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) { findStatuses() }
            if (!isAdded) return@launch
            (b.recycler.adapter as StatusAdapter).submitList(files)
            b.layoutEmpty.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility    = if (files.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }
    private fun findStatuses(): List<File> {
        val sd = Environment.getExternalStorageDirectory(); val exts = setOf("jpg","jpeg","png","mp4","gif")
        val result = mutableListOf<File>()
        listOf(File(sd,"WhatsApp/Media/.Statuses"),File(sd,"Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File("/sdcard/WhatsApp/Media/.Statuses"),File(sd,"WhatsApp Business/Media/.Statuses"),
            File(sd,"Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses")
        ).forEach { dir ->
            if (dir.exists()&&dir.canRead()) dir.listFiles()
                ?.filter{it.isFile&&it.exists()&&!it.name.startsWith(".")&&it.extension.lowercase() in exts&&it.length()>0}
                ?.let{result.addAll(it)}
        }
        return result.distinctBy{"${it.name}_${it.length()}"}.sortedByDescending{it.lastModified()}
    }
    private fun preview(file: File) {
        val ctx = context ?: return
        val dialog = Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE); dialog.setContentView(R.layout.dialog_preview)
        val iv = dialog.findViewById<ImageView>(R.id.ivFull)
        val btnSave = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave)
        val btnClose= dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose)
        Glide.with(this).load(file).into(iv)
        val doSave={SaveUtil.save(ctx,file,"YMR Statuses");dialog.dismiss()}
        btnSave.setOnClickListener{doSave()}; iv.setOnClickListener{doSave()}; btnClose.setOnClickListener{dialog.dismiss()}
        dialog.show()
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
