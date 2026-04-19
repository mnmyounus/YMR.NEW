package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.data.database.ConvSummary
import com.mnmyounus.ymr.databinding.ItemConvBinding
import java.text.SimpleDateFormat; import java.util.*
class ConvAdapter(private val onClick: (ConvSummary)->Unit) : ListAdapter<ConvSummary,ConvAdapter.VH>(object:DiffUtil.ItemCallback<ConvSummary>(){
    override fun areItemsTheSame(a:ConvSummary,b:ConvSummary)=a.packageName==b.packageName&&a.senderName==b.senderName
    override fun areContentsTheSame(a:ConvSummary,b:ConvSummary)=a==b}) {
    inner class VH(val b:ItemConvBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,t:Int)=VH(ItemConvBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h:VH,pos:Int){val c=getItem(pos)
        h.b.tvInitials.text=c.senderName.take(1).uppercase(); h.b.tvName.text=c.senderName
        h.b.tvMsg.text=c.lastMessage; h.b.tvApp.text=c.appName
        h.b.tvTime.text=SimpleDateFormat("dd/MM",Locale.getDefault()).format(Date(c.lastTimestamp))
        h.b.tvBadge.visibility=if(c.messageCount>0)View.VISIBLE else View.GONE
        h.b.tvBadge.text=c.messageCount.toString(); h.b.root.setOnClickListener{onClick(c)}}
}
