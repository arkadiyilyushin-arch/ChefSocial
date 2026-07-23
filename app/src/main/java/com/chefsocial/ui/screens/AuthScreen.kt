package com.chefsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chefsocial.ui.components.CheflyBackground
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyBrown
import com.chefsocial.ui.theme.CheflyError
import com.chefsocial.ui.theme.CheflyLink
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflyCardColors
import com.chefsocial.ui.theme.cheflyTextFieldColors

private enum class AuthTab { LOGIN, REGISTER }

@Composable
fun AuthScreen(
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (email: String, password: String) -> Boolean,
    onAuthenticated: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var selectedTab by rememberSaveable { mutableIntStateOf(AuthTab.LOGIN.ordinal) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    CheflyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = strings.appTitle,
                    tint = CheflyBrown,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = strings.appTitle,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.authWelcomeBack,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                colors = cheflyCardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AuthTabRow(
                        loginLabel = strings.authLogin,
                        registerLabel = strings.authRegister,
                        selectedTab = if (selectedTab == AuthTab.LOGIN.ordinal) AuthTab.LOGIN else AuthTab.REGISTER,
                        onSelect = { tab ->
                            selectedTab = tab.ordinal
                            errorMessage = null
                        },
                    )

                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = strings.authEmail,
                        placeholder = strings.authEmailHint,
                        leadingIcon = Icons.Default.Email,
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = strings.authPassword,
                        placeholder = strings.authPasswordHint,
                        supportingText = strings.authPasswordHint,
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        showPasswordLabel = strings.showPassword,
                        hidePasswordLabel = strings.hidePassword,
                    )

                    if (selectedTab == AuthTab.REGISTER.ordinal) {
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = strings.authConfirmPassword,
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                            showPasswordLabel = strings.showPassword,
                            hidePasswordLabel = strings.hidePassword,
                        )
                    } else {
                        Text(
                            text = strings.authForgotPassword,
                            color = CheflyLink,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable(onClick = onForgotPassword),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = CheflyError,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Button(
                        onClick = {
                            errorMessage = when {
                                email.isBlank() || password.isBlank() ->
                                    strings.authFillAllFields
                                selectedTab == AuthTab.REGISTER.ordinal && password != confirmPassword ->
                                    strings.authPasswordMismatch
                                selectedTab == AuthTab.REGISTER.ordinal && password.length < 6 ->
                                    strings.authPasswordTooShort
                                selectedTab == AuthTab.LOGIN.ordinal -> {
                                    if (onLogin(email, password)) {
                                        onAuthenticated()
                                        null
                                    } else {
                                        strings.authInvalidCredentials
                                    }
                                }
                                else -> {
                                    if (onRegister(email, password)) {
                                        onAuthenticated()
                                        null
                                    } else {
                                        strings.authRegisterFailed
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CheflyTerracotta,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = if (selectedTab == AuthTab.LOGIN.ordinal) strings.authLogin else strings.authRegister,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTabRow(
    loginLabel: String,
    registerLabel: String,
    selectedTab: AuthTab,
    onSelect: (AuthTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        AuthTabChip(
            text = loginLabel,
            selected = selectedTab == AuthTab.LOGIN,
            onClick = { onSelect(AuthTab.LOGIN) },
            modifier = Modifier.weight(1f),
        )
        AuthTabChip(
            text = registerLabel,
            selected = selectedTab == AuthTab.REGISTER,
            onClick = { onSelect(AuthTab.REGISTER) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthTabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    placeholder: String = "",
    supportingText: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    showPasswordLabel: String = "",
    hidePasswordLabel: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) }
        } else {
            null
        },
        supportingText = supportingText?.let { hint ->
            { Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (passwordVisible) hidePasswordLabel else showPasswordLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            null
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions(keyboardType = KeyboardType.Email)
        },
        singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = cheflyTextFieldColors(),
    )
}
