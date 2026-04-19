package com.mnmyounus.ymr.data.database
import androidx.lifecycle.LiveData; import androidx.room.*
data class ConvSummary(val packageName: String, val appName: String, val senderName: String,
    val lastMessage: String, val lastTimestamp: Long, val messageCount: Int)
@Dao interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: MessageEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(m: List<MessageEntity>)
    @Query("SELECT * FROM messages WHERE packageName=:p AND senderName=:s ORDER BY timestamp ASC")
    fun getChat(p: String, s: String): LiveData<List<MessageEntity>>
    @Query("""SELECT packageName,appName,senderName,MAX(messageText) AS lastMessage,
        MAX(timestamp) AS lastTimestamp,COUNT(*) AS messageCount
        FROM messages GROUP BY packageName,senderName ORDER BY lastTimestamp DESC""")
    fun getConversations(): LiveData<List<ConvSummary>>
    @Query("SELECT COUNT(*) FROM messages") fun getCount(): LiveData<Int>
    @Query("SELECT * FROM messages ORDER BY timestamp ASC") suspend fun getAll(): List<MessageEntity>
    @Query("DELETE FROM messages") suspend fun deleteAll()
}
