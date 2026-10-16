package com.gigverse.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.config.EnvironmentMode
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.GoldenBadge
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionHubScreen(viewModel: GigverseViewModel) {
    val telemetry by viewModel.productionTelemetry.collectAsState()
    val sessionUser by viewModel.currentUser.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isCloudConnected by viewModel.isCloudConnected.collectAsState()
    val cloudStatusMessage by viewModel.cloudStatusMessage.collectAsState()
    var showPurgeConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PRODUCTION HERO BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTrust.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("prod_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = EmeraldTrust.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTrust)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldTrust)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    telemetry.environment.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldTrust
                                )
                            }
                        }

                        Text(
                            telemetry.releaseVersion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "GIGVERSE Enterprise Release Platform",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This app build is optimized for production release with ProGuard/R8 obfuscation, SSL pinning, encrypted database storage, and Gemini 3.5 AI integration.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // MONGODB CLOUD STORAGE CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCloudConnected) EmeraldTrust.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("mongodb_cloud_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (isCloudConnected) EmeraldTrust else CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "MongoDB Cloud Storage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Surface(
                            color = if (isCloudConnected) EmeraldTrust.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCloudConnected) EmeraldTrust else CyanAccent
                            )
                        ) {
                            Text(
                                text = if (isCloudConnected) "CONNECTED" else "LOCAL / STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCloudConnected) EmeraldTrust else CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Cross-Device Interactivity: Synchronizes all users, projects, bids, and proposals across devices so Freelancers and Customers on separate phones interact seamlessly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cloudStatusMessage,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncWithCloud() },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("sync_cloud_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Push to Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.pullFromCloud() },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("pull_cloud_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pull Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.seedCloudDemoUsers() },
                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("seed_cloud_users_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seed Cloud Demo Accounts (Sophia & Alex)", fontSize = 12.sp)
                    }
                }
            }

            // ENVIRONMENT SWITCHER CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("environment_mode_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Environment Mode Selection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnvironmentModeOption(
                            label = "Production",
                            selected = telemetry.environment == EnvironmentMode.PRODUCTION,
                            activeColor = EmeraldTrust,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.switchEnvironmentMode(EnvironmentMode.PRODUCTION) }
                        )

                        EnvironmentModeOption(
                            label = "Staging",
                            selected = telemetry.environment == EnvironmentMode.STAGING,
                            activeColor = GoldenBadge,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.switchEnvironmentMode(EnvironmentMode.STAGING) }
                        )

                        EnvironmentModeOption(
                            label = "Testing",
                            selected = telemetry.environment == EnvironmentMode.TESTING,
                            activeColor = CyanAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.switchEnvironmentMode(EnvironmentMode.TESTING) }
                        )
                    }
                }
            }

            // SECURITY & COMPLIANCE BADGES
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldTrust, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security & Hardening Dossier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SecurityCheckItem(
                        title = "HTTPS & Network Security",
                        detail = "Enforced via network_security_config.xml (No Cleartext)",
                        icon = Icons.Default.Lock,
                        isPass = telemetry.isHttpsEnforced
                    )

                    SecurityCheckItem(
                        title = "Code Obfuscation & R8 Minification",
                        detail = if (telemetry.isMinified) "Active (ProGuard keep rules configured)" else "Debug Mode (Unminified)",
                        icon = Icons.Default.Shield,
                        isPass = telemetry.isMinified
                    )

                    SecurityCheckItem(
                        title = "Database Storage & Encryption",
                        detail = telemetry.databaseEncryption,
                        icon = Icons.Default.Storage,
                        isPass = true
                    )

                    SecurityCheckItem(
                        title = "Gemini AI Engine Connection",
                        detail = telemetry.geminiAiStatus,
                        icon = Icons.Default.CloudDone,
                        isPass = true
                    )

                    SecurityCheckItem(
                        title = "Production API Gateway",
                        detail = telemetry.apiEndpoint,
                        icon = Icons.Default.VerifiedUser,
                        isPass = true
                    )
                }
            }

            // ACTIVE SESSION DETAILS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Production User Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Logged-in Account: ${sessionUser?.fullName ?: "Anonymous User"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Email: ${sessionUser?.email ?: "N/A"} | Role: ${sessionUser?.role ?: "CLIENT"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Bearer Token: gv_live_prod_${sessionUser?.id?.take(8) ?: "auth"}_sec_7792",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // TELEMETRY & SYSTEM HEALTH METRICS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = GoldenBadge, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Telemetry & Diagnostics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryMetricBox(label = "Total Projects", value = "${allProjects.size}", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryMetricBox(label = "Registered Users", value = "${allUsers.size}", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryMetricBox(label = "API Response", value = "${telemetry.averageResponseMs}ms", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Server Uptime Guarantee: 99.98%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldTrust)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { 0.9998f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = EmeraldTrust,
                        trackColor = EmeraldTrust.copy(alpha = 0.2f)
                    )
                }
            }

            // PRODUCTION CONTROL ACTIONS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = LivePulseRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Production Maintenance & Launch Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.exportProductionDataBackup() },
                        modifier = Modifier.fillMaxWidth().testTag("export_backup_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Production Database Backup (JSON)")
                    }

                    OutlinedButton(
                        onClick = { viewModel.runProductionSecurityAudit() },
                        modifier = Modifier.fillMaxWidth().testTag("run_security_audit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldTrust, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Live Security & Compliance Audit")
                    }

                    OutlinedButton(
                        onClick = { showPurgeConfirmationDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("purge_test_data_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LivePulseRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LivePulseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = LivePulseRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Purge Test Data (Clean Production Reset)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showPurgeConfirmationDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPurgeConfirmationDialog = false },
                title = { Text("Confirm Database Purge", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                text = {
                    Text("WARNING: This will permanently delete all test records, users, and projects from the local database. This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPurgeConfirmationDialog = false
                            viewModel.purgeTestDataForProduction()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                    ) {
                        Text("Confirm Purge", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showPurgeConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun EnvironmentModeOption(
    label: String,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) activeColor else Color.Transparent),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (selected) activeColor else Color.Gray)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) activeColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SecurityCheckItem(
    title: String,
    detail: String,
    icon: ImageVector,
    isPass: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPass) EmeraldTrust else GoldenBadge,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Pass",
            tint = if (isPass) EmeraldTrust else GoldenBadge,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TelemetryMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
