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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.ApplicationInfo
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vital.ui.components.GlassBottomNav
import com.vital.ui.components.QuickAddAction
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

    // Debug showcase route: only registered in a debuggable build, no BuildConfig needed.
    val context = LocalContext.current
    val isDebuggable = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val openDebugShowcase: (() -> Unit)? = if (isDebuggable) {
        { navController.navigate("debug") }
    } else {
        null
    }

    // One route for the nav and the QUICK ADD sheet, so the highlight always matches the page.
    val navigateToTab: (Int) -> Unit = { index ->
        selectedTab.value = index
        quickAddSheetOpen.value = false
        val route = when (index) {
            0 -> "today"
            1 -> "meals"
            2 -> "lift"
            3 -> "cardio"
            4 -> "weeks"
            else -> "settings"
        }
        navController.navigate(route) {
            popUpTo("today") { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            GlassBottomNav(
                selectedTab = selectedTab.value,
                onTabSelected = navigateToTab,
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
                        onNavigateToMeals = { navigateToTab(1) },
                        onNavigateToLift = { navigateToTab(2) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { navigateToTab(4) },
                        onNavigateToSettings = { navigateToTab(5) }
                    )
                }
                composable("meals") {
                    MealsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navigateToTab(0) },
                        onNavigateToLift = { navigateToTab(2) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { navigateToTab(4) },
                        onNavigateToSettings = { navigateToTab(5) }
                    )
                }
                composable("lift") {
                    LiftScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navigateToTab(0) },
                        onNavigateToMeals = { navigateToTab(1) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { navigateToTab(4) },
                        onNavigateToSettings = { navigateToTab(5) }
                    )
                }
                composable("cardio") {
                    CardioScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowMore = { navController.navigate("summary") },
                        onNavigateToToday = { navigateToTab(0) },
                        onNavigateToMeals = { navigateToTab(1) },
                        onNavigateToLift = { navigateToTab(2) },
                        onNavigateToWeeks = { navigateToTab(4) },
                        onNavigateToSettings = { navigateToTab(5) }
                    )
                }
                composable("weeks") {
                    WeeksScreen(
                        modifier = Modifier.padding(innerPadding),
                        onShowSummary = { navController.navigate("summary") },
                        onNavigateToToday = { navigateToTab(0) },
                        onNavigateToMeals = { navigateToTab(1) },
                        onNavigateToLift = { navigateToTab(2) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToSettings = { navigateToTab(5) }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToToday = { navigateToTab(0) },
                        onNavigateToMeals = { navigateToTab(1) },
                        onNavigateToLift = { navigateToTab(2) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { navigateToTab(4) },
                        onOpenDebugShowcase = openDebugShowcase
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
                if (isDebuggable) {
                    composable("debug") {
                        DebugShowcaseScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }

            // QUICK ADD bottom sheet (tap on +)
            QuickAddSheet(
                isOpen = quickAddSheetOpen.value,
                onDismiss = { quickAddSheetOpen.value = false },
                onAction = { action ->
                    when (action) {
                        QuickAddAction.LogMeal -> navigateToTab(1)
                        QuickAddAction.LogSet -> navigateToTab(2)
                        QuickAddAction.LogCardio -> navigateToTab(3)
                        QuickAddAction.LogBodyWeight -> navigateToTab(5)
                    }
                }
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
        com.vital.ui.theme.VitalColors.orange,
        com.vital.ui.theme.VitalColors.orange,
        com.vital.ui.theme.VitalColors.orange,
        com.vital.ui.theme.VitalColors.orange,
        com.vital.ui.theme.VitalColors.orange,
        com.vital.ui.theme.VitalColors.orange
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 8.dp),
        color = com.vital.ui.theme.VitalColors.wellFill,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, com.vital.ui.theme.VitalColors.wellStroke)
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
                            color = if (isSelected) Color.Black else com.vital.ui.theme.VitalColors.textMuted,
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