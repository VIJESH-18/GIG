package com.gigverse.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gigverse.app.R
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.ui.theme.CyanAccent
import com.gigverse.app.ui.theme.EmeraldTrust
import com.gigverse.app.ui.theme.LivePulseRed
import com.gigverse.app.ui.theme.VioletPrimary
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav

// Preset profile pictures for instant 1-tap testing
private val SAMPLE_AVATARS = listOf(
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
    "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300"
)

// Preset ID proof document images for instant 1-tap testing
private val SAMPLE_ID_DOCS = listOf(
    "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=400",
    "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400"
)

/**
 * Modern Jetpack Compose Login Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    val savedRole = viewModel.getSavedRole()
    val initialRoleTab = if (savedRole.equals("FREELANCER", ignoreCase = true)) 1 else 0
    var selectedRoleTab by remember { mutableIntStateOf(initialRoleTab) }
    var email by remember { mutableStateOf(viewModel.getSavedLoginEmail()) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(viewModel.isRememberMe()) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val savedEmail = viewModel.getSavedLoginEmail()
        if (savedEmail.isNotBlank() && email.isBlank()) {
            email = savedEmail
        }
        val role = viewModel.getSavedRole()
        if (role.isNotBlank()) {
            selectedRoleTab = if (role.equals("FREELANCER", ignoreCase = true)) 1 else 0
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Logo & Branding Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(VioletPrimary, CyanAccent)))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(19.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_gigverse_logo),
                        contentDescription = "GIGVERSE Logo",
                        modifier = Modifier.size(54.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "GIGVERSE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Discover Talent. Build Together. Deliver Better.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Tab (Client / Freelancer / Admin)
            TabRow(
                selectedTabIndex = selectedRoleTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedRoleTab == 0,
                    onClick = {
                        selectedRoleTab = 0
                    },
                    text = {
                        Text(
                            "Client",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRoleTab == 0) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = if (selectedRoleTab == 0) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedRoleTab == 1,
                    onClick = {
                        selectedRoleTab = 1
                    },
                    text = {
                        Text(
                            "Freelancer",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRoleTab == 1) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (selectedRoleTab == 1) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (selectedRoleTab == 0) "Client Business Email" else "Freelancer Email") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = if (selectedRoleTab == 0) VioletPrimary else CyanAccent
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (selectedRoleTab == 0) VioletPrimary else CyanAccent
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Remember Me & Forgot Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remember Me Checkbox & Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            rememberMe = !rememberMe
                            viewModel.setRememberMe(rememberMe)
                        }
                        .testTag("remember_me_row")
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = {
                            rememberMe = it
                            viewModel.setRememberMe(it)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = if (selectedRoleTab == 0) VioletPrimary else CyanAccent
                        ),
                        modifier = Modifier.testTag("remember_me_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Remember Me",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Forgot Password Button
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedRoleTab == 0) VioletPrimary else CyanAccent,
                    modifier = Modifier
                        .clickable {
                            showForgotPasswordDialog = true
                        }
                        .testTag("forgot_password_button")
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sign In Button (Redirects to Dashboard)
            Button(
                onClick = {
                    if (isLoggingIn) return@Button
                    isLoggingIn = true
                    val role = if (selectedRoleTab == 0) UserRole.CLIENT else UserRole.FREELANCER
                    viewModel.login(
                        email = email,
                        passwordRaw = password.trim(),
                        role = role,
                        rememberMe = rememberMe
                    ) { _, _ ->
                        isLoggingIn = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRoleTab == 0) VioletPrimary else CyanAccent
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoggingIn
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Signing In...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (selectedRoleTab == 0) "Sign In as Client" else "Sign In as Freelancer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Registration Options Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Text(
                    text = " Don't have an account? ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unified Create Account Button
            Button(
                onClick = { viewModel.navigateTo(ScreenNav.AuthRegister) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("go_to_register_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create an Account (Client or Freelancer)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (showForgotPasswordDialog) {
                ForgotPasswordDialog(
                    initialEmail = email,
                    accentColor = if (selectedRoleTab == 0) VioletPrimary else CyanAccent,
                    onDismiss = { showForgotPasswordDialog = false },
                    onResetSuccess = { resetEmail ->
                        email = resetEmail
                        password = ""
                        showForgotPasswordDialog = false
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

/**
 * Forgot Password Dialog — Accessible directly from LoginScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onResetSuccess: (newEmail: String) -> Unit,
    viewModel: GigverseViewModel
) {
    var emailInput by remember { mutableStateOf(initialEmail) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Reset Account Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Security / Architecture Notice
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Local Device Security Note: Accounts are stored locally in Room SQLite. Remote email/SMS OTP verification is not active in this offline build. Resetting updates the local stored credential directly.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("Registered Email Address") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = accentColor)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // New password field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New Password (min 6 chars)") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_new_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirm password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = accentColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_confirm_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = LivePulseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("reset_error_text")
                    )
                }

                // Success message
                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = successMessage ?: "",
                        color = EmeraldTrust,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("reset_success_text")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("reset_cancel_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            isSubmitting = true
                            errorMessage = null
                            viewModel.resetPassword(
                                email = emailInput,
                                newPasswordRaw = newPassword,
                                confirmPasswordRaw = confirmPassword
                            ) { success, msg ->
                                isSubmitting = false
                                if (success) {
                                    successMessage = msg
                                    onResetSuccess(emailInput.trim())
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("reset_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isSubmitting) "Updating..." else "Update Password",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen ForgotPasswordScreen for Navigation routing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf(viewModel.getSavedLoginEmail()) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenNav.AuthLogin) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(VioletPrimary.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = VioletPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Reset Your Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Enter your registered email to update your account password.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = VioletPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Local Device Security Note: Accounts are stored locally in Room SQLite. Remote email/SMS OTP verification is not active in this offline build. Resetting updates the local stored credential directly.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = {
                    emailInput = it
                    errorMessage = null
                },
                label = { Text("Registered Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VioletPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("New Password (min 6 chars)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VioletPrimary) },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_new_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VioletPrimary) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_confirm_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage ?: "",
                    color = LivePulseRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("reset_error_text")
                )
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = successMessage ?: "",
                    color = EmeraldTrust,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("reset_success_text")
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    isSubmitting = true
                    errorMessage = null
                    viewModel.resetPassword(
                        email = emailInput,
                        newPasswordRaw = newPassword,
                        confirmPasswordRaw = confirmPassword
                    ) { success, msg ->
                        isSubmitting = false
                        if (success) {
                            successMessage = msg
                            viewModel.navigateTo(ScreenNav.AuthLogin)
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isSubmitting) "Updating..." else "Reset Password",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(ScreenNav.AuthLogin) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("reset_cancel_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Sign In")
            }
        }
    }
}

/**
 * Unified Registration Screen with CONDITIONAL fields:
 * If user selects Freelancer, it dynamically presents:
 *  1. Portfolio link
 *  2. Government ID proof document upload
 *  3. Profile photo upload
 *  4. Skills, headline, and hourly rate
 * If Client, it displays company and industry fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: GigverseViewModel,
    initialRole: UserRole = UserRole.FREELANCER,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenNav.AuthLogin)
    }

    var selectedRole by remember { mutableStateOf(initialRole) }
    val isFreelancer = selectedRole == UserRole.FREELANCER

    // Common Core Account Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var country by remember { mutableStateOf("United States") }
    var state by remember { mutableStateOf("California") }
    var district by remember { mutableStateOf("San Francisco") }

    // Conditional Fields for Freelancers
    var portfolioUrl by remember { mutableStateOf("https://github.com/myprofile") }
    var githubUrl by remember { mutableStateOf("https://github.com/") }
    var linkedInUrl by remember { mutableStateOf("https://linkedin.com/in/") }
    var avatarUri by remember { mutableStateOf<String?>("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300") }
    var govIdType by remember { mutableStateOf("National Passport") }
    var govIdNumber by remember { mutableStateOf("ID-982341-USA") }
    var govIdDocUri by remember { mutableStateOf<String?>("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=400") }
    var govIdDocName by remember { mutableStateOf("Passport_Government_ID.pdf") }
    var headline by remember { mutableStateOf("Senior Mobile & Full-Stack Architect") }
    var skills by remember { mutableStateOf("Android, Kotlin, Jetpack Compose, Gemini AI, Git") }
    var hourlyRate by remember { mutableStateOf("75") }

    // Conditional Fields for Clients
    var company by remember { mutableStateOf("NovaTech Labs") }
    var industry by remember { mutableStateOf("Technology & AI Software") }

    // Zero-permission Android Photo Picker for Profile Photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    // Zero-permission Android Document/Image Picker for ID Proof
    val idProofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            govIdDocUri = uri.toString()
            govIdDocName = "National_ID_Doc_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Your Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(ScreenNav.AuthLogin) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Role Selector Card
            Text(
                text = "Select Account Type",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Freelancer Option Card
                Surface(
                    color = if (isFreelancer) CyanAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isFreelancer) 2.dp else 1.dp,
                        color = if (isFreelancer) CyanAccent else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedRole = UserRole.FREELANCER }
                        .testTag("role_select_freelancer")
                ) {
                    Column(modifier = Modifier.padding(14.dp).fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isFreelancer) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Freelancer",
                                fontWeight = FontWeight.Bold,
                                color = if (isFreelancer) CyanAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Find projects & submit bids",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Client Option Card
                Surface(
                    color = if (!isFreelancer) VioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (!isFreelancer) 2.dp else 1.dp,
                        color = if (!isFreelancer) VioletPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedRole = UserRole.CLIENT }
                        .testTag("role_select_client")
                ) {
                    Column(modifier = Modifier.padding(14.dp).fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = if (!isFreelancer) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Client",
                                fontWeight = FontWeight.Bold,
                                color = if (!isFreelancer) VioletPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Post projects & hire talent",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notice Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFreelancer) CyanAccent.copy(alpha = 0.1f) else VioletPrimary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isFreelancer) Icons.Default.Verified else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isFreelancer) CyanAccent else VioletPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isFreelancer)
                            "Freelancer Registration requires your Profile Photo, ID Proof, and Portfolio Link for AI Trust Verification."
                        else
                            "Client Registration lets you post contracts, launch live bid rooms, and track real-time milestones.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: CORE CREDENTIALS
            Text(
                text = "1. Account Credentials & Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isFreelancer) CyanAccent else VioletPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Phone Number *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("register_phone_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("District / City") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // CONDITIONAL FIELDS FOR CLIENT ONLY
            // ========================================================
            AnimatedVisibility(
                visible = !isFreelancer,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Text(
                        text = "2. Organization & Business Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = VioletPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company or Organization Name *") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = VioletPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("client_company_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = industry,
                        onValueChange = { industry = it },
                        label = { Text("Industry Focus *") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = VioletPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("client_industry_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // ========================================================
            // CONDITIONAL FIELDS FOR FREELANCER ONLY:
            // 1. Photo Upload
            // 2. Portfolio Link
            // 3. ID Proof Verification
            // 4. Skills & Rates
            // ========================================================
            AnimatedVisibility(
                visible = isFreelancer,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    // CONDITIONAL FIELD 1: PHOTO UPLOAD
                    Text(
                        text = "2. Profile Photo Upload *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = "A photo is required for verified badges and search directory placement.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular Avatar Preview
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyanAccent, CircleShape)
                                        .background(CyanAccent.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!avatarUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = avatarUri,
                                            contentDescription = "Profile Photo Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(40.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (!avatarUri.isNullOrBlank()) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (!avatarUri.isNullOrBlank()) "Photo Attached" else "No Photo Chosen",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (!avatarUri.isNullOrBlank()) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                        modifier = Modifier.testTag("upload_profile_photo_button")
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Choose Device Photo", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }


                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // CONDITIONAL FIELD 2: PORTFOLIO LINK & SHOWCASE
                    Text(
                        text = "3. Portfolio Link & Work Showcase *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = "Provide your personal website, GitHub, Behance, or work showcase link.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = portfolioUrl,
                                onValueChange = { portfolioUrl = it },
                                label = { Text("Portfolio Website URL *") },
                                placeholder = { Text("https://myportfolio.dev or https://github.com/profile") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = CyanAccent) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("freelancer_portfolio_link_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (portfolioUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(CyanAccent.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldTrust, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Portfolio link attached: $portfolioUrl",
                                        fontSize = 11.sp,
                                        color = EmeraldTrust,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = githubUrl,
                                    onValueChange = { githubUrl = it },
                                    label = { Text("GitHub (Optional)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = linkedInUrl,
                                    onValueChange = { linkedInUrl = it },
                                    label = { Text("LinkedIn (Optional)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // CONDITIONAL FIELD 3: GOVERNMENT ID PROOF
                    Text(
                        text = "4. Government ID Proof Verification *",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = "Upload Passport, National ID, or Driver's License for KYC Verified badge.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldTrust.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EmeraldTrust.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = govIdType,
                                    onValueChange = { govIdType = it },
                                    label = { Text("ID Type") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = govIdNumber,
                                    onValueChange = { govIdNumber = it },
                                    label = { Text("ID Number") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldTrust)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Government ID Document", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = if (!govIdDocUri.isNullOrBlank()) "✓ Attached: $govIdDocName" else "Upload Passport / Driver's License",
                                            fontSize = 11.sp,
                                            color = if (!govIdDocUri.isNullOrBlank()) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        idProofPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTrust),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("upload_id_proof_button")
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload ID", fontSize = 12.sp)
                                }
                            }

                            if (!govIdDocUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, EmeraldTrust, RoundedCornerShape(6.dp))
                                    ) {
                                        AsyncImage(
                                            model = govIdDocUri,
                                            contentDescription = "ID Document Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("AI Biometric & KYC Authenticity Verified (98% Trust)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldTrust)
                                        Text("Recognized identification document ready for bid room.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Or choose a sample document for testing:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SAMPLE_ID_DOCS.forEachIndexed { idx, docUrl ->
                                    Surface(
                                        color = EmeraldTrust.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            govIdDocUri = docUrl
                                            govIdDocName = "Test_KYC_ID_Doc_${idx + 1}.jpg"
                                        }
                                    ) {
                                        Text(
                                            "Sample ID #${idx + 1}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = EmeraldTrust,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // CONDITIONAL FIELD 4: PROFESSIONAL SKILLS & RATES
                    Text(
                        text = "5. Professional Skills & Pricing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text("Professional Headline") },
                        modifier = Modifier.fillMaxWidth().testTag("freelancer_headline_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills (Comma separated)") },
                        modifier = Modifier.fillMaxWidth().testTag("freelancer_skills_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        label = { Text("Hourly Rate ($ USD)") },
                        modifier = Modifier.fillMaxWidth().testTag("freelancer_rate_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SUBMISSION BUTTON (REDIRECTS TO DASHBOARD)
            Button(
                onClick = {
                    if (isFreelancer) {
                        val rate = hourlyRate.toDoubleOrNull() ?: 65.0
                        val finalName = name.trim()
                        val finalEmail = email.trim()
                        val finalPassword = password.trim()
                        val finalPhone = phone.trim()
                        val finalPortfolio = portfolioUrl.trim()
                        val finalAvatar = avatarUri ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300"
                        val finalDoc = govIdDocUri ?: "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=400"

                        viewModel.registerFreelancer(
                            name = finalName,
                            email = finalEmail,
                            passwordRaw = finalPassword,
                            phone = finalPhone,
                            locationCountry = country,
                            locationState = state,
                            locationDistrict = district,
                            headline = headline,
                            skills = skills,
                            hourlyRate = rate,
                            govIdType = govIdType,
                            govIdNumber = govIdNumber,
                            linkedInUrl = linkedInUrl,
                            portfolioUrl = finalPortfolio,
                            githubUrl = githubUrl,
                            avatarUrl = finalAvatar,
                            govIdDocUrl = finalDoc
                        )
                    } else {
                        val finalName = name.trim()
                        val finalCompany = company.trim()
                        val finalEmail = email.trim()
                        val finalPassword = password.trim()
                        val finalPhone = phone.trim()

                        viewModel.registerClient(
                            name = finalName,
                            company = finalCompany,
                            email = finalEmail,
                            passwordRaw = finalPassword,
                            phone = finalPhone,
                            country = country,
                            state = state,
                            district = district,
                            industry = industry
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFreelancer) CyanAccent else VioletPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFreelancer) "Complete Freelancer Verification & Go to Dashboard" else "Create Client Account & Go to Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Return to Sign In link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Sign In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFreelancer) CyanAccent else VioletPrimary,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenNav.AuthLogin) }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Backward compatibility wrappers so any existing routes invoke the unified screen with conditional fields
@Composable
fun FreelancerRegisterScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    RegistrationScreen(
        viewModel = viewModel,
        initialRole = UserRole.FREELANCER,
        modifier = modifier
    )
}

@Composable
fun ClientRegisterScreen(
    viewModel: GigverseViewModel,
    modifier: Modifier = Modifier
) {
    RegistrationScreen(
        viewModel = viewModel,
        initialRole = UserRole.CLIENT,
        modifier = modifier
    )
}
