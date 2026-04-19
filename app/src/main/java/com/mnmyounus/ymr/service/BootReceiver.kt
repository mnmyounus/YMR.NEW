package com.mnmyounus.ymr.service
import android.content.BroadcastReceiver; import android.content.Context; import android.content.Intent; import android.os.Build
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            val s = Intent(ctx, MediaWatcherService::class.java)
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(s) else ctx.startService(s)
        }
    }
}
