package edu.temple.flushfinder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import edu.temple.flushfinder.ui.theme.FlushFinderTheme
import org.junit.Rule
import org.junit.Test

class ReviewDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyReviewShowsValidationError() {
        val state = SearchState()
        val bathroom = BathroomLocation(
            bathroomId = 1,
            name = "Test Bathroom",
            customerOnly = false,
            latitude = 39.0,
            longitude = -75.0,
            rating = 4.0,
            changingStation = true,
            airDryer = true,
            paperTowels = true,
            handSanitizer = true,
            wheelchair = true,
            singleOccupancy = false
        )

        composeTestRule.setContent {
            FlushFinderTheme {
                ReviewDialog(
                    state = state,
                    bathroom = bathroom,
                    onSubmit = { _, _ -> }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Submit Review")
            .performClick()
        composeTestRule
            .onNodeWithText("Please type a review")
            .assertIsDisplayed()
    }

    @Test
    fun reviewDialogShowsBathroomName() {
        val state = SearchState()
        val bathroom = BathroomLocation(
            bathroomId = 1,
            name = "Charles Library",
            customerOnly = false,
            latitude = 39.0,
            longitude = -75.0,
            rating = 4.0,
            changingStation = true,
            airDryer = true,
            paperTowels = true,
            handSanitizer = true,
            wheelchair = true,
            singleOccupancy = false
        )

        composeTestRule.setContent {
            FlushFinderTheme {
                ReviewDialog(
                    state = state,
                    bathroom = bathroom,
                    onSubmit = { _, _ -> }
                )
            }
        }

        composeTestRule
            .onNodeWithText("How was the bathroom at Charles Library?")
            .assertIsDisplayed()
    }
}