package com.mnmyounus.ymr.service
import android.app.*; import android.content.ContentValues; import android.content.Intent
import android.media.MediaRecorder; import android.net.Uri; import android.os.*; import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import java.io.File; import java.text.SimpleDateFormat; import java.util.*

class CallRecorderService : Service() {
    companion object { const val ACTION_START="START"; const val ACTION_STOP="STOP"; var isRecording=false }
    private var recorder: MediaRecorder? = null; private var outFile: File? = null
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int { when(i?.action){ ACTION_START->start(); ACTION_STOP->stop() }; return START_NOT_STICKY }
    private fun start() {
        if (isRecording) return
        createChan(); startForeground(3001, buildNotif("● Recording call..."))
        val dir = getExternalFilesDir("YMR_Recordings") ?: filesDir; dir.mkdirs()
        outFile = File(dir, "YMR_Call_${SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(Date())}.m4a")
        recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100); setAudioEncodingBitRate(128000)
            setOutputFile(outFile!!.absolutePath)
            try { prepare(); start(); isRecording=true } catch(e: Exception) { isRecording=false; stopSelf() }
        }
    }
    private fun stop() {
        if (!isRecording) return
        try { recorder?.stop() } catch(_: Exception) {}
        recorder?.release(); recorder=null; isRecording=false
        outFile?.let { if(it.exists()&&it.length()>0) saveToGallery(it) }
        stopForeground(true); stopSelf()
    }
    private fun saveToGallery(f: File) = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cv = ContentValues().apply { put(MediaStore.Audio.Media.DISPLAY_NAME,f.name); put(MediaStore.Audio.Media.MIME_TYPE,"audio/mp4"); put(MediaStore.Audio.Media.RELATIVE_PATH,"Music/YMR Recordings"); put(MediaStore.Audio.Media.IS_PENDING,1) }
            val uri = contentResolver.insert(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),cv)?:return@runCatching
            contentResolver.openOutputStream(uri)?.use{out->f.inputStream().use{it.copyTo(out)}}
            cv.clear(); cv.put(MediaStore.Audio.Media.IS_PENDING,0); contentResolver.update(uri,cv,null,null)
        } else {
            val dest = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"YMR Recordings")
            dest.mkdirs(); f.copyTo(File(dest,f.name),overwrite=true)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(f)))
        }
    }
    private fun createChan() { val c=NotificationChannel("ymr_rec","YMR Recorder",NotificationManager.IMPORTANCE_LOW); (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c) }
    private fun buildNotif(text: String) = NotificationCompat.Builder(this,"ymr_rec").setContentTitle("YMR").setContentText(text).setSmallIcon(android.R.drawable.ic_btn_speak_now).setPriority(NotificationCompat.PRIORITY_LOW).build()
    override fun onDestroy() { if(isRecording) stop(); super.onDestroy() }
}
