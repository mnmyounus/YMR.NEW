package com.mnmyounus.ymr.ui.features
import android.Manifest; import android.content.Intent; import android.content.pm.PackageManager
import android.media.MediaPlayer; import android.os.Build; import android.os.Bundle; import android.view.*; import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.RecAdapter; import com.mnmyounus.ymr.databinding.FragmentRecorderBinding
import com.mnmyounus.ymr.service.CallRecorderService; import java.io.File
class RecorderFragment : Fragment() {
    private var _b: FragmentRecorderBinding? = null; private val b get() = _b!!
    private var player: MediaPlayer? = null
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if(it) toggleRec() else Toast.makeText(requireContext(),"Mic permission required",Toast.LENGTH_SHORT).show() }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentRecorderBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val adapter = RecAdapter { file -> play(file) }
        b.recyclerRec.layoutManager = LinearLayoutManager(requireContext()); b.recyclerRec.adapter = adapter
        loadRecordings(adapter); updateUI()
        b.btnToggleRec.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
                permLauncher.launch(Manifest.permission.RECORD_AUDIO)
            else toggleRec()
        }
    }
    private fun toggleRec() {
        val ctx=requireContext()
        if (CallRecorderService.isRecording) { ctx.stopService(Intent(ctx,CallRecorderService::class.java)); CallRecorderService.isRecording=false }
        else { val i=Intent(ctx,CallRecorderService::class.java).apply{action=CallRecorderService.ACTION_START}; if(Build.VERSION.SDK_INT>=26) ctx.startForegroundService(i) else ctx.startService(i) }
        b.root.postDelayed({ if(isAdded){updateUI();loadRecordings(b.recyclerRec.adapter as RecAdapter)} },600)
    }
    private fun updateUI() {
        if (!isAdded) return
        b.tvRecStatus.text=if(CallRecorderService.isRecording)"● Recording..." else "Not Recording"
        b.tvRecStatus.setTextColor(requireContext().getColor(if(CallRecorderService.isRecording) com.mnmyounus.ymr.R.color.red_warn else com.mnmyounus.ymr.R.color.green_ok))
        b.btnToggleRec.text=if(CallRecorderService.isRecording) getString(com.mnmyounus.ymr.R.string.stop_record) else getString(com.mnmyounus.ymr.R.string.start_record)
    }
    private fun loadRecordings(adapter: RecAdapter) {
        val dir=requireContext().getExternalFilesDir("YMR_Recordings")?:return
        val files=dir.listFiles()?.filter{it.extension=="m4a"}?.sortedByDescending{it.lastModified()}?:emptyList()
        adapter.submitList(files); b.tvNoRec.visibility=if(files.isEmpty())View.VISIBLE else View.GONE
    }
    private fun play(file: File) { player?.stop();player?.release(); player=MediaPlayer().apply{setDataSource(file.absolutePath);prepare();start();setOnCompletionListener{release()}} }
    override fun onResume() { super.onResume(); updateUI() }
    override fun onDestroyView() { player?.stop();player?.release();player=null;super.onDestroyView();_b=null }
}
