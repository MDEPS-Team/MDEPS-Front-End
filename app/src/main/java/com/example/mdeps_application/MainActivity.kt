package com.example.mdeps_application

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.gro.data.OfflineFirstMilestoneRepository
import com.example.mdeps_application.gro.data.OfflineFirstRiskRepository
import com.example.mdeps_application.gro.data.local.GroDatabase
import com.example.mdeps_application.gro.ui.MilestoneTimelineScreen
import com.example.mdeps_application.gro.ui.RiskMatrixScreen
import com.example.mdeps_application.iam.application.IamViewModel
import com.example.mdeps_application.iam.data.DataStoreAuthRepository
import com.example.mdeps_application.iam.data.iamAuthDataStore
import com.example.mdeps_application.iam.ui.LoginScreen
import com.example.mdeps_application.iam.ui.ProfileScreen
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.data.OfflineFirstProjectRepository
import com.example.mdeps_application.pto.data.local.PtoDatabase
import com.example.mdeps_application.pto.ui.HomeScreen
import com.example.mdeps_application.pto.ui.ProjectDetailScreen
import com.example.mdeps_application.pto.ui.ProjectListScreen
import com.example.mdeps_application.ui.AnalyticsScreen
import com.example.mdeps_application.ui.CreateProjectScreen
import com.example.mdeps_application.ui.GovernanceScreen
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MDEPS_APPLICATIONTheme(darkTheme = false) {
                VantagePmoApp()
            }
        }
    }
}

// ───────────────────────── NAVEGACIÓN ─────────────────────────
private const val ROUTE_CREATE = "create_project"
private const val ROUTE_PROJECT_DETAILS = "project_details/{projectId}"
private const val ROUTE_GRO_RISKS = "gro_risks/{projectId}"
private const val ROUTE_GRO_MILESTONES = "gro_milestones/{projectId}"
private const val ARG_PROJECT_ID = "projectId"
private fun projectDetailsRoute(projectId: Long): String = "project_details/$projectId"
private fun groRisksRoute(projectId: Long): String = "gro_risks/$projectId"
private fun groMilestonesRoute(projectId: Long): String = "gro_milestones/$projectId"

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Default.Home),
    Projects("projects", "Projects", Icons.AutoMirrored.Filled.List),
    Analytics("analytics", "Analytics", Icons.Default.Info),
    Governance("governance", "Governance", Icons.Default.CheckCircle),
    Profile("profile", "Profile", Icons.Default.Person),
}

@Composable
fun VantagePmoApp() {
    var loggedIn by rememberSaveable { mutableStateOf(false) }

    if (!loggedIn) {
        val context = LocalContext.current
        val authRepository = remember { DataStoreAuthRepository(context.iamAuthDataStore) }
        val iamViewModel: IamViewModel = viewModel(factory = IamViewModel.factory(authRepository))
        LoginScreen(
            viewModel = iamViewModel,
            onLoginSuccess = { loggedIn = true },
        )
    } else {
        MainShell()
    }
}

@Composable
private fun MainShell() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val context = LocalContext.current

    val ptoRepository = remember {
        OfflineFirstProjectRepository(PtoDatabase.create(context.applicationContext).projectDao())
    }
    val ptoViewModel: PtoViewModel = viewModel(factory = PtoViewModel.factory(ptoRepository))

    val groDatabase = remember {
        GroDatabase.create(context.applicationContext)
    }
    val groViewModelFactory = remember {
        GroViewModel.factory(
            riskRepository = OfflineFirstRiskRepository(groDatabase.riskDao()),
            milestoneRepository = OfflineFirstMilestoneRepository(groDatabase.milestoneDao()),
        )
    }

    val onProjectFlow = destination?.route?.startsWith("project_details") == true
    val onGroFlow = destination?.route?.startsWith("gro_") == true
    val hideBottomBar = onProjectFlow || onGroFlow

    Scaffold(
        containerColor = Vx.Bg,
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    Tab.entries.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.route } == true ||
                            (tab == Tab.Projects && (destination?.route == ROUTE_CREATE || onProjectFlow)) ||
                            (tab == Tab.Governance && onGroFlow)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { nav.navigateTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Vx.Blue,
                                selectedTextColor = Vx.Blue,
                                indicatorColor = Vx.BlueSoft,
                                unselectedIconColor = Vx.Muted,
                                unselectedTextColor = Vx.Muted,
                            ),
                        )
                    }
                }
            }
        },
    ) { inner ->
        val goProfile = { nav.navigateTab(Tab.Profile.route) }
        NavHost(nav, startDestination = Tab.Home.route, modifier = Modifier.padding(inner)) {
            composable(Tab.Home.route) {
                HomeScreen(
                    viewModel = ptoViewModel,
                    onProjectClick = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) {
                            launchSingleTop = true
                        }
                    },
                    onProfile = goProfile,
                )
            }
            composable(Tab.Projects.route) {
                ProjectListScreen(
                    viewModel = ptoViewModel,
                    onProjectClick = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId))
                    },
                )
            }
            composable(
                route = ROUTE_PROJECT_DETAILS,
                arguments = listOf(
                    navArgument(ARG_PROJECT_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong(ARG_PROJECT_ID) ?: return@composable
                LaunchedEffect(projectId) {
                    ptoViewModel.loadProjectDetails(projectId)
                }
                ProjectDetailScreen(
                    viewModel = ptoViewModel,
                    onNavigateBack = { nav.popBackStack() },
                    onNavigateToRisks = { projectId ->
                        nav.navigateToGroRisks(projectId)
                    },
                    onNavigateToMilestones = { projectId ->
                        nav.navigateToGroMilestones(projectId)
                    },
                )
            }
            composable(Tab.Analytics.route) { AnalyticsScreen(goProfile) }
            composable(Tab.Governance.route) {
                GovernanceScreen(onProfile = goProfile)
            }
            composable(Tab.Profile.route) { ProfileScreen() }
            composable(ROUTE_CREATE) { CreateProjectScreen(onClose = { nav.popBackStack() }) }
            composable(
                route = ROUTE_GRO_RISKS,
                arguments = listOf(
                    navArgument(ARG_PROJECT_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong(ARG_PROJECT_ID) ?: return@composable
                val groViewModel: GroViewModel = viewModel(
                    key = "gro_$projectId",
                    factory = groViewModelFactory,
                )
                LaunchedEffect(projectId) {
                    groViewModel.loadGovernanceData(projectId)
                }
                RiskMatrixScreen(
                    viewModel = groViewModel,
                    projectId = projectId,
                    onNavigateBack = { nav.popBackStack() },
                )
            }
            composable(
                route = ROUTE_GRO_MILESTONES,
                arguments = listOf(
                    navArgument(ARG_PROJECT_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong(ARG_PROJECT_ID) ?: return@composable
                val groViewModel: GroViewModel = viewModel(
                    key = "gro_$projectId",
                    factory = groViewModelFactory,
                )
                LaunchedEffect(projectId) {
                    groViewModel.loadGovernanceData(projectId)
                }
                MilestoneTimelineScreen(
                    viewModel = groViewModel,
                    projectId = projectId,
                    onNavigateBack = { nav.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateToGroRisks(projectId: Long) {
    navigate(groRisksRoute(projectId)) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToGroMilestones(projectId: Long) {
    navigate(groMilestonesRoute(projectId)) {
        launchSingleTop = true
    }
}
