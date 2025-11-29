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

var verticalDragOffset = 0f
var horizontalDragOffset = 0f

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var resultStack by remember { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    Box(
        modifier = Modifier.pointerInput(Unit) {
                detectDragGestures (onDragEnd = {
                    val absHorizontal = abs(horizontalDragOffset)
                    val absVertical = abs(verticalDragOffset)

                    if (absHorizontal > absVertical) {
                        if (horizontalDragOffset > 0) {
                            println("Свайп вправо")
                            resultStack = reorderCards(resultStack)
                        } else {
                            println("Свайп влево")
                            resultStack = reorderCards(resultStack)
                        }
                    } else {
                        if (verticalDragOffset > 0) {
                            println("Свайп вниз")
                            isRotated = false
                        } else {
                            println("Свайп вверх")
                            isRotated = true
                        }
                    }

                    // Сброс значений для следующего жеста
                    horizontalDragOffset = 0f
                    verticalDragOffset = 0f
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