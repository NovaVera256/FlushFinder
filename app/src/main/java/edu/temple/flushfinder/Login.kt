package edu.temple.flushfinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun Login(state: AccountState, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        if (state.isLoggedIn.value) {
            Text(
                text = "You are logged in as ${state.username.value}",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = {
                    state.isLoggedIn.value = false
                    state.password.value = ""
                    state.errorMessage.value = null
                          },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        } else {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = state.username.value,
                onValueChange = {
                    state.username.value = it
                    state.errorMessage.value = null
                                },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(MaterialTheme.colorScheme.onPrimary)
            )

            OutlinedTextField(
                value = state.password.value,
                onValueChange = {
                    state.password.value = it
                    state.errorMessage.value = null
                                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(MaterialTheme.colorScheme.onPrimary)
            )

            if (state.errorMessage.value != null) {
                Text(
                    text = state.errorMessage.value!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(

                onClick = {

                    if (state.username.value.isBlank() || state.password.value.isBlank()) {
                        state.errorMessage.value = "Please enter both username and password."
                    } else {
                        state.errorMessage.value = null
                        state.isLoggedIn.value = true
                    }
                          },

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
            }

            TextButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Account")
            }
        }
    }
}