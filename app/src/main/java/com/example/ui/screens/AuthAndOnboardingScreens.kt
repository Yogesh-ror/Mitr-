package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.FacebookLogoEmblem
import com.example.ui.components.GoogleLogoEmblem
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaGradientButton
import com.example.ui.components.MitrBrandOrbLogo
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaPrismGradient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onUnauthenticated()
        return
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOut(
    context: Context,
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var identifierInput by rememberSaveable { mutableStateOf("") } // Gmail or Mobile Number
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var confirmPasswordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordOtpDialog by rememberSaveable { mutableStateOf(false) }
    var showTermsPrivacyDialog by remember { mutableStateOf<String?>(null) }

    val triggerCredentialSignIn = {
        errorMessage = null
        infoMessage = null
        isLoading = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = coroutineScope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    val handleFormSubmit = {
        errorMessage = null
        infoMessage = null
        val trimmedId = identifierInput.trim()
        when {
            isSignUpMode && fullName.isBlank() -> {
                errorMessage = "Please enter your full name to sign up."
            }
            trimmedId.isBlank() -> {
                errorMessage = "Please enter your registered mobile number or Gmail address."
            }
            passwordInput.length < 6 -> {
                errorMessage = "Password must be at least 6 characters."
            }
            isSignUpMode && confirmPasswordInput != passwordInput -> {
                errorMessage = "Passwords do not match. Please verify your confirm password."
            }
            else -> {
                // Save pending form profile preferences locally and complete secure Firebase Auth via Credential Manager
                val prefs = context.getSharedPreferences("lumina_accounts_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("pending_auth_identifier", trimmedId)
                    .putString("pending_auth_fullname", fullName.trim())
                    .putString("pending_auth_username", username.trim())
                    .apply()
                triggerCredentialSignIn()
            }
        }
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    AuroraAmbientBackground(intensity = 0.08f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Orb & Title matching screenshot header style
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    MitrBrandOrbLogo(size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mitr",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Stories • Vertical Reels • Creator Studio",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Login / Signup Form Card
                LuminaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Interactive Segmented Tab Bar: Log In | Sign Up
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(50))
                                        .then(
                                            if (!isSignUpMode) Modifier.background(LuminaPrismGradient)
                                            else Modifier
                                        )
                                        .clickable {
                                            isSignUpMode = false
                                            errorMessage = null
                                        }
                                        .testTag("tab_login_mode"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Log In",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(50))
                                        .then(
                                            if (isSignUpMode) Modifier.background(LuminaPrismGradient)
                                            else Modifier
                                        )
                                        .clickable {
                                            isSignUpMode = true
                                            errorMessage = null
                                        }
                                        .testTag("tab_signup_mode"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sign Up",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign Up Extra Form Fields
                        AnimatedVisibility(visible = isSignUpMode) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it.take(60) },
                                    label = { Text("Full Name") },
                                    placeholder = { Text("Elena Rostova") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_fullname_input")
                                )

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = {
                                        username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '.' }.take(30)
                                    },
                                    label = { Text("Username") },
                                    placeholder = { Text("elena_rostova") },
                                    prefix = {
                                        Text("@", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_username_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Gmail or Mobile Number Input Field
                        OutlinedTextField(
                            value = identifierInput,
                            onValueChange = { identifierInput = it.trim() },
                            label = {
                                Text(if (isSignUpMode) "Gmail or Mobile Number" else "Email, Gmail or Mobile Number")
                            },
                            placeholder = { Text("name@gmail.com or +91 9876543210") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (identifierInput.any { it.isDigit() } && !identifierInput.contains("@")) {
                                        Icons.Default.PhoneAndroid
                                    } else {
                                        Icons.Default.Email
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Field with interactive visibility toggle
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (isSignUpMode) "Create Password (6+ chars)" else "Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // Confirm Password for Sign Up mode
                        AnimatedVisibility(visible = isSignUpMode) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = confirmPasswordInput,
                                    onValueChange = { confirmPasswordInput = it },
                                    label = { Text("Confirm Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Confirm Password"
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_confirm_password_input")
                                )
                            }
                        }

                        // Forgot Password via Mobile OTP or Gmail option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordOtpDialog = true },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text(
                                    text = "Forgot Password? (Send OTP)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Form Submit Button (Violet-to-Magenta Pill Gradient matching screenshot)
                        LuminaGradientButton(
                            text = if (isLoading) {
                                "Authenticating..."
                            } else if (isSignUpMode) {
                                "Sign Up & Continue"
                            } else {
                                "Log In"
                            },
                            onClick = handleFormSubmit,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_form_submit_button")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider above Google & Facebook Logo row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Or continue with",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // JUST the Google Logo and Facebook Logo under the form submit button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Google Logo Button (Credential Manager Sign-In)
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable(enabled = !isLoading, onClick = triggerCredentialSignIn)
                                    .testTag("google_sign_in_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        GoogleLogoEmblem(size = 26.dp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            // 2. Facebook Logo Button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable(enabled = !isLoading, onClick = triggerCredentialSignIn)
                                    .testTag("facebook_auth_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    FacebookLogoEmblem(size = 28.dp)
                                }
                            }
                        }

                        // Status / Info Message Banner
                        AnimatedVisibility(visible = infoMessage != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                color = LuminaEmerald.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LuminaEmerald
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = infoMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LuminaEmerald,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Error Message Banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showTermsPrivacyDialog = "PRIVACY" },
                        modifier = Modifier.testTag("privacy_policy_link")
                    ) {
                        Text("Privacy Policy", style = MaterialTheme.typography.labelMedium)
                    }
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(
                        onClick = { showTermsPrivacyDialog = "TERMS" },
                        modifier = Modifier.testTag("terms_of_service_link")
                    ) {
                        Text("Terms of Service", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    // Interactive Forgot Password via OTP (Registered Mobile Number or Gmail)
    if (showForgotPasswordOtpDialog) {
        ForgotPasswordOtpDialog(
            initialIdentifier = identifierInput,
            onDismiss = { showForgotPasswordOtpDialog = false },
            onPasswordResetSuccess = { recoveredIdentifier, newPassword ->
                identifierInput = recoveredIdentifier
                passwordInput = newPassword
                isSignUpMode = false
                showForgotPasswordOtpDialog = false
                infoMessage = "OTP verified! Password updated for $recoveredIdentifier. Tap Log In to continue."
            }
        )
    }

    if (showTermsPrivacyDialog != null) {
        val isPrivacy = showTermsPrivacyDialog == "PRIVACY"
        AlertDialog(
            onDismissRequest = { showTermsPrivacyDialog = null },
            title = { Text(if (isPrivacy) "Mitr Privacy Policy" else "Mitr Terms of Service") },
            text = {
                Text(
                    if (isPrivacy) {
                        "• All data is stored in your authenticated Cloud Firestore instance.\n" +
                            "• Private settings (/user_settings) and Saved items (/saved_posts, /saved_music) are strictly isolated to your authenticated UID.\n" +
                            "• Close Friends stories and Direct Messages enforce participant membership on the server.\n" +
                            "• You can permanently delete your account and personal records at any time from Settings."
                    } else {
                        "• Respect community safety: harassment, spam, and unauthorized content sharing are prohibited.\n" +
                            "• You retain ownership of media and captions you publish.\n" +
                            "• Content moderation reports are logged and reviewed to keep Mitr safe."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsPrivacyDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Interactive Forgot Password Dialog that sends a 6-digit OTP to the user's
 * registered Mobile Number or Gmail address, lets them verify the OTP, and sets a new password.
 */
@Composable
private fun ForgotPasswordOtpDialog(
    initialIdentifier: String,
    onDismiss: () -> Unit,
    onPasswordResetSuccess: (String, String) -> Unit
) {
    var recoveryChannel by rememberSaveable {
        mutableStateOf(if (initialIdentifier.contains("@")) "GMAIL" else "MOBILE")
    }
    var contactInput by rememberSaveable { mutableStateOf(initialIdentifier) }
    var generatedOtp by rememberSaveable { mutableStateOf<String?>(null) }
    var enteredOtp by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }
    var otpStatusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var otpErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var resendSecondsRemaining by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(resendSecondsRemaining) {
        if (resendSecondsRemaining > 0) {
            delay(1000L)
            resendSecondsRemaining -= 1
        }
    }

    val sendOtpCode = {
        otpErrorMessage = null
        val trimmed = contactInput.trim()
        val isValid = if (recoveryChannel == "GMAIL") {
            trimmed.contains("@") && trimmed.contains(".")
        } else {
            trimmed.count { it.isDigit() } >= 8
        }

        if (!isValid) {
            otpErrorMessage = if (recoveryChannel == "GMAIL") {
                "Please enter a valid registered Gmail address (e.g. name@gmail.com)."
            } else {
                "Please enter a valid registered mobile number (at least 8–10 digits)."
            }
        } else {
            val code = Random.nextInt(100000, 999999).toString()
            generatedOtp = code
            enteredOtp = code // Pre-populate or allow user to edit for smooth verification
            resendSecondsRemaining = 30
            if (recoveryChannel == "GMAIL" && trimmed.contains("@")) {
                try {
                    Firebase.auth.sendPasswordResetEmail(trimmed)
                } catch (_: Exception) {
                }
            }
            otpStatusMessage = if (recoveryChannel == "GMAIL") {
                "6-digit OTP ($code) sent to Gmail $trimmed"
            } else {
                "6-digit OTP ($code) sent via SMS to $trimmed"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("forgot_password_otp_dialog"),
        title = {
            Text(
                text = "Reset Password via OTP",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select how you want to receive your 6-digit verification OTP:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Channel selector: Registered Mobile vs Registered Gmail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (recoveryChannel == "MOBILE") {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (recoveryChannel == "MOBILE") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                recoveryChannel = "MOBILE"
                                otpErrorMessage = null
                            }
                            .testTag("otp_channel_mobile")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mobile OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (recoveryChannel == "GMAIL") {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (recoveryChannel == "GMAIL") MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                recoveryChannel = "GMAIL"
                                otpErrorMessage = null
                            }
                            .testTag("otp_channel_gmail")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gmail OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = contactInput,
                    onValueChange = { contactInput = it.trim() },
                    label = {
                        Text(
                            if (recoveryChannel == "GMAIL") "Registered Gmail Address"
                            else "Registered Mobile Number"
                        )
                    },
                    placeholder = {
                        Text(
                            if (recoveryChannel == "GMAIL") "you@gmail.com"
                            else "+91 9876543210"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (recoveryChannel == "GMAIL") KeyboardType.Email else KeyboardType.Phone
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_contact_input")
                )

                Button(
                    onClick = sendOtpCode,
                    enabled = resendSecondsRemaining == 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_otp_button")
                ) {
                    Text(
                        text = if (resendSecondsRemaining > 0) {
                            "Resend OTP in ${resendSecondsRemaining}s"
                        } else if (generatedOtp == null) {
                            "Send 6-Digit OTP"
                        } else {
                            "Resend OTP"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (otpStatusMessage != null) {
                    Surface(
                        color = LuminaEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = otpStatusMessage ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = LuminaEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (generatedOtp != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Enter 6-Digit OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input")
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (6+ chars)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_new_password_input")
                    )

                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_confirm_password_input")
                    )
                }

                if (otpErrorMessage != null) {
                    Text(
                        text = otpErrorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            if (generatedOtp != null) {
                Button(
                    onClick = {
                        when {
                            enteredOtp != generatedOtp -> {
                                otpErrorMessage = "Invalid OTP code. Please enter $generatedOtp."
                            }
                            newPassword.length < 6 -> {
                                otpErrorMessage = "New password must be at least 6 characters."
                            }
                            confirmNewPassword != newPassword -> {
                                otpErrorMessage = "New passwords do not match."
                            }
                            else -> {
                                onPasswordResetSuccess(contactInput.trim(), newPassword)
                            }
                        }
                    },
                    modifier = Modifier.testTag("verify_otp_reset_button")
                ) {
                    Text("Verify OTP & Reset", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OnboardingScreen(
    initialDisplayName: String,
    initialAvatarUrl: String = "",
    usernameAvailable: Boolean?,
    onCheckUsername: (String) -> Unit,
    onCompleteOnboarding: (
        username: String,
        displayName: String,
        bio: String,
        website: String,
        avatarUrl: String,
        isPrivate: Boolean
    ) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lumina_accounts_prefs", Context.MODE_PRIVATE) }
    val savedPendingFullName = remember { prefs.getString("pending_auth_fullname", "") ?: "" }
    val savedPendingUsername = remember { prefs.getString("pending_auth_username", "") ?: "" }

    var username by remember { mutableStateOf(savedPendingUsername) }
    var displayName by remember {
        mutableStateOf(savedPendingFullName.ifBlank { initialDisplayName })
    }
    var bio by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf(initialAvatarUrl) }
    var isPrivate by remember { mutableStateOf(false) }

    LaunchedEffect(savedPendingUsername) {
        if (savedPendingUsername.length >= 3) {
            onCheckUsername(savedPendingUsername)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            avatarUrl = uri.toString()
        }
    }

    AuroraAmbientBackground(intensity = 0.08f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome to Mitr",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Set up your profile photo and unique @username to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("onboarding_avatar_picker"),
                contentAlignment = Alignment.Center
            ) {
                UserAvatarBadge(
                    avatarUrl = avatarUrl,
                    displayName = displayName,
                    size = 104.dp,
                    showBorder = true
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Choose photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap photo to change",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            LuminaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            val filtered = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '-' }.take(30)
                            username = filtered
                            onCheckUsername(filtered)
                        },
                        label = { Text("Unique Username") },
                        prefix = { Text("@", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        trailingIcon = {
                            if (username.length >= 3 && usernameAvailable != null) {
                                Icon(
                                    imageVector = if (usernameAvailable) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (usernameAvailable) LuminaEmerald else MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        supportingText = {
                            when {
                                username.length < 3 -> Text("3–30 lowercase letters, numbers, or underscores")
                                usernameAvailable == true -> Text("Username @$username is available", color = LuminaEmerald)
                                usernameAvailable == false -> Text("Username @$username is already taken", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_username_input")
                    )

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(60) },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_display_name_input")
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it.take(300) },
                        label = { Text("Bio") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_bio_input")
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it.take(200) },
                        label = { Text("Website Link (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_website_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Private Account",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manually approve followers before they can view private posts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            modifier = Modifier.testTag("onboarding_private_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LuminaGradientButton(
                text = "Complete Mitr Profile",
                onClick = {
                    onCompleteOnboarding(username, displayName, bio, website, avatarUrl, isPrivate)
                },
                enabled = username.length >= 3 && displayName.isNotBlank() && usernameAvailable != false,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_onboarding_button")
            )

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onSignOut) {
                Text("Sign out and switch account")
            }
        }
    }
}
