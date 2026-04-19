package com.mnmyounus.ymr.ui.home
import android.content.Intent; import android.os.Build; import android.os.Bundle; import android.view.*
import androidx.fragment.app.Fragment; import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.FragmentHomeBinding
import com.mnmyounus.ymr.service.CallRecorderService; import com.mnmyounus.ymr.service.MediaWatcherService
import com.mnmyounus.ymr.util.PrefsUtil; import com.mnmyounus.ymr.viewmodel.MainViewModel
class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null; private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentHomeBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s); val ctx = requireContext()
        vm.count.observe(viewLifecycleOwner) { b.tvMsgCount.text = it.toString() }
        b.switchSvc.isChecked = PrefsUtil.isSvcEnabled(ctx)
        b.tvSvcStatus.text = if (PrefsUtil.isSvcEnabled(ctx)) getString(R.string.svc_on) else getString(R.string.svc_off)
        b.switchSvc.setOnCheckedChangeListener { _, on ->
            PrefsUtil.setSvcEnabled(ctx, on)
            b.tvSvcStatus.text = if (on) getString(R.string.svc_on) else getString(R.string.svc_off)
            b.dotSvc.setBackgroundColor(ctx.getColor(if (on) R.color.green_ok else R.color.red_warn))
        }
        b.dotSvc.setBackgroundColor(ctx.getColor(if (PrefsUtil.isSvcEnabled(ctx)) R.color.green_ok else R.color.red_warn))
        runCatching {
            val wi = Intent(ctx, MediaWatcherService::class.java)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(wi) else ctx.startService(wi)
        }
        val goFeatures = View.OnClickListener { findNavController().navigate(R.id.featuresFragment) }
        b.cardMessages.setOnClickListener(goFeatures); b.cardMedia.setOnClickListener(goFeatures)
        b.cardStatus.setOnClickListener(goFeatures);   b.cardCalls.setOnClickListener(goFeatures)
        b.btnRecord.setOnClickListener {
            if (CallRecorderService.isRecording) {
                ctx.stopService(Intent(ctx, CallRecorderService::class.java))
                CallRecorderService.isRecording = false
            } else {
                val i = Intent(ctx, CallRecorderService::class.java).apply { action = CallRecorderService.ACTION_START }
                if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
            }
            updateRecBtn()
        }
        updateRecBtn()
    }
    override fun onResume() { super.onResume(); updateRecBtn() }
    private fun updateRecBtn() {
        if (!isAdded) return
        b.tvRecStatus.text = if (CallRecorderService.isRecording) "● Recording..." else "Tap to start"
        b.btnRecord.text = if (CallRecorderService.isRecording) getString(R.string.stop_record) else getString(R.string.start_record)
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
