package com.mnmyounus.ymr.service
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.mnmyounus.ymr.data.database.AppDatabase
import com.mnmyounus.ymr.data.database.MessageEntity
import com.mnmyounus.ymr.util.PrefsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YMRNotificationListener : NotificationListenerService() {
    companion object {
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
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val appName = APPS[sbn.packageName] ?: return
        if (!PrefsUtil.isSvcEnabled(applicationContext)) return
        val filter = PrefsUtil.getFilter(applicationContext)
        if (filter.isNotEmpty() && sbn.packageName != filter) return

        val extras = sbn.notification?.extras ?: return
        // Read title
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            ?: extras.getString("android.title")?.trim() ?: return
        if (title.isEmpty() || title == appName) return

        // Read message text - try all keys
        val textLine = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
        val bigText  = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val lines    = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        val rawText  = when {
            !bigText.isNullOrEmpty() && bigText.length > (textLine?.length ?: 0) -> bigText
            !textLine.isNullOrEmpty() -> textLine
            lines != null && lines.isNotEmpty() -> lines.last()?.toString()?.trim()
            else -> null
        } ?: return
        if (rawText.isEmpty()) return

        // Label media types
        val body = when (rawText.trim().lowercase()) {
            "image", "photo"                             -> "📷 Image"
            "video"                                      -> "🎥 Video"
            "sticker"                                    -> "🎨 Sticker"
            "audio", "voice message", "ptt", "voice note" -> "🎵 Voice/Audio"
            "document", "file"                           -> "📄 Document"
            "gif"                                        -> "🎞 GIF"
            "contact", "contact card"                    -> "👤 Contact"
            "location", "live location"                  -> "📍 Location"
            "missed voice call", "missed video call",
            "you missed a call"                          -> "📵 Missed Call"
            else -> if (rawText.startsWith("view once", ignoreCase = true)) "👁 View Once" else rawText
        }

        // Group detection: "SenderName: message text"
        val colonIdx = rawText.indexOf(":")
        val isGroup  = colonIdx in 1..29

        val senderName = if (isGroup) "$title › ${rawText.substring(0, colonIdx).trim()}"
                         else title

        scope.launch {
            AppDatabase.get(applicationContext).dao().insert(
                MessageEntity(
                    packageName = sbn.packageName,
                    appName     = appName,
                    senderName  = senderName,
                    messageText = body,
                    timestamp   = System.currentTimeMillis(),
                    isGroup     = isGroup
                )
            )
        }
    }
}
