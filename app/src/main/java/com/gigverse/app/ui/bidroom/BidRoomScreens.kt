package com.gigverse.app.ui.bidroom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.gigverse.app.ai.GigverseAiEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.data.model.BidEntity
import com.gigverse.app.ui.components.TrustScoreBadge
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBidRoomScreen(
    projectId: String,
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val projectStream = remember(projectId) { viewModel.getProjectStream(projectId) }
    val project by projectStream.collectAsState()

    val bidsStream = remember(projectId) { viewModel.getBidsStream(projectId) }
    val bids by bidsStream.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val isClient = currentUser?.role == "CLIENT"

    var bidAmountStr by remember { mutableStateOf("") }
    var durationDaysStr by remember { mutableStateOf("") }
    var pitch by remember { mutableStateOf("") }

    // Dialog and Gemini AI States
    var showPostProjectDialog by remember { mutableStateOf(false) }
    var newProjTitle by remember { mutableStateOf("") }
    var newProjRequirements by remember { mutableStateOf("") }
    var newProjBudget by remember { mutableStateOf("2000") }
    var newProjDuration by remember { mutableStateOf("14") }
    var newProjSkills by remember { mutableStateOf("Android, Kotlin, Jetpack Compose, Gemini AI") }
    var newProjLocation by remember { mutableStateOf("All Locations") }

    var isEnhancingWithGemini by remember { mutableStateOf(false) }
    var isGeneratingPitchWithGemini by remember { mutableStateOf(false) }
    var isAnalyzingRiskWithGemini by remember { mutableStateOf(false) }
    var geminiRiskAnalysisResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val transition = rememberInfiniteTransition(label = "auction_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // DIALOG: Post Project in Bidding Room
    if (showPostProjectDialog) {
        AlertDialog(
            onDismissRequest = { showPostProjectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, tint = LivePulseRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Project in Bidding Room", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Specify your project requirements to launch an active reverse-auction on the live bidding floor.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newProjTitle,
                        onValueChange = { newProjTitle = it },
                        label = { Text("Project Title") },
                        placeholder = { Text("e.g. Enterprise Realtime App") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newProjRequirements,
                        onValueChange = { newProjRequirements = it },
                        label = { Text("Project Requirements & Deliverables") },
                        placeholder = { Text("Give details on architecture, UI and core milestones...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Gemini AI Enhancement Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isEnhancingWithGemini = true
                                val enhanced = viewModel.enhanceRequirementsWithGemini(
                                    if (newProjRequirements.isBlank()) {
                                        if (newProjTitle.isNotBlank()) newProjTitle else "Build mobile application with modern architecture, database persistence, and live auction capabilities."
                                    } else newProjRequirements
                                )
                                newProjRequirements = enhanced
                                isEnhancingWithGemini = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("gemini_enhance_requirements_button"),
                        enabled = !isEnhancingWithGemini
                    ) {
                        if (isEnhancingWithGemini) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI Refining Scope...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enhance Requirements with Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newProjBudget,
                            onValueChange = { newProjBudget = it },
                            label = { Text("Budget ($)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = newProjDuration,
                            onValueChange = { newProjDuration = it },
                            label = { Text("Days") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = newProjSkills,
                        onValueChange = { newProjSkills = it },
                        label = { Text("Required Skills") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newProjLocation,
                        onValueChange = { newProjLocation = it },
                        label = { Text("Allowed Location / Region") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val budget = newProjBudget.toDoubleOrNull() ?: 2000.0
                        val days = newProjDuration.toIntOrNull() ?: 14
                        viewModel.postNewProject(
                            title = if (newProjTitle.isBlank()) "Live Bidding Contract: Enterprise Mobile System" else newProjTitle,
                            description = if (newProjRequirements.isBlank()) "Full-lifecycle app development delivered to client specifications." else newProjRequirements,
                            category = "Mobile Dev",
                            requiredSkills = newProjSkills,
                            budget = budget,
                            budgetType = "FIXED",
                            deadlineDays = days,
                            scope = "Medium",
                            enableBidRoom = true,
                            bidRoomAllowedLocation = newProjLocation
                        )
                        showPostProjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("launch_project_in_bid_room_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Project into Bid Room", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPostProjectDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bid room project not found")
            }
        } else {
            val p = project!!
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Action: Post Project Directly in Bidding Room
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LivePulseRed.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LivePulseRed.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, tint = LivePulseRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Post a Project in Bidding Room", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Give requirements to launch your own real-time reverse auction.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Button(
                                onClick = { showPostProjectDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("open_post_project_dialog_button")
                            ) {
                                Text("Post Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Live Header Card with Regional Filter
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LivePulseRed.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(LivePulseRed.copy(alpha = 0.12f), Color.Transparent)
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(LivePulseRed.copy(alpha = pulseAlpha))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AUCTION ACTIVE",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = LivePulseRed,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ending Soon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = p.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = VioletPrimary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Filter: ${p.bidRoomAllowedLocation}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VioletPrimary
                                            )
                                        }
                                    }

                                    Surface(
                                        color = EmeraldTrust.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Ceiling: $${p.budget.toInt()}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldTrust
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Gemini AI Project Risk Analyzer Action
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isAnalyzingRiskWithGemini = true
                                            val analysis = viewModel.analyzeProjectRiskWithGemini(
                                                requirements = p.description,
                                                budget = p.budget,
                                                deadlineDays = p.deadlineDays
                                            )
                                            geminiRiskAnalysisResult = analysis
                                            isAnalyzingRiskWithGemini = false
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("analyze_risk_gemini_button"),
                                    enabled = !isAnalyzingRiskWithGemini
                                ) {
                                    if (isAnalyzingRiskWithGemini) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini AI Analyzing Contract...", fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = VioletPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Analyze Contract Risk with Gemini AI", fontSize = 11.sp)
                                    }
                                }

                                if (geminiRiskAnalysisResult != null) {
                                    val r = geminiRiskAnalysisResult ?: ""
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = EmeraldTrust.copy(alpha = 0.08f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Gemini Safety Index", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldTrust)
                                                Text("AI Feasibility Rating", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldTrust)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(r, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bid Submission Section (For Freelancer)
                if (!isClient) {
                    item {
                        val isCaliforniaMatch = currentUser?.locationState?.contains(p.bidRoomAllowedLocation, ignoreCase = true) == true ||
                                p.bidRoomAllowedLocation.contains("All", ignoreCase = true)
                        val isOnWork = currentUser?.workStatus == "ON_WORK"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Place Competitive Bid",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    // Gemini AI Pitch Generator Button
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isGeneratingPitchWithGemini = true
                                                val amt = bidAmountStr.toDoubleOrNull() ?: (p.budget * 0.9)
                                                val generated = viewModel.generateBidPitchWithGemini(
                                                    projectTitle = p.title,
                                                    requirements = p.description,
                                                    bidAmount = amt
                                                )
                                                pitch = generated
                                                isGeneratingPitchWithGemini = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("gemini_draft_pitch_button"),
                                        enabled = !isGeneratingPitchWithGemini
                                    ) {
                                        if (isGeneratingPitchWithGemini) {
                                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = VioletPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gemini Pitch", fontSize = 11.sp, color = VioletPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (!isCaliforniaMatch) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ Notice: This bid room is restricted to '${p.bidRoomAllowedLocation}'. You are registered in '${currentUser?.locationState}'.",
                                        fontSize = 12.sp,
                                        color = LivePulseRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = bidAmountStr,
                                        onValueChange = { bidAmountStr = it },
                                        label = { Text("Your Bid ($ USD)") },
                                        placeholder = { Text("${(p.budget * 0.9).toInt()}") },
                                        modifier = Modifier.weight(1f).testTag("live_bid_amount_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = durationDaysStr,
                                        onValueChange = { durationDaysStr = it },
                                        label = { Text("Days to Deliver") },
                                        placeholder = { Text("7") },
                                        modifier = Modifier.weight(1f).testTag("live_bid_duration_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pitch,
                                    onValueChange = { pitch = it },
                                    label = { Text("Pitch & Why You're the Best Fit") },
                                    modifier = Modifier.fillMaxWidth().testTag("live_bid_pitch_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val amt = bidAmountStr.toDoubleOrNull() ?: (p.budget * 0.9)
                                        val days = durationDaysStr.toIntOrNull() ?: 7
                                        viewModel.placeBidInBidRoom(
                                            projectId = p.id,
                                            bidAmount = amt,
                                            durationDays = days,
                                            pitch = if (pitch.isBlank()) "Immediate turnaround from verified local talent." else pitch
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_live_bid_button"),
                                    enabled = !isOnWork,
                                    colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isOnWork) "Unavailable: Currently On Work" else "Submit Live Auction Bid",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Live Bids Floor (${bids.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(bids) { bid ->
                    BidFloorItemCard(
                        bid = bid,
                        isClient = isClient,
                        onAward = {
                            viewModel.awardBidRoomWinner(p.id, bid)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun BidFloorItemCard(
    bid: BidEntity,
    isClient: Boolean,
    onAward: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bid.freelancerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = bid.freelancerLocation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${bid.bidAmount.toInt()}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldTrust
                    )
                    Text(
                        text = "${bid.durationDays} Days",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bid.pitch,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrustScoreBadge(score = bid.freelancerTrustScore)

                if (isClient) {
                    Button(
                        onClick = onAward,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept & Hire Direct", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
