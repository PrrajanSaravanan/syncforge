package com.syncforge.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Motion vocabulary for navigation. Expo-out easing (0.16, 1, 0.3, 1) is the
 * design system's signature curve; exits run shorter than enters so the incoming
 * screen always leads.
 */
private val ExpoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private const val ENTER = 380
private const val EXIT = 260

/** Tab switches: a quick cross-fade with a hair of scale, no lateral movement. */
fun tabEnter(): EnterTransition =
    fadeIn(tween(260, easing = ExpoOut)) +
        scaleIn(initialScale = 0.98f, animationSpec = tween(320, easing = ExpoOut))

fun tabExit(): ExitTransition =
    fadeOut(tween(180)) + scaleOut(targetScale = 0.99f, animationSpec = tween(200))

/** Push onto the stack: the detail slides in from the right, parent eases back. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.pushEnter(): EnterTransition =
    slideInHorizontally(tween(ENTER, easing = ExpoOut)) { (it * 0.22f).toInt() } +
        fadeIn(tween(ENTER, easing = ExpoOut))

fun AnimatedContentTransitionScope<NavBackStackEntry>.pushExit(): ExitTransition =
    slideOutHorizontally(tween(EXIT, easing = ExpoOut)) { -(it * 0.08f).toInt() } +
        fadeOut(tween(EXIT))

fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    slideInHorizontally(tween(ENTER, easing = ExpoOut)) { -(it * 0.08f).toInt() } +
        fadeIn(tween(ENTER, easing = ExpoOut))

fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    slideOutHorizontally(tween(EXIT, easing = ExpoOut)) { (it * 0.22f).toInt() } +
        fadeOut(tween(EXIT))

/** Modal-feeling routes (AI, settings) rise from below. */
fun modalEnter(): EnterTransition =
    fadeIn(tween(300, easing = ExpoOut)) +
        scaleIn(initialScale = 0.94f, animationSpec = tween(340, easing = ExpoOut))

fun modalExit(): ExitTransition =
    fadeOut(tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(240))

/** Auth → app handoff: a confident fade-through, no sliding. */
fun authEnter(): EnterTransition = fadeIn(tween(520, easing = ExpoOut))
fun authExit(): ExitTransition = fadeOut(tween(320))
