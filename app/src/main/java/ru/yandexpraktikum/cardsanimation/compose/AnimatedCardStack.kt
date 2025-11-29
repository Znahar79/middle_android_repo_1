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

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: Int = 0
)

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
                detectDragGestures(
                    onDragEnd = {
                        /*if (!animationState.isAnimating) {
                            val threshold = 100f
                            val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
                            val isHorizontalDominant = abs(horizontalDragOffset) > abs(verticalDragOffset)

                            when {
                                isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                    handleVerticalSwipe(
                                        verticalDragDistance = verticalDragOffset,
                                        onFanStateChange = { newFanState -> isRotated = newFanState }
                                    )
                                }
                                isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
                                    handleHorizontalSwipe(
                                        horizontalDragDistance = horizontalDragOffset,
                                        onCardsReorder = {
                                            // TODO: Заменить анимированной версией в следующих этапах
                                            currentCards = reorderCards(currentCards)
                                        }
                                    )
                                }
                            }
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f*/
                    }
                ) { _, dragAmount ->
                    val (x, y) = dragAmount
                    verticalDragOffset = x
                    horizontalDragOffset = y

                    if (abs(x) > abs(y)) {
                        if (x > 0) {
                            println("Свайп вправо")
                            resultStack = reorderCards(resultStack)
                        } else {
                            println("Свайп влево")
                            resultStack = reorderCards(resultStack)
                        }
                    } else {
                        if (y > 0) {
                            isRotated = false
                            println("Свайп вниз")
                        } else {
                            println("Свайп вверх")
                            isRotated = true
                        }
                    }
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
                    cardData = cardData
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}