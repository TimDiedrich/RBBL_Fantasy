package com.timdiedrich.rbblfantasy.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.timdiedrich.rbblfantasy.utils.CountryCodeConverter

@Composable
fun PlayerAvatar(
    imageUrl: String?,
    playerName: String,
    nationality: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(modifier = modifier.size(size)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = playerName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .border(0.5.dp, Color.Black, CircleShape)
                .clip(CircleShape),
        )
        CountryFlagImage(
            nationality,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(size / 2.5f)
                .border(0.5.dp, Color.White, CircleShape)
                .clip(CircleShape)
        )
    }
}

@Composable
fun CountryFlagImage(
    nationalityAlpha3: String?,
    modifier: Modifier = Modifier
){
    val alpha2 = CountryCodeConverter.toAlpha2(nationalityAlpha3) ?: "UN"
    AsyncImage(
        model = "https://flagcdn.com/w40/${alpha2.lowercase()}.png",
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
