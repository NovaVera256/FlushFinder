package edu.temple.flushfinder

import android.content.Context
import android.content.SharedPreferences
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import java.io.UnsupportedEncodingException


enum class Page {
    NewLocation,
    Search,
    Account
}

data class Amenities (
    val paper: MutableState<Boolean> = mutableStateOf(false),
    val dryer: MutableState<Boolean> = mutableStateOf(false),
    val customerOnly: MutableState<Boolean> = mutableStateOf(false),
    val accessible: MutableState<Boolean> = mutableStateOf(false),
    val changingStation: MutableState<Boolean> = mutableStateOf(false),
    val sanitizer: MutableState<Boolean> = mutableStateOf(false),
    val soap: MutableState<Boolean> = mutableStateOf(false)
)

data class SearchState (
    val searchDistance: MutableFloatState = mutableFloatStateOf(0f),

    val accessOptions: List<String> = listOf("Free", "Customers", "Door Code"),
    val accessSelection: MutableState<List<Boolean>> = mutableStateOf(listOf(false, false, false)),

    val amenities: Amenities = Amenities(),
    
    val reviewing: MutableState<Boolean> = mutableStateOf(false),
    val reviewTextState: MutableState<String> = mutableStateOf(""),
    val reviewRatingState: MutableIntState = mutableIntStateOf(0),
    val reviewError: MutableState<String?> = mutableStateOf(null),



    val searchVisible: MutableState<Boolean> = mutableStateOf(true),
    val showMap: MutableState<Boolean> = mutableStateOf(false),
    val isSearching: MutableState<Boolean> = mutableStateOf(false),
    val errorMessage: MutableState<String?> = mutableStateOf(null),
    val results: MutableState<List<BathroomLocation>> = mutableStateOf(emptyList()),
    val userLatitude: MutableState<Double?> = mutableStateOf(null),
    val userLongitude: MutableState<Double?> = mutableStateOf(null)
)

data class AccountState (
    val username: MutableState<String> = mutableStateOf(""),
    val password: MutableState<String> = mutableStateOf(""),
    val token: MutableState<String?> = mutableStateOf(null),
    val isLoggedIn: MutableState<Boolean> = mutableStateOf(false),
    val isLoading: MutableState<Boolean> = mutableStateOf(false),
    val isRegisterMode: MutableState<Boolean> = mutableStateOf(false),
    val errorMessage: MutableState<String?> = mutableStateOf(null)
)

data class NewLocationState (
    val dummy: Boolean = true
)

data class BathroomLocation(
    val bathroomId: Int,
    val name: String,
    val customerOnly: Boolean?,
    val latitude: Double,
    val longitude: Double,
    val rating: Double?,
    val changingStation: Boolean?,
    val airDryer: Boolean?,
    val paperTowels: Boolean?,
    val handSanitizer: Boolean?,
    val wheelchair: Boolean?
)

/*
 * ViewModel for the entire application
 * Each page is given its own data class (see SearchState) to manage structure
 */
class MainViewModel (
    val page: MutableState<Page> = mutableStateOf(Page.Search),
    val newLocation: NewLocationState = NewLocationState(),
    val search: SearchState = SearchState(),
    val account: AccountState = AccountState(),
    val nfcFound: MutableState<String?> = mutableStateOf(null),
    var onTokenChanged: (String?) -> Unit = { }
): ViewModel()

const val WEB_TOKEN_STORAGE_KEY = "FLUSHFINDER_WEB_TOKEN"
const val USERNAME_STORAGE_KEY = "FLUSHFINDER_USERNAME"

class MainActivity : ComponentActivity() {
    val viewmodel: MainViewModel by viewModels()

    var nfcAdapter: NfcAdapter? = null

    var intentFilters: Array<IntentFilter> = emptyArray()
    lateinit var techList: Array<Array<String?>>

    lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).run{
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        var token: String?
        var username: String?

        getPreferences(MODE_PRIVATE).run {
            token = getString(WEB_TOKEN_STORAGE_KEY, null)
            username = getString(USERNAME_STORAGE_KEY, null)
        }

        viewmodel.account.token.value = token
        viewmodel.account.username.value = username.orEmpty()

        if(token != null) viewmodel.account.isLoggedIn.value = true

        viewmodel.onTokenChanged = {it ->
            getPreferences(MODE_PRIVATE).edit {
                if(it != null) {
                    putString(WEB_TOKEN_STORAGE_KEY, it)
                    putString(USERNAME_STORAGE_KEY, viewmodel.account.username.value)
                }
                else {
                    remove(WEB_TOKEN_STORAGE_KEY)
                    remove(USERNAME_STORAGE_KEY)
                }
            }
        }

        setContent {
            Root(viewmodel)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
            )

            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType("application/vnd.flushfinder")
                } catch (e: IntentFilter.MalformedMimeTypeException) {
                    throw RuntimeException("nfc tag failed to scan correctly", e)
                }
            }

            techList = arrayOf(arrayOf(NfcF::class.java.name))

            intentFilters = arrayOf(ndef)
        }





    }

    public override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(this)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, intentFilters, techList)
        }
    }

    override fun onNewIntent(intent: Intent) {

        if(nfcAdapter != null) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    ?.also { rawMessages ->
                        val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }

                        if (messages[0].records[0].toMimeType() == "application/vnd.flushfinder")
                            viewmodel.nfcFound.value =
                                parseTextRecord(messages[0].records[0])

                    }
            }
        }

        super.onNewIntent(intent)
    }
}

private fun parseTextRecord(record: NdefRecord): String {
    val payload = record.getPayload()
    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
    val languageCodeLength = payload[0].toInt() and 63
    try {
        return String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            charset(textEncoding)
        )
    } catch (e: UnsupportedEncodingException) {
        return ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Root(state: MainViewModel) {
    FlushFinderTheme(true) {
        Scaffold(
            topBar = {
                TopAppBar({
                    Text("FlushFinderr")
                })
            },
            bottomBar = {
                NavigationBar {
                    val colors = NavigationBarItemColors(
                        MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIconColor = MaterialTheme.colorScheme.surface,
                        disabledTextColor = MaterialTheme.colorScheme.surface
                    );
                    NavigationBarItem(
                        state.page.value == Page.NewLocation,
                        {
                            state.page.value = Page.NewLocation
                        },
                        {
                            Icon(Icons.Default.AddLocationAlt, null)
                        },
                        label = {
                            Text("Upload")
                        },
                        colors = colors
                    )
                    NavigationBarItem(
                        state.page.value == Page.Search,
                        {
                            state.page.value = Page.Search
                        },
                        {
                            Icon(Icons.Default.Search, null)
                        },
                        label = {
                            Text("Find")
                        },
                        colors = colors
                    )
                    NavigationBarItem(
                        state.page.value == Page.Account,
                        {
                            state.page.value = Page.Account
                        },
                        {
                            Icon(Icons.Default.AccountCircle, null)
                        },
                        label = {
                            Text("Account")
                        },
                        colors = colors
                    )
                }
            }
        ) { innerPadding ->
            when (state.page.value) {
                Page.NewLocation -> NewLocationPage(state.newLocation, innerPadding) {
                    Log.d("NewLocation", "Submitting new location $it")
                }
                Page.Search -> SearchPage(
                    state = state.search,
                    innerPadding = innerPadding,
                    authToken = state.account.token.value
                )
                Page.Account -> AccountPage(state.account, innerPadding, onTokenChanged = state.onTokenChanged)
            }

            state.nfcFound.value?.let {
                Dialog(
                    onDismissRequest = {
                        state.nfcFound.value = null
                    }
                ) {
                    NfcDialog(state)
                }
            }


        }
    }
}


@Composable
fun NfcDialog(state: MainViewModel) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if(state.account.token.value.isNullOrBlank()) {
            Text(
                text = "Please log in to unlock this bathroom.",
                modifier = Modifier
                    .padding(8.dp)
            )
        } else {
            Text(
                text = "Welcome to the bathroom, " + state.account.username.value + "!",
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    val vm = MainViewModel() {}
    Root(vm)
}

@Preview(showBackground = true)
@Composable
fun nfcDiagogPreview() {
    val vm = MainViewModel() {}
    NfcDialog(vm)
}