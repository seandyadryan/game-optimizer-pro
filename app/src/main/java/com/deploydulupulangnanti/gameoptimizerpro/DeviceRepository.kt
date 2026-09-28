package com.deploydulupulangnanti.gameoptimizerpro

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import org.json.JSONArray
import org.json.JSONObject

class DeviceRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("optimizer", Context.MODE_PRIVATE)

    fun snapshot(): DeviceSnapshot {
        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java).getMemoryInfo(memory)
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val temp = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val power = context.getSystemService(PowerManager::class.java)
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        val network = when {
            capabilities == null -> NetworkStatus.OFFLINE
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkStatus.NO_INTERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.ETHERNET
            else -> NetworkStatus.CONNECTED
        }
        return DeviceSnapshot(
            battery = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else null,
            temperature = if (temp != Int.MIN_VALUE) temp / 10f else null,
            availableRam = memory.availMem, totalRam = memory.totalMem,
            freeStorage = StatFs(Environment.getDataDirectory().path).availableBytes,
            powerSaver = power.isPowerSaveMode,
            thermalStatus = if (Build.VERSION.SDK_INT >= 29) power.currentThermalStatus else null,
            network = network
        )
    }

    @Suppress("DEPRECATION")
    fun apps(): List<GameApp> = context.packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
    ).map {
        GameApp(it.activityInfo.packageName, it.loadLabel(context.packageManager).toString(),
            it.activityInfo.applicationInfo.category == ApplicationInfo.CATEGORY_GAME)
    }.filter { it.packageName != context.packageName }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }

    fun favorites(): Set<String> = prefs.getStringSet("games", emptySet())!!.toSet()
    fun toggleGame(pkg: String) {
        val values = favorites().toMutableSet()
        if (!values.add(pkg)) values.remove(pkg)
        prefs.edit().putStringSet("games", values).apply()
    }
    fun profile(pkg: String): GameProfile = GameProfile.fromStored(prefs.getString("profile:$pkg", GameProfile.BALANCED.name)!!)
    fun setProfile(pkg: String, profile: GameProfile) { prefs.edit().putString("profile:$pkg", profile.name).apply() }
    fun hasOnboarded(): Boolean = prefs.getBoolean("onboarded", false)
    fun onboard() { prefs.edit().putBoolean("onboarded", true).apply() }

    fun active(): ActiveSession? = runCatching {
        val json = JSONObject(prefs.getString("active", null) ?: return null)
        ActiveSession(json.getString("game"), json.getLong("started"), GameProfile.fromStored(json.getString("profile")), json.optString("packageName").takeIf { it.isNotBlank() })
    }.getOrNull()
    fun start(game: GameApp) {
        check(active() == null) { "An active session already exists" }
        prefs.edit().putString("active", JSONObject().put("game", game.label)
            .put("started", System.currentTimeMillis()).put("profile", profile(game.packageName).name)
            .put("packageName", game.packageName).toString()).apply()
    }
    fun sessions(): List<PlaySession> = runCatching {
        val array = JSONArray(prefs.getString("sessions", "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            PlaySession(item.getString("game"), item.getLong("started"), item.getLong("ended"), GameProfile.fromStored(item.getString("profile")), item.optString("packageName").takeIf { it.isNotBlank() })
        }
    }.getOrDefault(emptyList())
    fun finish() {
        val current = active() ?: return
        val values = (listOf(PlaySession(current.game, current.started, System.currentTimeMillis(), current.profile, current.packageName)) + sessions()).take(100)
        val array = JSONArray()
        values.forEach { array.put(JSONObject().put("game", it.game).put("started", it.started).put("ended", it.ended).put("profile", it.profile.name).put("packageName", it.packageName)) }
        prefs.edit().remove("active").putString("sessions", array.toString()).apply()
    }
    fun clearHistory() { prefs.edit().remove("sessions").apply() }
}
