package com.mnmyounus.ymr.viewmodel
import android.app.Application; import androidx.lifecycle.*
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.data.repository.MessageRepository
import kotlinx.coroutines.launch
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MessageRepository(app)
    val conversations = repo.conversations
    val count         = repo.count
    fun getChat(p: String, s: String) = repo.getChat(p, s)
    fun deleteAll()    = viewModelScope.launch { repo.deleteAll() }
    suspend fun getAll() = repo.getAll()
    suspend fun insertAll(m: List<MessageEntity>) = repo.insertAll(m)
}
