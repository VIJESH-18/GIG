package com.gigverse.app.ui.tracking

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.data.model.MessageEntity
import com.gigverse.app.ui.components.ParcelMilestoneTracker
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackerScreen(
    projectId: String,
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val projectStream = remember(projectId) { viewModel.getProjectStream(projectId) }
    val project by projectStream.collectAsState()

    val messagesStream = remember(projectId) { viewModel.getMessagesStream(projectId) }
    val messages by messagesStream.collectAsState()

    val tasksStream = remember(projectId) { viewModel.getTasksForProject(projectId) }
    val tasks by tasksStream.collectAsState()

    val submissionsStream = remember(projectId) { viewModel.getSubmissionsForProject(projectId) }
    val submissions by submissionsStream.collectAsState()

    val disputesStream = remember(projectId) { viewModel.getDisputesForProject(projectId) }
    val disputes by disputesStream.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()
    val isClient = currentUser?.role == "CLIENT"

    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Milestone Tracking, 1: Kanban Tasks, 2: Deliverables & Revisions, 3: Discussion Chat, 4: Disputes

    var chatMessageInput by remember { mutableStateOf("") }
    var showAdvanceDialog by remember { mutableStateOf(false) }
    var advanceNotes by remember { mutableStateOf("") }
    var showReviewDialog by remember { mutableStateOf(false) }

    // Dialog state for Tasks
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }
    var newTaskPriority by remember { mutableStateOf("MEDIUM") }

    // Dialog state for Submissions
    var showSubmitDeliverableDialog by remember { mutableStateOf(false) }
    var deliverableTitle by remember { mutableStateOf("") }
    var deliverableLinks by remember { mutableStateOf("https://github.com/myproject/milestone") }
    var deliverableNotes by remember { mutableStateOf("") }

    // Dialog state for Revisions
    var showRevisionDialog by remember { mutableStateOf(false) }
    var targetSubmissionId by remember { mutableStateOf("") }
    var revisionNotes by remember { mutableStateOf("") }

    // Dialog state for Raising Dispute
    var showRaiseDisputeDialog by remember { mutableStateOf(false) }
    var disputeReason by remember { mutableStateOf("") }
    var disputeDesc by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Project not found")
            }
        } else {
            val p = project!!
            val isCompleted = p.status == "COMPLETED"

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Project Header Banner
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isCompleted) EmeraldTrust.copy(alpha = 0.2f) else VioletPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isCompleted) "COMPLETED & APPROVED" else "STAGE ${p.milestoneStage}/5 ACTIVE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) EmeraldTrust else VioletPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Client: ${p.clientCompany.ifBlank { p.clientName }} • Budget: $${p.budget.toInt()}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Talent: ${p.assignedFreelancerName ?: "Alex Mercer"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Workspace Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    edgePadding = 8.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Milestones \uD83D\uDCE6", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Kanban Tasks (${tasks.size}) \uD83D\uDCCB", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Submissions (${submissions.size}) \uD83D\uDE80", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Chat (${messages.size}) \uD83D\uDCAC", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Disputes (${disputes.size}) \u2696\uFE0F", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // TAB 0: AMAZON PARCEL MILESTONE TRACKING
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                ParcelMilestoneTracker(
                                    currentStage = p.milestoneStage,
                                    stageNotes = p.stageNotes
                                )
                            }

                            if (!isCompleted) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Milestone Controls",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Advance stages, submit deliverables, or trigger escrow approval.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                if (p.milestoneStage < 5) {
                                                    Button(
                                                        onClick = { showAdvanceDialog = true },
                                                        modifier = Modifier.weight(1f).testTag("advance_stage_button"),
                                                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Advance Stage")
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.completeProject(p.id, p.assignedFreelancerId ?: "")
                                                        showReviewDialog = true
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("approve_complete_button"),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Approve & Sign-off")
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = EmeraldTrust.copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldTrust)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Contract Completed & Escrow Released!",
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldTrust
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Funds paid out to ${p.assignedFreelancerName}. Freelancer is now AVAILABLE for new project invitations.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = { showReviewDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust)
                                            ) {
                                                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Submit Rating & Review")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: KANBAN TASKS
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Kanban Project Board", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Track sprints, deliverables, and backlog items.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { showAddTaskDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Task", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (tasks.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No tasks added yet. Click 'Add Task' to plan the sprint.")
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(tasks) { task ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = when (task.priority) {
                                                                "HIGH" -> LivePulseRed.copy(alpha = 0.15f)
                                                                "MEDIUM" -> CyanAccent.copy(alpha = 0.15f)
                                                                else -> EmeraldTrust.copy(alpha = 0.15f)
                                                            },
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                task.priority,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when (task.priority) {
                                                                    "HIGH" -> LivePulseRed
                                                                    "MEDIUM" -> CyanAccent
                                                                    else -> EmeraldTrust
                                                                },
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    if (task.description.isNotBlank()) {
                                                        Text(task.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    Text("Assigned: ${task.assigneeName}", fontSize = 10.sp, color = VioletPrimary)
                                                }

                                                // Column Status Pill & Advance button
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Surface(
                                                        color = when (task.status) {
                                                            "DONE" -> EmeraldTrust.copy(alpha = 0.15f)
                                                            "REVIEW" -> LivePulseRed.copy(alpha = 0.15f)
                                                            "IN_PROGRESS" -> CyanAccent.copy(alpha = 0.15f)
                                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                                        },
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            task.status,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (task.status) {
                                                                "DONE" -> EmeraldTrust
                                                                "REVIEW" -> LivePulseRed
                                                                "IN_PROGRESS" -> CyanAccent
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            },
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    TextButton(
                                                        onClick = { viewModel.advanceTaskStatus(task) }
                                                    ) {
                                                        Text("Move →", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: DELIVERABLES & REVISIONS
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Milestone Deliverables", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Freelancer submissions, approvals, and revision requests.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { showSubmitDeliverableDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Submit Work", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (submissions.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No milestone deliverables submitted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(submissions) { sub ->
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
                                                    Text(sub.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Surface(
                                                        color = when (sub.status) {
                                                            "APPROVED" -> EmeraldTrust.copy(alpha = 0.15f)
                                                            "REVISION_REQUESTED" -> LivePulseRed.copy(alpha = 0.15f)
                                                            else -> CyanAccent.copy(alpha = 0.15f)
                                                        },
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            sub.status,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (sub.status) {
                                                                "APPROVED" -> EmeraldTrust
                                                                "REVISION_REQUESTED" -> LivePulseRed
                                                                else -> CyanAccent
                                                            },
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("Deliverable Links: ${sub.deliverableLinks}", fontSize = 12.sp, color = VioletPrimary)
                                                if (sub.notes.isNotBlank()) {
                                                    Text("Notes: ${sub.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                                }
                                                if (sub.revisionComments.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Revision Feedback: ${sub.revisionComments}", fontSize = 11.sp, color = LivePulseRed, fontWeight = FontWeight.SemiBold)
                                                }

                                                if (sub.status == "SUBMITTED" && isClient) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                viewModel.reviewMilestoneSubmission(
                                                                    submissionId = sub.id,
                                                                    approve = true,
                                                                    revisionNotes = "",
                                                                    projectId = p.id,
                                                                    currentStage = p.milestoneStage
                                                                )
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Approve Milestone", fontSize = 11.sp)
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                targetSubmissionId = sub.id
                                                                showRevisionDialog = true
                                                            },
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Request Revision", fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: PROJECT COLLABORATION CHAT
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messages) { msg ->
                                    ChatMessageBubble(
                                        message = msg,
                                        isMine = msg.senderId == currentUser?.id
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatMessageInput,
                                    onValueChange = { chatMessageInput = it },
                                    placeholder = { Text("Send collaboration update...") },
                                    modifier = Modifier.weight(1f).testTag("chat_input_field"),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (chatMessageInput.isNotBlank()) {
                                            viewModel.sendMessage(p.id, chatMessageInput.trim())
                                            chatMessageInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(VioletPrimary)
                                        .testTag("chat_send_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: DISPUTE MEDIATION
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Dispute Mediation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Escalate scope disagreements or unapproved deliverables.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { showRaiseDisputeDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Raise Dispute", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (disputes.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No disputes recorded for this project. Escrow & collaboration healthy.")
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(disputes) { disp ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(disp.reason, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Surface(
                                                        color = if (disp.status == "OPEN") LivePulseRed.copy(alpha = 0.15f) else EmeraldTrust.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(disp.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (disp.status == "OPEN") LivePulseRed else EmeraldTrust, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(disp.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (disp.resolutionNotes.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Admin Resolution: ${disp.resolutionNotes}", fontSize = 11.sp, color = EmeraldTrust, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialog for Advancing Milestone
            if (showAdvanceDialog) {
                AlertDialog(
                    onDismissRequest = { showAdvanceDialog = false },
                    title = { Text("Advance to Stage ${p.milestoneStage + 1}") },
                    text = {
                        Column {
                            Text("Provide notes on what was accomplished in this milestone stage:", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = advanceNotes,
                                onValueChange = { advanceNotes = it },
                                placeholder = { Text("e.g. Completed UI integration & test suites.") },
                                modifier = Modifier.fillMaxWidth().testTag("advance_notes_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateMilestone(
                                    projectId = p.id,
                                    newStage = p.milestoneStage + 1,
                                    notes = if (advanceNotes.isBlank()) "Milestone completed successfully." else advanceNotes
                                )
                                showAdvanceDialog = false
                                advanceNotes = ""
                            }
                        ) {
                            Text("Confirm & Advance")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdvanceDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Dialog for Adding Task (Kanban)
            if (showAddTaskDialog) {
                AlertDialog(
                    onDismissRequest = { showAddTaskDialog = false },
                    title = { Text("Add Project Kanban Task") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                label = { Text("Task Title *") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newTaskDesc,
                                onValueChange = { newTaskDesc = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addTask(p.id, newTaskTitle, newTaskDesc, newTaskPriority)
                                showAddTaskDialog = false
                                newTaskTitle = ""
                                newTaskDesc = ""
                            }
                        ) {
                            Text("Create Task")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Dialog for Submitting Milestone Deliverable
            if (showSubmitDeliverableDialog) {
                AlertDialog(
                    onDismissRequest = { showSubmitDeliverableDialog = false },
                    title = { Text("Submit Milestone Deliverable") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = deliverableTitle,
                                onValueChange = { deliverableTitle = it },
                                label = { Text("Deliverable Title") },
                                placeholder = { Text("e.g. Milestone ${p.milestoneStage} APK & Code Repo") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = deliverableLinks,
                                onValueChange = { deliverableLinks = it },
                                label = { Text("Repository / Demo URL") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = deliverableNotes,
                                onValueChange = { deliverableNotes = it },
                                label = { Text("Completion Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.submitMilestoneDeliverable(
                                    projectId = p.id,
                                    stage = p.milestoneStage,
                                    title = if (deliverableTitle.isBlank()) "Stage ${p.milestoneStage} Deliverable" else deliverableTitle,
                                    links = deliverableLinks,
                                    notes = deliverableNotes
                                )
                                showSubmitDeliverableDialog = false
                                deliverableTitle = ""
                                deliverableNotes = ""
                            }
                        ) {
                            Text("Submit for Review")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubmitDeliverableDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Dialog for Requesting Revision
            if (showRevisionDialog) {
                AlertDialog(
                    onDismissRequest = { showRevisionDialog = false },
                    title = { Text("Request Milestone Revision") },
                    text = {
                        Column {
                            Text("Specify the exact changes required before milestone approval:", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = revisionNotes,
                                onValueChange = { revisionNotes = it },
                                placeholder = { Text("e.g. Please update button spacing and add email validation.") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.reviewMilestoneSubmission(
                                    submissionId = targetSubmissionId,
                                    approve = false,
                                    revisionNotes = revisionNotes.ifBlank { "Revisions requested by client." },
                                    projectId = p.id,
                                    currentStage = p.milestoneStage
                                )
                                showRevisionDialog = false
                                revisionNotes = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                        ) {
                            Text("Submit Revision Request")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRevisionDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Dialog for Raising Dispute
            if (showRaiseDisputeDialog) {
                AlertDialog(
                    onDismissRequest = { showRaiseDisputeDialog = false },
                    title = { Text("Open Dispute with Admin Mediation") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = disputeReason,
                                onValueChange = { disputeReason = it },
                                label = { Text("Dispute Reason *") },
                                placeholder = { Text("e.g. Scope disagreement or missed deadline") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = disputeDesc,
                                onValueChange = { disputeDesc = it },
                                label = { Text("Detailed Explanation *") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.raiseDispute(
                                    projectId = p.id,
                                    projectTitle = p.title,
                                    reason = disputeReason.ifBlank { "Milestone Scope Disagreement" },
                                    description = disputeDesc.ifBlank { "Mediation requested regarding deliverable expectations." }
                                )
                                showRaiseDisputeDialog = false
                                disputeReason = ""
                                disputeDesc = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LivePulseRed)
                        ) {
                            Text("File Dispute")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRaiseDisputeDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // Dialog for Completing Rating & Review
            if (showReviewDialog) {
                var ratingValue by remember { mutableDoubleStateOf(5.0) }
                var tagsInput by remember { mutableStateOf("Excellent Communication, On-Time, High Quality") }
                var commentInput by remember { mutableStateOf("Outstanding performance and clean architecture delivery.") }

                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text("Leave Feedback & Review") },
                    text = {
                        Column {
                            Text("Rating (1 to 5 Stars):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                (1..5).forEach { star ->
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "$star stars",
                                        tint = if (star <= ratingValue) Color(0xFFFFB800) else Color.Gray,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable { ratingValue = star.toDouble() }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("Highlight Tags") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = commentInput,
                                onValueChange = { commentInput = it },
                                label = { Text("Detailed Review") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.submitReview(
                                    projectId = p.id,
                                    toUserId = p.assignedFreelancerId ?: "freelancer_alex",
                                    rating = ratingValue,
                                    tags = tagsInput,
                                    comment = commentInput
                                )
                                showReviewDialog = false
                            }
                        ) {
                            Text("Post Review & Complete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) { Text("Skip") }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: MessageEntity,
    isMine: Boolean
) {
    val isSystem = message.senderRole == "SYSTEM"

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
        ) {
            Surface(
                color = if (isMine) VioletPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isMine) 14.dp else 2.dp,
                    bottomEnd = if (isMine) 2.dp else 14.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = message.senderName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMine) Color.White.copy(alpha = 0.85f) else CyanAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message.message,
                        fontSize = 13.sp,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
