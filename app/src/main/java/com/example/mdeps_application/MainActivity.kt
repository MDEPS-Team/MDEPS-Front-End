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
import com.example.mdeps_application.iam.application.IamViewModel
import com.example.mdeps_application.iam.data.DataStoreAuthRepository
import com.example.mdeps_application.iam.data.iamAuthDataStore
import com.example.mdeps_application.iam.ui.LoginScreen
import com.example.mdeps_application.iam.ui.ProfileScreen
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.data.OfflineFirstProjectRepository
import com.example.mdeps_application.pto.data.local.PtoDatabase
import com.example.mdeps_application.pto.ui.ProjectDetailScreen
import com.example.mdeps_application.pto.ui.ProjectListScreen
import com.example.mdeps_application.ui.AnalyticsScreen
import com.example.mdeps_application.ui.CreateProjectScreen
import com.example.mdeps_application.ui.GovernanceScreen
import com.example.mdeps_application.ui.HomeScreen
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
private const val ARG_PROJECT_ID = "projectId"

private fun projectDetailsRoute(projectId: Long): String = "project_details/$projectId"

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

    Scaffold(
        containerColor = Vx.Bg,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEach { tab ->
                    val onProjectFlow = destination?.route?.startsWith("project_details") == true
                    val selected = destination?.hierarchy?.any { it.route == tab.route } == true ||
                        (tab == Tab.Projects && (destination?.route == ROUTE_CREATE || onProjectFlow))
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
        },
    ) { inner ->
        val goProfile = { nav.navigateTab(Tab.Profile.route) }
        NavHost(nav, startDestination = Tab.Home.route, modifier = Modifier.padding(inner)) {
            composable(Tab.Home.route) { HomeScreen(goProfile) }
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
                )
            }
            composable(Tab.Analytics.route) { AnalyticsScreen(goProfile) }
            composable(Tab.Governance.route) { GovernanceScreen(goProfile) }
            composable(Tab.Profile.route) { ProfileScreen() }
            composable(ROUTE_CREATE) { CreateProjectScreen(onClose = { nav.popBackStack() }) }
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
