package com.sagrd.mentorly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sagrd.mentorly.presentation.navigation.AppNavigation
import com.sagrd.mentorly.ui.theme.MentorlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MentorlyTheme {
                AppNavigation()
            }
        }
    }
}
