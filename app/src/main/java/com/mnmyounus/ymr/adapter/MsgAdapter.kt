package com.mnmyounus.ymr.adapter
import android.view.*; import androidx.recyclerview.widget.*
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.databinding.ItemMsgBinding
import java.text.SimpleDateFormat; import java.util.*
class MsgAdapter:ListAdapter<MessageEntity,MsgAdapter.VH>(object:DiffUtil.ItemCallback<MessageEntity>(){
    override fun areItemsTheSame(a:MessageEntity,b:MessageEntity)=a.id==b.id
    override fun areContentsTheSame(a:MessageEntity,b:MessageEntity)=a==b}) {
    inner class VH(val b:ItemMsgBinding):RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(p:ViewGroup,t:Int)=VH(ItemMsgBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h:VH,pos:Int){val m=getItem(pos)
        h.b.tvText.text=m.messageText
        h.b.tvTime.text=SimpleDateFormat("dd MMM · hh:mm a",Locale.getDefault()).format(Date(m.timestamp))
        h.b.tvSender.visibility=if(m.isGroup)View.VISIBLE else View.GONE; h.b.tvSender.text=m.senderName}
}
