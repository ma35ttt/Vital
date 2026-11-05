package com.vital.ui.navigation

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vital.ui.LiftViewModel
import com.vital.ui.LiveIslandViewModel
import com.vital.ui.ReminderStatusViewModel
import com.vital.ui.TutorialViewModel
import com.vital.ui.components.MoreSheet
import com.vital.ui.components.QuickAddAction
import com.vital.ui.components.VitalBottomNav
import com.vital.ui.screens.CardioScreen
import com.vital.ui.screens.CoachChatScreen
import com.vital.ui.screens.DebugShowcaseScreen
import com.vital.ui.screens.ExerciseLibraryScreen
import com.vital.ui.screens.GoalScreen
import com.vital.ui.screens.LiftScreen
import com.vital.ui.screens.MealsScreen
import com.vital.ui.screens.ProgressScreen
import com.vital.ui.screens.SettingsScreen
import com.vital.ui.screens.SummaryScreen
import com.vital.ui.screens.TodayScreen
import com.vital.ui.screens.TutorialScreen
import com.vital.ui.screens.WeeksScreen
import com.vital.ui.theme.VitalColors
import kotlinx.coroutines.launch

/** The four content tabs, in bottom-nav order. Each becomes a page of the tab pager. */
private val TAB_ROUTES = listOf("meals", "lift", "today", "cardio")

/** The NavHost destination that hosts the tab pager; detail routes push on top of it. */
private const val TABS_ROUTE = "tabs"

/** Today is the centre tab and the start page. */
private const val TODAY_PAGE = 2

@Composable
fun VitalNavigation() {
    // First run: the walk-through replaces the shell until it is finished or skipped.
    val tutorialViewModel: TutorialViewModel = hiltViewModel()
    val showTutorial by tutorialViewModel.showTutorial.collectAsStateWithLifecycle()
    if (showTutorial) {
        TutorialScreen(viewModel = tutorialViewModel)
        return
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var moreOpen by remember { mutableStateOf(false) }

    // The four tabs are pages of one pager, so a swipe tracks the finger and settles with
    // velocity instead of jumping on a threshold. Detail screens are NavHost destinations on top.
    val pagerState = rememberPagerState(initialPage = TODAY_PAGE, pageCount = { TAB_ROUTES.size })
    // Set when a tab is chosen while a detail route is open; applied once the pager is composed.
    var pendingTab by remember { mutableStateOf<Int?>(null) }

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
    // Null before the NavHost settles on its start destination: treat that as the tabs.
    val onTabs = currentRoute == null || currentRoute == TABS_ROUTE

    // On a detail route the bar keeps its old behaviour and highlights More.
    val selectedIndex = if (moreOpen || !onTabs) 4 else pagerState.currentPage

    val navigateToTab: (Int) -> Unit = { index ->
        moreOpen = false
        if (index in TAB_ROUTES.indices) {
            if (onTabs) {
                scope.launch { pagerState.animateScrollToPage(index) }
            } else {
                // Pop the detail route first, then snap the pager once it is composed again.
                pendingTab = index
                navController.popBackStack(TABS_ROUTE, inclusive = false)
            }
        }
    }

    val openRoute: (String) -> Unit = { route ->
        moreOpen = false
        navController.navigate(route) { launchSingleTop = true }
    }

    val islandViewModel: LiveIslandViewModel = hiltViewModel()
    val islandState by islandViewModel.state.collectAsStateWithLifecycle()

    // Hoisted so the More sheet's "New workout" can ask Lift to open its creator.
    val liftViewModel: LiftViewModel = hiltViewModel()

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
                startDestination = TABS_ROUTE,
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                // Detail routes ease in from the right and settle back on pop.
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(300)) { it / 6 } +
                        fadeIn(animationSpec = tween(220))
                },
                exitTransition = { fadeOut(animationSpec = tween(180)) },
                popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(300)) { it / 6 } +
                        fadeOut(animationSpec = tween(180))
                }
            ) {
                composable(TABS_ROUTE) {
                    LaunchedEffect(pendingTab) {
                        pendingTab?.let { page ->
                            pagerState.scrollToPage(page)
                            pendingTab = null
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !moreOpen,
                        // Pre-compose the neighbours so the first frames of a swipe are ready.
                        beyondViewportPageCount = 1,
                        key = { it }
                    ) { page ->
                        when (page) {
                            0 -> MealsScreen(
                                modifier = Modifier.fillMaxSize(),
                                onShowSummary = { openRoute("summary") },
                                onNavigateToToday = { navigateToTab(2) },
                                onNavigateToLift = { navigateToTab(1) },
                                onNavigateToCardio = { navigateToTab(3) },
                                onNavigateToWeeks = { openRoute("weeks") },
                                onNavigateToSettings = { openRoute("settings") }
                            )
                            1 -> LiftScreen(
                                modifier = Modifier.fillMaxSize(),
                                onShowSummary = { openRoute("summary") },
                                onNavigateToToday = { navigateToTab(2) },
                                onNavigateToMeals = { navigateToTab(0) },
                                onNavigateToCardio = { navigateToTab(3) },
                                onNavigateToWeeks = { openRoute("weeks") },
                                onNavigateToSettings = { openRoute("settings") },
                                viewModel = liftViewModel
                            )
                            2 -> TodayScreen(
                                modifier = Modifier.fillMaxSize(),
                                onShowSummary = { openRoute("summary") },
                                onNavigateToMeals = { navigateToTab(0) },
                                onNavigateToLift = { navigateToTab(1) },
                                onNavigateToCardio = { navigateToTab(3) },
                                onNavigateToWeeks = { openRoute("weeks") },
                                onNavigateToSettings = { openRoute("settings") }
                            )
                            else -> CardioScreen(
                                modifier = Modifier.fillMaxSize(),
                                onShowSummary = { openRoute("summary") },
                                onNavigateToToday = { navigateToTab(2) },
                                onNavigateToMeals = { navigateToTab(0) },
                                onNavigateToLift = { navigateToTab(1) },
                                onNavigateToWeeks = { openRoute("weeks") },
                                onNavigateToSettings = { openRoute("settings") }
                            )
                        }
                    }
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
                composable("goal") {
                    GoalScreen(
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
                        QuickAddAction.NewWorkout -> {
                            navigateToTab(1)
                            liftViewModel.requestNewWorkout()
                        }
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
