package com.example.billbuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    // Primary
    primary          = AmberDark,
    onPrimary        = Color.White,

    primaryContainer = LightAmber,
    onPrimaryContainer = Color(0xFF3D1A00),

    // Secondary
    secondary        = Color(0xFFB45309),
    onSecondary      = Color.White,

    secondaryContainer = Beige,
    onSecondaryContainer = Color(0xFF3D2600),

    // Background & Surface
    background       = AppBackground,
    onBackground     = PrimaryText,

    surface          = CardSurface,
    onSurface        = PrimaryText,

    surfaceVariant   = Beige,
    onSurfaceVariant = Color(0xFF4A3F00),

    // Outline
    outline          = OutlineColor,
    outlineVariant   = LightAmber,

    // Error / Tertiary
    error            = ErrorRed,
    onError          = Color.White,
    errorContainer   = Color(0xFFFFEDED),
    onErrorContainer = Color(0xFF7F1111),

    tertiary         = SuccessGreen,
    onTertiary       = Color.White,
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = Color(0xFF14532D),

    // Scrim / Inverse
    scrim            = Color(0x99000000),
    inverseSurface   = Color(0xFF2C2820),
    inverseOnSurface = OnDarkText,
    inversePrimary   = AmberOnDark,
)

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary          = AmberOnDark,
    onPrimary        = Color(0xFF3D1A00),

    primaryContainer = Color(0xFF5C3A00),
    onPrimaryContainer = LightAmber,

    // Secondary
    secondary        = AmberOnDark,
    onSecondary      = Color(0xFF3D1A00),

    secondaryContainer = Color(0xFF3A3000),
    onSecondaryContainer = Beige,

    // Background & Surface
    background       = DarkBackground,
    onBackground     = OnDarkText,

    surface          = DarkSurface,
    onSurface        = OnDarkText,

    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = OnDarkMuted,

    // Outline
    outline          = Color(0xFF6B5E3A),
    outlineVariant   = Color(0xFF3A3426),

    // Error / Tertiary
    error            = Color(0xFFFF6B6B),
    onError          = Color(0xFF690000),
    errorContainer   = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    tertiary         = Color(0xFF4ADE80),
    onTertiary       = Color(0xFF003912),
    tertiaryContainer = Color(0xFF00531A),
    onTertiaryContainer = Color(0xFFB7F5C8),

    // Scrim / Inverse
    scrim            = Color(0x99000000),
    inverseSurface   = AppBackground,
    inverseOnSurface = PrimaryText,
    inversePrimary   = AmberDark,
)

@Composable
fun BillBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}