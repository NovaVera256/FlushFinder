package edu.temple.flushfinder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.temple.flushfinder.ui.theme.FlushFinderTheme

@Composable
fun LoginPage(state: AccountState, innerPadding: PaddingValues) {
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
                    state.token.value = null
                    state.isLoggedIn.value = false
                    state.password.value = ""
                    state.errorMessage.value = null
                    state.isRegisterMode.value = false
                          },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        } else {
            Text(
                text = if (state.isRegisterMode.value) "Create Account" else "Login",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = state.username.value,
                onValueChange = {
                    state.username.value = it
                    state.errorMessage.value = null
                                },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading.value,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
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
                enabled = !state.isLoading.value,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    cursorColor = MaterialTheme.colorScheme.secondary
                )
            )

            state.errorMessage.value?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isLoading.value) {
                CircularProgressIndicator()
            }

            Button(
                onClick = {
                    val username = state.username.value.trim()
                    val password = state.password.value

                    if (username.isBlank() || password.isBlank()) {
                        state.errorMessage.value = "Please enter both username and password."
                        return@Button
                    }

                    state.isLoading.value = true
                    state.errorMessage.value = null

                    val callback: (AuthResult) -> Unit = { result ->
                        state.isLoading.value = false

                        if (result.token != null) {
                            state.token.value = result.token
                            state.isLoggedIn.value = true
                            state.errorMessage.value = null
                            state.password.value = ""
                        } else {
                            state.token.value = null
                            state.isLoggedIn.value = false
                            state.errorMessage.value = result.error ?: "Authentication failed."
                        }
                    }

                    if (state.isRegisterMode.value) {
                        AuthApi.register(username, password, callback)
                    } else {
                        AuthApi.login(username, password, callback)
                    }
                          },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading.value
            ) {
                Text(if (state.isRegisterMode.value) "Create Account" else "Log In"
                )
            }
            TextButton(
                onClick = {
                    state.isRegisterMode.value = !state.isRegisterMode.value
                    state.errorMessage.value = null
                          },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading.value
            ) {
                Text(
                    if (state.isRegisterMode.value) {
                        "Already have an account? Log In"
                    } else {
                        "Need an account? Create Account"
                    },
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    FlushFinderTheme(true) {
        Scaffold {
            LoginPage(AccountState(), it)
        }
    }

}