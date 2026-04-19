package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.mnmyounus.ymr.databinding.ItemMediaBinding; import java.io.File
class MediaAdapter(private val onSave:(File)->Unit):ListAdapter<File,MediaAdapter.VH>(object:DiffUtil.ItemCallback<File>(){
    override fun areItemsTheSame(a:File,b:File)=a.path==b.path
    override fun areContentsTheSame(a:File,b:File)=a.lastModified()==b.lastModified()}) {
    inner class VH(val b:ItemMediaBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,t:Int)=VH(ItemMediaBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h:VH,pos:Int){val f=getItem(pos)
        Glide.with(h.b.root.context).load(f).centerCrop().into(h.b.ivThumb)
        h.b.ivPlay.visibility=if(f.extension.lowercase() in listOf("mp4","3gp"))View.VISIBLE else View.GONE
        h.b.btnSave.setOnClickListener{onSave(f)}}
}
