package com.example.jellyfinryan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.example.jellyfinryan.viewmodel.LoginViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var serverUrl by remember { mutableStateOf("http://") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(480.dp)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Connect to Jellyfin",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("Server URL") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.login(serverUrl, username, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loginState.isLoading && serverUrl.isNotBlank() && username.isNotBlank()
                        ) {
                            if (loginState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Connect")
                            }
                        }
                    }

                    if (loginState.errorMessage != null) {
                        item {
                            Text(
                                text = loginState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}