package edu.temple.flushfinder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import org.intellij.lang.annotations.JdkConstants


@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (12f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0x50000000)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize).height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}


@Composable
fun Review(state: SearchState, bathroom: BathroomLocation?) {

//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0x45000000))
//            .clickable{
//
//            },
//
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement =  Arrangement.Center,
//
//
//        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "How was the bathroom at " + bathroom?.name + "?",
                    modifier = Modifier
                        .padding(8.dp)
                )

                HorizontalDivider()

                Text(
                    text = "Rating out of 5:",
                    modifier = Modifier
                        .padding(8.dp)
                )
                StarRatingBar(
                    maxStars = 5,
                    rating = state.reviewRatingState.floatValue ?: 0f,
                    onRatingChanged = {
                        state.reviewRatingState.floatValue = it
                    }
                )

//            OutlinedTextField(
//                value = state.reviewTextState.value,
//                onValueChange = {
//                    state.reviewTextState.value = it
//
//                },
//                modifier = Modifier
//                    .fillMaxWidth(),
//                label = Text("Type your review..."),
//                )
                OutlinedTextField(
                    value = state.reviewTextState.value,
                    onValueChange = {
                        state.reviewTextState.value = it
                    },
                    label = { Text("Write your review...") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp),
                    enabled = true,
                    colors = OutlinedTextFieldDefaults.colors(MaterialTheme.colorScheme.onPrimary)
                )

                Button(
                    onClick = {
                        if(state.reviewTextState.value == "")
                            state.reviewError.value = "Please type a review"
                        else if(state.reviewRatingState.floatValue == 0f)
                            state.reviewError.value = "Please provide a rating"
                        else
                            state.reviewError.value = null
                            state.reviewing.value = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("Submit Review")
                }

                state.reviewError.value?.let{
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
       // }
    //}
}

@Preview
@Composable
fun previewReview() {
    FlushFinderTheme {
        Review(
            state = SearchState(),
            bathroom = BathroomLocation(
                45678,
                "Charles Library",
                65.5,
                46.7,
                4.5,
                true,
                false,
                true,
                false
            )
        )
    }
}
