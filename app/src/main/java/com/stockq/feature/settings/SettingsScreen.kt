package com.stockq.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockq.R
import com.stockq.core.ui.components.StockQTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val logoRes = if (isDark) R.drawable.stockq_logo_dark else R.drawable.stockq_logo_light
    val context = LocalContext.current
    val installedVersion = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
    }.getOrDefault("Unknown")

    Scaffold(
        topBar = {
            StockQTopBar(
                title = stringResource(R.string.title_settings),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Store & Business Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = formState.storeName,
                        onValueChange = { viewModel.onStoreNameChange(it) },
                        label = { Text("Store / Company Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.ownerName,
                        onValueChange = { viewModel.onOwnerNameChange(it) },
                        label = { Text("Owner / Contact Person") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.phone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.gstin,
                        onValueChange = { viewModel.onGstinChange(it) },
                        label = { Text("GSTIN / Tax ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.address,
                        onValueChange = { viewModel.onAddressChange(it) },
                        label = { Text("Business Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.currencySymbol,
                        onValueChange = { viewModel.onCurrencyChange(it) },
                        label = { Text("Currency Symbol") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    formState.message?.let { msg ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(msg, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Button(
                        onClick = { viewModel.saveProfile() },
                        enabled = !formState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (formState.isSaving) "Saving..." else "Save Profile")
                    }
                }
            }

            // Backup & Restore Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBackup() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = stringResource(R.string.title_backup_restore),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.title_backup_restore),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Export or restore offline SQLite database backup file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // About StockQ & Privacy Brand Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "StockQ Brand Logo",
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your business. Your data. Your control.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Creator: SuBZeRo", style = MaterialTheme.typography.bodySmall)
                        Text("App name: StockQ", style = MaterialTheme.typography.bodySmall)
                        Text("Installed version: $installedVersion", style = MaterialTheme.typography.bodySmall)
                        Text("Application ID: ${context.packageName}", style = MaterialTheme.typography.bodySmall)
                        Text("Gradle namespace: com.stockq", style = MaterialTheme.typography.bodySmall)
                        Text("Repository: github.com/Dr-SuBZeRo/StockQ", style = MaterialTheme.typography.bodySmall)
                        Text("Your business data is stored locally on this device. StockQ has no account, ads, analytics, or cloud sync.", style = MaterialTheme.typography.bodySmall)
                        Text("Updates are manual. Open the release page and download/install an update yourself.", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Dr-SuBZeRo/StockQ/releases"))
                                runCatching { context.startActivity(intent) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Check for Updates") }
                    }
                }
            }
        }
    }
}
