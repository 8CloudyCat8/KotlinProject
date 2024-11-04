package com.example.kotlinproject.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinproject.models.UserPreferences
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences,
    onLanguageChanged: (String) -> Unit,
    onNavigateToMovieList: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("ru-RU" to "Русский", "en-EN" to "Английский")
    var selectedLanguage by remember { mutableStateOf("ru-RU") }

    var sliderValue by remember { mutableFloatStateOf(1f) }
    var pendingSliderValue by remember { mutableFloatStateOf(sliderValue) }

    LaunchedEffect(Unit) {
        userPreferences.languageFlow.collect { language ->
            selectedLanguage = language
        }
    }

    LaunchedEffect(Unit) {
        userPreferences.sliderValueFlow.collect { value ->
            sliderValue = value
            pendingSliderValue = value
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Настройки",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Выберите язык:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = languages.find { it.first == selectedLanguage }?.second ?: "Русский",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(12.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge
                )

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    languages.forEach { (langCode, langLabel) ->
                        DropdownMenuItem(
                            text = { Text(langLabel, color = Color.Black) },
                            onClick = {
                                expanded = false
                                selectedLanguage = langCode
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Минимальный рейтинг: %.1f".format(pendingSliderValue),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = pendingSliderValue,
                    onValueChange = { newValue ->
                        pendingSliderValue = (newValue * 10).roundToInt() / 10f
                    },
                    valueRange = 1f..10f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFA500),
                        activeTrackColor = Color(0xFFFFA500),
                        inactiveTrackColor = Color.Gray
                    )
                )

                Button(
                    onClick = {
                        scope.launch {
                            userPreferences.saveLanguage(selectedLanguage)
                            userPreferences.saveSliderValue(pendingSliderValue)
                            onLanguageChanged(selectedLanguage)
                            onNavigateToMovieList()
                        }
                    },
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Готово", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}