package com.tored.bridgelauncher.services.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import java.util.concurrent.ConcurrentHashMap

/** Launcher activities of apps that exist only in a managed (work) profile, keyed by package name. */
object WorkProfileApps
{
    data class Target(val component: ComponentName, val user: UserHandle)

    private val _targets = ConcurrentHashMap<String, Target>()

    fun clear() = _targets.clear()
    fun contains(packageName: String) = _targets.containsKey(packageName)
    fun put(packageName: String, target: Target)
    {
        _targets[packageName] = target
    }

    /** Starts the work-profile copy of [packageName]; false when there is none. */
    fun tryLaunch(context: Context, packageName: String): Boolean
    {
        val target = _targets[packageName] ?: return false
        context.getSystemService(LauncherApps::class.java).startMainActivity(target.component, target.user, null, null)
        return true
    }
}
