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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.mdeps_application.R
import androidx.appcompat.app.AppCompatActivity
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
import com.example.mdeps_application.ass.application.AnalyticsViewModel
import com.example.mdeps_application.ass.ui.AnalyticsScreen
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.gro.data.OfflineFirstMilestoneRepository
import com.example.mdeps_application.gro.data.OfflineFirstRiskRepository
import com.example.mdeps_application.gro.data.local.GroDatabase
import com.example.mdeps_application.gro.ui.MilestoneTimelineScreen
import com.example.mdeps_application.gro.ui.RiskMatrixScreen
import com.example.mdeps_application.iam.application.IamViewModel
import com.example.mdeps_application.iam.application.ProfileViewModel
import com.example.mdeps_application.iam.data.DataStoreAuthRepository
import com.example.mdeps_application.iam.data.ProfilePreferencesStore
import com.example.mdeps_application.iam.data.iamAuthDataStore
import com.example.mdeps_application.iam.data.profileDataStore
import com.example.mdeps_application.iam.ui.LoginScreen
import com.example.mdeps_application.iam.ui.ProfileScreen
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.data.OfflineFirstProjectRepository
import com.example.mdeps_application.pto.data.local.PtoDatabase
import com.example.mdeps_application.pto.ui.HomeScreen
import com.example.mdeps_application.pto.ui.ProjectDetailScreen
import com.example.mdeps_application.pto.ui.CreateInitiativeScreen
import com.example.mdeps_application.pto.ui.ProjectListScreen
import com.example.mdeps_application.app.data.OnboardingStore
import com.example.mdeps_application.app.data.onboardingDataStore
import com.example.mdeps_application.app.data.GovernancePreferencesStore
import com.example.mdeps_application.app.data.governanceDataStore
import com.example.mdeps_application.gro.application.GovernanceHubViewModel
import com.example.mdeps_application.gro.ui.GovernanceHubScreen
import com.example.mdeps_application.ui.LocalAppSnackbarHostState
import com.example.mdeps_application.ui.OnboardingDialog
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VantagePmoApp()
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

private enum class Tab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_tab_home, Icons.Default.Home),
    Projects("projects", R.string.nav_tab_projects, Icons.AutoMirrored.Filled.List),
    Analytics("analytics", R.string.nav_tab_analytics, Icons.Default.Info),
    Governance("governance", R.string.nav_tab_governance, Icons.Default.CheckCircle),
    Profile("profile", R.string.nav_tab_profile, Icons.Default.Person),
}

@Composable
fun VantagePmoApp() {
    // No recordar sesión entre reinicios fríos: obliga pantalla de acceso (email o biometría).
    var sessionUnlocked by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val profilePreferencesStore = remember { ProfilePreferencesStore(context.profileDataStore) }
    val darkStudio by profilePreferencesStore.observeDarkStudioTheme()
        .collectAsStateWithLifecycle(initialValue = false)

    MDEPS_APPLICATIONTheme(darkTheme = darkStudio) {
        if (!sessionUnlocked) {
            val authRepository = remember { DataStoreAuthRepository(context.iamAuthDataStore) }
            val iamViewModel: IamViewModel = viewModel(factory = IamViewModel.factory(authRepository))
            LoginScreen(
                viewModel = iamViewModel,
                onLoginSuccess = { sessionUnlocked = true },
            )
        } else {
            MainShell(onLogout = { sessionUnlocked = false })
        }
    }
}

@Composable
private fun MainShell(onLogout: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val context = LocalContext.current

    val authRepository = remember { DataStoreAuthRepository(context.iamAuthDataStore) }
    val iamViewModel: IamViewModel = viewModel(factory = IamViewModel.factory(authRepository))

    val ptoRepository = remember {
        OfflineFirstProjectRepository(PtoDatabase.create(context.applicationContext).projectDao())
    }
    val groDatabase = remember {
        GroDatabase.create(context.applicationContext)
    }
    val riskRepository = remember { OfflineFirstRiskRepository(groDatabase.riskDao()) }
    val milestoneRepository = remember { OfflineFirstMilestoneRepository(groDatabase.milestoneDao()) }
    val ptoViewModel: PtoViewModel = viewModel(
        factory = PtoViewModel.factory(
            projectRepository = ptoRepository,
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
        ),
    )
    val groViewModelFactory = remember {
        GroViewModel.factory(
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
            projectRepository = ptoRepository,
        )
    }
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.factory(
            projectRepository = ptoRepository,
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
        ),
    )
    val governancePreferencesStore = remember { GovernancePreferencesStore(context.governanceDataStore) }
    val governanceHubViewModel: GovernanceHubViewModel = viewModel(
        factory = GovernanceHubViewModel.factory(
            preferencesStore = governancePreferencesStore,
            projectRepository = ptoRepository,
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
        ),
    )
    val profilePreferencesStore = remember { ProfilePreferencesStore(context.profileDataStore) }
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            authRepository = authRepository,
            profilePreferencesStore = profilePreferencesStore,
            projectRepository = ptoRepository,
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
            governancePreferencesStore = governancePreferencesStore,
        ),
    )

    val onProjectFlow = destination?.route?.startsWith("project_details") == true
    val onGroFlow = destination?.route?.startsWith("gro_") == true
    val hideBottomBar = onProjectFlow || onGroFlow
    var analyticsVisitNonce by remember { mutableIntStateOf(0) }
    val isAnalyticsTab = destination?.route == Tab.Analytics.route
    LaunchedEffect(isAnalyticsTab) {
        if (isAnalyticsTab) {
            analyticsVisitNonce++
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val onboardingStore = remember { OnboardingStore(context.onboardingDataStore) }
    val onboardingCompleted by onboardingStore.hasCompletedOnboarding()
        .collectAsStateWithLifecycle(initialValue = null)
    var onboardingStep by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.route } == true ||
                            (tab == Tab.Projects && (destination?.route == ROUTE_CREATE || onProjectFlow)) ||
                            (tab == Tab.Governance && onGroFlow)
                        val tabLabel = stringResource(tab.labelRes)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { nav.navigateTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tabLabel) },
                            label = { Text(tabLabel, fontSize = 11.sp) },
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
        val iamState by iamViewModel.uiState.collectAsStateWithLifecycle()
        val analyticsState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
        val riskCountByProject = analyticsState.projectsWithHighRisks.associate {
            it.projectId to it.activeHighRiskCount
        }
        val displayName = iamState.user?.name
        val openProject: (Long) -> Unit = { projectId ->
            nav.navigate(projectDetailsRoute(projectId)) {
                launchSingleTop = true
            }
        }
        NavHost(nav, startDestination = Tab.Home.route, modifier = Modifier.padding(inner)) {
            composable(Tab.Home.route) {
                HomeScreen(
                    viewModel = ptoViewModel,
                    analyticsViewModel = analyticsViewModel,
                    userDisplayName = displayName,
                    onProjectClick = openProject,
                    onOpenProjects = { nav.navigateTab(Tab.Projects.route) },
                    onOpenAnalytics = { nav.navigateTab(Tab.Analytics.route) },
                    onOpenGovernance = { nav.navigateTab(Tab.Governance.route) },
                    onProfile = goProfile,
                )
            }
            composable(Tab.Projects.route) {
                ProjectListScreen(
                    viewModel = ptoViewModel,
                    riskCountByProject = riskCountByProject,
                    onProjectClick = openProject,
                    onCreateProject = {
                        nav.navigate(ROUTE_CREATE) { launchSingleTop = true }
                    },
                    onOpenProjectRisks = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroRisks(projectId)
                    },
                    onOpenAnalytics = { nav.navigateTab(Tab.Analytics.route) },
                )
            }
            composable(
                route = ROUTE_PROJECT_DETAILS,
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
                    ptoViewModel.loadProjectDetails(projectId)
                    groViewModel.loadGovernanceData(projectId)
                }
                ProjectDetailScreen(
                    viewModel = ptoViewModel,
                    groViewModel = groViewModel,
                    onNavigateBack = { nav.popBackStack() },
                    onNavigateToRisks = { id ->
                        nav.navigateToGroRisks(id)
                    },
                    onNavigateToMilestones = { id ->
                        nav.navigateToGroMilestones(id)
                    },
                )
            }
            composable(Tab.Analytics.route) {
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onProfile = goProfile,
                    onOpenProjectRisks = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) {
                            launchSingleTop = true
                        }
                        nav.navigateToGroRisks(projectId)
                    },
                    onOpenGovernanceForProject = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroRisks(projectId)
                    },
                    animationNonce = analyticsVisitNonce,
                )
            }
            composable(Tab.Governance.route) {
                GovernanceHubScreen(
                    viewModel = governanceHubViewModel,
                    onOpenProjectRisks = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroRisks(projectId)
                    },
                    onOpenProjectMilestones = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroMilestones(projectId)
                    },
                    onProfile = goProfile,
                )
            }
            composable(Tab.Profile.route) {
                val endSession: () -> Unit = {
                    scope.launch {
                        authRepository.clearSession()
                        iamViewModel.endActiveSession()
                        onLogout()
                    }
                }
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = endSession,
                    onOpenAuditTrail = { nav.navigateTab(Tab.Governance.route) },
                )
            }
            composable(ROUTE_CREATE) {
                CreateInitiativeScreen(
                    viewModel = ptoViewModel,
                    onClose = { nav.popBackStack() },
                    onCreated = { projectId ->
                        nav.popBackStack()
                        if (projectId != null) {
                            openProject(projectId)
                        }
                    },
                )
            }
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

        if (onboardingCompleted == false) {
            OnboardingDialog(
                step = onboardingStep,
                onNext = { onboardingStep++ },
                onFinish = {
                    scope.launch { onboardingStore.setOnboardingCompleted() }
                },
                onSkip = {
                    scope.launch { onboardingStore.setOnboardingCompleted() }
                },
            )
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
