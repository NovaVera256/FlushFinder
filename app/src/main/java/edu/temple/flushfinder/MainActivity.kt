package edu.temple.flushfinder

import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
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

enum class Page {
    Review,
    Search,
    Account
}

data class SearchState (
    val searchDistance: MutableFloatState = mutableFloatStateOf(0f)
)

/*
 * ViewModel for the entire application
 * Each page is given its own data class (see SearchState) to manage structure
 */
class MainViewModel (
    val page: MutableState<Page> = mutableStateOf(Page.Search),
    val search: SearchState = SearchState()
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
            SearchPage(state.search, innerPadding)
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
        horizontalAlignment = Alignment.CenterHorizontally
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

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    val vm = MainViewModel()
    Root(vm)
}