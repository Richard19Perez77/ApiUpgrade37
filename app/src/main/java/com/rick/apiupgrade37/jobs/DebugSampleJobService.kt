package com.rick.apiupgrade37.jobs

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

/**
 * Tiny JobService so [android.app.job.JobScheduler.getPendingJobReasonStats] has something
 * to inspect. API 37 adds that aggregated pending-reason map; older code used
 * [android.app.job.JobScheduler.getPendingJobReason] (API 34) or
 * [android.app.job.JobScheduler.getPendingJobReasonsHistory] (API 36).
 */
class DebugSampleJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "job ${params.jobId} started")
        jobFinished(params, false)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean = false

    companion object {
        const val JOB_ID = 3701
        private const val TAG = "DebugSampleJob"
    }
}
