package com.gigverse.app.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gigverse.app.ai.GeminiChatMessage
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSpecificCopilotScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenNav.Dashboard)
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val copilotMessages by viewModel.copilotMessages.collectAsState()
    val isThinking by viewModel.isCopilotThinking.collectAsState()
    val selectedModel by viewModel.copilotSelectedModel.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val user = currentUser
    val isFreelancer = user?.role == "FREELANCER"
    val isClient = user?.role == "CLIENT"

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    // Auto-scroll to bottom on new messages
    LaunchedEffect(copilotMessages.size, isThinking) {
        if (copilotMessages.isNotEmpty()) {
            listState.animateScrollToItem(copilotMessages.size - 1)
        }
    }

    // Role-specific quick suggested prompts
    val quickSuggestions = remember(user?.id, user?.role) {
        if (isFreelancer) {
            listOf(
                "🚀 Write a winning bid proposal for an Android & Compose project",
                "💰 How can I justify and increase my $${user?.hourlyRate ?: 75}/hr rate?",
                "🛡️ Audit my ${user?.trustScore ?: 96}% Trust Score & Portfolio",
                "🤝 How to handle a client requesting additional scope without budget?"
            )
        } else if (isClient) {
            listOf(
                "📋 Structure milestone SOW for my ${user?.companyName ?: "company"} project",
                "🎯 Recommended budget and delivery timeline for mobile app MVP",
                "⚖️ How to write clear milestone acceptance criteria to avoid disputes?",
                "🔍 What key technical questions should I ask in freelancer interviews?"
            )
        } else {
            listOf(
                "⚖️ Dispute resolution mediation best practices",
                "🛡️ Audit marketplace trust metrics & KYC verification criteria",
                "📊 Platform escrow safety and payout compliance"
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Personalized User Profile Dossier Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFreelancer) CyanAccent.copy(alpha = 0.08f)
                    else if (isClient) VioletPrimary.copy(alpha = 0.08f)
                    else LivePulseRed.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFreelancer) CyanAccent else VioletPrimary,
                                    shape = CircleShape
                                )
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user?.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user?.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    if (isClient) Icons.Default.Business else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isFreelancer) CyanAccent else VioletPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.fullName ?: "Member",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (user?.isGovIdVerified == true) {
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = EmeraldTrust, modifier = Modifier.size(14.dp))
                                }
                            }

                            if (isFreelancer) {
                                Text(
                                    text = "${user?.headline ?: "Software Engineer"} • $${user?.hourlyRate ?: 75}/hr",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            } else if (isClient) {
                                Text(
                                    text = "${user?.companyName ?: "Independent"} • ${user?.industry ?: "Technology"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Trust / Spending Badge
                        Surface(
                            color = if (isFreelancer) EmeraldTrust.copy(alpha = 0.15f) else VioletPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isFreelancer) "${user?.trustScore ?: 96}% Trust" else "$${user?.totalSpent ?: 0.0} Spent",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFreelancer) EmeraldTrust else VioletPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Specific highlights row
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isFreelancer) {
                            Text(
                                text = "🎯 Skills: ${user?.skills ?: "Kotlin, Compose"}",
                                fontSize = 10.sp,
                                color = CyanAccent,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            if (!user?.portfolioWebsite.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = EmeraldTrust, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Portfolio Active", fontSize = 10.sp, color = EmeraldTrust, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            Text(
                                text = "🏢 Domain: ${user?.industry ?: "Software"} • ${user?.projectsPostedCount ?: 0} Projects Launched",
                                fontSize = 10.sp,
                                color = VioletPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 2. Gemini Model Selector Bar (gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3.1-flash-lite)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedModel == "gemini-3.5-flash",
                            onClick = { viewModel.setCopilotModel("gemini-3.5-flash") },
                            label = { Text("gemini-3.5-flash (Standard)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = VioletPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedModel == "gemini-3.1-pro-preview",
                            onClick = { viewModel.setCopilotModel("gemini-3.1-pro-preview") },
                            label = { Text("gemini-3.1-pro (Deep Reasoning)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedModel == "gemini-3.1-flash-lite-preview",
                            onClick = { viewModel.setCopilotModel("gemini-3.1-flash-lite-preview") },
                            label = { Text("gemini-3.1-flash-lite (Fast)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldTrust.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldTrust
                            )
                        )
                    }
                }
            }

            // 3. Multi-Turn Chat Thread (or Empty Welcome with Quick Chips)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (copilotMessages.isEmpty()) {
                    // Welcome & Quick Suggestions
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VioletPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VioletPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isFreelancer) "Hello, ${user?.fullName}! I'm your Career Copilot."
                            else if (isClient) "Welcome, ${user?.fullName}! I'm your Hiring Architect."
                            else "Welcome, Admin! I'm your Mediation Assistant.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "I have your credentials loaded and ready to write proposals, estimate timelines, or evaluate contracts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = "Tap a suggested topic to get started:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        quickSuggestions.forEach { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.sendCopilotMessage(prompt)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (isFreelancer) CyanAccent else VioletPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(prompt, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Chat Messages LazyColumn
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(copilotMessages) { msg ->
                            val isUser = msg.role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                if (!isUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(VioletPrimary, CyanAccent))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Surface(
                                    color = if (isUser) VioletPrimary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isUser) 14.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 14.dp
                                    ),
                                    border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                                    modifier = Modifier.widthIn(max = 310.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isUser) "You" else "GigVerse Copilot (${msg.modelUsed})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) Color.White.copy(alpha = 0.8f) else CyanAccent
                                            )

                                            if (!isUser) {
                                                IconButton(
                                                    onClick = { clipboardManager.setText(AnnotatedString(msg.text)) },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = msg.text,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            user?.fullName?.take(1) ?: "U",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 34.dp, top = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = CyanAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini is analyzing your profile & drafting response...",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Input Text Area
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = {
                            Text(
                                if (isFreelancer) "Ask for proposal, rate advice, or skills match..."
                                else "Ask for SOW, budget estimation, or candidate criteria...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copilot_input_field"),
                        shape = RoundedCornerShape(22.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputPrompt.isNotBlank() && !isThinking) {
                                viewModel.sendCopilotMessage(inputPrompt.trim())
                                inputPrompt = ""
                            }
                        },
                        enabled = inputPrompt.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputPrompt.isNotBlank() && !isThinking) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("copilot_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputPrompt.isNotBlank() && !isThinking) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
