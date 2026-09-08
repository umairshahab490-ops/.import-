package com.umairshahab.etea.studyplan.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager == null || !alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AlertScheduler.rescheduleUpcoming(context)
            } catch (_: Exception) {
                // Never crash
            } finally {
                pendingResult.finish()
            }
        }
    }
}
