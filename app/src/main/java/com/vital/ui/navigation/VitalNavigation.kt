package com.vital.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vital.ui.components.GlassBottomNav
import com.vital.ui.components.QuickAddSheet
import com.vital.ui.screens.CardioScreen
import com.vital.ui.screens.CoachChatScreen
import com.vital.ui.screens.DebugShowcaseScreen
import com.vital.ui.screens.LiftScreen
import com.vital.ui.screens.MealsScreen
import com.vital.ui.screens.ProgressScreen
import com.vital.ui.screens.SettingsScreen
import com.vital.ui.screens.SummaryDialScreen
import com.vital.ui.screens.TodayScreen
import com.vital.ui.screens.WeeksScreen
import com.vital.ui.MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text

@Composable
fun VitalNavigation() {
    val navController = rememberNavController()
    val selectedTab = remember { mutableStateOf(0) }
    val quickAddSheetOpen = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            GlassBottomNav(
                selectedTab = selectedTab.value,
                onTabSelected = { index ->
                    selectedTab.value = index
                    when (index) {
                        0 -> navController.navigate("today") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                        1 -> navController.navigate("meals") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                        2 -> navController.navigate("lift") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                        3 -> navController.navigate("cardio") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                        4 -> navController.navigate("weeks") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                        5 -> navController.navigate("settings") { popUpTo("today") { inclusive = true }; launchSingleTop = true }
                    }
                },
                onPlusClick = { quickAddSheetOpen.value = true }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController, "today") {
                composable("today") {
                    TodayScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToMeals = { navController.navigate("meals") },
                        onNavigateToLift = { navController.navigate("lift") },
                        onNavigateToCardio = { navController.navigate("cardio") },
                        onNavigateToWeeks = { navController.navigate("weeks") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("meals") {
                    MealsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navController.popBackStack("today", false) },
                        onNavigateToLift = { navController.navigate("lift") },
                        onNavigateToCardio = { navController.navigate("cardio") },
                        onNavigateToWeeks = { navController.navigate("weeks") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("lift") {
                    LiftScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navController.popBackStack("today", false) },
                        onNavigateToMeals = { navController.navigate("meals") },
                        onNavigateToCardio = { navController.navigate("cardio") },
                        onNavigateToWeeks = { navController.navigate("weeks") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("cardio") {
                    CardioScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navController.popBackStack("today", false) },
                        onNavigateToMeals = { navController.navigate("meals") },
                        onNavigateToLift = { navController.navigate("lift") },
                        onNavigateToWeeks = { navController.navigate("weeks") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("weeks") {
                    WeeksScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navController.popBackStack("today", false) },
                        onNavigateToMeals = { navController.navigate("meals") },
                        onNavigateToLift = { navController.navigate("lift") },
                        onNavigateToCardio = { navController.navigate("cardio") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToToday = { navController.popBackStack("today", false) },
                        onNavigateToMeals = { navController.navigate("meals") },
                        onNavigateToLift = { navController.navigate("lift") },
                        onNavigateToCardio = { navController.navigate("cardio") },
                        onNavigateToWeeks = { navController.navigate("weeks") }
                    )
                }
                composable("summary") {
                    SummaryDialScreen(
                        onClose = { navController.popBackStack() }
                    )
                }
                composable("coach") {
                    CoachChatScreen(
                        onClose = { navController.popBackStack() }
                    )
                }
                composable("progress") {
                    ProgressScreen(
                        onClose = { navController.popBackStack() }
                    )
                }
            }

            // Quick Add Sheet
            QuickAddSheet(
                isOpen = quickAddSheetOpen.value,
                onDismiss = { quickAddSheetOpen.value = false },
                onLogMeal = { navController.navigate("meals") },
                onLogSet = { navController.navigate("lift") },
                onLogCardio = { navController.navigate("cardio") },
                onLogBodyWeight = { navController.navigate("settings") }
            )
        }
    }
}

@Composable
fun TabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>
) {

    val tabColors = listOf(
        com.vital.ui.theme.VitalColors.AccentTDY,
        com.vital.ui.theme.VitalColors.AccentMLS,
        com.vital.ui.theme.VitalColors.AccentLFT,
        com.vital.ui.theme.VitalColors.AccentCRD,
        com.vital.ui.theme.VitalColors.AccentWKS,
        com.vital.ui.theme.VitalColors.AccentSET
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 8.dp),
        color = com.vital.ui.theme.VitalColors.Card,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, com.vital.ui.theme.VitalColors.NearBlackRowStroke)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedTab
                val accentColor = tabColors[index]

                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .clickable { onTabSelected(index) },
                    color = if (isSelected) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.Black else com.vital.ui.theme.VitalColors.MutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

data class TabItem(val label: String, val icon: String)

val MAIN_TABS = listOf(
    TabItem("TDY", "today"),
    TabItem("MLS", "meals"),
    TabItem("LFT", "lift"),
    TabItem("CRD", "cardio"),
    TabItem("WKS", "weeks"),
    TabItem("SET", "settings")
)