package edu.temple.flushfinder

import android.util.Log
import android.widget.CheckBox
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLocationPage(state: NewLocationState, innerPadding: PaddingValues, onSubmit: (BathroomLocation) -> Unit) {
    var confirmSubmit by remember { mutableStateOf(false) }
    var locationName by remember{mutableStateOf("")}

    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }

    val accessible = remember { mutableStateOf(false) }
    val airDryer = remember { mutableStateOf(false) }
    val paperTowels = remember { mutableStateOf(false) }
    val changingStation = remember { mutableStateOf(false) }
    val sanitizer = remember { mutableStateOf(false) }
    val customerOnly = remember { mutableStateOf(false) }
    val singleOccupancy = remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Upload Bathroom", style = MaterialTheme.typography.titleLarge)
        Card(
            Modifier.height(250.dp)
            .fillMaxWidth()
        ) {
            LocationSelector { lat, lon ->
                latitude = lat
                longitude = lon
            }
        }

        Column (
            Modifier.verticalScroll(rememberScrollState()).clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),

        ) {
            TextField(
                locationName,
                { locationName = it },
                label = { Text("Location Name") },
                singleLine = true
            )

            Text("About this location:")

            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CheckableText("Accessible", accessible)
                VerticalDivider()
                CheckableText("Changing Station", changingStation)
            }
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CheckableText("Air Dryer", airDryer)
                VerticalDivider()
                CheckableText("Paper Towels", paperTowels)
            }
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CheckableText("Hand Sanitizer", sanitizer)
                VerticalDivider()
                CheckableText("Customers Only", customerOnly)
            }
            Row(
                Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CheckableText("Private Bathrooms", singleOccupancy)
            }

            Button({ confirmSubmit = true }) {
                Text("Upload New Location!")
            }
        }
    }

    if(confirmSubmit) ConfirmSubmit(
        {confirmSubmit = false}
    ) {
        //TODO: clear variables, congratulate user
        confirmSubmit = false
        onSubmit(BathroomLocation(
            bathroomId = 0,
            name = locationName,
            latitude = latitude,
            longitude = longitude,
            rating = null,
            changingStation = changingStation.value,
            airDryer = airDryer.value,
            paperTowels = paperTowels.value,
            wheelchair = accessible.value,
            handSanitizer = sanitizer.value,
            customerOnly = customerOnly.value,
            singleOccupancy = singleOccupancy.value
        ))
    }

    fun dismiss() {
        state.showSuccess.value = false
        accessible.value = false
        airDryer.value = false
        paperTowels.value = false
        changingStation.value = false
        sanitizer.value = false
        customerOnly.value = false
        singleOccupancy.value = false
        locationName = ""
    }
    if(state.showSuccess.value) {
        Dialog(::dismiss) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Column (
                    Modifier.padding(20.dp),
                ) {
                    Text("Successfully submitted!")
                    Button(::dismiss) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmSubmit(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismiss) {
        Card (
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column (
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("Submit?")
                Text("Make sure all information is correct before submitting")
                Row (
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onDismiss) {
                        Text("Cancel")
                    }
                    Button(onConfirm) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckableText(name: String, state: MutableState<Boolean>) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)
        Checkbox(
            state.value,
            { state.value = it }
        )
    }
}

@Composable
fun LocationSelector(onChange: (Double, Double) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val cameraPosition = rememberCameraPositionState {
        coroutineScope.launch {
            getBestLastKnownLocation(context)?.let {
                onChange(it.latitude, it.longitude)
                animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            LatLng(it.latitude, it.longitude),
                            20f,
                            0f,
                            0f
                        )
                    ),
                    1000
                )
            }
        }
    }

    LaunchedEffect(cameraPosition.position) {
        if(cameraPosition.isMoving) {
            cameraPosition.position.target.let {
                onChange(it.latitude, it.longitude)
            }
        }
    }

    Box (
        contentAlignment = Alignment.Center
    ) {
        GoogleMap(
            Modifier.fillMaxSize(),
            cameraPositionState = cameraPosition,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission(context)
            )
        ) {}
        Icon(
            Icons.Default.LocationSearching,
            null,
            tint = Color.DarkGray
        )
    }
}

@Preview
@Composable
fun NLPPreview() {
    FlushFinderTheme(true) {
        Scaffold() {
            NewLocationPage(NewLocationState(), it) {}
        }
    }
}

@Preview
@Composable
fun ConfirmPreview() {
    FlushFinderTheme(true) {
        Scaffold() {
            Log.d("",it.toString())
            ConfirmSubmit({}) { }
        }
    }
}