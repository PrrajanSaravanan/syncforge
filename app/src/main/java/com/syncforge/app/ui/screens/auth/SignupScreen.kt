package com.syncforge.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncforge.app.ui.components.AuroraBackground
import com.syncforge.app.ui.components.Eyebrow
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeIconButton
import com.syncforge.app.ui.components.ForgePrimaryButton
import com.syncforge.app.ui.components.ForgeProgressBar
import com.syncforge.app.ui.components.GlassCard
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val roles = listOf(
    "Engineer", "Engineering Manager", "Designer",
    "Product Manager", "SRE", "Data Engineer",
)

@Composable
fun SignupScreen(
    onCreated: () -> Unit,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val t = LocalTokens.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(roles.first()) }
    var org by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // Simple local strength meter — four heuristics, no validation service.
    val strength = remember(password) {
        var score = 0f
        if (password.length >= 8) score += 0.3f
        if (password.any { it.isDigit() }) score += 0.23f
        if (password.any { !it.isLetterOrDigit() }) score += 0.24f
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score += 0.23f
        score.coerceIn(0f, 1f)
    }
    val strengthLabel = when {
        password.isEmpty() -> "Use 8+ characters with a number and a symbol"
        strength < 0.4f -> "Weak password"
        strength < 0.75f -> "Fair password"
        else -> "Strong password"
    }
    val strengthColor = when {
        strength < 0.4f -> t.danger
        strength < 0.75f -> t.warning
        else -> t.success
    }

    AuroraBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = Space.lg),
        ) {
            Spacer(Modifier.height(Space.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ForgeIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to sign in",
                    onClick = onBack,
                )
                Spacer(Modifier.width(Space.sm))
                Column {
                    Eyebrow("Step 1 of 1")
                    Text(
                        "Create your account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.textPrimary,
                    )
                }
            }

            Spacer(Modifier.height(Space.xs))
            Text(
                "Everything stays on this device — no account is created on a server.",
                style = MaterialTheme.typography.bodySmall,
                color = t.textTertiary,
            )

            Spacer(Modifier.height(Space.lg))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 6 },
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radii.cardLarge,
                    contentPadding = Space.lg,
                    strong = true,
                ) {
                    Column {
                        AuthField(
                            label = "Full name",
                            value = name,
                            onValueChange = { name = it; nameError = null },
                            icon = Icons.Rounded.PersonOutline,
                            placeholder = "Ada Lovelace",
                            error = nameError,
                        )
                        Spacer(Modifier.height(Space.md))
                        AuthField(
                            label = "Work email",
                            value = email,
                            onValueChange = { email = it; emailError = null },
                            icon = Icons.Rounded.MailOutline,
                            placeholder = "you@company.com",
                            keyboardType = KeyboardType.Email,
                            error = emailError,
                        )
                        Spacer(Modifier.height(Space.md))
                        AuthField(
                            label = "Password",
                            value = password,
                            onValueChange = { password = it; passwordError = null },
                            icon = Icons.Rounded.Lock,
                            placeholder = "Create a password",
                            isPassword = true,
                            error = passwordError,
                        )

                        // Live strength meter sits under the password field.
                        Spacer(Modifier.height(Space.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ForgeProgressBar(
                                progress = strength,
                                modifier = Modifier.weight(1f),
                                height = 4.dp,
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(strengthColor, strengthColor),
                                ),
                            )
                            Spacer(Modifier.width(Space.xs))
                            Text(
                                strengthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (password.isEmpty()) t.textTertiary else strengthColor,
                            )
                        }

                        Spacer(Modifier.height(Space.md))
                        Text(
                            "Role",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = t.textSecondary,
                        )
                        Spacer(Modifier.height(Space.xs))
                        // Roles are a small fixed set, so chips beat a dropdown here.
                        Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                            roles.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                                    pair.forEach { option ->
                                        ForgeChip(
                                            label = option,
                                            selected = role == option,
                                            onClick = { role = option },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        Spacer(Modifier.height(Space.md))
                        AuthField(
                            label = "Organization",
                            value = org,
                            onValueChange = { org = it },
                            icon = Icons.Rounded.Business,
                            placeholder = "Northwind Labs",
                            imeAction = ImeAction.Done,
                            helper = "Used as your workspace name",
                        )

                        Spacer(Modifier.height(Space.lg))
                        ForgePrimaryButton(
                            text = "Create account",
                            icon = Icons.Rounded.Badge,
                            loading = loading,
                            onClick = {
                                nameError = if (name.isBlank()) "Enter your name" else null
                                emailError = if (!email.contains('@')) "Enter a valid email" else null
                                passwordError = when {
                                    password.length < 8 -> "Use at least 8 characters"
                                    else -> null
                                }
                                if (nameError != null || emailError != null || passwordError != null) {
                                    onMessage("Please fix the highlighted fields")
                                    return@ForgePrimaryButton
                                }
                                scope.launch {
                                    loading = true
                                    delay(1_000)
                                    loading = false
                                    onCreated()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.lg))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = t.textTertiary,
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "Sign in",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = t.accent,
                    modifier = Modifier
                        .clip(Radii.pill)
                        .springPress(pressedScale = 0.94f, onClick = onBack)
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(Space.xl))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "By continuing you accept the demo terms.",
                    style = MaterialTheme.typography.labelSmall,
                    color = t.textTertiary.copy(alpha = 0.65f),
                )
            }
            Spacer(Modifier.height(Space.xl))
        }
    }
}
