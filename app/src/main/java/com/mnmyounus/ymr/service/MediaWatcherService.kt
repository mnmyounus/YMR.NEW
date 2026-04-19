package com.mnmyounus.ymr.service
import android.app.*; import android.content.ContentValues; import android.content.Intent
import android.net.Uri; import android.os.*; import android.provider.MediaStore
import androidx.core.app.NotificationCompat; import java.io.File

class MediaWatcherService : Service() {
    private val obs = mutableListOf<RecursiveFileObserver>()
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        createChan(); startForeground(2001, buildNotif()); watch(); return START_STICKY
    }
    private fun watch() {
        obs.forEach { it.stopWatching() }; obs.clear()
        val sd = Environment.getExternalStorageDirectory()
        listOf(
            File(sd, "WhatsApp/Media/WhatsApp Images"),
            File(sd, "WhatsApp/Media/WhatsApp Video"),
            File(sd, "WhatsApp/Media/WhatsApp Documents"),
            File(sd, "WhatsApp/Media/WhatsApp Audio"),
            File(sd, "WhatsApp/Media/WhatsApp Voice Notes"),
            File(sd, "WhatsApp/Media/WhatsApp Images/Private"),
            File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"),
            File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"),
            File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"),
            File(sd, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Private"),
            File(sd, "WhatsApp Business/Media/WhatsApp Images"),
            File(sd, "WhatsApp Business/Media/WhatsApp Video"),
            File(sd, "WhatsApp Business/Media/WhatsApp Documents")
        ).forEach { dir ->
            dir.mkdirs()
            runCatching { RecursiveFileObserver(dir.absolutePath) { f -> saveFile(f) }.also { it.startWatching(); obs.add(it) } }
        }
    }
    private fun saveFile(src: File) = runCatching {
        if (!src.exists() || !src.canRead() || src.name.startsWith(".") || src.length() == 0L) return@runCatching
        val ext = src.extension.lowercase()
        if (ext !in setOf("jpg","jpeg","png","mp4","3gp","pdf","doc","docx","mp3","aac","ogg","opus","gif","webp")) return@runCatching
        val isViewOnce = src.absolutePath.contains("Private", ignoreCase = true)
        val folder = if (isViewOnce) "YMR ViewOnce" else "YMR AutoSaved"
        val isVid = ext in listOf("mp4","3gp"); val isImg = ext in listOf("jpg","jpeg","png","gif","webp")
        val isAud = ext in listOf("mp3","aac","ogg","opus")
        val mime  = when { isVid->"video/$ext"; isImg->if(ext=="jpg")"image/jpeg" else "image/$ext"; isAud->"audio/$ext"; else->"application/octet-stream" }
        val name  = "${folder.replace(" ","_")}_${System.currentTimeMillis()}.$ext"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // PUBLIC MediaStore — files survive app uninstall & WhatsApp deletion
            val col = when { isVid->MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); isAud->MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); isImg->MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY); else->MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) }
            val rel = when { isVid->"Movies/$folder"; isAud->"Music/$folder"; isImg->"Pictures/$folder"; else->"Download/$folder" }
            val cv  = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME,name); put(MediaStore.MediaColumns.MIME_TYPE,mime); put(MediaStore.MediaColumns.RELATIVE_PATH,rel); put(MediaStore.MediaColumns.IS_PENDING,1) }
            val uri = contentResolver.insert(col,cv) ?: return@runCatching
            contentResolver.openOutputStream(uri)?.use { out -> src.inputStream().use { it.copyTo(out) } }
            cv.clear(); cv.put(MediaStore.MediaColumns.IS_PENDING,0); contentResolver.update(uri,cv,null,null)
        } else {
            // Android 9 — PUBLIC external (not app private folder)
            val base = when { isVid->Environment.DIRECTORY_MOVIES; isAud->Environment.DIRECTORY_MUSIC; isImg->Environment.DIRECTORY_PICTURES; else->Environment.DIRECTORY_DOWNLOADS }
            val dir  = File(Environment.getExternalStoragePublicDirectory(base), folder)
            dir.mkdirs(); val dest = File(dir,name)
            if (!dest.exists()) { src.copyTo(dest, overwrite=false); sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(dest))) }
        }
    }
    private fun createChan() { val c = NotificationChannel("ymr_w","YMR Watcher",NotificationManager.IMPORTANCE_MIN); (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c) }
    private fun buildNotif() = NotificationCompat.Builder(this,"ymr_w").setContentTitle("YMR").setContentText("Auto-saving media — permanent storage").setSmallIcon(android.R.drawable.ic_menu_save).setPriority(NotificationCompat.PRIORITY_MIN).build()
    override fun onDestroy() { obs.forEach { it.stopWatching() }; super.onDestroy() }
}
