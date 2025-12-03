package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var resultStack by remember { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(onDragEnd = {
                if (!animationState.isAnimating) {
                    val absHorizontal = abs(horizontalDragOffset)
                    val absVertical = abs(verticalDragOffset)

                    if (absHorizontal > absVertical) {
                        resultStack = reorderCards(resultStack)
                        animationState = CardSwapAnimationState(true, AnimationPhase.PHASE_ONE)
                    } else {
                        if (verticalDragOffset > 0) {
                            isRotated = false
                        } else {
                            isRotated = true
                        }
                    }

                    horizontalDragOffset = 0f
                    verticalDragOffset = 0f
                }
            }) { _, dragAmount ->
                val (x, y) = dragAmount

                verticalDragOffset += y
                horizontalDragOffset += x
            }
        },
        contentAlignment = Alignment.Center
    ) {
        resultStack.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    animationState = animationState,
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step,
                            i,
                            { step ->
                                val isAnimating = animationState.animationStep > AnimationPhase.IDLE
                                animationState = when (animationState.animationStep) {
                                    AnimationPhase.PHASE_THREE -> {
                                        CardSwapAnimationState()
                                    }
                                    AnimationPhase.IDLE -> {
                                        animationState.copy(
                                            animationStep = AnimationPhase.PHASE_ONE,
                                            isAnimating = true
                                        )
                                    }
                                    else -> {
                                        animationState.copy(
                                            animationStep = step,
                                            isAnimating = isAnimating
                                        )
                                    }
                                }
                            }, {
                                animationState = CardSwapAnimationState()
                            }
                        )
                    }

                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

fun handleAnimationStepComplete(
    step: AnimationPhase,
    cardIndex: Int,
    onStepChange: (AnimationPhase) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            AnimationPhase.PHASE_ONE -> onStepChange(AnimationPhase.PHASE_TWO)
            AnimationPhase.PHASE_TWO -> onAnimationComplete()
            else -> {}
        }
    }
}