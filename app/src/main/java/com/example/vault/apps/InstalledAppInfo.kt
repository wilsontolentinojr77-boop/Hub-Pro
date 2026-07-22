package com.example.vault.apps

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false,
    var isLocked: Boolean = false,
    var isHiddenFromLauncher: Boolean = false,
    val category: String = "General",
    val iconDrawable: Drawable? = null
)
