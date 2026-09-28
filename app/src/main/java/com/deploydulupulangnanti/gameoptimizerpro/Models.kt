package com.deploydulupulangnanti.gameoptimizerpro

data class DeviceSnapshot(
    val battery: Int? = null,
    val temperature: Float? = null,
    val availableRam: Long = 0,
    val totalRam: Long = 0,
    val freeStorage: Long = 0,
    val powerSaver: Boolean = false,
    val thermalStatus: Int? = null,
    val network: NetworkStatus = NetworkStatus.OFFLINE
)

enum class NetworkStatus(val label: Int) {
    OFFLINE(R.string.network_offline), NO_INTERNET(R.string.network_no_internet),
    WIFI(R.string.network_wifi), CELLULAR(R.string.network_cellular),
    ETHERNET(R.string.network_ethernet), CONNECTED(R.string.network_connected)
}

enum class GameProfile(val title: Int, val subtitle: Int, val guidance: Int) {
    BALANCED(R.string.profile_balanced, R.string.profile_balanced_subtitle, R.string.profile_balanced_guidance),
    COMPETITIVE(R.string.profile_competitive, R.string.profile_competitive_subtitle, R.string.profile_competitive_guidance),
    ENDURANCE(R.string.profile_endurance, R.string.profile_endurance_subtitle, R.string.profile_endurance_guidance);

    companion object {
        // Version 1.0 stored Indonesian labels. Preserve those sessions during upgrades.
        fun fromStored(value: String): GameProfile = when (value) {
            "Seimbang" -> BALANCED
            "Kompetitif" -> COMPETITIVE
            "Hemat daya" -> ENDURANCE
            else -> entries.firstOrNull { it.name == value } ?: BALANCED
        }
    }
}

data class GameApp(val packageName: String, val label: String, val isGame: Boolean)
data class PlaySession(val game: String, val started: Long, val ended: Long, val profile: GameProfile, val packageName: String? = null) {
    val durationMinutes: Long get() = ((ended - started).coerceAtLeast(0) / 60_000)
}
data class ActiveSession(val game: String, val started: Long, val profile: GameProfile, val packageName: String? = null)

enum class Readiness(val title: Int, val description: Int) {
    HOT(R.string.readiness_hot, R.string.readiness_hot_detail),
    LOW_BATTERY(R.string.readiness_low, R.string.readiness_low_detail),
    POWER_SAVER(R.string.readiness_saver, R.string.readiness_saver_detail),
    LOADING(R.string.readiness_loading, R.string.readiness_loading_detail),
    READY(R.string.readiness_ready, R.string.readiness_ready_detail)
}

fun readiness(snapshot: DeviceSnapshot): Readiness = when {
    (snapshot.thermalStatus ?: 0) >= 3 || (snapshot.temperature ?: 0f) >= 42f ->
        Readiness.HOT
    snapshot.battery != null && snapshot.battery <= 20 ->
        Readiness.LOW_BATTERY
    snapshot.powerSaver -> Readiness.POWER_SAVER
    snapshot.totalRam == 0L -> Readiness.LOADING
    else -> Readiness.READY
}
