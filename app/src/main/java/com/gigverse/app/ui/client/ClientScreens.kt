package com.gigverse.app.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.gigverse.app.ai.GigverseAiEngine
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.ui.components.DurationGraphCard
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
fun ClientDashboardScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allFreelancers by viewModel.allFreelancers.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val aiSmartFilter by viewModel.aiSmartFilterEnabled.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val locationFilter by viewModel.locationFilter.collectAsState()

    val myActiveContracts = allProjects.filter { it.clientId == currentUser?.id && it.status == "IN_PROGRESS" }
    val myFloatedProjects = allProjects.filter { it.clientId == currentUser?.id && (it.status == "OPEN" || it.status == "BID_ROOM_ACTIVE") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(ScreenNav.PostProject) },
                containerColor = VioletPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("float_new_project_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Post Project")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Float Project", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Client Hero Banner
                ClientHeroCard(
                    client = currentUser,
                    activeContractsCount = myActiveContracts.size,
                    floatedCount = myFloatedProjects.size,
                    onHostBidRoom = { viewModel.navigateTo(ScreenNav.PostProject) }
                )
            }

            // Dedicated AI Project & Hiring Advisor Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(ScreenNav.UserSpecificCopilot) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VioletPrimary.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VioletPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Project & Hiring Advisor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VioletPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = VioletPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text("Gemini 3.5", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VioletPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("Tuned for ${currentUser?.companyName ?: "your business"} in ${currentUser?.industry ?: "Software"}. Tap for SOW & budget advice!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Active In-Progress Contracts (Parcel Tracker Preview)
            if (myActiveContracts.isNotEmpty()) {
                item {
                    Text(
                        text = "🚀 Active In-Progress Contracts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(myActiveContracts) { project ->
                    ActiveContractParcelSummaryCard(
                        project = project,
                        onClick = { viewModel.navigateTo(ScreenNav.LiveTracker(project.id)) }
                    )
                }
            }

            // Quick Filters Header & AI Smart Match Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vetted Talent Network",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Surface(
                        color = if (aiSmartFilter) CyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleAiSmartFilter() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (aiSmartFilter) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Smart Match",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (aiSmartFilter) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Category Chips
            item {
                val categories = listOf("All", "Mobile", "UI/UX", "AI & ML", "Cloud", "Web3")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Freelancer Directory Cards
            val filteredFreelancers = allFreelancers.filter { freelancer ->
                val matchesCategory = selectedCategory == "All" || freelancer.skills.contains(selectedCategory, ignoreCase = true)
                val matchesLocation = locationFilter == "All" || freelancer.locationState.contains(locationFilter, ignoreCase = true) || freelancer.locationCountry.contains(locationFilter, ignoreCase = true)
                matchesCategory && matchesLocation
            }.let { list ->
                if (aiSmartFilter) {
                    list.sortedByDescending { it.trustScore }
                } else {
                    list
                }
            }

            items(filteredFreelancers) { freelancer ->
                FreelancerTalentCard(
                    freelancer = freelancer,
                    onContact = {
                        // Open proposal/detail
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp)) // Space for FAB
            }
        }
    }
}

@Composable
fun ClientHeroCard(
    client: UserEntity?,
    activeContractsCount: Int,
    floatedCount: Int,
    onHostBidRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(VioletPrimary.copy(alpha = 0.15f), Color.Transparent),
                        radius = 450f
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Client Portal • ${client?.companyName ?: "NovaTech"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = client?.fullName ?: "Sophia Vance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = EmeraldTrust.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Escrow Active",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldTrust
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Active Work",
                        value = "$activeContractsCount Project${if (activeContractsCount != 1) "s" else ""}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Floated Gigs",
                        value = "$floatedCount Open",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Total Spent",
                        value = "$${client?.totalSpent?.toInt() ?: 12500}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricMiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveContractParcelSummaryCard(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("active_contract_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldTrust, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE COLLAB • Stage ${project.milestoneStage}/5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTrust
                    )
                }

                Text(
                    text = "Tap for Live Tracking 📦",
                    fontSize = 11.sp,
                    color = VioletPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )

            Text(
                text = "Talent: ${project.assignedFreelancerName ?: "Assigned"} • Budget: $${project.budget.toInt()}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress Bar Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (step in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    step <= project.milestoneStage -> EmeraldTrust
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun FreelancerTalentCard(
    freelancer: UserEntity,
    onContact: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(VioletPrimary, CyanAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = freelancer.fullName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = freelancer.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TrustScoreBadge(score = freelancer.trustScore, userEntity = freelancer)
                    }

                    Text(
                        text = freelancer.headline,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${freelancer.locationDistrict}, ${freelancer.locationState}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        WorkStatusBadge(status = freelancer.workStatus)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            VerificationBadgesRow(user = freelancer)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${freelancer.rating} (${freelancer.ratingCount} reviews)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${freelancer.onTimeDeliveryRate}% on-time",
                        fontSize = 11.sp,
                        color = EmeraldTrust
                    )
                }

                Text(
                    text = "$${freelancer.hourlyRate.toInt()}/hr",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioletPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProjectScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mobile Dev") }
    var requiredSkills by remember { mutableStateOf("Android, Jetpack Compose, Kotlin, Room") }
    var budgetStr by remember { mutableStateOf("1500") }
    var deadlineDaysStr by remember { mutableStateOf("14") }
    var scope by remember { mutableStateOf("Medium") }
    var enableBidRoom by remember { mutableStateOf(false) }
    var bidRoomLocation by remember { mutableStateOf("California") }
    var isEnhancingWithGemini by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val budget = budgetStr.toDoubleOrNull() ?: 1000.0
    val skillsCount = requiredSkills.split(",").size
    val (aiEstDays, aiConfidence, graphPoints) = GigverseAiEngine.predictProjectDuration(
        scope = scope,
        budget = budget,
        skillsCount = skillsCount,
        category = category
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Float New Project", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenNav.Dashboard) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "1. Project Scope & Specifications",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = VioletPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Project Title (e.g. Next-Gen Mobile App)") },
                modifier = Modifier.fillMaxWidth().testTag("post_project_title_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Requirements & Deliverables") },
                modifier = Modifier.fillMaxWidth().height(110.dp).testTag("post_project_desc_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gemini AI Requirements Enhancement Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isEnhancingWithGemini = true
                        val enhanced = viewModel.enhanceRequirementsWithGemini(
                            if (description.isBlank()) {
                                if (title.isNotBlank()) title else "Enterprise mobile application with high scalability, database persistence, and clean UI."
                            } else description
                        )
                        description = enhanced
                        isEnhancingWithGemini = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("post_project_gemini_enhance_button"),
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = requiredSkills,
                onValueChange = { requiredSkills = it },
                label = { Text("Required Skills (Comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Budget & Scope",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = VioletPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Budget ($ USD)") },
                    modifier = Modifier.weight(1f).testTag("post_project_budget_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = deadlineDaysStr,
                    onValueChange = { deadlineDaysStr = it },
                    label = { Text("Target Days") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Duration Predictor with Graph Analysis
            DurationGraphCard(
                points = graphPoints,
                totalDays = aiEstDays,
                confidence = aiConfidence
            )

            Spacer(modifier = Modifier.height(20.dp))

            // FEATURE 9: LIVE BID ROOM (REVERSE AUCTION FLOOR)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (enableBidRoom) VioletPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (enableBidRoom) VioletPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            tint = if (enableBidRoom) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Live Bid Room (Auction Floor)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Conduct real-time competitive bidding",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enableBidRoom,
                            onCheckedChange = { enableBidRoom = it },
                            modifier = Modifier.testTag("toggle_bid_room_switch")
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = enableBidRoom) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Regional Location Restriction",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VioletPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = bidRoomLocation,
                                onValueChange = { bidRoomLocation = it },
                                placeholder = { Text("e.g. California, USA") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = VioletPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("bid_room_location_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val days = deadlineDaysStr.toIntOrNull() ?: aiEstDays
                    viewModel.postNewProject(
                        title = if (title.isBlank()) "Innovative Jetpack Compose Multi-Screen App" else title,
                        description = if (description.isBlank()) "Building a comprehensive enterprise Android application with modern reactive state management and high-fidelity animations." else description,
                        category = category,
                        requiredSkills = requiredSkills,
                        budget = budget,
                        budgetType = "FIXED",
                        deadlineDays = days,
                        scope = scope,
                        enableBidRoom = enableBidRoom,
                        bidRoomAllowedLocation = bidRoomLocation
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_float_project_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (enableBidRoom) "Launch Project & Open Bid Room" else "Float Project to Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
