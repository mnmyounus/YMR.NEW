package com.mnmyounus.ymr.data.repository
import android.content.Context
import com.mnmyounus.ymr.data.database.*
class MessageRepository(ctx: Context) {
    private val dao = AppDatabase.get(ctx).dao()
    val conversations = dao.getConversations()
    val count         = dao.getCount()
    fun getChat(p: String, s: String) = dao.getChat(p, s)
    suspend fun deleteAll()              = dao.deleteAll()
    suspend fun getAll()                 = dao.getAll()
    suspend fun insertAll(m: List<MessageEntity>) = dao.insertAll(m)
}
