package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.runtime.Immutable

@Immutable
data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationPhase = AnimationPhase.IDLE
)
