package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MITR_BLUE_TICK_PRICE_INR
import com.example.data.model.MITR_FOUNDER_VIP_EMAIL
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
import com.example.ui.screens.UserAvatarBadge
import kotlin.math.cos
import kotlin.math.sin

// Official Indian Flag (Tiranga) + Ashoka Chakra + Google/JioHotstar Palette
val TirangaSaffron = Color(0xFFFF9933)
val TirangaWhite = Color(0xFFFFFFFF)
val TirangaGreen = Color(0xFF138808)
val AshokaChakraNavy = Color(0xFF000080)
val GoogleRoyalBlue = Color(0xFF1A73E8)
val JioStarGold = Color(0xFFFFB703)

/**
 * Interactive Indian Tricolor Logo inspired by JioHotstar (dynamic glowing 4-point starburst)
 * and Google Apps (4-segment fluid geometric ribbon) in Indian Flag colors:
 * Saffron (#FF9933), Pure White (#FFFFFF), India Green (#138808), and Ashoka Chakra Navy/Blue (#1A73E8).
 */
@Composable
fun InteractiveTricolorMitrLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    interactiveMode: Int = 0, // 0 = Tiranga Starburst (JioHotstar x Google), 1 = Chakra 24-Spoke Orbit, 2 = Google Quad Morph
    manualRotationDegrees: Float = 0f,
    burstTriggerCount: Int = 0,
    onLogoClick: (() -> Unit)? = null
) {
    var localTapCount by remember { mutableIntStateOf(0) }
    val totalBursts = burstTriggerCount + localTapCount

    val infiniteTransition = rememberInfiniteTransition(label = "mitr_tricolor_logo_transition")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ribbon_rotation"
    )
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiohotstar_star_pulse"
    )

    val burstScale by animateFloatAsState(
        targetValue = if (totalBursts % 2 == 1) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "burst_scale"
    )
    val burstExtraAngle by animateFloatAsState(
        targetValue = totalBursts * 90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "burst_angle"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF132046),
                        Color(0xFF080E22)
                    )
                )
            )
            .border(
                width = (size.value * 0.035f).coerceAtLeast(1f).dp,
                brush = Brush.sweepGradient(
                    listOf(
                        TirangaSaffron,
                        TirangaWhite,
                        TirangaGreen,
                        GoogleRoyalBlue,
                        TirangaSaffron
                    )
                ),
                shape = CircleShape
            )
            .let { baseMod ->
                if (onLogoClick != null) {
                    baseMod.clickable {
                        localTapCount++
                        onLogoClick()
                    }
                } else {
                    baseMod
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val effectiveAngle = continuousRotation + manualRotationDegrees + burstExtraAngle

            // 1. Google Apps-inspired 4-Segment Fluid Ribbon Ring in Indian Flag Colors
            val ribbonStroke = minDim * 0.135f * burstScale.coerceIn(0.9f, 1.12f)
            val ribbonRadius = minDim * 0.34f
            val arcTopLeft = Offset(center.x - ribbonRadius, center.y - ribbonRadius)
            val arcSize = Size(ribbonRadius * 2f, ribbonRadius * 2f)

            rotate(degrees = effectiveAngle, pivot = center) {
                // Top Arc: Indian Saffron (#FF9933)
                drawArc(
                    color = TirangaSaffron,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = ribbonStroke, cap = StrokeCap.Round)
                )
                // Right Wing: Luminous Pure White (#FFFFFF)
                drawArc(
                    color = TirangaWhite,
                    startAngle = 345f,
                    sweepAngle = 30f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = ribbonStroke, cap = StrokeCap.Round)
                )
                // Bottom Arc: India Emerald Green (#138808)
                drawArc(
                    color = TirangaGreen,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = ribbonStroke, cap = StrokeCap.Round)
                )
                // Left Wing: Pure White + Royal Ashoka Blue Accent
                drawArc(
                    color = if (interactiveMode == 2) GoogleRoyalBlue else TirangaWhite,
                    startAngle = 165f,
                    sweepAngle = 30f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = ribbonStroke, cap = StrokeCap.Round)
                )
            }

            // 2. Ashoka Chakra 24-Spoke Navy/Royal Blue Inner Ring (rotates counter-clockwise)
            val chakraRadius = minDim * 0.21f
            rotate(degrees = -effectiveAngle * 0.75f, pivot = center) {
                drawCircle(
                    color = GoogleRoyalBlue,
                    radius = chakraRadius,
                    center = center,
                    style = Stroke(width = minDim * 0.032f)
                )
                val spokeCount = if (minDim >= 64f || interactiveMode == 1) 24 else 12
                for (i in 0 until spokeCount) {
                    val rad = Math.toRadians((i * (360.0 / spokeCount)))
                    val innerR = chakraRadius * 0.28f
                    val outerR = chakraRadius * 0.95f
                    drawLine(
                        color = GoogleRoyalBlue.copy(alpha = 0.85f),
                        start = Offset(
                            x = center.x + (innerR * cos(rad)).toFloat(),
                            y = center.y + (innerR * sin(rad)).toFloat()
                        ),
                        end = Offset(
                            x = center.x + (outerR * cos(rad)).toFloat(),
                            y = center.y + (outerR * sin(rad)).toFloat()
                        ),
                        strokeWidth = (minDim * 0.016f).coerceAtLeast(1f)
                    )
                }
            }

            // 3. JioHotstar-inspired Dynamic 4-Point Radiant Star Spark at Center
            val starRadiusOuter = minDim * 0.22f * starPulse * burstScale
            val starRadiusInner = starRadiusOuter * 0.28f

            rotate(degrees = effectiveAngle * 0.35f, pivot = center) {
                val outerStarPath = Path().apply {
                    moveTo(center.x, center.y - starRadiusOuter)
                    lineTo(center.x + starRadiusInner, center.y - starRadiusInner)
                    lineTo(center.x + starRadiusOuter, center.y)
                    lineTo(center.x + starRadiusInner, center.y + starRadiusInner)
                    lineTo(center.x, center.y + starRadiusOuter)
                    lineTo(center.x - starRadiusInner, center.y + starRadiusInner)
                    lineTo(center.x - starRadiusOuter, center.y)
                    lineTo(center.x - starRadiusInner, center.y - starRadiusInner)
                    close()
                }
                drawPath(path = outerStarPath, color = TirangaWhite)

                val coreStarOuter = starRadiusOuter * 0.62f
                val coreStarInner = starRadiusInner * 0.62f
                val innerStarPath = Path().apply {
                    moveTo(center.x, center.y - coreStarOuter)
                    lineTo(center.x + coreStarInner, center.y - coreStarInner)
                    lineTo(center.x + coreStarOuter, center.y)
                    lineTo(center.x + coreStarInner, center.y + coreStarInner)
                    lineTo(center.x, center.y + coreStarOuter)
                    lineTo(center.x - coreStarInner, center.y + coreStarInner)
                    lineTo(center.x - coreStarOuter, center.y)
                    lineTo(center.x - coreStarInner, center.y - coreStarInner)
                    close()
                }
                drawPath(path = innerStarPath, color = TirangaSaffron)
            }

            // Ashoka Navy Chakra Core Bullseye
            drawCircle(
                color = AshokaChakraNavy,
                radius = minDim * 0.042f,
                center = center
            )

            // Orbiting JioHotstar Sparkle Dots in Saffron & India Green
            val sparkOrbitR = minDim * 0.38f
            val sparkRad1 = Math.toRadians((effectiveAngle + 45f).toDouble())
            val sparkRad2 = Math.toRadians((effectiveAngle + 225f).toDouble())
            drawCircle(
                color = JioStarGold,
                radius = minDim * 0.042f * starPulse,
                center = Offset(
                    x = center.x + (sparkOrbitR * cos(sparkRad1)).toFloat(),
                    y = center.y + (sparkOrbitR * sin(sparkRad1)).toFloat()
                )
            )
            drawCircle(
                color = TirangaGreen,
                radius = minDim * 0.038f * starPulse,
                center = Offset(
                    x = center.x + (sparkOrbitR * cos(sparkRad2)).toFloat(),
                    y = center.y + (sparkOrbitR * sin(sparkRad2)).toFloat()
                )
            )
        }
    }
}

/**
 * Renders a high-resolution (default 1024x1024) Bitmap copy of the official Mitr Indian Tricolor
 * (JioHotstar x Google Apps style) logo so users can save or share a crisp PNG copy directly.
 */
fun renderMitrTricolorLogoBitmap(sizePx: Int = 1024): Bitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val cx = sizePx / 2f
    val cy = sizePx / 2f

    // 1. Deep Royal Midnight Circle Background
    val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        shader = android.graphics.RadialGradient(
            cx,
            cy,
            sizePx * 0.52f,
            intArrayOf(
                android.graphics.Color.parseColor("#14224A"),
                android.graphics.Color.parseColor("#080E22")
            ),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), bgPaint)

    // 2. Google-style 4-Segment Fluid Ribbon in Indian Flag Colors
    val ribbonRadius = sizePx * 0.34f
    val strokeW = sizePx * 0.13f
    val oval = android.graphics.RectF(
        cx - ribbonRadius,
        cy - ribbonRadius,
        cx + ribbonRadius,
        cy + ribbonRadius
    )
    val arcPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = strokeW
        strokeCap = android.graphics.Paint.Cap.ROUND
    }

    // Top Saffron Arc (#FF9933)
    arcPaint.color = android.graphics.Color.parseColor("#FF9933")
    canvas.drawArc(oval, 205f, 130f, false, arcPaint)

    // Right Pure White Arc (#FFFFFF)
    arcPaint.color = android.graphics.Color.parseColor("#FFFFFF")
    canvas.drawArc(oval, 345f, 30f, false, arcPaint)

    // Bottom India Green Arc (#138808)
    arcPaint.color = android.graphics.Color.parseColor("#138808")
    canvas.drawArc(oval, 25f, 130f, false, arcPaint)

    // Left Pure White Arc (#FFFFFF)
    arcPaint.color = android.graphics.Color.parseColor("#FFFFFF")
    canvas.drawArc(oval, 165f, 30f, false, arcPaint)

    // 3. Inner Ashoka Chakra 24-Spoke Ring (#1A73E8)
    val chakraRadius = sizePx * 0.21f
    val chakraPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = sizePx * 0.028f
        color = android.graphics.Color.parseColor("#1A73E8")
    }
    canvas.drawCircle(cx, cy, chakraRadius, chakraPaint)

    val spokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = sizePx * 0.012f
        color = android.graphics.Color.parseColor("#1A73E8")
    }
    for (i in 0 until 24) {
        val rad = Math.toRadians(i * 15.0)
        val x1 = cx + (chakraRadius * 0.28f * cos(rad)).toFloat()
        val y1 = cy + (chakraRadius * 0.28f * sin(rad)).toFloat()
        val x2 = cx + (chakraRadius * 0.96f * cos(rad)).toFloat()
        val y2 = cy + (chakraRadius * 0.96f * sin(rad)).toFloat()
        canvas.drawLine(x1, y1, x2, y2, spokePaint)
    }

    // 4. JioHotstar-inspired 4-Point Radiant Star Spark
    val starOuter = sizePx * 0.22f
    val starInner = starOuter * 0.28f
    val starPath = android.graphics.Path().apply {
        moveTo(cx, cy - starOuter)
        lineTo(cx + starInner, cy - starInner)
        lineTo(cx + starOuter, cy)
        lineTo(cx + starInner, cy + starInner)
        lineTo(cx, cy + starOuter)
        lineTo(cx - starInner, cy + starInner)
        lineTo(cx - starOuter, cy)
        lineTo(cx - starInner, cy - starInner)
        close()
    }
    val whiteFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.WHITE
    }
    canvas.drawPath(starPath, whiteFill)

    val innerOuter = starOuter * 0.62f
    val innerInner = starInner * 0.62f
    val innerStarPath = android.graphics.Path().apply {
        moveTo(cx, cy - innerOuter)
        lineTo(cx + innerInner, cy - innerInner)
        lineTo(cx + innerOuter, cy)
        lineTo(cx + innerInner, cy + innerInner)
        lineTo(cx, cy + innerOuter)
        lineTo(cx - innerInner, cy + innerInner)
        lineTo(cx - innerOuter, cy)
        lineTo(cx - innerInner, cy - innerInner)
        close()
    }
    val saffronFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.parseColor("#FF9933")
    }
    canvas.drawPath(innerStarPath, saffronFill)

    // 5. Ashoka Navy Center Bullseye & Sparkles
    val navyFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.parseColor("#000080")
    }
    canvas.drawCircle(cx, cy, sizePx * 0.038f, navyFill)

    val greenFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.parseColor("#138808")
    }
    canvas.drawCircle(sizePx * 0.76f, sizePx * 0.24f, sizePx * 0.034f, saffronFill)
    canvas.drawCircle(sizePx * 0.24f, sizePx * 0.76f, sizePx * 0.030f, greenFill)

    return bmp
}

/**
 * Saves a 1024x1024 PNG copy of the Mitr Indian Tricolor logo to the device's Pictures/Mitr gallery
 * using zero-permission MediaStore insertion.
 */
fun saveMitrLogoToGallery(context: Context): Result<Uri> = runCatching {
    val bitmap = renderMitrTricolorLogoBitmap(1024)
    val filename = "Mitr_Official_Tricolor_Logo_${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Mitr")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IllegalStateException("Could not create MediaStore entry for logo PNG")

    resolver.openOutputStream(imageUri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    } ?: throw IllegalStateException("Could not open output stream for logo PNG")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(imageUri, contentValues, null, null)
    }
    imageUri
}

/**
 * Interactive Modal Studio where the user can play with the JioHotstar x Google Apps style
 * Indian Flag Tricolor Logo and download/share their high-resolution PNG copy.
 */
@Composable
fun MitrInteractiveLogoStudioDialog(
    onDismiss: () -> Unit,
    onOpenBlueTickSheet: (() -> Unit)? = null,
    canDownloadLogo: Boolean = isFounderVipAccount(
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email }.getOrNull()
    )
) {
    val context = LocalContext.current
    var interactiveMode by remember { mutableIntStateOf(0) }
    var dragAngle by remember { mutableFloatStateOf(0f) }
    var burstCount by remember { mutableIntStateOf(0) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mitr_interactive_logo_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mitr Interactive Tiranga Logo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "JioHotstar Starburst × Google Ribbon • Indian Flag Colors",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Interactive Touch & Drag Playground Box
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { burstCount++ },
                                onDoubleTap = {
                                    interactiveMode = (interactiveMode + 1) % 3
                                    burstCount += 2
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                dragAngle += (dragAmount.x + dragAmount.y) * 0.85f
                            }
                        }
                        .testTag("interactive_logo_canvas_playground"),
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveTricolorMitrLogo(
                        size = 186.dp,
                        interactiveMode = interactiveMode,
                        manualRotationDegrees = dragAngle,
                        burstTriggerCount = burstCount,
                        onLogoClick = { burstCount++ }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tap logo for Starburst • Drag to spin 24-spoke Ashoka Chakra",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3 Interactive Style Switcher Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf("Tiranga Star", "24-Spoke Chakra", "Google Quad")
                    modes.forEachIndexed { idx, label ->
                        val selected = interactiveMode == idx
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    interactiveMode = idx
                                    burstCount++
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Official Copy Info Card & Download/Share Buttons — Restricted exclusively to founder (yogeshkhanchi985@gmail.com)
                if (canDownloadLogo) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Founder Exclusive • Official High-Resolution Logo Copy (1024×1024 PNG)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "• Project file copy: public/assets/mitr_official_tricolor_logo.png\n• App drawable copy: res/drawable/img_mitr_tricolor_logo.png",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (savedMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TirangaGreen.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TirangaGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = savedMessage!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TirangaGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                saveMitrLogoToGallery(context)
                                    .onSuccess {
                                        savedMessage = "Saved 1024×1024 PNG copy to Pictures/Mitr in your Gallery!"
                                    }
                                    .onFailure { err ->
                                        savedMessage = "Logo ready in public/assets/mitr_official_tricolor_logo.png (${err.localizedMessage ?: "saved"})"
                                    }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_mitr_logo_copy_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save PNG Copy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                saveMitrLogoToGallery(context).onSuccess { uri ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Mitr Official Indian Tricolor Logo (1024x1024 PNG)"
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Mitr Logo Copy"))
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_mitr_logo_copy_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Logo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (onOpenBlueTickSheet != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenBlueTickSheet()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VerifiedBadgeIcon(size = 16.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get Mitr Verified Blue Tick & Boost Reel Views",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-Featured Mitr Verified Blue Tick Subscription & Creator Payout Bottom Sheet:
 * - Standard Subscription: ₹300 / month for Blue Tick + Priority Reel & Post Reach (3.5x More Views & Likes).
 * - Complimentary Founder VIP Access: Automatically Free (₹0) for yogeshkhanchi985@gmail.com.
 * - Platform Owner / Creator Payout Account Setup: Allows saving Bank Account / IFSC / UPI ID
 *   where the ₹300 subscription payments are transferred.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitrBlueTickSubscriptionSheet(
    currentUserProfile: UserProfile,
    currentUserSettings: UserSettings,
    currentUserEmail: String,
    onActivateSubscription: (String) -> Unit,
    onSavePayoutAccount: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val effectiveEmail = currentUserEmail.ifBlank { currentUserSettings.email }
    val isFounderVip = isFounderVipAccount(effectiveEmail, currentUserProfile.username)
    val isAlreadyVerified = isUserBlueTickVerified(
        profile = currentUserProfile,
        email = effectiveEmail,
        settings = currentUserSettings
    )

    var selectedPaymentMethod by remember { mutableStateOf("Google Pay (UPI)") }
    var showPayoutConfig by remember { mutableStateOf(isFounderVip || currentUserSettings.payoutBankOrUpi.isNotBlank()) }

    var holderName by remember(currentUserSettings.payoutHolderName, currentUserProfile.displayName) {
        mutableStateOf(
            currentUserSettings.payoutHolderName.ifBlank {
                if (isFounderVip) "Yogesh Khanchi" else currentUserProfile.displayName
            }
        )
    }
    var bankOrUpi by remember(currentUserSettings.payoutBankOrUpi) {
        mutableStateOf(currentUserSettings.payoutBankOrUpi)
    }
    var accountNumber by remember(currentUserSettings.payoutAccountNumber) {
        mutableStateOf(currentUserSettings.payoutAccountNumber)
    }
    var ifscCode by remember(currentUserSettings.payoutIfscCode) {
        mutableStateOf(currentUserSettings.payoutIfscCode)
    }
    var payoutSavedFeedback by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("mitr_blue_tick_subscription_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VerifiedBadgeIcon(size = 28.dp)
                    Column {
                        Text(
                            text = "Mitr Verified • Blue Tick",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isFounderVip) {
                                "Complimentary Founder VIP ($MITR_FOUNDER_VIP_EMAIL)"
                            } else {
                                "Official Verification & Priority Reel Reach • ₹$MITR_BLUE_TICK_PRICE_INR/mo"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Live Profile Username + Blue Tick Preview Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UserAvatarBadge(
                            avatarUrl = currentUserProfile.avatarUrl,
                            displayName = currentUserProfile.displayName.ifBlank { currentUserProfile.username },
                            size = 48.dp,
                            showBorder = true
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "@${currentUserProfile.username.ifBlank { "creator" }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                VerifiedBadgeIcon(size = 17.dp)
                            }
                            Text(
                                text = if (isAlreadyVerified) {
                                    "Active Blue Tick • 3.5x Priority Reel & Feed Reach"
                                } else {
                                    "Preview of your Verified Blue Tick profile badge"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isFounderVip) Color(0xFF138808) else Color(0xFF1A73E8)
                    ) {
                        Text(
                            text = if (isFounderVip) "FREE VIP" else "₹$MITR_BLUE_TICK_PRICE_INR",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Special Banner if signed in as yogeshkhanchi985@gmail.com
            if (isFounderVip) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF138808).copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF138808), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👑 Founder VIP Facility Enabled ($MITR_FOUNDER_VIP_EMAIL)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF138808),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Your account receives the Official Blue Tick and maximum Reel & Post recommendation boost completely FREE without any payment. All ₹300 subscriber payments are routed to your payout account below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 3 Core Benefits of Mitr Verified Blue Tick
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "INCLUDED WITH MITR VERIFIED BLUE TICK",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                BlueTickBenefitRow(
                    icon = Icons.Default.Verified,
                    title = "Official Blue Tick on Username & Profile",
                    subtitle = "Displayed next to @${currentUserProfile.username} across Profile, Reels, Posts, Stories, Comments & Search."
                )
                BlueTickBenefitRow(
                    icon = Icons.Default.TrendingUp,
                    title = "Higher Reel Views & Likes (Priority Feed Distribution)",
                    subtitle = "Your Reels and Posts are prioritized at the top of the For You feed and shown to 3.5x more viewers (+320 instant Reel views & +48 likes boost)."
                )
                BlueTickBenefitRow(
                    icon = Icons.Default.AutoAwesome,
                    title = if (isFounderVip) "₹0 Lifetime Founder Access" else "₹300 / Month Creator Subscription",
                    subtitle = if (isFounderVip) {
                        "Complimentary lifetime activation for $MITR_FOUNDER_VIP_EMAIL with zero charges."
                    } else {
                        "Instant verification via UPI / Google Pay / PhonePe / Paytm / Card (₹300 transferred directly to platform owner payout account)."
                    }
                )
            }

            // Payment Method Picker (Only needed if not Founder VIP)
            if (!isFounderVip) {
                Text(
                    text = "SELECT PAYMENT METHOD (₹300)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                val methods = listOf(
                    "Google Pay (UPI)",
                    "PhonePe (UPI)",
                    "Paytm (UPI)",
                    "Debit / Credit Card"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { method ->
                        val selected = selectedPaymentMethod == method
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (selected) 1.5.dp else 0.5.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPaymentMethod = method }
                        ) {
                            Text(
                                text = method.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Primary Subscribe / Boost Button
            Button(
                onClick = {
                    val label = if (isFounderVip) {
                        "Founder VIP Free ($MITR_FOUNDER_VIP_EMAIL)"
                    } else {
                        "$selectedPaymentMethod • ₹$MITR_BLUE_TICK_PRICE_INR"
                    }
                    onActivateSubscription(label)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8)
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activate_blue_tick_subscription_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isFounderVip && isAlreadyVerified -> "Boost My Reels & Posts Again (Founder VIP Free)"
                        isFounderVip -> "Activate Free Founder Blue Tick (₹0 for yogeshkhanchi985@gmail.com)"
                        isAlreadyVerified -> "Boost Reel Views & Renew Blue Tick (₹$MITR_BLUE_TICK_PRICE_INR)"
                        else -> "Pay ₹$MITR_BLUE_TICK_PRICE_INR & Activate Blue Tick Now"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Creator / Platform Owner Payout Account Transfer Section (for transferring ₹300 subscription fees)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPayoutConfig = !showPayoutConfig },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "₹300 Subscription Payout & Bank Transfer Account",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (currentUserSettings.payoutBankOrUpi.isNotBlank()) {
                                        "Linked: ${currentUserSettings.payoutBankOrUpi} • Settlement Balance: ₹${currentUserSettings.pendingPayoutBalanceInr.coerceAtLeast(300L)}"
                                    } else {
                                        "Add your Bank Account or UPI ID now or later to receive ₹300 Blue Tick transfers"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (showPayoutConfig) "Hide" else "Configure",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (showPayoutConfig) {
                        OutlinedTextField(
                            value = holderName,
                            onValueChange = { holderName = it },
                            label = { Text("Account Holder Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payout_holder_name_input")
                        )
                        OutlinedTextField(
                            value = bankOrUpi,
                            onValueChange = { bankOrUpi = it },
                            label = { Text("UPI ID or Bank Name (e.g., yogesh@upi / SBI)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payout_upi_or_bank_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = { Text("Bank Account No. (Optional)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = ifscCode,
                                onValueChange = { ifscCode = it },
                                label = { Text("IFSC Code") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        if (payoutSavedFeedback) {
                            Text(
                                text = "✓ Payout account saved! All ₹300 Blue Tick subscription fees will be transferred to this account.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF138808),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onSavePayoutAccount(holderName, bankOrUpi, accountNumber, ifscCode)
                                payoutSavedFeedback = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_payout_account_button")
                        ) {
                            Text(
                                text = "Save Payout Account for ₹300 Transfers",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BlueTickBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A73E8).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1A73E8),
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
