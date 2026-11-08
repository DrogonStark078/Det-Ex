package com.detex.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.detex.app.data.model.CountryCurrency
import com.detex.app.data.model.SupportedCountries
import com.detex.app.ui.auth.AuthOtpScreen
import com.detex.app.ui.auth.AuthPhoneScreen
import com.detex.app.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots, screen recordings, and app switcher recents leaks
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Hardware-backed EncryptedSharedPreferences (AES-256 SIV for keys, AES-256 GCM for values)
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val securePrefs = EncryptedSharedPreferences.create(
            "detex_secure_prefs",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val isAuth = securePrefs.getBoolean("is_authenticated", false)
        val savedCountryCode = securePrefs.getString("country_iso", "IN") ?: "IN"
        val savedPhone = securePrefs.getString("user_phone", "") ?: ""
        val initialCountry = SupportedCountries.list.find { it.countryCode == savedCountryCode }
            ?: SupportedCountries.DEFAULT

        setContent {
            val darkTheme = isSystemInDarkTheme()
            var selectedCountry by remember { mutableStateOf(initialCountry) }
            var activePhone by remember { mutableStateOf(savedPhone) }
            var activeOtp by remember { mutableStateOf("") }

            MaterialTheme(
                colorScheme = if (darkTheme) DarkThemeColors else LightThemeColors
            ) {
                val navController = rememberNavController()
                val startDestination = if (isAuth) "home" else "auth_phone"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // Screen 0A: Phone Number Entry & Country Selector
                    composable("auth_phone") {
                        AuthPhoneScreen(
                            onOtpDispatched = { phone, otp, country ->
                                activePhone = phone
                                activeOtp = otp
                                selectedCountry = country
                                navController.navigate("auth_otp")
                            },
                            onSkipGuest = { country ->
                                selectedCountry = country
                                activePhone = "Guest"
                                securePrefs.edit()
                                    .putBoolean("is_authenticated", true)
                                    .putString("country_iso", country.countryCode)
                                    .putString("user_phone", "Guest")
                                    .apply()

                                navController.navigate("home") {
                                    popUpTo("auth_phone") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Screen 0B: OTP Verification Screen
                    composable("auth_otp") {
                        AuthOtpScreen(
                            fullPhoneNumber = activePhone,
                            initialOtp = activeOtp,
                            onVerified = {
                                securePrefs.edit()
                                    .putBoolean("is_authenticated", true)
                                    .putString("country_iso", selectedCountry.countryCode)
                                    .putString("user_phone", activePhone)
                                    .apply()

                                navController.navigate("home") {
                                    popUpTo("auth_phone") { inclusive = true }
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Screen 1: Home Dashboard (0-based Ledger & Selected Currency)
                    composable("home") {
                        HomeScreen(
                            context = this@MainActivity,
                            userCountry = selectedCountry,
                            userPhone = activePhone,
                            onSignOut = {
                                securePrefs.edit().clear().apply()
                                activePhone = ""
                                activeOtp = ""
                                navController.navigate("auth_phone") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Material Design 3 Emerald Theme Colors
private val DarkThemeColors = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

private val LightThemeColors = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1)
)
