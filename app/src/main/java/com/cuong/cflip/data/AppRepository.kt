package com.cuong.cflip.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** Lists every app that can be launched - the data source for the cover app drawer. */
class AppRepository(private val context: Context) {

    fun loadLaunchableApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { ri ->
                AppInfo(
                    packageName = ri.activityInfo.packageName,
                    label = ri.loadLabel(pm).toString(),
                    icon = runCatching { ri.loadIcon(pm) }.getOrNull(),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
