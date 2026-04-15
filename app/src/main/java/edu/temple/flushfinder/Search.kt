package edu.temple.flushfinder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlin.math.roundToInt
import android.Manifest
import android.content.ComponentCallbacks
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch

@Composable
fun CheckableAmenity(name: String, state: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)

        androidx.compose.material3.Checkbox(
            checked = state.value,
            onCheckedChange = {
                state.value = it
            }
        )
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

private fun sliderToBathroomCount(value: Float): Int {
    return (1 + (value * 9f).roundToInt()).coerceIn(1, 10)
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarse = ContextCompat.checkSelfPermission(
        context,Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fine || coarse
}

private fun getBestLastKnownLocation(context: Context): Location? {

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
    )

    var bestLocation: Location? = null

    providers.forEach { provider ->
        try {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null && (bestLocation == null || location.accuracy < bestLocation!!.accuracy)) {
                bestLocation = location
            }
        } catch (_: SecurityException) {

        } catch (_: IllegalArgumentException) {

        }
    }

    return bestLocation
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(state: SearchState, innerPadding: PaddingValues, authToken: String?) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sliderInteractionSource = remember { MutableInteractionSource() }
    val sliderState = remember { SliderState(state.searchDistance.floatValue) }
    val cameraPositionState = rememberCameraPositionState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!granted) {
            state.errorMessage.value = "Location permission is required to search nearby bathrooms."
        }
    }

    val flushAnimation = remember { Animatable(0f) }

    BackHandler(state.showMap.value) {
        state.searchVisible.value = true
        state.showMap.value = false
        scope.launch { flushAnimation.snapTo(0f) }
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
    val flushPath = Morph(start = full, end = flushed).toPath(flushAnimation.value*0.75f).asComposePath()

    val flushShape = object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val path = flushPath.copy()
            val matrix = Matrix()
            matrix.reset()
            val bounds = path.getBounds()
            val minDimension = min(size.width, size.height)
            matrix.scale(minDimension / bounds.width, size.height / bounds.height)
            matrix.translate(-bounds.left, -bounds.top)
            //matrix.scale((1f-flushAnimation.value), (1f-flushAnimation.value))
            //matrix.rotateZ(360*flushAnimation.value)
            path.transform(matrix)
            return Outline.Generic(path)
        }
    }

    val userLatLng = if (state.userLatitude.value != null && state.userLongitude.value != null) {
        LatLng(state.userLatitude.value!!, state.userLongitude.value!!)
    } else {
        null
    }

    LaunchedEffect(state.results.value, state.userLatitude.value, state.userLongitude.value, state.showMap.value) {
        if (state.showMap.value) {
            val target = when {
                state.results.value.isNotEmpty() -> LatLng(
                    state.results.value.first().latitude,
                    state.results.value.first().longitude
                )

                userLatLng != null -> userLatLng

                else -> LatLng(39.9526, -75.1652)
            }

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(target, 14f)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (state.showMap.value) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission(context)
                )
            ) {
                userLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You are here"
                    )
                }

                state.results.value.forEach { bathroom ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(bathroom.latitude, bathroom.longitude)
                        ),
                        title = "Bathroom #${bathroom.name}",
                        snippet = bathroom.rating?.let { rating -> "Rating: $rating" } ?: "No rating"
                    )
                }
            }
        }

        if(state.searchVisible.value) Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(24.dp))
                .padding(12.dp)
                .align(Alignment.TopCenter)
                .rotate(360*flushAnimation.value)
                .scale(1-flushAnimation.value)
                //.background(Color.Red.copy(alpha=flushAnimation.value))
                .clip(flushShape)
                .background(MaterialTheme.colorScheme.background)
                .background(Color.Blue.copy(alpha=flushAnimation.value)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Search Distance")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Slider(
                    modifier = Modifier.padding(20.dp),
                    value = sliderState.value,
                    onValueChange = {
                        sliderState.value = it
                        state.searchDistance.floatValue = it
                    },
                    valueRange = 0f..1f,
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = sliderInteractionSource,
                            thumbSize = DpSize(20.dp, 20.dp)
                        )
                    }
                )
                Text(
                    text = "Nearest ${sliderToBathroomCount(sliderState.value)} bathrooms",
                    modifier = Modifier.offset(y = (-20).dp)
                )
            }

            HorizontalDivider()

            Text("Access")
            AccessBox(state.accessOptions, state.accessSelection)

            HorizontalDivider()

            Text("Amenities")
            AmenitiesBox(state.amenities)

            state.errorMessage.value?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isSearching.value) {
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (!hasLocationPermission(context)) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        return@Button
                    }

                    val location = getBestLastKnownLocation(context)
                    if (location == null) {
                        state.errorMessage.value = "Unable to get your current location yet. Try again in a moment."
                        return@Button
                    }



                    if (authToken.isNullOrBlank()) {
                        state.errorMessage.value = "Please log in before searching."
                        return@Button
                    }

                    state.userLatitude.value = location.latitude
                    state.userLongitude.value = location.longitude
                    state.isSearching.value = true
                    state.errorMessage.value = null

                    scope.launch {
                        state.showMap.value = true
                        if(flushAnimation.animateTo(
                            1f,
                            tween(
                                1500,
                                easing = Easing {it*it}
                            )
                        ).endReason == AnimationEndReason.Finished) {
                            state.searchVisible.value = false
                        }
                    }

                    SearchApi.searchLocations(
                        number = sliderToBathroomCount(sliderState.value),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        changingStation = state.amenities.changingStation.value,
                        airDryer = state.amenities.dryer.value,
                        paperTowels = state.amenities.paper.value,
                        wheelchair = state.amenities.accessible.value,
                        token = authToken
                    ) { response ->
                        state.isSearching.value = false

                        if (response.error != null) {
                            state.errorMessage.value = response.error
                            state.results.value = emptyList()
                        } else {
                            state.errorMessage.value = null
                            state.results.value = response.results
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                enabled = !state.isSearching.value
            ) {
                Text("Find The Flush!")
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SearchPagePreview() {
    FlushFinderTheme {
        SearchPage(
            state = SearchState(),
            innerPadding = PaddingValues(),
            authToken = null
        )
    }
}