package dev.marcelpinto.permissionktx.internal

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

internal class AndroidActivityProvider(
    val context: Context
) : Application.ActivityLifecycleCallbacks {

    private var currentActivity: WeakReference<Activity>? = null

    fun get(): Activity? = currentActivity?.get()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // On pre-created is only called for 29+, just check if the activity was not already there
        if (activity != currentActivity?.get()) {
            currentActivity = WeakReference(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivity?.get()) {
            currentActivity?.clear()
        }
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }
}
