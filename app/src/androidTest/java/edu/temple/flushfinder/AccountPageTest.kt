package edu.temple.flushfinder

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import org.junit.Rule
import org.junit.Test

class AccountPageTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyLoginShowsValidationError() {
        val state = AccountState()

        composeTestRule.setContent {
            FlushFinderTheme {
                AccountPage(
                    state = state,
                    innerPadding = PaddingValues(),
                    onTokenChanged = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Log In")
            .performClick()

        composeTestRule
            .onNodeWithText("Please enter both username and password.")
            .assertIsDisplayed()
    }

    @Test
    fun createAccountButtonSwitchesToCreateAccountMode() {
        val state = AccountState()
        composeTestRule.setContent {
            FlushFinderTheme {
                AccountPage(
                    state = state,
                    innerPadding = PaddingValues(),
                    onTokenChanged = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Need an account? Create Account")
            .performClick()

        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Already have an account? Log In")
            .assertIsDisplayed()
    }

    @Test
    fun loggedInStateShowsLogoutButton() {
        val state = AccountState()
        state.username.value = "testuser"
        state.token.value = "fake-token"
        state.isLoggedIn.value = true

        composeTestRule.setContent {
            FlushFinderTheme {
                AccountPage(
                    state = state,
                    innerPadding = PaddingValues(),
                    onTokenChanged = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("You are logged in as testuser")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Log Out")
            .assertIsDisplayed()
    }

    @Test
    fun logoutClearsTokenAndLoggedInState() {
        val state = AccountState()
        var latestToken: String? = "fake-token"

        state.username.value = "testuser"
        state.token.value = "fake-token"
        state.isLoggedIn.value = true

        composeTestRule.setContent {
            FlushFinderTheme {
                AccountPage(
                    state = state,
                    innerPadding = PaddingValues(),
                    onTokenChanged = {latestToken = it}
                )
            }
        }

        composeTestRule.onNodeWithText("Log Out").performClick()
        assert(!state.isLoggedIn.value)
        assert(state.token.value == null)
        assert(latestToken == null)
    }

    @Test
    fun typingUsernameAndPasswordUpdatesState() {
        val state = AccountState()
        composeTestRule.setContent {
            FlushFinderTheme {
                AccountPage(
                    state = state,
                    innerPadding = PaddingValues(),
                    onTokenChanged = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Username")
            .performTextInput("testingyeah")
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")
        assert(state.username.value == "testingyeah")
        assert(state.password.value == "password123")
    }
}