package com.gigverse.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gigverse.app.ai.GigverseAiEngine
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.ui.components.TrustScoreBadge
import com.gigverse.app.ui.components.VerificationBadgesRow
import com.gigverse.app.ui.components.WorkStatusBadge
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (currentUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not loaded")
            }
        } else {
            val u = currentUser!!

            var updatedAvatarUri by remember(u.avatarUrl) { mutableStateOf(u.avatarUrl) }
            var updatedGovIdDocUri by remember(u.govIdDocUrl) { mutableStateOf(u.govIdDocUrl) }
            var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    updatedAvatarUri = uri.toString()
                }
            }

            val idProofPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    updatedGovIdDocUri = uri.toString()
                }
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar & Role Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, VioletPrimary, CircleShape)
                                .background(Brush.linearGradient(listOf(VioletPrimary, CyanAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (u.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = u.avatarUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = u.fullName.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = u.fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (u.role == "CLIENT") "${u.companyName} • ${u.industry}" else u.headline,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkStatusBadge(status = u.workStatus)
                            TrustScoreBadge(score = u.trustScore, userEntity = u)
                        }
                    }
                }

                // Switch Role / Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = VioletPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Switch Active Role", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Current: ${u.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Button(
                                onClick = {
                                    if (u.role == "CLIENT") {
                                        viewModel.switchUserRole(UserRole.FREELANCER)
                                    } else {
                                        viewModel.switchUserRole(UserRole.CLIENT)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (u.role == "CLIENT") "Switch to Freelancer" else "Switch to Client", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Dedicated Freelancer Profile Photo & ID Proof Update Section
                if (u.role == "FREELANCER") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldTrust.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTrust.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldTrust)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Update Profile Photo & Govt ID Proof",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = EmeraldTrust
                                    )
                                    Text(
                                        text = "Keep your biometric profile & government credentials current to maintain your 95%+ AI Trust badge.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Profile Photo Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, VioletPrimary, CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = updatedAvatarUri.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300" },
                                            contentDescription = "Updated Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Profile Photo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Attached", fontSize = 11.sp, color = EmeraldTrust)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("update_profile_photo_button")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Photo", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ID Proof Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.5.dp, EmeraldTrust, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = updatedGovIdDocUri.ifBlank { "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=400" },
                                            contentDescription = "Updated ID Proof",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Govt ID Proof Document", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${u.govIdType} • Verified", fontSize = 11.sp, color = EmeraldTrust)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        idProofPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("update_id_proof_button")
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload ID", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.updateFreelancerProfileDocs(
                                        newAvatarUrl = updatedAvatarUri,
                                        newGovIdDocUrl = updatedGovIdDocUri
                                    )
                                    viewModel.navigateTo(ScreenNav.Dashboard)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_profile_update_and_redirect_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Updates & Return to Dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Verified Credentials
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Verification & Social IDs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        VerificationBadgesRow(user = u)

                        Spacer(modifier = Modifier.height(14.dp))

                        CredentialInfoRow(
                            icon = Icons.Default.Fingerprint,
                            label = "Govt ID Proof",
                            value = "${u.govIdType} (${u.govIdNumber.ifBlank { "Verified" }})"
                        )
                        CredentialInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = u.email
                        )
                        CredentialInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Mobile",
                            value = u.phone
                        )
                        CredentialInfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Location",
                            value = "${u.locationDistrict}, ${u.locationState}, ${u.locationCountry}"
                        )
                        if (u.linkedInUrl.isNotBlank()) {
                            CredentialInfoRow(
                                icon = Icons.Default.Link,
                                label = "LinkedIn",
                                value = u.linkedInUrl
                            )
                        }
                        if (u.githubUrl.isNotBlank()) {
                            CredentialInfoRow(
                                icon = Icons.Default.Link,
                                label = "GitHub",
                                value = u.githubUrl
                            )
                        }
                        if (u.portfolioWebsite.isNotBlank()) {
                            CredentialInfoRow(
                                icon = Icons.Default.Link,
                                label = "Portfolio",
                                value = u.portfolioWebsite
                            )
                        }
                    }
                }

                // AI Trust Breakdown
                val breakdown = GigverseAiEngine.calculateTrustScore(u)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldTrust.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTrust.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI Trust Score Breakdown", fontWeight = FontWeight.Bold, color = EmeraldTrust)
                            Text("${breakdown.totalScore}%", fontWeight = FontWeight.Black, fontSize = 20.sp, color = EmeraldTrust)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = breakdown.summaryInsight,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // App Preferences & Settings Card
                val currentLang by viewModel.currentLanguage.collectAsState()
                val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("App Preferences & Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Language Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Platform Language", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (currentLang == "ta") "தமிழ் மொழி (Tamil)" else "English (US)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleLanguage() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (currentLang == "ta") "Switch to English" else "Switch to தமிழ்", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Appearance Theme Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Display Theme", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (isDarkTheme) "Dark Modern Interface" else "Light Clean Interface", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleTheme() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isDarkTheme) "Switch to Light" else "Switch to Dark", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Admin and Production Hub Action Cards
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Platform Governance & Engineering",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = VioletPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(ScreenNav.AdminDashboard) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Admin Panel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { viewModel.navigateTo(ScreenNav.ProductionHub) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Prod Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CredentialInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "$label: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProjectsScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active Contracts, 1: Floated/Open, 2: Completed
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val myProjects = allProjects.filter { p ->
        p.clientId == currentUser?.id || p.assignedFreelancerId == currentUser?.id
    }

    val activeList = myProjects.filter { it.status == "IN_PROGRESS" }
    val openList = myProjects.filter { it.status == "OPEN" || it.status == "BID_ROOM_ACTIVE" }
    val completedList = myProjects.filter { it.status == "COMPLETED" }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active (${activeList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Floated (${openList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Completed (${completedList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            val listToShow = when (selectedTab) {
                0 -> activeList
                1 -> openList
                else -> completedList
            }

            if (listToShow.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No projects in this category yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listToShow) { p ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (p.status == "IN_PROGRESS" || p.status == "COMPLETED") {
                                        viewModel.navigateTo(ScreenNav.LiveTracker(p.id))
                                    } else {
                                        viewModel.navigateTo(ScreenNav.ProjectDetail(p.id))
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = p.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = when (p.status) {
                                            "IN_PROGRESS" -> VioletPrimary.copy(alpha = 0.15f)
                                            "COMPLETED" -> EmeraldTrust.copy(alpha = 0.15f)
                                            else -> CyanAccent.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = p.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (p.status) {
                                                "IN_PROGRESS" -> VioletPrimary
                                                "COMPLETED" -> EmeraldTrust
                                                else -> CyanAccent
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Budget: $${p.budget.toInt()} • Freelancer: ${p.assignedFreelancerName ?: "Pending"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (p.status == "IN_PROGRESS") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📦 Stage ${p.milestoneStage}/5: ${p.stageNotes}",
                                            fontSize = 11.sp,
                                            color = VioletPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showDeleteConfirmationDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("delete_account_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                ) {
                    Text("Delete Account", color = Color.White)
                }

                if (showDeleteConfirmationDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteConfirmationDialog = false },
                        title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Are you sure you want to permanently delete your account and personal profile data? This action cannot be undone.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteConfirmationDialog = false
                                    viewModel.deleteAccount()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                            ) {
                                Text("Permanently Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDeleteConfirmationDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                OutlinedButton(
                    onClick = { /* Moderation placeholder */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Report User / Content", color = LivePulseRed)
                }
            }
        }
    }
}
