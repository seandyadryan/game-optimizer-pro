package com.deploydulupulangnanti.gameoptimizerpro

import android.content.Context
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameIconLoader(context: Context) {
    private val packageManager = context.applicationContext.packageManager
    private val cache = LruCache<String, ImageBitmap>(64)

    fun load(packageName: String): ImageBitmap? = cache.get(packageName) ?: runCatching {
        packageManager.getApplicationIcon(packageName).toBitmap(144, 144).asImageBitmap().also { cache.put(packageName, it) }
    }.getOrNull()
}

@Composable
fun GameIcon(packageName: String?, label: String, loader: GameIconLoader, size: Dp = 48.dp) {
    val bitmap by produceState<ImageBitmap?>(null, packageName, loader) {
        value = packageName?.let { withContext(Dispatchers.IO) { loader.load(it) } }
    }
    val description = stringResource(R.string.game_icon_description, label)
    val shape = RoundedCornerShape(size / 4)
    if (bitmap != null) {
        Image(bitmap!!, contentDescription = description,
            modifier = Modifier.size(size).clip(shape).testTag("game_icon_$packageName"))
    } else {
        Box(Modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.primary.copy(alpha = .12f))
            .testTag("game_icon_fallback_$packageName"), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.SportsEsports, description, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(size * .65f))
        }
    }
}
