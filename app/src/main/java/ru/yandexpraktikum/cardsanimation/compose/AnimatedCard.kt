package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.yandexpraktikum.cardsanimation.ANIMATION_DURATION_LONG
import ru.yandexpraktikum.cardsanimation.ANIMATION_DURATION_SHORT
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    animationState: CardSwapAnimationState,
    onAnimationStepComplete: ((AnimationPhase) -> Unit)
) {
    val rotationAnimation by animateFloatAsState(
        targetValue = when {
            animationState.animationStep == AnimationPhase.PHASE_THREE -> targetRotation
            animationState.isAnimating -> targetRotation
            else -> targetRotation
        },
        animationSpec = tween(durationMillis = if (animationState.animationStep == AnimationPhase.PHASE_THREE) ANIMATION_DURATION_SHORT else ANIMATION_DURATION_LONG),
        finishedListener = {
            if (animationState.animationStep == AnimationPhase.PHASE_THREE && animationState.isAnimating) onAnimationStepComplete.invoke(AnimationPhase.PHASE_THREE)
        },
        label = "rotation"
    )
    val density = LocalDensity.current

    val shouldBringToFront = animationState.isAnimating && animationState.animationStep >= AnimationPhase.PHASE_TWO

    val animatedTranslationX by animateFloatAsState(
        targetValue = when {
            animationState.isAnimating && animationState.animationStep == AnimationPhase.PHASE_ONE -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * cos(rotationRad).toFloat()
            }
            animationState.isAnimating && animationState.animationStep == AnimationPhase.PHASE_TWO -> 0f // Движение в центр
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (animationState.isAnimating) {
                when (animationState.animationStep) {
                    AnimationPhase.PHASE_ONE -> onAnimationStepComplete.invoke(AnimationPhase.PHASE_ONE)
                    AnimationPhase.PHASE_TWO -> onAnimationStepComplete.invoke(AnimationPhase.PHASE_TWO)
                    AnimationPhase.PHASE_THREE -> onAnimationStepComplete.invoke(AnimationPhase.PHASE_THREE)
                    else -> {}
                }
            }
        },
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = when {
            animationState.isAnimating && animationState.animationStep == AnimationPhase.PHASE_ONE -> {
                val moveDistance = with(density) { 50.dp.toPx() }
                val rotationRad = Math.toRadians(targetRotation.toDouble())
                moveDistance * sin(rotationRad).toFloat()
            }
            animationState.isAnimating && animationState.animationStep == AnimationPhase.PHASE_TWO -> 0f // Движение в центр
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "translationY"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                rotationZ = rotationAnimation
                translationX = if (animationState.isAnimating) animatedTranslationX else 0f
                translationY = if (animationState.isAnimating) animatedTranslationY else 0f
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            }.let { modifier ->
                if (shouldBringToFront) {
                    modifier.zIndex(1000f)
                } else {
                    modifier
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}