package com.clearphone.launcher

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import androidx.compose.material3.Icon

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.shape.GenericShape

val DiamondShape: Shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)         // top-center
    lineTo(size.width, size.height / 2f) // right-center
    lineTo(size.width / 2f, size.height) // bottom-center
    lineTo(0f, size.height / 2f)         // left-center
    close()
}

@Composable
fun AppIconImage(
    drawable: Drawable,
    sizeDp: Dp,
    shape: IconShape
) {
    val clipShape = when (shape) {
        IconShape.CIRCLE -> CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(16.dp)
        IconShape.DIAMOND -> DiamondShape
    }

    Box(
        modifier = Modifier
            .size(sizeDp)
            .clip(clipShape)
            .background(Color.Transparent)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = rememberDrawablePainter(drawable),
            contentDescription = null,
            modifier = Modifier
                .size(sizeDp)
                .clip(clipShape)
                .padding(2.dp),
            tint = Color.Unspecified
        )
    }
}
