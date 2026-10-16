package com.gigverse.app.ui.freelancer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
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
import com.gigverse.app.ui.components.WorkStatusBadge
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@Composable
fun FreelancerDashboardScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val floatedProjects by viewModel.floatedProjects.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val isOnWork = currentUser?.workStatus == "ON_WORK"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            FreelancerHeroHeader(freelancer = currentUser)
        }

        // Dedicated AI Copilot Card tuned to Freelancer's Profile
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenNav.UserSpecificCopilot) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyanAccent.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AI Career & Bid Copilot", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = CyanAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("Gemini 3.5", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("Personalized to your ${currentUser?.skills?.split(",")?.firstOrNull()?.trim() ?: "tech"} skills & ${currentUser?.trustScore ?: 96}% trust score.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Single Active Work Rule Alert Banner
        if (isOnWork && currentUser?.currentActiveProjectId != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(ScreenNav.LiveTracker(currentUser!!.currentActiveProjectId!!))
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VioletPrimary.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = VioletPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Currently On Active Contract",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = VioletPrimary
                            )
                            Text(
                                text = "Rule: 1 active client at a time. Tap to update your parcel progress and milestones!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = VioletPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Track 📦",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Search Bar & Filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search projects, skills, tech stack...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("freelancer_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Chips
        item {
            val categories = listOf("All", "Mobile Dev", "UI/UX", "AI & Algorithms", "Cloud", "Web3")
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

        item {
            Text(
                text = "⚡ Live Floated Projects (${floatedProjects.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }

        val filteredProjects = floatedProjects.filter { project ->
            val matchesCategory = selectedCategory == "All" || project.category.contains(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || project.title.contains(searchQuery, ignoreCase = true) || project.requiredSkills.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        if (filteredProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No floated projects match your filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredProjects) { project ->
                FloatedProjectCard(
                    project = project,
                    freelancer = currentUser,
                    onClick = { viewModel.navigateTo(ScreenNav.ProjectDetail(project.id)) },
                    onEnterBidRoom = { viewModel.navigateTo(ScreenNav.BidRoom(project.id)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FreelancerHeroHeader(freelancer: UserEntity?) {
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
                        colors = listOf(CyanAccent.copy(alpha = 0.12f), Color.Transparent),
                        radius = 400f
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(VioletPrimary, CyanAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = freelancer?.fullName?.take(2)?.uppercase() ?: "AM",
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
                                text = freelancer?.fullName ?: "Alex Mercer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TrustScoreBadge(score = freelancer?.trustScore ?: 96, userEntity = freelancer)
                        }

                        Text(
                            text = freelancer?.headline ?: "Staff Android & AI Engineer",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WorkStatusBadge(status = freelancer?.workStatus ?: "AVAILABLE")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${freelancer?.rating ?: 4.96}★ (${freelancer?.completedProjectsCount ?: 24} done)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatedProjectCard(
    project: ProjectEntity,
    freelancer: UserEntity?,
    onClick: () -> Unit,
    onEnterBidRoom: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("floated_project_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (project.isBidRoomEnabled) LivePulseRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VioletPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = project.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                }

                if (project.isBidRoomEnabled) {
                    Surface(
                        color = LivePulseRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onEnterBidRoom() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = LivePulseRed, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE BID ROOM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LivePulseRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = project.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                project.requiredSkills.split(",").take(4).forEach { skill ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = skill.trim(),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$${project.budget.toInt()} ${project.budgetType}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTrust,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Est. ${project.aiEstimatedDays} days • Scope: ${project.scope}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (project.isBidRoomEnabled) {
                        Button(
                            onClick = onEnterBidRoom,
                            colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Bid Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val projectStream = remember(projectId) { viewModel.getProjectStream(projectId) }
    val project by projectStream.collectAsState()

    val proposalsStream = remember(projectId) { viewModel.getProposalsStream(projectId) }
    val proposals by proposalsStream.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val isClient = currentUser?.role == "CLIENT"

    var bidAmountStr by remember { mutableStateOf("") }
    var estDaysStr by remember { mutableStateOf("") }
    var coverLetter by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenNav.Dashboard) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Project not found")
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
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = VioletPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = p.category,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VioletPrimary
                                    )
                                }

                                Surface(
                                    color = EmeraldTrust.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$${p.budget.toInt()} ${p.budgetType}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldTrust
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = p.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = p.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = p.clientCompany.ifBlank { p.clientName }, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = p.clientLocation, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // AI Duration Predictor Graph for this project
                item {
                    val (_, aiConfidence, graphPoints) = GigverseAiEngine.predictProjectDuration(
                        scope = p.scope,
                        budget = p.budget,
                        skillsCount = p.requiredSkills.split(",").size,
                        category = p.category
                    )
                    DurationGraphCard(
                        points = graphPoints,
                        totalDays = p.aiEstimatedDays,
                        confidence = aiConfidence
                    )
                }

                // If Bid Room is active -> Quick Entrance
                if (p.isBidRoomEnabled) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LivePulseRed.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LivePulseRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("⚡ Live Bid Room is Active", fontWeight = FontWeight.Bold, color = LivePulseRed)
                                    Text("Allowed Locations: ${p.bidRoomAllowedLocation}", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(ScreenNav.BidRoom(p.id)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                                ) {
                                    Text("Join Auction")
                                }
                            }
                        }
                    }
                }

                // Freelancer Submit Proposal Form (If not already client and project open)
                if (!isClient && (p.status == "OPEN" || p.status == "BID_ROOM_ACTIVE")) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Submit Your Proposal & Bid",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Note: If hired, you will be locked into this project until completed and approved.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = bidAmountStr,
                                        onValueChange = { bidAmountStr = it },
                                        label = { Text("Your Bid ($ USD)") },
                                        placeholder = { Text("${p.budget.toInt()}") },
                                        modifier = Modifier.weight(1f).testTag("proposal_bid_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = estDaysStr,
                                        onValueChange = { estDaysStr = it },
                                        label = { Text("Days to Deliver") },
                                        placeholder = { Text("${p.deadlineDays}") },
                                        modifier = Modifier.weight(1f).testTag("proposal_days_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = coverLetter,
                                    onValueChange = { coverLetter = it },
                                    label = { Text("Cover Letter & Architecture Approach") },
                                    modifier = Modifier.fillMaxWidth().height(95.dp).testTag("proposal_letter_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val isOnWork = currentUser?.workStatus == "ON_WORK"

                                Button(
                                    onClick = {
                                        val bid = bidAmountStr.toDoubleOrNull() ?: p.budget
                                        val days = estDaysStr.toIntOrNull() ?: p.deadlineDays
                                        viewModel.submitProposal(
                                            projectId = p.id,
                                            bidAmount = bid,
                                            estimatedDays = days,
                                            coverLetter = if (coverLetter.isBlank()) "Experienced engineer ready to deliver production-grade code adhering strictly to specifications." else coverLetter
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_proposal_button"),
                                    enabled = !isOnWork,
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isOnWork) "Unavailable: Currently On Active Contract" else "Submit Proposal to Client",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Proposal Submissions List
                item {
                    Text(
                        text = "Candidate Proposals (${proposals.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(proposals) { prop ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = prop.freelancerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                TrustScoreBadge(score = prop.freelancerTrustScore)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Bid: $${prop.bidAmount.toInt()} in ${prop.estimatedDays} days",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldTrust
                            )

                            Text(
                                text = prop.coverLetter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (isClient && p.status == "OPEN") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.acceptProposal(
                                            projectId = p.id,
                                            proposalId = prop.id,
                                            freelancerId = prop.freelancerId,
                                            freelancerName = prop.freelancerName
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Accept & Hire Freelancer (Lock into Single Project)")
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
