package com.gigverse.app.ui.admin

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.GigverseLocalization
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenNav.Dashboard)
    }

    val currentLang by viewModel.currentLanguage.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allDisputes by viewModel.allDisputes.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Disputes, 1: Users, 2: Projects

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Platform KPI Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = VioletPrimary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Projects", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${allProjects.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CyanAccent.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Verified Users", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${allUsers.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = LivePulseRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Open Disputes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${allDisputes.count { it.status == "OPEN" }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LivePulseRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Disputes (${allDisputes.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Users & KYC (${allUsers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Projects (${allProjects.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // DISPUTES RESOLUTION VIEW
                    if (allDisputes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No disputes reported. Platform health is 100%.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allDisputes) { disp ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(disp.reason, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Surface(
                                                color = if (disp.status == "OPEN") LivePulseRed.copy(alpha = 0.15f) else EmeraldTrust.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    disp.status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (disp.status == "OPEN") LivePulseRed else EmeraldTrust,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Project: ${disp.projectTitle}", fontSize = 12.sp, color = VioletPrimary, fontWeight = FontWeight.Medium)
                                        Text("Filed by: ${disp.raisedByUserName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(disp.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                                        if (disp.resolutionNotes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Resolution: ${disp.resolutionNotes}", fontSize = 11.sp, color = EmeraldTrust, fontWeight = FontWeight.SemiBold)
                                        }

                                        if (disp.status == "OPEN") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        viewModel.resolveDispute(disp.id, "Escrow mediated by Admin: Milestone deliverable accepted.")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve Deliverable & Resolve", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.resolveDispute(disp.id, "Escrow refunded to client per revision limit terms.")
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Refund Escrow", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // USER & KYC GOVERNANCE VIEW
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allUsers) { user ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (user.role == "CLIENT") VioletPrimary.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (user.role == "CLIENT") Icons.Default.Business else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.role == "CLIENT") VioletPrimary else CyanAccent
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (user.isGovIdVerified) {
                                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = EmeraldTrust, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text("${user.role} • ${user.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (user.role == "FREELANCER") {
                                            Text("Trust Score: ${user.trustScore}% • Rate: $${user.hourlyRate}/hr", fontSize = 11.sp, color = CyanAccent)
                                        } else {
                                            Text("Company: ${user.companyName.ifBlank { "Independent" }}", fontSize = 11.sp, color = VioletPrimary)
                                        }
                                    }

                                    Surface(
                                        color = if (user.isGovIdVerified) EmeraldTrust.copy(alpha = 0.15f) else LivePulseRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            if (user.isGovIdVerified) "KYC PASS" else "PENDING",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.isGovIdVerified) EmeraldTrust else LivePulseRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // PROJECTS MONITOR VIEW
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allProjects) { proj ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.navigateTo(ScreenNav.LiveTracker(proj.id))
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(proj.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                        Surface(
                                            color = VioletPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "Stage ${proj.milestoneStage}/5",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VioletPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Client: ${proj.clientName} • Budget: $${proj.budget}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Assigned: ${proj.assignedFreelancerName ?: "Open for Bids"}", fontSize = 11.sp, color = CyanAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
