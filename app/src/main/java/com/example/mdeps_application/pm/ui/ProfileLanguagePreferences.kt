package com.example.mdeps_application.pm.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.mdeps_application.R

private const val LANGUAGE_EN = "en"
private const val LANGUAGE_ES = "es"

@Composable
fun ProfileLanguagePreferenceSection(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit = {},
) {
    val selectedLanguage = resolveSelectedAppLanguage()
    val englishLabel = stringResource(R.string.profile_language_en)
    val spanishLabel = stringResource(R.string.profile_language_es)
    val englishA11y = stringResource(R.string.profile_a11y_language_en)
    val spanishA11y = stringResource(R.string.profile_a11y_language_es)

    VxCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_preferences_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Vx.Ink,
            )
            Text(
                text = stringResource(R.string.profile_preferences_subtitle),
                fontSize = 12.sp,
                color = Vx.Muted,
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                SegmentedButton(
                    selected = selectedLanguage == LANGUAGE_EN,
                    onClick = {
                        applyAppLanguage(LANGUAGE_EN)
                        onLanguageSelected(LANGUAGE_EN)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.semantics { contentDescription = englishA11y },
                ) {
                    Text(englishLabel)
                }
                SegmentedButton(
                    selected = selectedLanguage == LANGUAGE_ES,
                    onClick = {
                        applyAppLanguage(LANGUAGE_ES)
                        onLanguageSelected(LANGUAGE_ES)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.semantics { contentDescription = spanishA11y },
                ) {
                    Text(spanishLabel)
                }
            }
        }
    }
}

private fun applyAppLanguage(languageTag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
}

/**
 * Reflects only the explicit per-app locale choice, not the device default,
 * so both options stay actionable until the user picks one.
 */
private fun resolveSelectedAppLanguage(): String {
    val applicationLocales = AppCompatDelegate.getApplicationLocales()
    if (!applicationLocales.isEmpty) {
        return when (applicationLocales[0]?.language) {
            LANGUAGE_ES -> LANGUAGE_ES
            else -> LANGUAGE_EN
        }
    }
    return LANGUAGE_EN
}
