package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.databinding.ItemRecBinding
import java.io.File; import java.text.SimpleDateFormat; import java.util.*
class RecAdapter(private val onPlay:(File)->Unit):ListAdapter<File,RecAdapter.VH>(object:DiffUtil.ItemCallback<File>(){
    override fun areItemsTheSame(a:File,b:File)=a.path==b.path
    override fun areContentsTheSame(a:File,b:File)=a.lastModified()==b.lastModified()}) {
    inner class VH(val b:ItemRecBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,t:Int)=VH(ItemRecBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h:VH,pos:Int){val f=getItem(pos)
        h.b.tvName.text=f.name
        h.b.tvDate.text=SimpleDateFormat("dd MMM yyyy · HH:mm",Locale.getDefault()).format(Date(f.lastModified()))
        h.b.btnPlay.setOnClickListener{onPlay(f)}}
}
