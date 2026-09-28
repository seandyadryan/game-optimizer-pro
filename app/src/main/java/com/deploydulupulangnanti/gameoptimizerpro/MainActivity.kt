package com.deploydulupulangnanti.gameoptimizerpro

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource as s
import androidx.compose.ui.platform.testTag
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
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

private val Lime = Color(0xFFB9F66B)
private val Background = Color(0xFF0C1019)
private val Panel = Color(0xFF171E2B)
private val Muted = Color(0xFFA3AEC2)
private val colors = darkColorScheme(primary = Lime, onPrimary = Background, background = Background,
    surface = Panel, onSurface = Color(0xFFF0F4FC), onSurfaceVariant = Muted, secondary = Color(0xFF9BAEFF))

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { MaterialTheme(colorScheme = colors) { OptimizerApp() } }
    }

    @Composable
    private fun OptimizerApp() {
        val repo = remember { DeviceRepository(applicationContext) }
        val icons = remember { GameIconLoader(applicationContext) }
        val locale = LocalConfiguration.current.locales[0]
        var page by rememberSaveable { mutableIntStateOf(0) }
        val tabScrollStates = List(4) { rememberLazyListState() }
        var onboarded by remember { mutableStateOf(repo.hasOnboarded()) }
        var snapshot by remember { mutableStateOf(DeviceSnapshot()) }
        var apps by remember { mutableStateOf(emptyList<GameApp>()) }
        var favorites by remember { mutableStateOf(repo.favorites()) }
        var profiles by remember { mutableStateOf(emptyMap<String, GameProfile>()) }
        var active by remember { mutableStateOf(repo.active()) }
        var sessions by remember { mutableStateOf(repo.sessions()) }
        var picker by rememberSaveable { mutableStateOf(false) }
        var selected by remember { mutableStateOf<GameApp?>(null) }
        var error by remember { mutableStateOf<Int?>(null) }
        var languagePicker by rememberSaveable { mutableStateOf(false) }
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
            runCatching { startActivity(Intent(action)) }.onFailure { error = R.string.error_settings }
        }
        Scaffold(containerColor = Background, bottomBar = {
            NavigationBar(containerColor = Background, tonalElevation = 0.dp) {
                listOf(s(R.string.nav_home) to Icons.Rounded.Dashboard, s(R.string.nav_games) to Icons.Rounded.SportsEsports,
                    s(R.string.nav_activity) to Icons.Rounded.QueryStats, s(R.string.nav_settings) to Icons.Rounded.Tune).forEachIndexed { index, item ->
                    NavigationBarItem(selected = page == index, onClick = { page = index }, modifier = Modifier.testTag("nav_$index"),
                        icon = { Icon(item.second, contentDescription = item.first) }, label = { Text(item.first, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Lime, indicatorColor = Panel))
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), state = tabScrollStates[page], contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(Lime, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Bolt, null, tint = Background, modifier = Modifier.size(30.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(s(R.string.brand_title), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(s(R.string.brand_subtitle), color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                        Surface(color = Lime.copy(alpha = .13f), shape = RoundedCornerShape(6.dp)) {
                            Text(s(R.string.pro_badge), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (active != null) item {
                    Tile {
                        Eyebrow(s(R.string.active_session))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GameIcon(active!!.packageName, active!!.game, icons)
                            Text(active!!.game, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        }
                        Text(s(R.string.session_started, date(active!!.started, locale)), color = Muted)
                        Button(onClick = { repo.finish(); active = null; sessions = repo.sessions() }) { Text(s(R.string.finish_session)) }
                    }
                }
                when (page) {
                    0 -> {
                        item { Column { Eyebrow(s(R.string.hero_eyebrow)); Text(s(R.string.hero_title), fontSize = 36.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold) } }
                        item {
                            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF243528), Panel)), RoundedCornerShape(24.dp)).padding(22.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Verified, null, tint = Lime); Spacer(Modifier.width(8.dp)); Eyebrow(s(R.string.device_condition)) }
                                    Text(s(readiness(snapshot).title), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text(s(readiness(snapshot).description), color = Muted, fontSize = 14.sp)
                                    Button(onClick = { page = 1 }, modifier = Modifier.fillMaxWidth().testTag("open_library")) { Text(s(R.string.open_library), Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null) }
                                }
                            }
                        }
                        item { SectionTitle(s(R.string.device_overview), s(R.string.refresh_interval)) }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Metric(s(R.string.battery), snapshot.battery?.let { NumberFormat.getPercentInstance(locale).format(it / 100.0) } ?: s(R.string.unavailable), Icons.Rounded.BatteryFull, Modifier.weight(1f))
                                Metric(s(R.string.battery_temperature), snapshot.temperature?.let { s(R.string.unit_celsius, decimal(it.toDouble(), locale)) } ?: s(R.string.unavailable), Icons.Rounded.Thermostat, Modifier.weight(1f))
                            }
                        }
                        item { Tile {
                            Text(s(R.string.available_ram), fontWeight = FontWeight.SemiBold)
                            Text(s(R.string.ram_fraction, gb(snapshot.availableRam, locale), gb(snapshot.totalRam, locale)), color = Lime)
                            LinearProgressIndicator(progress = { if (snapshot.totalRam > 0) snapshot.availableRam.toFloat() / snapshot.totalRam else 0f }, modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Background)
                            Text(s(R.string.storage_network, gb(snapshot.freeStorage, locale), s(snapshot.network.label)), color = Muted, fontSize = 12.sp)
                        } }
                        item { SectionTitle(s(R.string.prepare_title), s(R.string.prepare_subtitle)) }
                        item { Tile {
                            SettingRow(Icons.Rounded.Wifi, s(R.string.network_title), s(R.string.network_hint)) { openSettings(Settings.ACTION_WIFI_SETTINGS) }
                            SettingRow(Icons.Rounded.DoNotDisturbOn, s(R.string.sound_title), s(R.string.sound_hint)) { openSettings(Settings.ACTION_SOUND_SETTINGS) }
                            SettingRow(Icons.Rounded.Brightness6, s(R.string.display_title), s(R.string.display_hint)) { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
                            SettingRow(Icons.Rounded.BatteryChargingFull, s(R.string.battery), s(R.string.battery_hint)) { openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
                        } }
                        item { Text(s(R.string.metrics_note), color = Muted, fontSize = 12.sp) }
                    }
                    1 -> {
                        item { Text(s(R.string.library_title), fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Text(s(R.string.library_subtitle), color = Muted) }
                        item { Button(onClick = { picker = true }, modifier = Modifier.fillMaxWidth().testTag("add_game")) { Icon(Icons.Rounded.Add, null); Text(s(R.string.add_game)) } }
                        val library = apps.filter { it.isGame || it.packageName in favorites }
                        if (library.isEmpty()) item { EmptyState(Icons.Rounded.SportsEsports, s(R.string.library_empty), s(R.string.library_empty_detail)) }
                        items(library, key = { it.packageName }) { game ->
                            Tile {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GameIcon(game.packageName, game.label, icons)
                                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(game.label, fontWeight = FontWeight.Bold); Text(s((profiles[game.packageName] ?: repo.profile(game.packageName)).title), color = Muted, fontSize = 12.sp) }
                                    IconButton(onClick = { selected = game }) { Icon(Icons.Rounded.Tune, s(R.string.profile_for, game.label)) }
                                }
                                Button(onClick = {
                                    if (active != null) error = R.string.error_active
                                    else {
                                        val intent = packageManager.getLaunchIntentForPackage(game.packageName)
                                        if (intent == null) error = R.string.error_missing
                                        else runCatching { startActivity(intent); repo.start(game); active = repo.active() }
                                            .onFailure { error = R.string.error_launch }
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.PlayArrow, null); Text(s(R.string.start_session)) }
                            }
                        }
                        item { Text(s(R.string.library_note), color = Muted, fontSize = 12.sp) }
                    }
                    2 -> {
                        item { Text(s(R.string.activity_title), fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Metric(s(R.string.sessions_saved), integer(sessions.size.toLong(), locale), Icons.Rounded.SportsEsports, Modifier.weight(1f))
                            Metric(s(R.string.total_minutes), integer(sessions.sumOf { it.durationMinutes }, locale), Icons.Rounded.Schedule, Modifier.weight(1f))
                        } }
                        item { Text(s(R.string.session_note), color = Muted, fontSize = 12.sp) }
                        if (sessions.isEmpty()) item { EmptyState(Icons.Rounded.QueryStats, s(R.string.activity_empty), s(R.string.activity_empty_detail)) }
                        items(sessions) { session -> Tile {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GameIcon(session.packageName, session.game, icons)
                                Text(session.game, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            Text(s(R.string.session_summary, date(session.started, locale), s(session.profile.title)), color = Muted, fontSize = 12.sp)
                            Text(if (session.durationMinutes == 0L) s(R.string.less_than_minute) else s(R.string.duration_minutes, integer(session.durationMinutes, locale)), color = Lime)
                        } }
                    }
                    3 -> {
                        item { Text(s(R.string.nav_settings), fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        item { Tile { SettingRow(Icons.Rounded.Language, s(R.string.language_title), currentLanguageLabel(), Modifier.testTag("language_settings")) { languagePicker = true } } }
                        item { Tile {
                            Eyebrow(s(R.string.made_for_you))
                            Text(s(R.string.privacy_headline), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(s(R.string.privacy_summary), color = Muted)
                        } }
                        item { Tile {
                            SettingRow(Icons.Rounded.PrivacyTip, s(R.string.privacy_title), s(R.string.privacy_hint), Modifier.testTag("privacy")) { privacy = true }
                            SettingRow(Icons.Rounded.DeleteOutline, s(R.string.clear_history), s(R.string.clear_history_hint)) { clear = true }
                        } }
                        item { Tile {
                            Text(s(R.string.about_title), fontWeight = FontWeight.Bold)
                            Text(s(R.string.about_body, BuildConfig.VERSION_NAME), color = Muted)
                            Text(s(R.string.limitations), color = Muted, fontSize = 12.sp)
                        } }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        if (!onboarded) AlertDialog(onDismissRequest = {}, icon = { Icon(Icons.Rounded.Bolt, null, tint = Lime) },
            title = { Text(s(R.string.welcome_title)) },
            text = { Text(s(R.string.welcome_body), Modifier.verticalScroll(rememberScrollState())) },
            dismissButton = { TextButton(onClick = { languagePicker = true }) { Text(s(R.string.language_title)) } },
            confirmButton = { Button(onClick = { repo.onboard(); onboarded = true }, modifier = Modifier.testTag("get_started")) { Text(s(R.string.get_started)) } })
        if (picker) {
            var query by rememberSaveable { mutableStateOf("") }
            AlertDialog(onDismissRequest = { picker = false }, title = { Text(s(R.string.picker_title)) }, text = {
                Column { OutlinedTextField(query, { query = it }, label = { Text(s(R.string.search_apps)) }, singleLine = true, modifier = Modifier.testTag("app_search"))
                    LazyColumn(Modifier.heightIn(max = 360.dp)) {
                        val matches = apps.filter { it.label.contains(query, ignoreCase = true) }
                        if (matches.isEmpty()) item { Text(s(R.string.no_apps), Modifier.padding(16.dp)) }
                        items(matches, key = { it.packageName }) { app -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = app.isGame || app.packageName in favorites, enabled = !app.isGame,
                                onCheckedChange = { repo.toggleGame(app.packageName); favorites = repo.favorites() })
                            GameIcon(app.packageName, app.label, icons, size = 36.dp)
                            Text(app.label, modifier = Modifier.weight(1f).padding(start = 8.dp))
                        } }
                    }
                }
            }, confirmButton = { TextButton(onClick = { picker = false }, modifier = Modifier.testTag("picker_done")) { Text(s(R.string.done)) } })
        }
        selected?.let { game ->
            var profile by remember(game.packageName) { mutableStateOf(repo.profile(game.packageName)) }
            AlertDialog(onDismissRequest = { selected = null }, icon = { GameIcon(game.packageName, game.label, icons) }, title = { Text(s(R.string.profile_for, game.label)) }, text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameProfile.entries.forEach { option -> Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = option == profile, onClick = { profile = option })
                        Column { Text(s(option.title), fontWeight = FontWeight.Bold); Text(s(option.subtitle), color = Muted, fontSize = 12.sp) }
                    } }
                    Text(s(profile.guidance), color = Lime)
                    Text(s(R.string.apply_manually), fontSize = 12.sp)
                }
            }, confirmButton = { Button(onClick = { repo.setProfile(game.packageName, profile); profiles = profiles + (game.packageName to profile); selected = null }) { Text(s(R.string.save_profile)) } }, dismissButton = { TextButton(onClick = { selected = null }) { Text(s(R.string.cancel)) } })
        }
        if (privacy) AlertDialog(onDismissRequest = { privacy = false }, title = { Text(s(R.string.privacy_title)) }, text = {
            Text(s(R.string.privacy_body, s(R.string.support_url)), Modifier.verticalScroll(rememberScrollState()))
        }, confirmButton = { TextButton(onClick = { privacy = false }, modifier = Modifier.testTag("privacy_close")) { Text(s(R.string.close)) } })
        if (clear) AlertDialog(onDismissRequest = { clear = false }, title = { Text(s(R.string.delete_title)) }, text = { Text(s(R.string.delete_body)) },
            confirmButton = { TextButton(onClick = { repo.clearHistory(); sessions = emptyList(); clear = false }) { Text(s(R.string.delete)) } }, dismissButton = { TextButton(onClick = { clear = false }) { Text(s(R.string.cancel)) } })
        error?.let { message -> AlertDialog(onDismissRequest = { error = null }, title = { Text(s(R.string.attention)) }, text = { Text(s(message)) }, confirmButton = { TextButton(onClick = { error = null }) { Text(s(R.string.understood)) } }) }
        if (languagePicker) LanguagePicker(onDismiss = { languagePicker = false })
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
@Composable private fun SettingRow(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, action: () -> Unit) {
    Surface(onClick = action, modifier = modifier, color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Lime); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 12.sp) }; Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Muted, modifier = Modifier.size(18.dp))
        }
    }
}
@Composable private fun EmptyState(icon: ImageVector, title: String, subtitle: String) { Tile {
    Icon(icon, null, tint = Lime, modifier = Modifier.size(44.dp)); Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted)
} }
@Composable private fun gb(bytes: Long, locale: Locale) = s(R.string.unit_gib, decimal(bytes / 1_073_741_824.0, locale))
private fun decimal(value: Double, locale: Locale) = NumberFormat.getNumberInstance(locale).apply { minimumFractionDigits = 1; maximumFractionDigits = 1 }.format(value)
private fun integer(value: Long, locale: Locale) = NumberFormat.getIntegerInstance(locale).format(value)
private fun date(millis: Long, locale: Locale) = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(Date(millis))
