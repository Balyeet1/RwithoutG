package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.GreetingImage
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme

@Composable
fun GreetingImageDisplay(
    image: GreetingImage,
    version: Long = 0L,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Surface(
        modifier = modifier.size(image.size.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (image.uri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image.uri)
                    .memoryCacheKey("${image.uri}_$version")
                    .build(),
                contentDescription = stringResource(R.string.greeting_image_description),
                contentScale = contentScale
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.default_image_description),
                contentScale = contentScale
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingImageDisplayPreview() {
    RewardWithoutGuiltTheme {
        GreetingImageDisplay(
            image = GreetingImage(
                uri = null,
                size = 150f
            )
        )
    }
}
