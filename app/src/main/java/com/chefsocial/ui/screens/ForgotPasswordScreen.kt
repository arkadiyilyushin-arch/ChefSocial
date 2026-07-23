package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.CheflyTopBarWithBack
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyError
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflyCardColors
import com.chefsocial.ui.theme.cheflyTextFieldColors
import com.chefsocial.ui.viewmodel.ChefViewModel

private enum class ForgotStep { EMAIL, NEW_PASSWORD, SUCCESS }

@Composable
fun ForgotPasswordScreen(
    viewModel: ChefViewModel,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var step by rememberSaveable { mutableStateOf(ForgotStep.EMAIL.name) }
    var email by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val currentStep = ForgotStep.valueOf(step)

    CheflyScaffold(
        topBar = {
            CheflyTopBarWithBack(
                title = { Text(strings.forgotPasswordTitle) },
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = when (currentStep) {
                    ForgotStep.EMAIL -> strings.forgotPasswordSubtitle
                    ForgotStep.NEW_PASSWORD -> strings.authForgotPasswordHint
                    ForgotStep.SUCCESS -> strings.forgotPasswordSuccess
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = cheflyCardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (currentStep) {
                        ForgotStep.EMAIL -> {
                            AuthField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    errorMessage = null
                                },
                                label = strings.authEmail,
                                placeholder = strings.authEmailHint,
                                leadingIcon = Icons.Default.Email,
                            )
                        }
                        ForgotStep.NEW_PASSWORD -> {
                            AuthField(
                                value = email,
                                onValueChange = {},
                                label = strings.authEmail,
                                leadingIcon = Icons.Default.Email,
                                enabled = false,
                            )
                            AuthField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMessage = null
                                },
                                label = strings.forgotPasswordNewPassword,
                                placeholder = strings.authPasswordHint,
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                showPasswordLabel = strings.showPassword,
                                hidePasswordLabel = strings.hidePassword,
                            )
                            AuthField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                label = strings.authConfirmPassword,
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                showPasswordLabel = strings.showPassword,
                                hidePasswordLabel = strings.hidePassword,
                            )
                        }
                        ForgotStep.SUCCESS -> {
                            Text(
                                text = strings.forgotPasswordSuccess,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = CheflyError,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }

                    when (currentStep) {
                        ForgotStep.EMAIL -> {
                            Button(
                                onClick = {
                                    errorMessage = when {
                                        email.isBlank() -> strings.authFillAllFields
                                        !viewModel.canResetPassword(email) ->
                                            strings.forgotPasswordUnknownEmail
                                        else -> {
                                            step = ForgotStep.NEW_PASSWORD.name
                                            null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CheflyTerracotta,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text(strings.forgotPasswordContinue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        ForgotStep.NEW_PASSWORD -> {
                            Button(
                                onClick = {
                                    errorMessage = when {
                                        newPassword.length < 6 -> strings.authPasswordTooShort
                                        newPassword != confirmPassword -> strings.authPasswordMismatch
                                        !viewModel.resetPassword(email, newPassword) ->
                                            strings.passwordChangeFailed
                                        else -> {
                                            step = ForgotStep.SUCCESS.name
                                            null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CheflyTerracotta,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text(strings.forgotPasswordSubmit, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        ForgotStep.SUCCESS -> {
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CheflyTerracotta,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text(strings.forgotPasswordBackToLogin, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (currentStep != ForgotStep.SUCCESS) {
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(strings.forgotPasswordBackToLogin)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    enabled: Boolean = true,
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
        enabled = enabled,
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) }
        } else {
            null
        },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
        ),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = cheflyTextFieldColors(),
    )
}
