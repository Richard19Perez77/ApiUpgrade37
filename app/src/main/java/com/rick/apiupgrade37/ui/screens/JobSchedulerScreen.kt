package com.rick.apiupgrade37.ui.screens

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rick.apiupgrade37.core.AndroidApis
import com.rick.apiupgrade37.jobs.DebugSampleJobService
import com.rick.apiupgrade37.ui.FeatureBody
import com.rick.apiupgrade37.ui.FeatureScaffold
import java.util.concurrent.TimeUnit

@Composable
fun JobSchedulerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("No job scheduled") }

    FeatureScaffold("Job debug stats", onBack) { padding ->
        FeatureBody(
            padding,
            "API 37: JobScheduler.getPendingJobReasonStats(jobId) returns a map of " +
                "PENDING_JOB_REASON_* → Duration. That folds getPendingJobReasons() and " +
                "getPendingJobReasonsHistory() into one call with cumulative wait time.\n\n" +
                "This demo schedules a charging-constrained job so the reason is usually " +
                "CONSTRAINT_CHARGING while the device is unplugged."
        ) {
            Button(
                onClick = {
                    val scheduler = context.getSystemService(JobScheduler::class.java)
                    val job = JobInfo.Builder(
                        DebugSampleJobService.JOB_ID,
                        ComponentName(context, DebugSampleJobService::class.java)
                    )
                        .setRequiresCharging(true)
                        .setMinimumLatency(TimeUnit.HOURS.toMillis(1))
                        .build()
                    scheduler.schedule(job)
                    status = dumpReasons(scheduler)
                }
            ) { Text("Schedule charging-constrained job") }
            Button(
                onClick = {
                    val scheduler = context.getSystemService(JobScheduler::class.java)
                    status = dumpReasons(scheduler)
                }
            ) { Text("Read pending reasons") }
            Text(status)
        }
    }
}

private fun dumpReasons(scheduler: JobScheduler): String = buildString {
    val id = DebugSampleJobService.JOB_ID
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        append("getPendingJobReason=").append(scheduler.getPendingJobReason(id)).append('\n')
    }
    if (AndroidApis.isAndroid17) {
        val stats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            scheduler.getPendingJobReasonStats(id)
        } else {
            TODO("VERSION.SDK_INT < CINNAMON_BUN")
        }
        append("getPendingJobReasonStats:\n")
        if (stats.isEmpty()) append("  (empty)\n")
        else stats.forEach { (reason, duration) ->
            append("  reason=").append(reason).append(" duration=").append(duration).append('\n')
        }
    } else {
        append("getPendingJobReasonStats requires API 37\n")
        // API 36: scheduler.getPendingJobReasonsHistory(id)
        // API 34: scheduler.getPendingJobReason(id)
    }
}
