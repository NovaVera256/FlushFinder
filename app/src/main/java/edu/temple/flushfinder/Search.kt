package edu.temple.flushfinder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StampedPathEffectStyle.Companion.Morph
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Feature
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.PointTransformer
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.google.maps.android.compose.GoogleMap
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import kotlin.math.max
import kotlin.math.min

@Composable
fun CheckableAmenity(name: String, state: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)

        Checkbox(state.value, {
            state.value = it
        })
    }
}

@Composable
fun AmenitiesBox(state: Amenities) {
    Row(
        Modifier.padding(20.dp).height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            CheckableAmenity("Paper", state.paper)
            CheckableAmenity("Dryer", state.dryer)
            CheckableAmenity("Soap", state.soap)
        }
        VerticalDivider()
        Column(
            horizontalAlignment = Alignment.End
        ) {
            CheckableAmenity("Wheelchair Accessible", state.accessible)
            CheckableAmenity("Changing Station", state.changingStation)
            CheckableAmenity("Hand Sanitizer", state.sanitizer)
        }
    }
}

@Composable
fun AccessBox(options: List<String>, selection: MutableState<List<Boolean>>) {
    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, string ->

            SegmentedButton(
                selection.value[index],
                {
                    selection.value = selection.value.mapIndexed { i, v ->
                        if(index == i) !v
                        else v
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = SegmentedButtonDefaults.colors(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.background,
                    inactiveContentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(string)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(state: SearchState, innerPadding: PaddingValues) {
    val flushAnimation = animateFloatAsState(
        if(state.showMap.value) 1f else 0f,
        spring(stiffness = Spring.StiffnessVeryLow)
    ) {
        state.searchVisible.value = false
    }

    val flushed = RoundedPolygon.star(
        11,
        rounding = CornerRounding(0.03f)
    )

    val full = RoundedPolygon(
        vertices = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            1f, 1f,
            -1f, 1f
        )
    )
    val flushPath = Morph(start = full, end = flushed).toPath(flushAnimation.value).asComposePath()

    val flushShape = object: Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val path = flushPath.copy()
            var matrix = Matrix()
            matrix.reset()
            val bounds = path.getBounds()//full.calculateBounds().let { Rect(it[0], it[1], it[2], it[3]) }
            val minDimension = min(size.width, size.height)
            matrix.scale(minDimension / bounds.width, size.height / bounds.height)
            matrix.translate(-bounds.left, -bounds.top)
            matrix.scale((1f-flushAnimation.value), (1f-flushAnimation.value))
            matrix.rotateZ(360*flushAnimation.value)
            path.transform(matrix)
            return Outline.Generic(path)
        }

    }
    Box {
        Card {
            Text("THIS IS THE MAP")
        }
        //GoogleMap()

        if (state.searchVisible.value) Column(
            Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                //.background(Color.Red)
                .clip(flushShape)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Search Distance")
            val sliderInteractionSource = remember {
                MutableInteractionSource()
            }
            val sliderState = remember {
                SliderState(0.2f)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Slider(
                    modifier = Modifier.padding(20.dp),
                    value = sliderState.value,
                    onValueChange = {
                        sliderState.value = it
                    },
                    thumb = {
                        SliderDefaults.Thumb(
                            sliderInteractionSource,
                            thumbSize = DpSize(20.dp, 20.dp),
                        )
                    },
                    steps = 9,
                )
                Text("Nearest ${sliderState.value} bathrooms", Modifier.offset(y = -20.dp))
            }

            HorizontalDivider()

            Text("Access")

            AccessBox(state.accessOptions, state.accessSelection)

            HorizontalDivider()

            Text("Amenities")

            AmenitiesBox(state.amenities)

            Spacer(Modifier.weight(1f))

            Button(
                {
                    state.showMap.value = true
                },
                Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Text("Find The Flush!")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPagePreview() {
    FlushFinderTheme {
        SearchPage(SearchState(), PaddingValues())
    }
}