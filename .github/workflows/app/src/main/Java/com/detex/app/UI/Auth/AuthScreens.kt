package com.detex.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detex.app.data.model.CountryCurrency
import com.detex.app.data.model.SupportedCountries
import com.detex.app.service.OtpAuthManager
import com.detex.app.service.VerificationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onDismiss: () -> Unit,
    onSelect: (CountryCurrency) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        SupportedCountries.list.filter {
            it.countryName.contains(searchQuery, ignoreCase = true) ||
            it.dialCode.contains(searchQuery) ||
            it.currencyCode.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Select Country & Currency", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search country, dial code, or currency...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                items(filteredCountries) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.flagEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(item.countryName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${item.dialCode} • ${item.currencyCode} (${item.currencySymbol})", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Text(item.dialCode, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 14.sp)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun AuthPhoneScreen(
    onOtpDispatched: (phone: String, generatedOtp: String, selectedCountry: CountryCurrency) -> Unit,
    onSkipGuest: (selectedCountry: CountryCurrency) -> Unit
) {
    var selectedCountry by remember { mutableStateOf(SupportedCountries.DEFAULT) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var rawPhoneNumber by remember { mutableStateOf("") }
    val cleanDigits = rawPhoneNumber.filter { it.isDigit() }
    val isValidPhone = cleanDigits.length >= (selectedCountry.phoneLength - 2)

    if (showCountryPicker) {
        CountryPickerSheet(
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                selectedCountry = country
                showCountryPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF10B981), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("DX", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Welcome to Det-Ex", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Privacy-first expense intelligence with 256-bit AES encryption.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("PHONE NUMBER", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rawPhoneNumber,
                onValueChange = { if (it.length <= 15) rawPhoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Mobile number") },
                leadingIcon = {
                    Row(
                        modifier = Modifier
                            .clickable { showCountryPicker = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCountry.flagEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedCountry.dialCode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Currency: ${selectedCountry.currencyCode} (${selectedCountry.currencySymbol})", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color(0xFF064E3B).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF064E3B))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hardware-backed AES-256 local database encryption.", fontSize = 11.sp, color = Color(0xFF6EE7B7))
                }
            }
        }

        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Button(
                onClick = {
                    val fullNumber = "${selectedCountry.dialCode}$cleanDigits"
                    val newOtp = OtpAuthManager.generateNewOtp(fullNumber)
                    onOtpDispatched(fullNumber, newOtp, selectedCountry)
                },
                enabled = isValidPhone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Get OTP", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
            TextButton(
                onClick = { onSkipGuest(selectedCountry) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Text("Continue as Offline Guest", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AuthOtpScreen(
    fullPhoneNumber: String,
    initialOtp: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var enteredOtp by remember { mutableStateOf("") }
    var currentOtpDisplay by remember { mutableStateOf(initialOtp) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verification Code", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Code generated for $fullPhoneNumber", color = Color(0xFF10B981), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = enteredOtp,
                onValueChange = {
                    if (it.length <= 6) {
                        enteredOtp = it.filter { c -> c.isDigit() }
                        errorMessage = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter 6-digit OTP") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Simulated Incoming SMS", fontSize = 11.sp, color = Color.Gray)
                        Text("Det-Ex OTP: $currentOtpDisplay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { enteredOtp = currentOtpDisplay },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Auto-fill", fontSize = 11.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Didn't receive code?", fontSize = 12.sp, color = Color.Gray)
                TextButton(
                    onClick = {
                        val newCode = OtpAuthManager.generateNewOtp(fullPhoneNumber)
                        currentOtpDisplay = newCode
                        enteredOtp = ""
                        errorMessage = null
                        Toast.makeText(context, "New OTP generated", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Generate New OTP", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = {
                when (val result = OtpAuthManager.verifyOtp(fullPhoneNumber, enteredOtp)) {
                    is VerificationResult.Success -> {
                        Toast.makeText(context, "Authentication successful!", Toast.LENGTH_SHORT).show()
                        onVerified()
                    }
                    is VerificationResult.Incorrect -> errorMessage = "Incorrect OTP. ${result.remaining} attempts remaining."
                    is VerificationResult.Expired -> errorMessage = "OTP has expired. Please generate a new code."
                    is VerificationResult.MaxAttemptsExceeded -> errorMessage = "Max attempts exceeded. Generate new code."
                    is VerificationResult.NotFound -> errorMessage = "No active session. Re-enter phone."
                }
            },
            enabled = enteredOtp.length == 6,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            Text("Verify & Enter Det-Ex", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.CheckCircle, contentDescription = null)
        }
    }
}
