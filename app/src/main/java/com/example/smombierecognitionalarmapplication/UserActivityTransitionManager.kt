package com.example.smombierecognitionalarmapplication

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.example.smombierecognitionalarmapplication.utils.CUSTOM_INTENT_USER_ACTION
import com.example.smombierecognitionalarmapplication.utils.CUSTOM_REQUEST_CODE_USER_ACTION
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.tasks.await

class UserActivityTransitionManager(context: Context) {
    companion object {
        fun getActivityType(int: Int): String {
            return when (int) {
                0 -> "IN_VEHICLE"
                1 -> "ON_BICYCLE"
                2 -> "ON_FOOT"
                3 -> "STILL"
                4 -> "UNKNOWN"
                5 -> "TILTING"
                7 -> "WALKING"
                8 -> "RUNNING"
                else -> "UNKNOWN"
            }
        }

        fun getTransitionType(int: Int): String {
            return when (int) {
                0 -> "STARTED"
                1 -> "STOPPED"
                else -> ""
            }
        }
    }
    // list of activity transitions to be monitored
    private val activityTransitions: List<ActivityTransition> by lazy {
        listOf(
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
        )
    }

    private val activityClient = ActivityRecognition.getClient(context)

    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_USER_ACTION,
            Intent(CUSTOM_INTENT_USER_ACTION),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getUserActivity(detectedActivity: Int, transitionType: Int): ActivityTransition {
        return ActivityTransition.Builder().setActivityType(detectedActivity)
            .setActivityTransition(transitionType).build()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(ACTIVITY_RECOGNITION)
    suspend fun registerActivityTransitions() = kotlin.runCatching {
        activityClient.requestActivityTransitionUpdates(
            ActivityTransitionRequest(activityTransitions),
            pendingIntent
        ).await()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(ACTIVITY_RECOGNITION)
    suspend fun deregisterActivityTransitions() = kotlin.runCatching {
        activityClient.removeActivityUpdates(pendingIntent).await()
    }
}