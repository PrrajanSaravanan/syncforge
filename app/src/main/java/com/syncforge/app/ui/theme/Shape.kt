package com.syncforge.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SyncForgeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Named radii used directly by bespoke surfaces. */
object Radii {
    val pill = RoundedCornerShape(999.dp)
    val chip = RoundedCornerShape(10.dp)
    val card = RoundedCornerShape(20.dp)
    val cardLarge = RoundedCornerShape(26.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val bubbleMine = RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
    val bubbleTheirs = RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
}
