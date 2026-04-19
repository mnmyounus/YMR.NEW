package com.mnmyounus.ymr
import android.Manifest; import android.content.ComponentName; import android.content.pm.PackageManager
import android.os.Build; import android.os.Bundle; import android.provider.Settings; import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity; import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment; import androidx.navigation.ui.setupWithNavController
import com.mnmyounus.ymr.databinding.ActivityMainBinding
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val perm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); b = ActivityMainBinding.inflate(layoutInflater); setContentView(b.root)
        setSupportActionBar(b.toolbar)
        val nav = (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment).navController
        b.bottomNav.setupWithNavController(nav)
        nav.addOnDestinationChangedListener { _, dest, _ ->
            b.bottomNav.visibility = if (dest.id == R.id.chatFragment) View.GONE else View.VISIBLE
        }
        checkBanner(); requestPerms()
    }
    override fun onResume() { super.onResume(); checkBanner() }
    private fun checkBanner() {
        val ok = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            ?.contains(ComponentName(this, com.mnmyounus.ymr.service.YMRNotificationListener::class.java).flattenToString()) == true
        b.bannerNotif.visibility = if (ok) View.GONE else View.VISIBLE
        b.bannerNotif.setOnClickListener { startActivity(android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }
    private fun requestPerms() {
        val need = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (!has(Manifest.permission.READ_MEDIA_IMAGES))  need += Manifest.permission.READ_MEDIA_IMAGES
            if (!has(Manifest.permission.READ_MEDIA_VIDEO))   need += Manifest.permission.READ_MEDIA_VIDEO
            if (!has(Manifest.permission.READ_MEDIA_AUDIO))   need += Manifest.permission.READ_MEDIA_AUDIO
            if (!has(Manifest.permission.POST_NOTIFICATIONS)) need += Manifest.permission.POST_NOTIFICATIONS
        } else {
            if (!has(Manifest.permission.READ_EXTERNAL_STORAGE))  need += Manifest.permission.READ_EXTERNAL_STORAGE
            if (!has(Manifest.permission.WRITE_EXTERNAL_STORAGE)) need += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        if (!has(Manifest.permission.READ_CALL_LOG)) need += Manifest.permission.READ_CALL_LOG
        if (!has(Manifest.permission.READ_CONTACTS)) need += Manifest.permission.READ_CONTACTS
        if (!has(Manifest.permission.RECORD_AUDIO))  need += Manifest.permission.RECORD_AUDIO
        if (need.isNotEmpty()) perm.launch(need.toTypedArray())
    }
    private fun has(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}
