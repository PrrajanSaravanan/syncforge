package com.syncforge.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
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
import com.syncforge.app.ui.components.GlassCard
import com.syncforge.app.ui.components.ForgePrimaryButton
import com.syncforge.app.ui.components.ForgeSecondaryButton
import com.syncforge.app.ui.components.springPress
import com.syncforge.app.ui.screens.splash.ForgeMark
import com.syncforge.app.ui.theme.LocalTokens
import com.syncforge.app.ui.theme.Radii
import com.syncforge.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignIn: () -> Unit,
    onGuest: () -> Unit,
    onSignUp: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val t = LocalTokens.current
    val scope = rememberCoroutineScope()

    // Pre-filled with the demo identity so the prototype is one tap from the app.
    var email by remember { mutableStateOf("aarav.mehta@syncforge.io") }
    var password by remember { mutableStateOf("forge-demo") }
    var remember_ by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    AuroraBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Space.xxxl))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 4 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(66.dp)
                            .clip(Radii.card)
                            .background(t.accentGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        ForgeMark(Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(Space.md))
                    Text(
                        "Welcome back",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = t.textPrimary,
                    )
                    Spacer(Modifier.height(Space.xxs))
                    Text(
                        "Sign in to your engineering workspace",
                        style = MaterialTheme.typography.bodyMedium,
                        color = t.textTertiary,
                    )
                }
            }

            Spacer(Modifier.height(Space.xl))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 150)) +
                    slideInVertically(tween(700, delayMillis = 150)) { it / 5 },
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radii.cardLarge,
                    contentPadding = Space.lg,
                    strong = true,
                    glow = t.accent,
                ) {
                    Column {
                        AuthField(
                            label = "Work email",
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            icon = Icons.Rounded.MailOutline,
                            placeholder = "you@company.com",
                            keyboardType = KeyboardType.Email,
                            error = emailError,
                        )
                        Spacer(Modifier.height(Space.md))
                        AuthField(
                            label = "Password",
                            value = password,
                            onValueChange = { password = it },
                            icon = Icons.Rounded.Lock,
                            placeholder = "Enter your password",
                            isPassword = true,
                            imeAction = ImeAction.Done,
                        )

                        Spacer(Modifier.height(Space.sm))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ForgeCheckbox(
                                checked = remember_,
                                onCheckedChange = { remember_ = it },
                                label = "Remember me",
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Forgot password?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = t.accent,
                                modifier = Modifier
                                    .clip(Radii.pill)
                                    .springPress(pressedScale = 0.94f) {
                                        onMessage("Password reset is disabled in this prototype")
                                    }
                                    .padding(horizontal = 6.dp, vertical = 5.dp),
                            )
                        }

                        Spacer(Modifier.height(Space.lg))
                        ForgePrimaryButton(
                            text = "Sign in",
                            icon = Icons.AutoMirrored.Rounded.ArrowForward,
                            loading = loading,
                            onClick = {
                                if (!email.contains('@')) {
                                    emailError = "Enter a valid work email address"
                                    return@ForgePrimaryButton
                                }
                                scope.launch {
                                    loading = true
                                    delay(900)          // simulated handshake
                                    loading = false
                                    onSignIn()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(Space.md))
                        OrDivider()
                        Spacer(Modifier.height(Space.md))
                        SocialRow(
                            onProvider = { onMessage("$it sign-in is UI only in this build") },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.lg))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 300)),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ForgeSecondaryButton(
                        text = "Continue as guest",
                        onClick = onGuest,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Space.lg))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "New to SyncForge?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = t.textTertiary,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Create an account",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = t.accent,
                            modifier = Modifier
                                .clip(Radii.pill)
                                .springPress(pressedScale = 0.94f, onClick = onSignUp)
                                .padding(horizontal = 5.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.xxl))
            Text(
                "Frontend prototype · no data leaves this device",
                style = MaterialTheme.typography.labelSmall,
                color = t.textTertiary.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(Space.xl))
        }
    }
}
