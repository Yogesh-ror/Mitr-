package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aura / Mitr Editorial Palette — Porcelain White, Royal Violet & Magenta-Rose Gradient
val LuminaCoral = Color(0xFF5B37F2)       // Primary Electric Violet-Indigo
val LuminaRose = Color(0xFF5B37F2)        // Primary Violet-Indigo
val LuminaViolet = Color(0xFF9333EA)      // Rich Orchid Violet
val LuminaIndigo = Color(0xFF4F46E5)      // Verified Badge / Subtitle Indigo
val LuminaAmber = Color(0xFFE1306C)       // Vibrant Magenta-Rose Accent
val LuminaCyan = Color(0xFFC026D3)        // Luminous Fuchsia Accent
val LuminaEmerald = Color(0xFF10B981)     // Online / Verified Emerald

// Dark Mode — Layered Obsidian & Midnight Violet Surfaces
val LuminaObsidian = Color(0xFF0A0B14)
val LuminaDarkSurface = Color(0xFF121422)
val LuminaDarkCard = Color(0xFF1A1D30)
val LuminaDarkBorder = Color(0xFF2A2E47)

// Light Mode — Porcelain Lavender-White & Pure White Card Surfaces (Matches Screenshots)
val LuminaLightBackground = Color(0xFFF6F7FC)
val LuminaLightSurface = Color(0xFFFFFFFF)
val LuminaLightCard = Color(0xFFEEF0F8)
val LuminaLightBorder = Color(0xFFE4E7F2)

// Text Hierarchy
val LuminaDarkTextPrimary = Color(0xFFF8FAFC)
val LuminaDarkTextSecondary = Color(0xFF94A3B8)
val LuminaLightTextPrimary = Color(0xFF121420)
val LuminaLightTextSecondary = Color(0xFF5E6478)

// Signature Violet-to-Magenta Gradients (Matches Edit Profile, Story Rings & FAB in Screenshots)
val LuminaPrismGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF5B37F2), Color(0xFF9D28B0), Color(0xFFD81B60))
)

val LuminaNeonGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF9333EA), Color(0xFFE1306C))
)

val LuminaGlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF5B37F2).copy(alpha = 0.20f),
        Color(0xFFD81B60).copy(alpha = 0.14f),
        Color(0xFFE4E7F2).copy(alpha = 0.65f)
    )
)
