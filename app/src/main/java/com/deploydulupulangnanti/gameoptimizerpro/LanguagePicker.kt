package com.deploydulupulangnanti.gameoptimizerpro

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import java.util.Locale

@Composable
fun currentLanguageLabel(): String {
    val displayLocale = LocalConfiguration.current.locales[0]
    val selected = AppCompatDelegate.getApplicationLocales()[0]
    return if (selected == null) stringResource(R.string.language_system)
    else supportedLanguages.firstOrNull { it.tag.equals(selected.toLanguageTag(), ignoreCase = true) }?.nativeName
        ?: selected.getDisplayName(displayLocale)
}

@Composable
fun LanguagePicker(onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val displayLocale = LocalConfiguration.current.locales[0]
    val selected = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    fun choose(tag: String) {
        onDismiss()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.language_hint))
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.language_search)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth().testTag("language_search"))
                LazyColumn(Modifier.heightIn(max = 360.dp).testTag("language_list")) {
                    item {
                        LanguageRow(stringResource(R.string.language_system), selected.isEmpty(), "system") { choose("") }
                    }
                    items(supportedLanguages.filter {
                        it.nativeName.contains(query, true) || it.tag.contains(query, true) ||
                            Locale.forLanguageTag(it.tag).getDisplayName(displayLocale).contains(query, true)
                    }, key = { it.tag }) { language ->
                        LanguageRow(language.nativeName, language.tag.equals(selected, true), language.tag) { choose(language.tag) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

@Composable
private fun LanguageRow(title: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().testTag("language_$tag").clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null)
        Text(title, Modifier.weight(1f).padding(start = 10.dp))
    }
}
