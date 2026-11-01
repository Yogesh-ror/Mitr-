package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaGlassBorderGradient
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet

/**
 * Custom vector icons matching the latest modern social media iconography:
 * - Clapperboard Reels icon (Outlined & Filled)
 * - Messenger / Direct Chat bubble & Paper-Plane Send icons (Outlined & Filled)
 * - Rounded Home, Search, and Create Plus-Squircle icons
 * - Chat Bubble with Plus (top bar action icon from screenshots)
 */
object MitrIcons {

    val ReelsOutlined: ImageVector by lazy {
        ImageVector.Builder(
            name = "ReelsOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 3f)
                lineTo(17f, 3f)
                quadTo(21f, 3f, 21f, 7f)
                lineTo(21f, 17f)
                quadTo(21f, 21f, 17f, 21f)
                lineTo(7f, 21f)
                quadTo(3f, 21f, 3f, 17f)
                lineTo(3f, 7f)
                quadTo(3f, 3f, 7f, 3f)
                close()
                moveTo(3f, 8.5f)
                lineTo(21f, 8.5f)
                moveTo(8.5f, 3f)
                lineTo(10.8f, 8.5f)
                moveTo(14.5f, 3f)
                lineTo(16.8f, 8.5f)
                moveTo(10f, 11.5f)
                lineTo(15.5f, 14.7f)
                lineTo(10f, 17.9f)
                close()
            }
        }.build()
    }

    val ReelsFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "ReelsFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 3f)
                lineTo(17f, 3f)
                quadTo(21f, 3f, 21f, 7f)
                lineTo(21f, 17f)
                quadTo(21f, 21f, 17f, 21f)
                lineTo(7f, 21f)
                quadTo(3f, 21f, 3f, 17f)
                lineTo(3f, 7f)
                quadTo(3f, 3f, 7f, 3f)
                close()
                moveTo(3f, 8.5f)
                lineTo(21f, 8.5f)
                moveTo(8.5f, 3f)
                lineTo(10.8f, 8.5f)
                moveTo(14.5f, 3f)
                lineTo(16.8f, 8.5f)
            }
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9.8f, 11.2f)
                lineTo(16f, 14.7f)
                lineTo(9.8f, 18.2f)
                close()
            }
        }.build()
    }

    val MessageOutlined: ImageVector by lazy {
        ImageVector.Builder(
            name = "MessageOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 4f)
                lineTo(18f, 4f)
                quadTo(21f, 4f, 21f, 7f)
                lineTo(21f, 15f)
                quadTo(21f, 18f, 18f, 18f)
                lineTo(9f, 18f)
                lineTo(4.5f, 21f)
                lineTo(4.5f, 18f)
                quadTo(3f, 18f, 3f, 15f)
                lineTo(3f, 7f)
                quadTo(3f, 4f, 6f, 4f)
                close()
            }
        }.build()
    }

    val MessageFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "MessageFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.3f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 4f)
                lineTo(18f, 4f)
                quadTo(21f, 4f, 21f, 7f)
                lineTo(21f, 15f)
                quadTo(21f, 18f, 18f, 18f)
                lineTo(9f, 18f)
                lineTo(4.5f, 21f)
                lineTo(4.5f, 18f)
                quadTo(3f, 18f, 3f, 15f)
                lineTo(3f, 7f)
                quadTo(3f, 4f, 6f, 4f)
                close()
            }
        }.build()
    }

    val ChatPlusBubble: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChatPlusBubble",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3.2f)
                curveTo(7.0f, 3.2f, 3.0f, 6.8f, 3.0f, 11.4f)
                curveTo(3.0f, 13.9f, 4.2f, 16.1f, 6.2f, 17.6f)
                lineTo(5.2f, 20.8f)
                lineTo(8.8f, 19.2f)
                curveTo(9.8f, 19.5f, 10.9f, 19.6f, 12f, 19.6f)
                curveTo(17.0f, 19.6f, 21.0f, 16.0f, 21.0f, 11.4f)
                curveTo(21.0f, 6.8f, 17.0f, 3.2f, 12f, 3.2f)
                close()
                moveTo(12f, 8.6f)
                lineTo(12f, 14.2f)
                moveTo(9.2f, 11.4f)
                lineTo(14.8f, 11.4f)
            }
        }.build()
    }

    val PaperPlaneSend: ImageVector by lazy {
        ImageVector.Builder(
            name = "PaperPlaneSend",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(21.5f, 3.5f)
                lineTo(10.5f, 14.5f)
                moveTo(21.5f, 3.5f)
                lineTo(14.5f, 21.5f)
                lineTo(10.5f, 14.5f)
                lineTo(2.5f, 10.5f)
                close()
            }
        }.build()
    }

    val CreateSquarePlus: ImageVector by lazy {
        ImageVector.Builder(
            name = "CreateSquarePlus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7f, 3f)
                lineTo(17f, 3f)
                quadTo(21f, 3f, 21f, 7f)
                lineTo(21f, 17f)
                quadTo(21f, 21f, 17f, 21f)
                lineTo(7f, 21f)
                quadTo(3f, 21f, 3f, 17f)
                lineTo(3f, 7f)
                quadTo(3f, 3f, 7f, 3f)
                close()
                moveTo(12f, 8f)
                lineTo(12f, 16f)
                moveTo(8f, 12f)
                lineTo(16f, 12f)
            }
        }.build()
    }
}

/**
 * Authentic 4-Color Google "G" Logo drawn on Canvas for the Login/Signup social row.
 */
@Composable
fun GoogleLogoEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = this.size.minDimension * 0.20f
        val diameter = this.size.minDimension - stroke
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        val arcSize = Size(diameter, diameter)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 200f,
            sweepAngle = 115f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 65f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Blue right arc + horizontal bar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -15f,
            sweepAngle = 65f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        val cy = this.size.height / 2f
        val cx = this.size.width / 2f
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(this.size.width - stroke * 0.25f, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Butt
        )
    }
}

/**
 * Authentic Royal Blue Facebook "f" Logo drawn on Canvas for the Login/Signup social row.
 */
@Composable
fun FacebookLogoEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = Color(0xFF1877F2),
            radius = w / 2f,
            center = Offset(w / 2f, h / 2f)
        )
        val fPath = Path().apply {
            moveTo(w * 0.58f, h * 0.85f)
            lineTo(w * 0.58f, h * 0.53f)
            lineTo(w * 0.70f, h * 0.53f)
            lineTo(w * 0.72f, h * 0.40f)
            lineTo(w * 0.58f, h * 0.40f)
            lineTo(w * 0.58f, h * 0.32f)
            quadraticTo(w * 0.58f, h * 0.25f, w * 0.66f, h * 0.25f)
            lineTo(w * 0.73f, h * 0.25f)
            lineTo(w * 0.73f, h * 0.14f)
            quadraticTo(w * 0.67f, h * 0.13f, w * 0.61f, h * 0.13f)
            quadraticTo(w * 0.44f, h * 0.13f, w * 0.44f, h * 0.30f)
            lineTo(w * 0.44f, h * 0.40f)
            lineTo(w * 0.33f, h * 0.40f)
            lineTo(w * 0.33f, h * 0.53f)
            lineTo(w * 0.44f, h * 0.53f)
            lineTo(w * 0.44f, h * 0.85f)
            close()
        }
        drawPath(path = fPath, color = Color.White)
    }
}

/**
 * Official Blue Tick Verified Checkmark Badge (#1A73E8 / #0095F6).
 */
@Composable
fun VerifiedBadgeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0095F6), Color(0xFF1A73E8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified Blue Tick",
            tint = Color.White,
            modifier = Modifier.size(size * 0.68f)
        )
    }
}

/**
 * Interactive Indian Tricolor (JioHotstar Starburst x Google Ribbon) Brand Logo.
 * Tapping the logo triggers a spring starburst animation and opens the Interactive Logo Studio
 * where the user can play with the logo and save/share a 1024x1024 PNG copy.
 */
@Composable
fun MitrBrandOrbLogo(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    enableStudioDialogOnClick: Boolean = true
) {
    var showLogoStudio by remember { mutableStateOf(false) }

    InteractiveTricolorMitrLogo(
        modifier = modifier.testTag("mitr_interactive_brand_logo"),
        size = size,
        onLogoClick = if (enableStudioDialogOnClick) {
            { showLogoStudio = true }
        } else {
            null
        }
    )

    if (showLogoStudio) {
        MitrInteractiveLogoStudioDialog(
            onDismiss = { showLogoStudio = false }
        )
    }
}

/**
 * Top Application Bar matching all 3 screenshots:
 * Left: [Dark Orb Logo] + "Mitr" + "|" + sectionTitle ("Home Feed" / "Reels" / "Profile")
 * Right: Search Icon, Bell Icon with pink dot, Chat+ Icon, and circular Profile Avatar
 */
@Composable
fun MitrTopHeaderBar(
    sectionTitle: String,
    notificationBadgeCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onQuickMessageClick: () -> Unit = {},
    avatarContent: @Composable () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MitrBrandOrbLogo(size = 34.dp)
                Text(
                    text = "Mitr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("top_bar_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (notificationBadgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE1306C))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (notificationBadgeCount > 99) "99+" else "$notificationBadgeCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 7.dp, end = 7.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE1306C))
                        )
                    }
                }

                IconButton(
                    onClick = onQuickMessageClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("top_bar_messages_button")
                ) {
                    Icon(
                        imageVector = MitrIcons.ChatPlusBubble,
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                avatarContent()
            }
        }
    }
}

/**
 * Ambient background canvas with soft editorial porcelain-violet tinting.
 */
@Composable
fun AuroraAmbientBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.05f,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LuminaCoral.copy(alpha = intensity * 0.50f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.12f, h * 0.05f),
                    radius = w * 0.7f
                ),
                center = Offset(w * 0.12f, h * 0.05f),
                radius = w * 0.7f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LuminaAmber.copy(alpha = intensity * 0.40f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.2f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.88f, h * 0.2f),
                radius = w * 0.65f
            )
        }
        content()
    }
}

/**
 * Clean elevated white card with subtle 1.dp border.
 */
@Composable
fun LuminaGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderBrush: Brush = LuminaGlassBorderGradient,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, brush = borderBrush, shape = shape),
        shape = shape,
        color = containerColor,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(content = content)
    }
}

/**
 * Primary action pill button with purple-to-magenta gradient matching the screenshots.
 */
@Composable
fun LuminaGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    gradient: Brush = LuminaPrismGradient
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(
                brush = if (enabled) gradient else Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Custom Canvas Emblem for Mitr Hero banners & Auth Header.
 */
@Composable
fun LuminaPrismArtwork(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LuminaCoral.copy(alpha = 0.24f),
                        LuminaAmber.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = r
                ),
                center = center,
                radius = r
            )

            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(LuminaCoral, LuminaAmber, LuminaViolet, LuminaCyan, LuminaCoral)
                ),
                center = center,
                radius = r * 0.74f,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        Surface(
            modifier = Modifier.size(size * 0.56f),
            shape = CircleShape,
            color = Color(0xFF141728),
            border = BorderStroke(2.dp, LuminaPrismGradient),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.26f)
                    )
                }
            }
        }
    }
}

/**
 * Pill chip with active purple-magenta gradient fill.
 */
@Composable
fun LuminaPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) {
                    LuminaPrismGradient
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.32f) else MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
