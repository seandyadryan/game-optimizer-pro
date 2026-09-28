package com.deploydulupulangnanti.gameoptimizerpro

data class DeviceSnapshot(
    val battery: Int? = null,
    val temperature: Float? = null,
    val availableRam: Long = 0,
    val totalRam: Long = 0,
    val freeStorage: Long = 0,
    val powerSaver: Boolean = false,
    val thermalStatus: Int? = null,
    val network: String = "Offline"
)

enum class GameProfile(val title: String, val subtitle: String, val guidance: String) {
    BALANCED("Seimbang", "Untuk bermain sehari-hari", "Mulai dari grafis medium dan 60 FPS jika game mendukung. Turunkan grafis bila perangkat terasa panas."),
    COMPETITIVE("Kompetitif", "Utamakan respons & kestabilan", "Pilih grafis rendah dan frame rate tinggi yang didukung game. Gunakan jaringan stabil dan aktifkan Jangan Ganggu secara manual."),
    ENDURANCE("Hemat daya", "Sesi panjang, konsumsi lebih ringan", "Pilih batas 30 FPS dan grafis rendah di dalam game. Kurangi kecerahan layar dan istirahat berkala.")
}

data class GameApp(val packageName: String, val label: String, val isGame: Boolean)
data class PlaySession(val game: String, val started: Long, val ended: Long, val profile: String) {
    val durationMinutes: Long get() = ((ended - started).coerceAtLeast(0) / 60_000)
}
data class ActiveSession(val game: String, val started: Long, val profile: String)

fun readiness(snapshot: DeviceSnapshot): Pair<String, String> = when {
    (snapshot.thermalStatus ?: 0) >= 3 || (snapshot.temperature ?: 0f) >= 42f ->
        "Saatnya jeda" to "Perangkat sedang panas. Biarkan mendingin sebelum memulai sesi."
    snapshot.battery != null && snapshot.battery <= 20 ->
        "Daya mulai rendah" to "Pertimbangkan sesi lebih singkat atau isi daya sebelum bermain."
    snapshot.powerSaver -> "Mode hemat aktif" to "Penghemat baterai dapat membatasi performa. Tinjau pengaturan sesuai kebutuhan."
    snapshot.totalRam == 0L -> "Memeriksa perangkat" to "Menunggu data perangkat yang tersedia."
    else -> "Siap untuk sesi berikutnya" to "Kondisi dasar terlihat baik. Performa tetap bergantung pada perangkat dan game."
}
