package com.gigverse.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigverse.app.R
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.ui.admin.AdminDashboardScreen
import com.gigverse.app.ui.admin.ProductionHubScreen
import com.gigverse.app.ui.ai.UserSpecificCopilotScreen
import com.gigverse.app.ui.auth.ClientRegisterScreen
import com.gigverse.app.ui.auth.ForgotPasswordScreen
import com.gigverse.app.ui.auth.FreelancerRegisterScreen
import com.gigverse.app.ui.auth.LoginScreen
import com.gigverse.app.ui.auth.RegistrationScreen
import com.gigverse.app.ui.bidroom.LiveBidRoomScreen
import com.gigverse.app.ui.client.ClientDashboardScreen
import com.gigverse.app.ui.client.PostProjectScreen
import com.gigverse.app.ui.freelancer.FreelancerDashboardScreen
import com.gigverse.app.ui.freelancer.ProjectDetailScreen
import com.gigverse.app.ui.gigs.GigCatalogScreen
import com.gigverse.app.ui.profile.MyProjectsScreen
import com.gigverse.app.ui.profile.ProfileScreen
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.GigverseLocalization
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.tracking.LiveTrackerScreen
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigverseAppRoot(viewModel: GigverseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentScreen !is ScreenNav.Dashboard && currentScreen !is ScreenNav.AuthLogin && currentScreen !is ScreenNav.AuthRegister) {
        if (currentScreen is ScreenNav.ForgotPassword) {
            viewModel.navigateTo(ScreenNav.AuthLogin)
        } else {
            viewModel.navigateTo(ScreenNav.Dashboard)
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val isAuthScreen = currentScreen is ScreenNav.AuthLogin ||
            currentScreen is ScreenNav.AuthRegister ||
            currentScreen is ScreenNav.AuthRegisterFreelancer ||
            currentScreen is ScreenNav.AuthRegisterClient ||
            currentScreen is ScreenNav.ForgotPassword

    Scaffold(
        topBar = {
            if (!isAuthScreen) {
                val isMainTab = currentScreen is ScreenNav.Dashboard || currentScreen is ScreenNav.MyProjects || currentScreen is ScreenNav.Profile || currentScreen is ScreenNav.FreelancerDirectory
                val screenTitle = when (currentScreen) {
                    is ScreenNav.Dashboard -> "GIGVERSE"
                    is ScreenNav.MyProjects -> "My Projects"
                    is ScreenNav.FreelancerDirectory -> "Gig Catalog"
                    is ScreenNav.BidRoom -> "Live Bid Room"
                    is ScreenNav.LiveTracker -> "Project Tracker"
                    is ScreenNav.UserSpecificCopilot -> "AI Copilot"
                    is ScreenNav.ProductionHub -> "Production Hub"
                    is ScreenNav.AdminDashboard -> "Platform Governance"
                    is ScreenNav.Profile -> "Profile"
                    is ScreenNav.PostProject -> "Float Project"
                    else -> "GIGVERSE"
                }

                TopAppBar(
                    navigationIcon = {
                        if (!isMainTab) {
                            IconButton(onClick = { viewModel.navigateTo(ScreenNav.Dashboard) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, VioletPrimary, RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_gigverse_logo),
                                    contentDescription = "GIGVERSE",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = screenTitle,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // English / Tamil Language Switcher
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.toggleLanguage() }
                            ) {
                                Text(
                                    text = if (currentLang == "ta") "தமிழ்" else "EN",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Theme Mode Switcher
                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Gemini AI Copilot Action Icon
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenNav.UserSpecificCopilot) },
                                modifier = Modifier.size(36.dp).testTag("appbar_copilot_button")
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = "AI Copilot",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Direct Production Hub Action Icon
                            IconButton(
                                onClick = { viewModel.navigateTo(ScreenNav.ProductionHub) },
                                modifier = Modifier.size(36.dp).testTag("appbar_prod_hub_button")
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Production Hub",
                                    tint = EmeraldTrust,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Direct Exit / Log Out Button
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.size(36.dp).testTag("appbar_logout_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen) {
                GigverseBottomNavBar(
                    currentScreen = currentScreen,
                    currentLang = currentLang,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is ScreenNav.AuthLogin -> LoginScreen(viewModel = viewModel)
                is ScreenNav.AuthRegister -> RegistrationScreen(viewModel = viewModel)
                is ScreenNav.AuthRegisterFreelancer -> FreelancerRegisterScreen(viewModel = viewModel)
                is ScreenNav.AuthRegisterClient -> ClientRegisterScreen(viewModel = viewModel)
                is ScreenNav.Dashboard -> {
                    when (currentUser?.role) {
                        "CLIENT" -> ClientDashboardScreen(viewModel = viewModel)
                        "ADMIN" -> AdminDashboardScreen(viewModel = viewModel)
                        else -> FreelancerDashboardScreen(viewModel = viewModel)
                    }
                }
                is ScreenNav.PostProject -> PostProjectScreen(viewModel = viewModel)
                is ScreenNav.ProjectDetail -> ProjectDetailScreen(projectId = screen.projectId, viewModel = viewModel)
                is ScreenNav.BidRoom -> LiveBidRoomScreen(projectId = screen.projectId, viewModel = viewModel)
                is ScreenNav.LiveTracker -> LiveTrackerScreen(projectId = screen.projectId, viewModel = viewModel)
                is ScreenNav.CollaborationChat -> LiveTrackerScreen(projectId = screen.projectId, viewModel = viewModel)
                is ScreenNav.MyProjects -> MyProjectsScreen(viewModel = viewModel)
                is ScreenNav.FreelancerDirectory -> GigCatalogScreen(viewModel = viewModel)
                is ScreenNav.Profile -> ProfileScreen(viewModel = viewModel)
                is ScreenNav.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                is ScreenNav.UserSpecificCopilot -> UserSpecificCopilotScreen(viewModel = viewModel)
                is ScreenNav.ProductionHub -> ProductionHubScreen(viewModel = viewModel)
                is ScreenNav.ForgotPassword -> ForgotPasswordScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GigverseBottomNavBar(
    currentScreen: ScreenNav,
    currentLang: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigate: (ScreenNav) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
    ) {
        // Bottom Navigation Quick Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable {
                    if (currentScreen !is ScreenNav.FreelancerDirectory) {
                        onNavigate(ScreenNav.FreelancerDirectory)
                    }
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        onSearchQueryChange(newQuery)
                        if (currentScreen !is ScreenNav.FreelancerDirectory) {
                            onNavigate(ScreenNav.FreelancerDirectory)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottomnav_search_input"),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = GigverseLocalization.get("search_placeholder", currentLang),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val isDashboard = currentScreen is ScreenNav.Dashboard || currentScreen is ScreenNav.AdminDashboard
            val isSearch = currentScreen is ScreenNav.FreelancerDirectory
            val isBidRoom = currentScreen is ScreenNav.BidRoom
            val isCopilot = currentScreen is ScreenNav.UserSpecificCopilot
            val isMyProjects = currentScreen is ScreenNav.MyProjects || currentScreen is ScreenNav.LiveTracker
            val isProfile = currentScreen is ScreenNav.Profile

            NavigationBarItem(
                selected = isDashboard,
                onClick = { onNavigate(ScreenNav.Dashboard) },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text(GigverseLocalization.get("market", currentLang), fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_dashboard")
            )

            NavigationBarItem(
                selected = isSearch,
                onClick = { onNavigate(ScreenNav.FreelancerDirectory) },
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text(GigverseLocalization.get("search", currentLang), fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_search")
            )

            NavigationBarItem(
                selected = isBidRoom,
                onClick = { onNavigate(ScreenNav.BidRoom("proj_102")) },
                icon = { Icon(Icons.Default.Gavel, contentDescription = "Bid Room") },
                label = { Text(GigverseLocalization.get("bid_room", currentLang), fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LivePulseRed,
                    selectedTextColor = LivePulseRed,
                    indicatorColor = LivePulseRed.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_bid_room")
            )

            NavigationBarItem(
                selected = isCopilot,
                onClick = { onNavigate(ScreenNav.UserSpecificCopilot) },
                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Copilot") },
                label = { Text("AI Copilot", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_copilot")
            )

            NavigationBarItem(
                selected = isMyProjects,
                onClick = { onNavigate(ScreenNav.MyProjects) },
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "My Work") },
                label = { Text(GigverseLocalization.get("my_work", currentLang), fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_my_work")
            )

            NavigationBarItem(
                selected = isProfile,
                onClick = { onNavigate(ScreenNav.Profile) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text(GigverseLocalization.get("profile", currentLang), fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_profile")
            )
        }
    }
}
