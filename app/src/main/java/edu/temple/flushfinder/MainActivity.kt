package edu.temple.flushfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.lifecycle.ViewModel
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import java.util.EnumSet

enum class Page {
    Review,
    Search,
    Account
}

class Amenities {
    companion object {
        val names = listOf(
            "Changing Station",
            "Dryer",
            "Wheelchair Accessible",
            "Paper",
        )
        val ChangingStation = 1
        val Dryer = 2
        val Accessible = 4
        val Paper = 8
    }
}

data class SearchState (
    val searchDistance: MutableFloatState = mutableFloatStateOf(0f),

    val accessOptions: List<String> = listOf("Free", "Customers", "Door Code"),
    val accessSelection: MutableState<List<Boolean>> = mutableStateOf(listOf(false, false, false)),

    val amenitiesOptions: List<String> = Amenities.names,
    val amenitiesSelection: MutableIntState = mutableIntStateOf(0)
)

data class AccountState (
    val dummy: Boolean = true
)

data class ReviewState (
    val dummy: Boolean = true
)

/*
 * ViewModel for the entire application
 * Each page is given its own data class (see SearchState) to manage structure
 */
class MainViewModel (
    val page: MutableState<Page> = mutableStateOf(Page.Search),

    val review: ReviewState = ReviewState(),
    val search: SearchState = SearchState(),
    val account: AccountState = AccountState()
): ViewModel()

class MainActivity : ComponentActivity() {
    val viewmodel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Root(viewmodel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Root(state: MainViewModel) {
    FlushFinderTheme {
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
                        state.page.value == Page.Review,
                        {
                            state.page.value = Page.Review
                        },
                        {
                            Icon(Icons.Default.StarRate, null)
                        },
                        label = {
                            Text("Review")
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
                Page.Review -> ReviewPage(state.review, innerPadding)
                Page.Search -> SearchPage(state.search, innerPadding)
                Page.Account -> AccountPage(state.account, innerPadding)
            }
        }
    }
}


@Composable
fun ReviewPage(state: ReviewState, innerPadding: PaddingValues) {
    Text("Review stuff")
}

@Composable
fun AmenitiesBox(options: List<String>, selected: MutableIntState) {
    Row(
        Modifier.padding(20.dp).height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            options.forEachIndexed { index, string ->
                if(index.mod(2) == 0) return@forEachIndexed;

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(string)

                    Checkbox(selected.intValue and (1 shl index) != 0, {
                        selected.intValue = selected.intValue xor (1 shl index)
                    })
                }
            }
        }
        VerticalDivider()
        Column(
            horizontalAlignment = Alignment.End
        ) {
            options.forEachIndexed { index, string ->
                if(index.mod(2) == 1) return@forEachIndexed

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(string)

                    Checkbox(selected.intValue and (1 shl index) != 0, {
                        selected.intValue = selected.intValue xor (1 shl index)
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(state: SearchState, innerPadding: PaddingValues) {
    Column(
        Modifier
            .padding(innerPadding)
            .fillMaxWidth(),
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
            steps = 10
        )

        HorizontalDivider()

        Text("Access")

        MultiChoiceSegmentedButtonRow {
            state.accessOptions.forEachIndexed { index, string ->

                SegmentedButton(
                    state.accessSelection.value[index],
                    {
                        state.accessSelection.value = state.accessSelection.value.mapIndexed { i, v ->
                            if(index == i) !v
                            else v
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, state.accessOptions.size),
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

        HorizontalDivider()

        Text("Amenities")

        AmenitiesBox(state.amenitiesOptions, state.amenitiesSelection)

        Spacer(Modifier.weight(1f))

        Button(
            {
            },
            Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Text("Find The Flush!")
        }
    }
}

@Composable
fun AccountPage(state: AccountState, innerPadding: PaddingValues) {
    Text("Account stuff")
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    val vm = MainViewModel()
    Root(vm)
}