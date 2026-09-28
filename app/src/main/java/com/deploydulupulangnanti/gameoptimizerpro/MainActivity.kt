package com.deploydulupulangnanti.gameoptimizerpro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Lime = Color(0xFFB9F66B)
private val Background = Color(0xFF0C1019)
private val Panel = Color(0xFF171E2B)
private val Muted = Color(0xFFA3AEC2)
private val colors = darkColorScheme(primary = Lime, onPrimary = Background, background = Background,
    surface = Panel, onSurface = Color(0xFFF0F4FC), onSurfaceVariant = Muted, secondary = Color(0xFF9BAEFF))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme(colorScheme = colors) { OptimizerApp() } }
    }

    @Composable
    private fun OptimizerApp() {
        val repo = remember { DeviceRepository(applicationContext) }
        var page by rememberSaveable { mutableIntStateOf(0) }
        var onboarded by remember { mutableStateOf(repo.hasOnboarded()) }
        var snapshot by remember { mutableStateOf(DeviceSnapshot()) }
        var apps by remember { mutableStateOf(emptyList<GameApp>()) }
        var favorites by remember { mutableStateOf(repo.favorites()) }
        var profiles by remember { mutableStateOf(emptyMap<String, GameProfile>()) }
        var active by remember { mutableStateOf(repo.active()) }
        var sessions by remember { mutableStateOf(repo.sessions()) }
        var picker by rememberSaveable { mutableStateOf(false) }
        var selected by remember { mutableStateOf<GameApp?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var privacy by remember { mutableStateOf(false) }
        var clear by remember { mutableStateOf(false) }
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                apps = withContext(Dispatchers.IO) { repo.apps() }
                while (true) {
                    snapshot = withContext(Dispatchers.IO) { repo.snapshot() }
                    delay(5000)
                }
            }
        }
        fun openSettings(action: String) {
            runCatching { startActivity(Intent(action)) }.onFailure { error = "Pengaturan ini tidak tersedia pada perangkat Anda." }
        }
        Scaffold(containerColor = Background, bottomBar = {
            NavigationBar(containerColor = Background, tonalElevation = 0.dp) {
                listOf("Beranda" to Icons.Rounded.Dashboard, "Game" to Icons.Rounded.SportsEsports,
                    "Aktivitas" to Icons.Rounded.QueryStats, "Pengaturan" to Icons.Rounded.Tune).forEachIndexed { index, item ->
                    NavigationBarItem(selected = page == index, onClick = { page = index },
                        icon = { Icon(item.second, contentDescription = item.first) }, label = { Text(item.first, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, indicatorColor = Panel))
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(Lime, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Bolt, "", tint = Background, modifier = Modifier.size(30.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("GAME OPTIMIZER", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("PUSAT KENDALI GAMING", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
                        }
                        Surface(color = Lime.copy(alpha = .13f), shape = RoundedCornerShape(6.dp)) {
                            Text("PRO", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (active != null) item {
                    Tile {
                        Eyebrow("SESI BERJALAN")
                        Text(active!!.game, style = MaterialTheme.typography.titleLarge)
                        Text("Timer manual dimulai ${date(active!!.started)}. Akhiri setelah selesai bermain.", color = Muted)
                        Button(onClick = { repo.finish(); active = null; sessions = repo.sessions() }) { Text("Selesaikan sesi") }
                    }
                }
                when (page) {
                    0 -> {
                        item { Column { Eyebrow("PLAY SMART. STAY IN CONTROL."); Text("Siapkan sesi\nterbaikmu.", fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold) } }
                        item {
                            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF243528), Panel)), RoundedCornerShape(24.dp)).padding(22.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Verified, null, tint = Lime); Spacer(Modifier.width(8.dp)); Eyebrow("KONDISI PERANGKAT") }
                                    Text(readiness(snapshot).first, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text(readiness(snapshot).second, color = Muted, fontSize = 14.sp)
                                    Button(onClick = { page = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Buka pustaka game"); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null) }
                                }
                            }
                        }
                        item { SectionTitle("Sekilas perangkat", "Diperbarui tiap 5 detik") }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Metric("Baterai", snapshot.battery?.let { "$it%" } ?: "—", Icons.Rounded.BatteryFull, Modifier.weight(1f))
                                Metric("Suhu baterai", snapshot.temperature?.let { String.format(Locale.getDefault(), "%.1f°C", it) } ?: "—", Icons.Rounded.Thermostat, Modifier.weight(1f))
                            }
                        }
                        item { Tile {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("RAM tersedia", fontWeight = FontWeight.SemiBold); Text("${gb(snapshot.availableRam)} / ${gb(snapshot.totalRam)}", color = Lime) }
                            LinearProgressIndicator(progress = { if (snapshot.totalRam > 0) snapshot.availableRam.toFloat() / snapshot.totalRam else 0f }, modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Background)
                            Text("Ruang kosong ${gb(snapshot.freeStorage)}  •  ${snapshot.network}", color = Muted, fontSize = 12.sp)
                        } }
                        item { SectionTitle("Persiapan bermain", "Anda mengendalikan pengaturan") }
                        item { Tile {
                            SettingRow(Icons.Rounded.Wifi, "Jaringan", "Pilih koneksi yang stabil") { openSettings(Settings.ACTION_WIFI_SETTINGS) }
                            SettingRow(Icons.Rounded.DoNotDisturbOn, "Suara & Jangan Ganggu", "Tinjau suara dan notifikasi perangkat") { openSettings(Settings.ACTION_SOUND_SETTINGS) }
                            SettingRow(Icons.Rounded.Brightness6, "Layar", "Sesuaikan kecerahan & refresh rate") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                            SettingRow(Icons.Rounded.BatteryChargingFull, "Baterai", "Tinjau mode hemat daya") { openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                        } }
                        item { Text("Suhu di atas adalah suhu baterai, bukan CPU/GPU. Aplikasi tidak mengubah FPS, RAM, atau pengaturan internal game.", color = Muted, fontSize = 12.sp) }
                    }
                    1 -> {
                        item { Text("Pustaka game", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Text("Satu tempat untuk game dan profil bermain Anda.", color = Muted) }
                        item { Button(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Add, null); Text("Tambahkan game") } }
                        val library = apps.filter { it.isGame || it.packageName in favorites }
                        if (library.isEmpty()) item { EmptyState(Icons.Rounded.SportsEsports, "Pustaka masih kosong", "Tambahkan aplikasi yang terpasang untuk mulai menyiapkan sesi pertama Anda.") }
                        items(library, key = { it.packageName }) { game ->
                            Tile {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.SportsEsports, null, tint = Lime, modifier = Modifier.size(36.dp))
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(game.label, fontWeight = FontWeight.Bold); Text((profiles[game.packageName] ?: repo.profile(game.packageName)).title, color = Muted, fontSize = 12.sp) }
                                    IconButton(onClick = { selected = game }) { Icon(Icons.Rounded.Tune, "Profil ${game.label}") }
                                }
                                Button(onClick = {
                                    if (active != null) error = "Selesaikan sesi aktif sebelum memulai sesi lain."
                                    else {
                                        val intent = packageManager.getLaunchIntentForPackage(game.packageName)
                                        if (intent == null) error = "Game tidak tersedia. Mungkin sudah dihapus dari perangkat."
                                        else runCatching { startActivity(intent); repo.start(game); active = repo.active() }
                                            .onFailure { error = "Game tidak dapat dibuka pada perangkat ini." }
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.PlayArrow, null); Text("Mulai sesi") }
                            }
                        }
                        item { Text("Game yang dikenali Android muncul otomatis. Game lain dapat ditambahkan manual. Profil berisi rekomendasi yang Anda terapkan di dalam game.", color = Muted, fontSize = 12.sp) }
                    }
                    2 -> {
                        item { Text("Aktivitas bermain", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Metric("Sesi tersimpan", sessions.size.toString(), Icons.Rounded.SportsEsports, Modifier.weight(1f))
                            Metric("Total menit", sessions.sumOf { it.durationMinutes }.toString(), Icons.Rounded.Schedule, Modifier.weight(1f))
                        } }
                        item { Text("Durasi dihitung dari tombol mulai hingga selesai manual, termasuk waktu di luar game. Maksimal 100 sesi terakhir disimpan.", color = Muted, fontSize = 12.sp) }
                        if (sessions.isEmpty()) item { EmptyState(Icons.Rounded.QueryStats, "Cerita Anda dimulai di sini", "Selesaikan satu sesi untuk melihat riwayat bermain.") }
                        items(sessions) { session -> Tile {
                            Text(session.game, fontWeight = FontWeight.Bold)
                            Text("${date(session.started)} • ${session.profile}", color = Muted, fontSize = 12.sp)
                            Text(if (session.durationMinutes == 0L) "Kurang dari 1 menit" else "${session.durationMinutes} menit", color = Lime)
                        } }
                    }
                    3 -> {
                        item { Text("Pengaturan", fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Tile {
                            Eyebrow("DIBUAT UNTUK ANDA")
                            Text("Privasi, sejak awal.", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Tanpa akun, tanpa iklan, tanpa pelacak. Data pustaka dan sesi tetap di perangkat Anda.", color = Muted)
                        } }
                        item { Tile {
                            SettingRow(Icons.Rounded.PrivacyTip, "Kebijakan privasi", "Pelajari penggunaan data lokal") { privacy = true }
                            SettingRow(Icons.Rounded.DeleteOutline, "Hapus riwayat sesi", "Pustaka dan profil tetap tersimpan") { clear = true }
                        } }
                        item { Tile {
                            Text("Tentang Game Optimizer PRO", fontWeight = FontWeight.Bold)
                            Text("Versi ${BuildConfig.VERSION_NAME}\nPendamping gaming untuk memantau kondisi perangkat, menyiapkan pengaturan, dan mencatat sesi.", color = Muted)
                            Text("Tidak melakukan overclock, membersihkan RAM aplikasi lain, mengunci FPS, atau mengubah berkas game. Hasil performa tidak dijamin.", color = Muted, fontSize = 12.sp)
                        } }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        if (!onboarded) AlertDialog(onDismissRequest = {}, icon = { Icon(Icons.Rounded.Bolt, null, tint = Lime) },
            title = { Text("Selamat datang di PRO") },
            text = { Text("Siapkan sesi gaming dengan data perangkat nyata, profil rekomendasi, dan pustaka pribadi.\n\nAplikasi tidak menambah RAM atau mengubah FPS game. Daftar aplikasi dan riwayat hanya disimpan di perangkat. Sesi dicatat secara manual.") },
            confirmButton = { Button(onClick = { repo.onboard(); onboarded = true }) { Text("Mulai") } })
        if (picker) {
            var query by rememberSaveable { mutableStateOf("") }
            AlertDialog(onDismissRequest = { picker = false }, title = { Text("Tambahkan ke pustaka") }, text = {
                Column { OutlinedTextField(query, { query = it }, label = { Text("Cari aplikasi") }, singleLine = true)
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        val matches = apps.filter { it.label.contains(query, ignoreCase = true) }
                        if (matches.isEmpty()) item { Text("Tidak ada aplikasi yang cocok.", Modifier.padding(16.dp)) }
                        items(matches, key = { it.packageName }) { app -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = app.isGame || app.packageName in favorites, enabled = !app.isGame,
                                onCheckedChange = { repo.toggleGame(app.packageName); favorites = repo.favorites() })
                            Text(app.label, modifier = Modifier.weight(1f))
                        } }
                    }
                }
            }, confirmButton = { TextButton(onClick = { picker = false }) { Text("Selesai") } })
        }
        selected?.let { game ->
            var profile by remember(game.packageName) { mutableStateOf(repo.profile(game.packageName)) }
            AlertDialog(onDismissRequest = { selected = null }, title = { Text("Profil ${game.label}") }, text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameProfile.entries.forEach { option -> Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = option == profile, onClick = { profile = option })
                        Column { Text(option.title, fontWeight = FontWeight.Bold); Text(option.subtitle, color = Muted, fontSize = 12.sp) }
                    } }
                    Text(profile.guidance, color = Lime)
                    Text("Terapkan rekomendasi secara manual di pengaturan game.", fontSize = 12.sp)
                }
            }, confirmButton = { Button(onClick = { repo.setProfile(game.packageName, profile); profiles = profiles + (game.packageName to profile); selected = null }) { Text("Simpan profil") } }, dismissButton = { TextButton(onClick = { selected = null }) { Text("Batal") } })
        }
        if (privacy) AlertDialog(onDismissRequest = { privacy = false }, title = { Text("Kebijakan privasi") }, text = {
            Text("Game Optimizer PRO tidak mengirim data ke server. Aplikasi membaca kondisi baterai, memori, penyimpanan, koneksi, dan aplikasi yang dapat diluncurkan untuk fitur dashboard dan pustaka.\n\nPilihan game, profil, dan hingga 100 sesi manual disimpan lokal. Tidak ada analitik, iklan, akun, atau penjualan data. Cadangan Android dinonaktifkan.\n\nHapus riwayat melalui Pengaturan, atau hapus seluruh data melalui pengaturan Android/uninstall. Dukungan: github.com/seandyadryan/game-optimizer-pro/issues", Modifier.verticalScroll(rememberScrollState()))
        }, confirmButton = { TextButton(onClick = { privacy = false }) { Text("Tutup") } })
        if (clear) AlertDialog(onDismissRequest = { clear = false }, title = { Text("Hapus riwayat?") }, text = { Text("Semua sesi yang telah selesai akan dihapus permanen dari perangkat ini.") },
            confirmButton = { TextButton(onClick = { repo.clearHistory(); sessions = emptyList(); clear = false }) { Text("Hapus") } }, dismissButton = { TextButton(onClick = { clear = false }) { Text("Batal") } })
        error?.let { message -> AlertDialog(onDismissRequest = { error = null }, title = { Text("Perlu diperhatikan") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { error = null }) { Text("Mengerti") } }) }
    }
}

@Composable private fun Tile(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Panel, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable private fun Eyebrow(text: String) { Text(text, color = Lime, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold) }
@Composable private fun SectionTitle(title: String, subtitle: String) { Column { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 12.sp) } }
@Composable private fun Metric(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = Panel) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Lime, modifier = Modifier.size(22.dp)); Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 12.sp)
    } }
}
@Composable private fun SettingRow(icon: ImageVector, title: String, subtitle: String, action: () -> Unit) {
    Surface(onClick = action, color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Lime); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 12.sp) }; Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Muted, modifier = Modifier.size(18.dp))
        }
    }
}
@Composable private fun EmptyState(icon: ImageVector, title: String, subtitle: String) { Tile {
    Icon(icon, null, tint = Lime, modifier = Modifier.size(44.dp)); Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted)
} }
private fun gb(bytes: Long) = String.format(Locale.getDefault(), "%.1f GiB", bytes / 1_073_741_824.0)
private fun date(millis: Long) = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(millis))
