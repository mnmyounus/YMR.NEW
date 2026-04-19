package com.mnmyounus.ymr.data.database
import android.content.Context; import androidx.room.*
@Database(entities=[MessageEntity::class], version=2, exportSchema=false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): MessageDao
    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(ctx: Context) = I ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "ymr_db")
                .fallbackToDestructiveMigration().build().also { I = it }
        }
    }
}
