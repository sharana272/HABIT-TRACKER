package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AppShareHelper {
    private const val PREFS_NAME = "sharan_habits_share_prefs"
    private const val KEY_CUSTOM_INSTALL_LINK = "custom_install_link"

    fun getInstallLink(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CUSTOM_INSTALL_LINK, null)?.trim()
        return if (!saved.isNullOrBlank()) {
            saved
        } else {
            context.getString(R.string.app_install_link)
        }
    }

    fun saveCustomInstallLink(context: Context, url: String) {
        val trimmed = url.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (trimmed.isEmpty()) {
            prefs.edit().remove(KEY_CUSTOM_INSTALL_LINK).apply()
        } else {
            prefs.edit().putString(KEY_CUSTOM_INSTALL_LINK, trimmed).apply()
        }
    }

    fun copyInstallLink(context: Context, installUrl: String = getInstallLink(context)) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Sharan Habits Install Link", installUrl))
    }

    fun shareInstallLink(context: Context, installUrl: String = getInstallLink(context)) {
        val shareMessage = buildString {
            appendLine("Install Sharan Habits to track daily routines, streaks, and smart reminders!")
            appendLine()
            append("Open & install here: ")
            append(installUrl)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Install Sharan Habits App")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        val chooserIntent = Intent.createChooser(sendIntent, "Share Sharan Habits install link via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

    suspend fun shareApkInstaller(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val sourceApk = File(context.applicationInfo.sourceDir)
            require(sourceApk.exists()) { "Installed APK package not found on device." }

            val cacheDir = File(context.cacheDir, "shared_apk").apply { mkdirs() }
            val targetApk = File(cacheDir, "Sharan_Habits.apk")
            sourceApk.copyTo(targetApk, overwrite = true)

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetApk
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "Sharan Habits APK Installer")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Tap the attached Sharan_Habits.apk file to install Sharan Habits on your Android phone."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                val chooserIntent = Intent.createChooser(sendIntent, "Send Sharan Habits APK via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(chooserIntent)
            }
        }
    }
}
