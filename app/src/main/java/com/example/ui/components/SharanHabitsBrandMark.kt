package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalExtendedColors

private val SharanBrandEmerald = Color(0xFF089068)
private val SharanBrandEmeraldHighlight = Color(0xFF10B981)
private val SharanBrandWhite = Color(0xFFFFFFFF)

/**
 * Crisp emerald green and white streak flame emblem featuring integrated SH initials,
 * matching the Sharan Habits adaptive launcher icon.
 */
@Composable
fun SharanFlameEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    val badgeShape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .clip(badgeShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SharanBrandEmeraldHighlight,
                        SharanBrandEmerald
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SharanBrandWhite.copy(alpha = 0.28f),
                shape = badgeShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.14f)
        ) {
            val w = this.size.width
            val h = this.size.height

            // Geometric streak flame silhouette in crisp white
            val flamePath = Path().apply {
                // Bottom center base
                moveTo(w * 0.50f, h * 0.96f)
                // Left lower belly curving out
                cubicTo(
                    w * 0.16f, h * 0.96f,
                    w * 0.06f, h * 0.68f,
                    w * 0.14f, h * 0.44f
                )
                // Small left lick tip
                cubicTo(
                    w * 0.17f, h * 0.35f,
                    w * 0.24f, h * 0.28f,
                    w * 0.27f, h * 0.24f
                )
                cubicTo(
                    w * 0.27f, h * 0.34f,
                    w * 0.32f, h * 0.40f,
                    w * 0.36f, h * 0.42f
                )
                // Main tall central flame peak
                cubicTo(
                    w * 0.33f, h * 0.24f,
                    w * 0.43f, h * 0.08f,
                    w * 0.54f, h * 0.02f
                )
                cubicTo(
                    w * 0.53f, h * 0.18f,
                    w * 0.62f, h * 0.30f,
                    w * 0.70f, h * 0.38f
                )
                // Right secondary flame tongue
                cubicTo(
                    w * 0.74f, h * 0.31f,
                    w * 0.77f, h * 0.25f,
                    w * 0.79f, h * 0.19f
                )
                cubicTo(
                    w * 0.87f, h * 0.34f,
                    w * 0.93f, h * 0.50f,
                    w * 0.91f, h * 0.68f
                )
                // Right belly returning to base
                cubicTo(
                    w * 0.89f, h * 0.86f,
                    w * 0.72f, h * 0.96f,
                    w * 0.50f, h * 0.96f
                )
                close()
            }

            drawPath(path = flamePath, color = SharanBrandWhite)
        }

        // Bold SH monogram positioned cleanly inside the lower flame bowl
        Text(
            text = "SH",
            color = SharanBrandEmerald,
            fontSize = (size.value * 0.31f).sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = size * 0.22f)
        )
    }
}

/**
 * Interactive in-app header badge pairing the emerald/white SH streak flame emblem
 * with the "Sharan Habits" wordmark.
 */
@Composable
fun SharanHabitsBrandBadge(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "brandBadgeScale"
    )

    Row(
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = onClick
            )
            .padding(end = 10.dp, top = 4.dp, bottom = 4.dp)
            .semantics { contentDescription = "Sharan Habits, jump to today" }
            .testTag("header_brand_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SharanFlameEmblem(size = 38.dp)

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "Sharan Habits",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                ),
                color = extendedColors.textPrimary
            )
            Text(
                text = "SH STREAK TRACKER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp
                ),
                color = SharanBrandEmeraldHighlight
            )
        }
    }
}
