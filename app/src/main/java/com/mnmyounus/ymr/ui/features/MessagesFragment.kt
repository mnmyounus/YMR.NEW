package com.mnmyounus.ymr.ui.features
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.adapter.ConvAdapter
import com.mnmyounus.ymr.databinding.FragmentListBinding
import com.mnmyounus.ymr.viewmodel.MainViewModel

class MessagesFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        b.btnAction.visibility = View.GONE
        b.tvEmpty.text = getString(R.string.no_msgs)

        val adapter = ConvAdapter { conv ->
            // CRITICAL FIX: use the Activity's NavController, not the fragment's
            // Child fragments inside ViewPager2 have a nested NavController
            // that doesn't know about chatFragment
            val navController = Navigation.findNavController(requireActivity(), R.id.navHost)
            navController.navigate(
                R.id.to_chat,
                bundleOf("pkg" to conv.packageName, "sender" to conv.senderName)
            )
        }

        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        vm.conversations.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.recycler.visibility    = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
