package com.vital.ui.navigation

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vital.ui.LiveIslandViewModel
import com.vital.ui.ReminderStatusViewModel
import com.vital.ui.TutorialViewModel
import com.vital.ui.components.MoreSheet
import com.vital.ui.components.QuickAddAction
import com.vital.ui.components.VITAL_TABS
import com.vital.ui.components.VitalBottomNav
import com.vital.ui.screens.CardioScreen
import com.vital.ui.screens.CoachChatScreen
import com.vital.ui.screens.DebugShowcaseScreen
import com.vital.ui.screens.ExerciseLibraryScreen
import com.vital.ui.screens.LiftScreen
import com.vital.ui.screens.MealsScreen
import com.vital.ui.screens.ProgressScreen
import com.vital.ui.screens.SettingsScreen
import com.vital.ui.screens.SummaryScreen
import com.vital.ui.screens.TodayScreen
import com.vital.ui.screens.TutorialScreen
import com.vital.ui.screens.WeeksScreen
import com.vital.ui.theme.VitalColors

@Composable
fun VitalNavigation() {
    // First run: the walk-through replaces the shell until it is finished or skipped.
    val tutorialViewModel: TutorialViewModel = hiltViewModel()
    val showTutorial by tutorialViewModel.showTutorial.collectAsStateWithLifecycle()
    if (showTutorial) {
        TutorialScreen(onFinish = tutorialViewModel::finish)
        return
    }

    val navController = rememberNavController()
    var moreOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isDebuggable = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val openDebugShowcase: (() -> Unit)? = if (isDebuggable) {
        { navController.navigate("debug") { launchSingleTop = true } }
    } else {
        null
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabIndex = when (currentRoute) {
        "meals" -> 0
        "lift" -> 1
        "today" -> 2
        "cardio" -> 3
        else -> 4
    }
    val selectedIndex = if (moreOpen) 4 else tabIndex

    val navigateToTab: (Int) -> Unit = { index ->
        moreOpen = false
        val route = VITAL_TABS.getOrNull(index)?.route ?: "today"
        if (route != "more") {
            if (route == "today") {
                // restoreState would also restore a detail route (e.g. Settings) saved on top
                // of Today, so return to Today by popping rather than re-navigating.
                if (!navController.popBackStack("today", inclusive = false)) {
                    navController.navigate("today") { launchSingleTop = true }
                }
            } else {
                navController.navigate(route) {
                    popUpTo("today") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val openRoute: (String) -> Unit = { route ->
        moreOpen = false
        navController.navigate(route) { launchSingleTop = true }
    }

    val islandViewModel: LiveIslandViewModel = hiltViewModel()
    val islandState by islandViewModel.state.collectAsStateWithLifecycle()

    // The rest notification is the point of the foreground service, so ask for
    // POST_NOTIFICATIONS the first time a rest actually starts (Android 13+).
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }
    var askedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(islandState.isActive) {
        if (islandState.isActive && !askedNotificationPermission &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            askedNotificationPermission = true
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val reminderViewModel: ReminderStatusViewModel = hiltViewModel()
    val moreDotVisible by reminderViewModel.moreDotVisible.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = VitalColors.paper,
        bottomBar = {
            VitalBottomNav(
                selectedIndex = selectedIndex,
                onTabSelected = { index ->
                    if (index == 4) {
                        moreOpen = !moreOpen
                    } else {
                        navigateToTab(index)
                    }
                },
                showMoreDot = moreDotVisible,
                // The nav divider is the only in-app rest surface; it drains as time runs out.
                restProgress = if (islandState.isActive) islandState.progress else null
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "today",
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                composable("today") {
                    TodayScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSummary = { openRoute("summary") },
                        onNavigateToMeals = { navigateToTab(0) },
                        onNavigateToLift = { navigateToTab(1) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { openRoute("weeks") },
                        onNavigateToSettings = { openRoute("settings") }
                    )
                }
                composable("meals") {
                    MealsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSummary = { openRoute("summary") },
                        onNavigateToToday = { navigateToTab(2) },
                        onNavigateToLift = { navigateToTab(1) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { openRoute("weeks") },
                        onNavigateToSettings = { openRoute("settings") }
                    )
                }
                composable("lift") {
                    LiftScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSummary = { openRoute("summary") },
                        onNavigateToToday = { navigateToTab(2) },
                        onNavigateToMeals = { navigateToTab(0) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { openRoute("weeks") },
                        onNavigateToSettings = { openRoute("settings") }
                    )
                }
                composable("cardio") {
                    CardioScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSummary = { openRoute("summary") },
                        onNavigateToToday = { navigateToTab(2) },
                        onNavigateToMeals = { navigateToTab(0) },
                        onNavigateToLift = { navigateToTab(1) },
                        onNavigateToWeeks = { openRoute("weeks") },
                        onNavigateToSettings = { openRoute("settings") }
                    )
                }
                composable("weeks") {
                    WeeksScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSummary = { openRoute("summary") },
                        onNavigateToToday = { navigateToTab(2) },
                        onNavigateToMeals = { navigateToTab(0) },
                        onNavigateToLift = { navigateToTab(1) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToSettings = { openRoute("settings") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("exercises") {
                    ExerciseLibraryScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBack = { navController.popBackStack() },
                        onOpenLift = { navigateToTab(1) }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToToday = { navigateToTab(2) },
                        onNavigateToMeals = { navigateToTab(0) },
                        onNavigateToLift = { navigateToTab(1) },
                        onNavigateToCardio = { navigateToTab(3) },
                        onNavigateToWeeks = { openRoute("weeks") },
                        onOpenDebugShowcase = openDebugShowcase,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("summary") {
                    SummaryScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBack = { navController.popBackStack() }
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
                        DebugShowcaseScreen(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            MoreSheet(
                isOpen = moreOpen,
                onDismiss = { moreOpen = false },
                onQuickAdd = { action ->
                    moreOpen = false
                    when (action) {
                        QuickAddAction.LogMeal -> navigateToTab(0)
                        QuickAddAction.LogSet -> navigateToTab(1)
                        QuickAddAction.LogCardio -> navigateToTab(3)
                        QuickAddAction.LogBodyWeight -> openRoute("settings")
                    }
                },
                onNavigate = openRoute,
                showDebug = isDebuggable
            )
        }
    }
}
