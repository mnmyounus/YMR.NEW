package com.mnmyounus.ymr.ui.features
import android.os.Bundle; import android.view.*
import androidx.fragment.app.Fragment; import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.MsgAdapter; import com.mnmyounus.ymr.databinding.FragmentListBinding
import com.mnmyounus.ymr.viewmodel.MainViewModel
class ChatFragment : Fragment() {
    private var _b: FragmentListBinding? = null; private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentListBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        b.layoutEmpty.visibility = View.GONE; b.recycler.visibility = View.VISIBLE
        val pkg = arguments?.getString("pkg") ?: return
        val sender = arguments?.getString("sender") ?: return
        activity?.title = sender
        val adapter = MsgAdapter()
        b.recycler.layoutManager = LinearLayoutManager(requireContext()).also { it.stackFromEnd = true }
        b.recycler.adapter = adapter
        vm.getChat(pkg, sender).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNotEmpty()) b.recycler.scrollToPosition(list.size - 1)
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
