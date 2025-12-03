package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private companion object {
        const val SWIPE_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false
    private var isAnimating = false
    private var animationStep = 0
    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f

    var onSwipeHorizontal: (() -> Unit) = {
        startCardSwapAnimation(cards[0])
    }
    var onSwipeTop: (() -> Unit) = {
        isRotated = true
        updateCardPositions()
    }
    var onSwipeBottom: (() -> Unit) = {
        isRotated = false
        updateCardPositions()
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                verticalDragOffset += distanceY
                horizontalDragOffset += distanceX
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                val diffY = e2.y - (e1?.y ?: 0F)
                val diffX = e2.x - (e1?.x ?: 0F)

                return when {
                    abs(diffX) > abs(diffY) && isHorizontalSwipeValid(diffX, velocityX) -> {
                        onSwipeHorizontal.invoke()
                        true
                    }

                    abs(diffY) > abs(diffX) && isVerticalSwipeValid(diffY, velocityY) -> {
                        if (diffY > 0) onSwipeBottom.invoke() else onSwipeTop.invoke()
                        true
                    }

                    else -> false
                }
            }
        })

    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    private fun setupCards() {
        clearCards()
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        // Возврат в исходное положение
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions() {
        val cardCount = cards.size

        cards.forEachIndexed { index, cardView ->
            // Расчёт расположения карт в исходной позиции
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }

            // Расчёт финальной позиции (для эффекта раскрытой колоды карт)
            val targetRotation = if (isRotated) {
                val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
                90f - (index * angleStep)
            } else {
                baseRotation
            }

            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f

            cardView.x = sharedX
            cardView.y = sharedY

            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight

            val startRotation = if (isRotated) baseRotation else cardView.rotation

            cardView.rotation = startRotation
            cardView.animateToRotation(targetRotation)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (isAnimating) return

        isAnimating = true
        animationStep = 1

        bottomCard.moveCardRight {
            animationStep = 2
            bringCardToFront(bottomCard)
            bottomCard.moveCardToTop {
                animationStep = 3
                reorderCardsData()
                animateAllCardsToFinalPositions()
            }
        }
    }

    private fun bringCardToFront(card: AnimatedCardView) {
        card.bringToFront()
        val elevationOffset = 24
        val maxElevation = (cards.size + elevationOffset).toFloat() * resources.displayMetrics.density
        card.stackView.cardElevation = maxElevation
    }

    private fun reorderCardsData() {
        val reorderedCards = cardDataList.drop(1) + cardDataList.first()
        cardDataList = reorderedCards

        val bottomCardView = cards.removeAt(0)
        cards.add(bottomCardView)

        cards.forEachIndexed { index, cardView ->
            cardView.setCardData(cardDataList[index])
        }
    }

    private fun animateAllCardsToFinalPositions() {
        var completedAnimations = 0
        val totalAnimations = cards.size

        cards.forEachIndexed { index, cardView ->
            val finalRotation = calculateFinalRotation(index)

            cardView.adjustToFinalPosition(finalRotation, index) {
                completedAnimations++
                if (completedAnimations == totalAnimations) {
                    finalizeCardPositions()
                }
            }
        }
    }

    private fun calculateFinalRotation(cardIndex: Int): Float {
        val cardCount = cards.size
        return if (isRotated) {
            val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f
            90f - (cardIndex * angleStep)
        } else {
            val angleStep = if (cardCount > 1) 45f / (cardCount - 1) else 0f
            22.5f - (cardIndex * angleStep)
        }
    }

    private fun finalizeCardPositions() {
        cards.forEachIndexed { index, card ->
            card.setStackPosition(index)
            val correctRotation = calculateFinalRotation(index)
            card.rotation = correctRotation
        }

        isAnimating = false
        animationStep = 0
    }

    private fun isHorizontalSwipeValid(diffX: Float, velocityX: Float) =
        abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD

    private fun isVerticalSwipeValid(diffY: Float, velocityY: Float) =
        abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
}