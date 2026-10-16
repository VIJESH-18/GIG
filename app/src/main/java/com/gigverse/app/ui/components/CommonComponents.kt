package com.gigverse.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.ai.GigverseAiEngine
import com.gigverse.app.data.model.DurationPredictionGraphPoint
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary

@Composable
fun TrustScoreBadge(
    score: Int,
    modifier: Modifier = Modifier,
    userEntity: UserEntity? = null,
    showLabel: Boolean = true
) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    val (badgeBg, badgeBorder, badgeText) = when {
        score >= 90 -> Triple(Color(0xFF064E3B).copy(alpha = 0.3f), EmeraldTrust, EmeraldTrust)
        score >= 75 -> Triple(Color(0xFF0C4A6E).copy(alpha = 0.3f), CyanAccent, CyanAccent)
        else -> Triple(Color(0xFF78350F).copy(alpha = 0.3f), Color(0xFFF59E0B), Color(0xFFF59E0B))
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, badgeBorder, RoundedCornerShape(20.dp))
            .clickable { showDetailsDialog = true }
            .testTag("trust_score_badge_${score}"),
        color = badgeBg,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "AI Trust Score",
                tint = badgeText,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$score%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText
            )
            if (showLabel) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "AI Trust",
                    fontSize = 10.sp,
                    color = badgeText.copy(alpha = 0.9f)
                )
            }
        }
    }

    if (showDetailsDialog && userEntity != null) {
        val breakdown = GigverseAiEngine.calculateTrustScore(userEntity)
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Got it")
                }
            },
            icon = {
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = EmeraldTrust,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "AI Trust Score: ${breakdown.totalScore}%",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = breakdown.tierLabel,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeBorder,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = breakdown.summaryInsight,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TrustScoreItemRow(
                        label = "Project Deadline Adherence",
                        pts = "${breakdown.deadlineAdherenceScore} / 30 pts",
                        icon = Icons.Default.TrendingUp
                    )
                    TrustScoreItemRow(
                        label = "Client Reviews & Rating (${userEntity.rating}★)",
                        pts = "${breakdown.clientFeedbackScore} / 30 pts",
                        icon = Icons.Default.Star
                    )
                    TrustScoreItemRow(
                        label = "Verified Govt ID, GitHub & LinkedIn",
                        pts = "${breakdown.credentialsScore} / 40 pts",
                        icon = Icons.Default.Fingerprint
                    )
                    if (breakdown.disputePenalty > 0) {
                        TrustScoreItemRow(
                            label = "Dispute Penalties",
                            pts = "-${breakdown.disputePenalty} pts",
                            icon = Icons.Default.Shield,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun TrustScoreItemRow(
    label: String,
    pts: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = EmeraldTrust
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = pts,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun WorkStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isAvailable = status.equals("AVAILABLE", ignoreCase = true)
    val bgColor = if (isAvailable) EmeraldTrust.copy(alpha = 0.15f) else VioletPrimary.copy(alpha = 0.15f)
    val textColor = if (isAvailable) EmeraldTrust else VioletPrimary
    val label = if (isAvailable) "Available" else "On Work (Active)"

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerificationBadgesRow(
    user: UserEntity,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (user.isGovIdVerified) {
            VerificationChip(icon = Icons.Default.Fingerprint, text = "Govt ID Verified")
        }
        if (user.githubUrl.isNotBlank()) {
            VerificationChip(icon = Icons.Default.Link, text = "GitHub Linked")
        }
        if (user.linkedInUrl.isNotBlank()) {
            VerificationChip(icon = Icons.Default.VerifiedUser, text = "LinkedIn Linked")
        }
        VerificationChip(icon = Icons.Default.Lock, text = "Escrow Secured")
    }
}

@Composable
fun VerificationChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = EmeraldTrust
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Amazon Parcel Tracking-Style Live Milestone Tracker
 * Visual vertical progress with animated pulse on current milestone
 */
@Composable
fun ParcelMilestoneTracker(
    currentStage: Int,
    stageNotes: String,
    modifier: Modifier = Modifier,
    onAdvanceMilestone: ((Int) -> Unit)? = null
) {
    val stages = listOf(
        Pair("1. Escrow & Contract Initialized", "Funds deposited in secure escrow. Project specification confirmed."),
        Pair("2. Architecture & Wireframes", "Technical blueprints, DB schemas, and UI designs reviewed."),
        Pair("3. Core Feature Development", "Active code implementation, API integrations, and continuous build delivery."),
        Pair("4. Quality Assurance & Review", "Unit tests verified, security checks completed, and client testing."),
        Pair("5. Final Delivery & Sign-Off", "Production code delivered, escrow released to freelancer, review exchange.")
    )

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = VioletPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Progress Tracking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Surface(
                    color = VioletPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Stage $currentStage / 5",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            stages.forEachIndexed { index, pair ->
                val stageNum = index + 1
                val isCompleted = stageNum < currentStage
                val isCurrent = stageNum == currentStage
                val isPending = stageNum > currentStage

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Step indicator with vertical connector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> EmeraldTrust
                                        isCurrent -> VioletPrimary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size((8 * pulseScale).dp)
                                        .background(Color.White, CircleShape)
                                )
                            } else {
                                Text(
                                    text = "$stageNum",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (index < stages.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(
                                        if (isCompleted) EmeraldTrust else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Step Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = pair.first,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.onSurface
                                isCompleted -> EmeraldTrust
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = pair.second,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        if (isCurrent && stageNotes.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = VioletPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Status Note: $stageNotes",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI-Based Duration Predictor with Graph Analysis Card
 * Renders graph curve showing milestones vs projected completion days
 */
@Composable
fun DurationGraphCard(
    points: List<DurationPredictionGraphPoint>,
    totalDays: Int,
    confidence: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Duration Predictor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    color = CyanAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${(confidence * 100).toInt()}% Confidence",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estimated Completion: ~$totalDays days based on scope complexity and velocity benchmarks.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Graph Analysis
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val maxDay = (totalDays * 1.25f).coerceAtLeast(10f)

                if (points.isNotEmpty()) {
                    val pathEstimated = Path()
                    val pathConservative = Path()

                    points.forEachIndexed { index, pt ->
                        val x = (index.toFloat() / (points.size - 1)) * width
                        val yEst = height - (pt.estimatedDay / maxDay) * height
                        val yCons = height - (pt.conservativeDay / maxDay) * height

                        if (index == 0) {
                            pathEstimated.moveTo(x, yEst)
                            pathConservative.moveTo(x, yCons)
                        } else {
                            pathEstimated.lineTo(x, yEst)
                            pathConservative.lineTo(x, yCons)
                        }

                        // Draw milestone dot
                        drawCircle(
                            color = VioletPrimary,
                            radius = 4.dp.toPx(),
                            center = Offset(x, yEst)
                        )
                    }

                    // Conservative dotted curve
                    drawPath(
                        path = pathConservative,
                        color = Color(0xFF94A3B8),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Estimated primary curve
                    drawPath(
                        path = pathEstimated,
                        brush = Brush.horizontalGradient(listOf(CyanAccent, VioletPrimary)),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Milestone X-axis tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = "M${pt.milestoneIndex}: ${pt.estimatedDay.toInt()}d",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
