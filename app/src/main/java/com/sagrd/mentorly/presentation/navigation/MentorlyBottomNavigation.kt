package com.sagrd.mentorly.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.sagrd.mentorly.ui.theme.MentorlyTheme

private data class BottomNavigationItem(
    val section: MentorlySection,
    val label: String,
    val icon: ImageVector
)

private val bottomNavigationItems = listOf(
    BottomNavigationItem(
        section = MentorlySection.HOME,
        label = "Inicio",
        icon = Icons.Outlined.Home
    ),
    BottomNavigationItem(
        section = MentorlySection.COURSES,
        label = "Cursos",
        icon = Icons.AutoMirrored.Outlined.MenuBook
    ),
    BottomNavigationItem(
        section = MentorlySection.LEARNING,
        label = "Aprendizaje",
        icon = Icons.Outlined.School
    ),
    BottomNavigationItem(
        section = MentorlySection.REVIEWS,
        label = "Revisiones",
        icon = Icons.Outlined.RateReview
    ),
    BottomNavigationItem(
        section = MentorlySection.PROFILE,
        label = "Perfil",
        icon = Icons.Outlined.Person
    )
)

@Composable
fun MentorlyBottomNavigation(
    currentSection: MentorlySection,
    onSectionSelected: (MentorlySection) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                selected = item.section == currentSection,
                onClick = {
                    onSectionSelected(item.section)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                },
                alwaysShowLabel = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MentorlyBottomNavigationPreview() {
    MentorlyTheme {
        MentorlyBottomNavigation(
            currentSection = MentorlySection.COURSES,
            onSectionSelected = {}
        )
    }
}
