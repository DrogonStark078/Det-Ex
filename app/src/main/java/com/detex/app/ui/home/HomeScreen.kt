package com.detex.app.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detex.app.data.local.AppDatabase
import com.detex.app.data.local.TransactionEntity
import com.detex.app.data.local.TransactionType
import com.detex.app.data.model.CountryCurrency
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    context: Context,
    userCountry: CountryCurrency,
    userPhone: String,
    onSignOut: () -> Unit
) {
    val db = remember { AppDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    val startOfMonth = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
        }.timeInMillis
    }

    val monthlySpent by db.transactionDao().getTotalSpentSince(startOfMonth).collectAsState(initial = 0.0)
    val monthlyIncome by db.transactionDao().getTotalIncomeSince(startOfMonth).collectAsState(initial = 0.0)
    val transactions by db.transactionDao().getAllTransactions().collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        db.transactionDao().insert(
                            TransactionEntity(
                                merchant = "Local Store",
                                amount = 25.0,
                                type = TransactionType.DEBIT,
                                category = "Shopping",
                                accountInfo = "Card"
                            )
                        )
                    }
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(Color(0xFF10B981), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("DX", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Det-Ex", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "${userCountry.flagEmoji} ${userCountry.currencyCode} (${userCountry.currencySymbol}) • AES-256",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = Color.Gray)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Total Month Spent", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            userCountry.formatAmount(monthlySpent),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Income", fontSize = 11.sp, color = Color.Gray)
                                Text(userCountry.formatAmount(monthlyIncome), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                            }
                            Column {
                                Text("Transactions", fontSize = 11.sp, color = Color.Gray)
                                Text("${transactions.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text("Savings", fontSize = 11.sp, color = Color.Gray)
                                Text(userCountry.formatAmount(monthlyIncome - monthlySpent), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("0 Expenses Recorded", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Database encrypted with AES-256. Tracking amounts in ${userCountry.currencyCode} (${userCountry.currencySymbol}).",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.merchant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${tx.category} • ${tx.accountInfo}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                (if (tx.type == TransactionType.CREDIT) "+ " else "- ") + userCountry.formatAmount(tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (tx.type == TransactionType.CREDIT) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
