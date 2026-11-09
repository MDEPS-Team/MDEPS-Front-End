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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
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
import com.example.mdeps_application.pm.application.ProfileViewModel
import com.example.mdeps_application.iam.data.DataStoreAuthRepository
import com.example.mdeps_application.pm.data.DataStoreProfileRepository
import com.example.mdeps_application.iam.data.iamAuthDataStore
import com.example.mdeps_application.pm.data.profileDataStore
import com.example.mdeps_application.iam.ui.LoginScreen
import com.example.mdeps_application.iam.ui.PasswordRecoveryScreen
import com.example.mdeps_application.iam.ui.RegisterFlowScreen
import com.example.mdeps_application.iam.ui.SplashScreen
import com.example.mdeps_application.iam.application.PasswordRecoveryViewModel
import com.example.mdeps_application.iam.application.RegistrationViewModel
import com.example.mdeps_application.ui.VxMotion
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import com.example.mdeps_application.pm.ui.ProfileScreen
import com.example.mdeps_application.pto.application.PtoViewModel
import com.example.mdeps_application.pto.data.OfflineFirstProjectRepository
import com.example.mdeps_application.pto.data.local.PtoDatabase
import com.example.mdeps_application.pto.ui.HomeScreen
import com.example.mdeps_application.pto.ui.ProjectDetailScreen
import com.example.mdeps_application.pto.ui.CreateInitiativeScreen
import com.example.mdeps_application.pto.ui.ProjectListScreen
import com.example.mdeps_application.pto.ui.KanbanScreen
import com.example.mdeps_application.pto.ui.RoadmapScreen
import com.example.mdeps_application.pto.application.KanbanViewModel
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
private const val ROUTE_KANBAN = "kanban?projectId={projectId}"
private const val ROUTE_ROADMAP = "roadmap/{projectId}"
private const val NO_PROJECT = -1L
private fun kanbanRoute(projectId: Long? = null): String = "kanban?projectId=${projectId ?: NO_PROJECT}"
private fun roadmapRoute(projectId: Long): String = "roadmap/$projectId"
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

private enum class AuthRoute { Splash, Login, Register, Recovery }

@Composable
fun VantagePmoApp() {
    // No recordar sesión entre reinicios fríos: obliga pantalla de acceso (email o biometría).
    var sessionUnlocked by remember { mutableStateOf(false) }
    var authRoute by rememberSaveable { mutableStateOf(AuthRoute.Splash) }
    // Cambia en cada entrada a un flujo para que su ViewModel arranque limpio.
    var flowNonce by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val profileRepository = remember { DataStoreProfileRepository(context.profileDataStore) }
    val darkStudio by profileRepository.observeDarkStudioTheme()
        .collectAsStateWithLifecycle(initialValue = false)
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }

    MDEPS_APPLICATIONTheme(darkTheme = darkStudio) {
        if (!sessionUnlocked) {
            val authRepository = remember { DataStoreAuthRepository(context.iamAuthDataStore) }
            val iamViewModel: IamViewModel = viewModel(factory = IamViewModel.factory(authRepository))
            val iamState by iamViewModel.uiState.collectAsStateWithLifecycle()
            var enteringFromRecovery by remember { mutableStateOf(false) }
            LaunchedEffect(iamState.isAuthenticated, enteringFromRecovery) {
                if (enteringFromRecovery && iamState.isAuthenticated) {
                    enteringFromRecovery = false
                    sessionUnlocked = true
                }
            }
            AnimatedContent(
                targetState = authRoute,
                transitionSpec = {
                    (
                        fadeIn(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut)) +
                            scaleIn(tween(VxMotion.STANDARD_MS, easing = VxMotion.EaseOut), initialScale = 0.98f)
                        ) togetherWith fadeOut(tween(VxMotion.FAST_MS - 40))
                },
                label = "authRoute",
            ) { route ->
                when (route) {
                    AuthRoute.Splash -> SplashScreen(
                        versionName = versionName,
                        onFinished = { authRoute = AuthRoute.Login },
                    )
                    AuthRoute.Login -> LoginScreen(
                        viewModel = iamViewModel,
                        onLoginSuccess = { sessionUnlocked = true },
                        onCreateAccount = {
                            flowNonce++
                            authRoute = AuthRoute.Register
                        },
                        onForgotPassword = {
                            flowNonce++
                            authRoute = AuthRoute.Recovery
                        },
                    )
                    AuthRoute.Register -> {
                        val registrationViewModel: RegistrationViewModel = viewModel(
                            key = "registration_$flowNonce",
                            factory = RegistrationViewModel.factory(authRepository),
                        )
                        RegisterFlowScreen(
                            viewModel = registrationViewModel,
                            onExit = { authRoute = AuthRoute.Login },
                            onFinished = { sessionUnlocked = true },
                        )
                    }
                    AuthRoute.Recovery -> {
                        val recoveryViewModel: PasswordRecoveryViewModel = viewModel(
                            key = "recovery_$flowNonce",
                            factory = PasswordRecoveryViewModel.factory(authRepository),
                        )
                        PasswordRecoveryScreen(
                            viewModel = recoveryViewModel,
                            entering = iamState.isLoading,
                            onExit = { authRoute = AuthRoute.Login },
                            onEnterApp = { email, password ->
                                enteringFromRecovery = true
                                iamViewModel.signIn(email, password)
                            },
                        )
                    }
                }
            }
        } else {
            MainShell(
                onLogout = {
                    authRoute = AuthRoute.Login
                    sessionUnlocked = false
                },
            )
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
    val kanbanViewModel: KanbanViewModel = viewModel(factory = KanbanViewModel.factory(ptoRepository))
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
    val profileRepository = remember { DataStoreProfileRepository(context.profileDataStore) }
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            authRepository = authRepository,
            profileRepository = profileRepository,
            projectRepository = ptoRepository,
            riskRepository = riskRepository,
            milestoneRepository = milestoneRepository,
            governancePreferencesStore = governancePreferencesStore,
        ),
    )

    val onProjectFlow = destination?.route?.startsWith("project_details") == true
    val onGroFlow = destination?.route?.startsWith("gro_") == true
    val onPlanningFlow = destination?.route?.let { it.startsWith("kanban") || it.startsWith("roadmap") } == true
    val hideBottomBar = onProjectFlow || onGroFlow || onPlanningFlow
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
                    userRole = iamState.user?.role,
                    onProjectClick = openProject,
                    onOpenProjects = { nav.navigateTab(Tab.Projects.route) },
                    onOpenAnalytics = { nav.navigateTab(Tab.Analytics.route) },
                    onOpenGovernance = { nav.navigateTab(Tab.Governance.route) },
                    onCreateProject = { nav.navigate(ROUTE_CREATE) { launchSingleTop = true } },
                    onOpenProjectRisks = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroRisks(projectId)
                    },
                    onOpenProjectMilestones = { projectId ->
                        nav.navigate(projectDetailsRoute(projectId)) { launchSingleTop = true }
                        nav.navigateToGroMilestones(projectId)
                    },
                    onOpenKanban = { nav.navigate(kanbanRoute()) { launchSingleTop = true } },
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
                    onOpenKanban = { nav.navigate(kanbanRoute()) { launchSingleTop = true } },
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
                    onOpenKanban = { id -> nav.navigate(kanbanRoute(id)) { launchSingleTop = true } },
                    onOpenRoadmap = { id -> nav.navigate(roadmapRoute(id)) { launchSingleTop = true } },
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
                route = ROUTE_KANBAN,
                arguments = listOf(
                    navArgument(ARG_PROJECT_ID) {
                        type = NavType.LongType
                        defaultValue = NO_PROJECT
                    },
                ),
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong(ARG_PROJECT_ID)?.takeIf { it != NO_PROJECT }
                KanbanScreen(
                    viewModel = kanbanViewModel,
                    initialProjectId = projectId,
                    defaultAssignee = iamState.user?.name,
                    onNavigateBack = { nav.popBackStack() },
                    onOpenRoadmap = { id -> nav.navigate(roadmapRoute(id)) { launchSingleTop = true } },
                )
            }
            composable(
                route = ROUTE_ROADMAP,
                arguments = listOf(navArgument(ARG_PROJECT_ID) { type = NavType.LongType }),
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getLong(ARG_PROJECT_ID) ?: return@composable
                val groViewModel: GroViewModel = viewModel(
                    key = "gro_$projectId",
                    factory = groViewModelFactory,
                )
                LaunchedEffect(projectId) {
                    groViewModel.loadGovernanceData(projectId)
                }
                RoadmapScreen(
                    projectId = projectId,
                    ptoViewModel = ptoViewModel,
                    kanbanViewModel = kanbanViewModel,
                    groViewModel = groViewModel,
                    onNavigateBack = { nav.popBackStack() },
                    onOpenKanban = { id -> nav.navigate(kanbanRoute(id)) { launchSingleTop = true } },
                    onOpenRisks = { id -> nav.navigateToGroRisks(id) },
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
