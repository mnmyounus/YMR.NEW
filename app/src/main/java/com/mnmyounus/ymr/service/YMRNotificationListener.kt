package com.mnmyounus.ymr.service
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.mnmyounus.ymr.data.database.AppDatabase
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.util.PrefsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YMRNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "YMR"
        // SupervisorJob keeps scope alive — not garbage collected
        private val job   = SupervisorJob()
        private val scope = CoroutineScope(Dispatchers.IO + job)

        val APPS = mapOf(
            "com.whatsapp"               to "WhatsApp",
            "com.whatsapp.w4b"           to "WhatsApp Business",
            "org.telegram.messenger"     to "Telegram",
            "org.telegram.messenger.web" to "Telegram X",
            "com.facebook.orca"          to "Messenger",
            "com.instagram.android"      to "Instagram",
            "com.viber.voip"             to "Viber",
            "com.snapchat.android"       to "Snapchat",
            "kik.android"                to "Kik",
            "com.discord"                to "Discord",
            "com.skype.raider"           to "Skype",
            "com.microsoft.teams"        to "Teams",
            "jp.naver.line.android"      to "LINE"
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "✅ Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "❌ Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val appName = APPS[sbn.packageName] ?: return
        if (!PrefsUtil.isSvcEnabled(applicationContext)) return
        val filter = PrefsUtil.getFilter(applicationContext)
        if (filter.isNotEmpty() && sbn.packageName != filter) return

        val extras = sbn.notification?.extras ?: return

        // Try every possible key WhatsApp/Telegram uses for the sender title
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            ?: extras.getString("android.title")?.trim()
            ?: return
        if (title.isEmpty() || title == appName) return

        // Try every possible key for message body
        val textLine = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
        val bigText  = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val lines    = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val subText  = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()

        val rawText = when {
            !bigText.isNullOrEmpty()  && bigText.length > (textLine?.length ?: 0) -> bigText
            !textLine.isNullOrEmpty() -> textLine
            lines != null && lines.isNotEmpty() -> lines.last()?.toString()?.trim()
            !subText.isNullOrEmpty() -> subText
            else -> null
        }?.takeIf { it.isNotEmpty() } ?: return

        // Label known media types
        val body = when (rawText.trim().lowercase()) {
            "image", "photo"                                -> "📷 Image"
            "video"                                         -> "🎥 Video"
            "sticker"                                       -> "🎨 Sticker"
            "audio", "voice message", "ptt", "voice note"  -> "🎵 Voice/Audio"
            "document", "file"                              -> "📄 Document"
            "gif"                                           -> "🎞 GIF"
            "contact", "contact card"                       -> "👤 Contact"
            "location", "live location"                     -> "📍 Location"
            "missed voice call", "missed video call",
            "you missed a call"                             -> "📵 Missed Call"
            else -> if (rawText.startsWith("view once", ignoreCase = true)) "👁 View Once" else rawText
        }

        val colonIdx = rawText.indexOf(":")
        val isGroup  = colonIdx in 1..29
        val sender   = if (isGroup) "$title › ${rawText.substring(0, colonIdx).trim()}" else title

        Log.d(TAG, "Saving: $appName | $sender | $body")

        scope.launch {
            try {
                AppDatabase.get(applicationContext).dao().insert(
                    MessageEntity(
                        packageName = sbn.packageName,
                        appName     = appName,
                        senderName  = sender,
                        messageText = body,
                        timestamp   = System.currentTimeMillis(),
                        isGroup     = isGroup
                    )
                )
                Log.d(TAG, "✅ Saved OK")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Save failed: ${e.message}")
            }
        }
    }
}
