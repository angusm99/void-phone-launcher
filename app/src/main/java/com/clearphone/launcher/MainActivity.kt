package com.clearphone.launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// ---------------------------------------------------------------------------
// Colour theme definitions — each theme is a set of accent colours
// ---------------------------------------------------------------------------
data class AccentTheme(
    val name: String,
    val primary: Color,        // Main accent
    val bright: Color,         // Brighter variant for titles
    val light: Color,          // Lightest — for glow/emphasis
    val dim: Color,            // Muted — for secondary text, labels
    val faint: Color,          // Very subtle — borders, dividers
    val dot: Color             // Colour shown in the picker dot
)

private val AppVersionName = BuildConfig.VERSION_NAME
private val AppBuildNumber = BuildConfig.VERSION_CODE.toString().padStart(3, '0')

private val ThemeBlue = AccentTheme(
    name = "Blue",
    primary = Color(0xFF2563EB),
    bright = Color(0xFF3B82F6),
    light = Color(0xFF60A5FA),
    dim = Color(0xFF1E40AF),
    faint = Color(0xFF1E3A5F),
    dot = Color(0xFF3B82F6)
)

private val ThemeGreen = AccentTheme(
    name = "Green",
    primary = Color(0xFF16A34A),
    bright = Color(0xFF22C55E),
    light = Color(0xFF4ADE80),
    dim = Color(0xFF166534),
    faint = Color(0xFF14532D),
    dot = Color(0xFF22C55E)
)

private val ThemePink = AccentTheme(
    name = "Pink",
    primary = Color(0xFFDB2777),
    bright = Color(0xFFEC4899),
    light = Color(0xFFF472B6),
    dim = Color(0xFF9D174D),
    faint = Color(0xFF831843),
    dot = Color(0xFFEC4899)
)

private val ThemeAmber = AccentTheme(
    name = "Amber",
    primary = Color(0xFFD97706),
    bright = Color(0xFFF59E0B),
    light = Color(0xFFFBBF24),
    dim = Color(0xFF92400E),
    faint = Color(0xFF78350F),
    dot = Color(0xFFF59E0B)
)

private fun themeFor(color: ThemeColor): AccentTheme = when (color) {
    ThemeColor.BLUE -> ThemeBlue
    ThemeColor.GREEN -> ThemeGreen
    ThemeColor.PINK -> ThemePink
    ThemeColor.AMBER -> ThemeAmber
}

// Near-black background — shared across all themes
private val NearBlack = Color(0xFF020508)   // slightly deeper than before
private val CardDark = Color(0xFF0E1520)
private val TextHint = Color(0xFF475569)

// ---------------------------------------------------------------------------
// Data
// ---------------------------------------------------------------------------
data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val component: String = ""   // "ComponentInfo{pkg/activity}" — for icon-pack matching
)

enum class Screen { Home, AllApps, Help }

// Helper: circle size in dp from IconSize enum
private fun iconSizeDp(size: IconSize): Dp = when (size) {
    IconSize.SMALL  -> 72.dp
    IconSize.MEDIUM -> 104.dp
    IconSize.LARGE  -> 148.dp
}

// Helper: column count from GridColumns enum
private fun gridColumnCount(cols: GridColumns): Int = when (cols) {
    GridColumns.ONE   -> 1
    GridColumns.TWO   -> 2
    GridColumns.THREE -> 3
}

// ---------------------------------------------------------------------------
// Activity
// ---------------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    private val favourites = mutableStateListOf<AppItem>()
    private lateinit var store: FavouritesStore
    private var installedApps: List<AppItem> = emptyList()
    private var showLabels   = mutableStateOf(true)
    private var iconShape    = mutableStateOf(IconShape.CIRCLE)
    private var themeColor   = mutableStateOf(ThemeColor.BLUE)
    private var terminalMode = mutableStateOf(false)
    private var showIcons    = mutableStateOf(false)
    private var iconSize     = mutableStateOf(IconSize.MEDIUM)
    private var gridColumns  = mutableStateOf(GridColumns.TWO)
    private var terminalHeaderName = mutableStateOf("void")
    private lateinit var iconPackManager: IconPackManager
    private var iconPackPackage = mutableStateOf("")              // "" = stock icons
    private var loadedIconPack  = mutableStateOf<IconPack?>(null)
    private var availableIconPacks: List<IconPackInfo> = emptyList()

    private val defaultPackages = listOf(
        "com.google.android.dialer",
        "com.google.android.apps.messaging",
        "com.whatsapp",
        "com.android.camera2",
        "com.google.android.apps.photos",
        "com.android.settings"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        store = FavouritesStore(this)
        iconPackManager = IconPackManager(this)
        installedApps = getInstalledApps()
        availableIconPacks = iconPackManager.getAvailableIconPacks()
        runBlocking {
            showLabels.value   = store.loadShowLabels()
            iconShape.value    = store.loadIconShape()
            themeColor.value   = store.loadThemeColor()
            terminalMode.value = store.loadTerminalMode()
            showIcons.value    = store.loadShowIcons()
            iconSize.value     = store.loadIconSize()
            gridColumns.value  = store.loadGridColumns()
            terminalHeaderName.value = store.loadTerminalHeader()
            iconPackPackage.value = store.loadIconPack()
            if (store.hasSavedFavourites()) {
                val savedPackages = store.loadFavourites()
                val resolved = savedPackages.mapNotNull { pkg ->
                    installedApps.find { it.packageName == pkg }
                }
                favourites.addAll(resolved)
            } else {
                val resolved = defaultPackages.mapNotNull { pkg ->
                    installedApps.find { it.packageName == pkg }
                }
                favourites.addAll(resolved)
                store.saveFavourites(favourites.map { it.packageName })
            }
        }

        // Load the saved icon pack off the main thread (parsing appfilter can be slow)
        if (iconPackPackage.value.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                loadedIconPack.value = iconPackManager.load(iconPackPackage.value)
            }
        }

        setContent {
            val accent = themeFor(themeColor.value)
            val colorScheme = darkColorScheme(
                primary = accent.primary,
                onPrimary = Color.White,
                background = NearBlack,
                onBackground = accent.bright,
                surface = CardDark,
                onSurface = accent.bright,
                surfaceVariant = CardDark,
                onSurfaceVariant = accent.dim,
                outline = accent.faint
            )
            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NearBlack
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.Home) }
                    val iconFor: (AppItem) -> Drawable = { app ->
                        loadedIconPack.value?.getIcon(app) ?: app.icon
                    }
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(
                            favourites        = favourites,
                            showLabels        = showLabels.value,
                            iconShape         = iconShape.value,
                            accent            = accent,
                            terminalMode      = terminalMode.value,
                            showIcons         = showIcons.value,
                            iconSize          = iconSize.value,
                            gridColumns       = gridColumns.value,
                            terminalHeaderName = terminalHeaderName.value,
                            iconFor           = iconFor,
                            iconPacks         = availableIconPacks,
                            currentIconPack   = iconPackPackage.value,
                            onAppClick        = { app -> launchApp(app.packageName) },
                            onOpenAppsClick   = { currentScreen = Screen.AllApps },
                            onRemoveFavourite = { app -> removeFavourite(app) },
                            onToggleLabels    = { toggleLabels() },
                            onToggleShape     = { toggleShape() },
                            onCycleTheme      = { cycleTheme() },
                            onToggleTerminal  = { toggleTerminal() },
                            onToggleIcons     = { toggleIcons() },
                            onCycleIconSize   = { cycleIconSize() },
                            onCycleColumns    = { cycleColumns() },
                            onUpdateTerminalHeader = { name -> updateTerminalHeader(name) },
                            onSelectIconPack  = { pkg -> selectIconPack(pkg) },
                            onHelpClick       = { currentScreen = Screen.Help },
                            onReturnToSystemLauncher = { returnToSystemLauncher() },
                            onSetAsDefault    = { setAsDefaultLauncher() }
                        )
                        Screen.AllApps -> AllAppsScreen(
                            apps           = installedApps,
                            favourites     = favourites,
                            iconShape      = iconShape.value,
                            accent         = accent,
                            iconFor        = iconFor,
                            onAppClick     = { app -> launchApp(app.packageName) },
                            onAddFavourite = { app -> addFavourite(app) },
                            onBack         = { currentScreen = Screen.Home }
                        )
                        Screen.Help -> HelpScreen(
                            accent = accent,
                            onBack = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    private fun getInstalledApps(): List<AppItem> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager
            .queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val activity = resolveInfo.activityInfo.name
                AppItem(
                    name        = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = pkg,
                    icon        = resolveInfo.loadIcon(packageManager),
                    component   = "ComponentInfo{$pkg/$activity}"
                )
            }
            .filter { it.packageName != packageName }
            .sortedBy { it.name.lowercase() }
    }

    private fun addFavourite(app: AppItem) {
        if (favourites.none { it.packageName == app.packageName }) {
            favourites.add(app)
            persistFavourites()
        }
    }

    private fun removeFavourite(app: AppItem) {
        favourites.remove(app)
        persistFavourites()
    }

    private fun persistFavourites() {
        CoroutineScope(Dispatchers.IO).launch {
            store.saveFavourites(favourites.map { it.packageName })
        }
    }

    private fun toggleLabels() {
        showLabels.value = !showLabels.value
        CoroutineScope(Dispatchers.IO).launch { store.saveShowLabels(showLabels.value) }
    }

    private fun toggleShape() {
        iconShape.value = when (iconShape.value) {
            IconShape.CIRCLE         -> IconShape.ROUNDED_SQUARE
            IconShape.ROUNDED_SQUARE -> IconShape.DIAMOND
            IconShape.DIAMOND        -> IconShape.CIRCLE
        }
        CoroutineScope(Dispatchers.IO).launch { store.saveIconShape(iconShape.value) }
    }

    private fun cycleTheme() {
        themeColor.value = when (themeColor.value) {
            ThemeColor.BLUE  -> ThemeColor.GREEN
            ThemeColor.GREEN -> ThemeColor.PINK
            ThemeColor.PINK  -> ThemeColor.AMBER
            ThemeColor.AMBER -> ThemeColor.BLUE
        }
        CoroutineScope(Dispatchers.IO).launch { store.saveThemeColor(themeColor.value) }
    }

    private fun returnToSystemLauncher() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun toggleIcons() {
        showIcons.value = !showIcons.value
        CoroutineScope(Dispatchers.IO).launch { store.saveShowIcons(showIcons.value) }
    }

    private fun toggleTerminal() {
        terminalMode.value = !terminalMode.value
        CoroutineScope(Dispatchers.IO).launch { store.saveTerminalMode(terminalMode.value) }
    }

    private fun cycleIconSize() {
        iconSize.value = when (iconSize.value) {
            IconSize.SMALL  -> IconSize.MEDIUM
            IconSize.MEDIUM -> IconSize.LARGE
            IconSize.LARGE  -> IconSize.SMALL
        }
        CoroutineScope(Dispatchers.IO).launch { store.saveIconSize(iconSize.value) }
    }

    private fun cycleColumns() {
        gridColumns.value = when (gridColumns.value) {
            GridColumns.ONE   -> GridColumns.TWO
            GridColumns.TWO   -> GridColumns.THREE
            GridColumns.THREE -> GridColumns.ONE
        }
        CoroutineScope(Dispatchers.IO).launch { store.saveGridColumns(gridColumns.value) }
    }

    private fun updateTerminalHeader(name: String) {
        terminalHeaderName.value = name.lowercase()
        CoroutineScope(Dispatchers.IO).launch { store.saveTerminalHeader(terminalHeaderName.value) }
    }

    private fun selectIconPack(pkg: String) {
        iconPackPackage.value = pkg
        // A pack only shows when icons are visible — turn them on so the user sees the effect.
        if (pkg.isNotEmpty() && !showIcons.value) {
            showIcons.value = true
            CoroutineScope(Dispatchers.IO).launch { store.saveShowIcons(true) }
        }
        if (pkg.isEmpty()) loadedIconPack.value = null
        CoroutineScope(Dispatchers.IO).launch {
            store.saveIconPack(pkg)
            if (pkg.isNotEmpty()) loadedIconPack.value = iconPackManager.load(pkg)
        }
    }

    fun setAsDefaultLauncher() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        startActivity(intent)
    }
}

// ---------------------------------------------------------------------------
// Home screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    favourites: List<AppItem>,
    showLabels: Boolean,
    iconShape: IconShape,
    accent: AccentTheme,
    terminalMode: Boolean,
    showIcons: Boolean,
    iconSize: IconSize,
    gridColumns: GridColumns,
    terminalHeaderName: String,
    iconFor: (AppItem) -> Drawable,
    iconPacks: List<IconPackInfo>,
    currentIconPack: String,
    onAppClick: (AppItem) -> Unit,
    onOpenAppsClick: () -> Unit,
    onRemoveFavourite: (AppItem) -> Unit,
    onToggleLabels: () -> Unit,
    onToggleShape: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleTerminal: () -> Unit,
    onToggleIcons: () -> Unit,
    onCycleIconSize: () -> Unit,
    onCycleColumns: () -> Unit,
    onUpdateTerminalHeader: (String) -> Unit,
    onSelectIconPack: (String) -> Unit,
    onHelpClick: () -> Unit,
    onReturnToSystemLauncher: () -> Unit,
    onSetAsDefault: () -> Unit
) {
    val font = if (terminalMode) FontFamily.Monospace else FontFamily.Default

    val infiniteTransition = rememberInfiniteTransition(label = "terminal")

    // Static burst timer A — 13s cycle, sparse spikes like CSS steps(1,end)
    val staticA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (terminalMode) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 13000
                0f    at 0    using androidx.compose.animation.core.LinearEasing
                0f    at 800  using androidx.compose.animation.core.LinearEasing
                0.40f at 820  using androidx.compose.animation.core.LinearEasing
                0f    at 840  using androidx.compose.animation.core.LinearEasing
                0f    at 1500 using androidx.compose.animation.core.LinearEasing
                0.26f at 1520 using androidx.compose.animation.core.LinearEasing
                0f    at 1540 using androidx.compose.animation.core.LinearEasing
                0f    at 2800 using androidx.compose.animation.core.LinearEasing
                0.34f at 2820 using androidx.compose.animation.core.LinearEasing
                0f    at 2840 using androidx.compose.animation.core.LinearEasing
                0f    at 4900 using androidx.compose.animation.core.LinearEasing
                0.28f at 4920 using androidx.compose.animation.core.LinearEasing
                0f    at 4940 using androidx.compose.animation.core.LinearEasing
                0f    at 7500 using androidx.compose.animation.core.LinearEasing
                0.32f at 7520 using androidx.compose.animation.core.LinearEasing
                0f    at 7540 using androidx.compose.animation.core.LinearEasing
                0f    at 9600 using androidx.compose.animation.core.LinearEasing
                0.22f at 9620 using androidx.compose.animation.core.LinearEasing
                0f    at 9640 using androidx.compose.animation.core.LinearEasing
                0f    at 13000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "staticA"
    )

    // Flicker timer — 17s cycle, sparse brightness spikes
    val flickerVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (terminalMode) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 17000
                0f    at 0
                0f    at 1400 using androidx.compose.animation.core.LinearEasing
                0.22f at 1450 using androidx.compose.animation.core.LinearEasing
                0f    at 1500 using androidx.compose.animation.core.LinearEasing
                0f    at 5800 using androidx.compose.animation.core.LinearEasing
                0.18f at 5850 using androidx.compose.animation.core.LinearEasing
                0f    at 5900 using androidx.compose.animation.core.LinearEasing
                0f    at 9700 using androidx.compose.animation.core.LinearEasing
                0.20f at 9750 using androidx.compose.animation.core.LinearEasing
                0f    at 9800 using androidx.compose.animation.core.LinearEasing
                0f    at 17000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flickerVal"
    )

    // Distortion bar — 15s cycle, brief horizontal glitch band
    val distortVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (terminalMode) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 15000
                0f   at 0
                0f   at 7200 using androidx.compose.animation.core.LinearEasing
                0.9f at 7280 using androidx.compose.animation.core.LinearEasing
                0.4f at 7340 using androidx.compose.animation.core.LinearEasing
                0.8f at 7400 using androidx.compose.animation.core.LinearEasing
                0.3f at 7460 using androidx.compose.animation.core.LinearEasing
                0f   at 7520 using androidx.compose.animation.core.LinearEasing
                0f   at 15000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "distortVal"
    )

    // Distortion bar position
    val distortPos by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = if (terminalMode) 0.63f else 0.28f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 15000
                0.28f at 0
                0.28f at 7280 using androidx.compose.animation.core.LinearEasing
                0.63f at 7400 using androidx.compose.animation.core.LinearEasing
                0.41f at 7520 using androidx.compose.animation.core.LinearEasing
                0.41f at 15000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "distortPos"
    )

    // Seed for static pattern — slow drift so crosshatch shifts position
    val staticSeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (terminalMode) 1000f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "staticSeed"
    )

    // Scanline crackle — fires as brief sparse bursts, not a smooth sweep
    // Two independent positions that snap on/off at different times in an 11s cycle
    val scanCrackle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (terminalMode) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 11000
                0f   at 0    using androidx.compose.animation.core.LinearEasing
                0f   at 2100 using androidx.compose.animation.core.LinearEasing  // off
                0.6f at 2120 using androidx.compose.animation.core.LinearEasing  // snap on at ~19% height
                0f   at 2160 using androidx.compose.animation.core.LinearEasing  // snap off
                0f   at 2200 using androidx.compose.animation.core.LinearEasing
                0.4f at 2220 using androidx.compose.animation.core.LinearEasing  // second flicker
                0f   at 2240 using androidx.compose.animation.core.LinearEasing
                0f   at 6500 using androidx.compose.animation.core.LinearEasing
                0.5f at 6520 using androidx.compose.animation.core.LinearEasing  // snap on at ~60% height
                0f   at 6570 using androidx.compose.animation.core.LinearEasing
                0f   at 6620 using androidx.compose.animation.core.LinearEasing
                0.3f at 6640 using androidx.compose.animation.core.LinearEasing
                0f   at 6660 using androidx.compose.animation.core.LinearEasing
                0f   at 11000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scanCrackle"
    )
    // Static Y positions for the crackle bands (don't scroll — just appear/disappear)
    val scanCracklePos1 = 0.22f  // upper band, ~22% down the screen
    val scanCracklePos2 = 0.61f  // lower band, ~61% down the screen

    // Radiation warning interrupt — fires once every ~300s (5 minutes)
    val radWarning by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 300000
                0f at 0
                0f at 240000 // 4 mins
                1f at 242000 // 2s fade in
                1f at 252000 // 10s hold
                0f at 254000 // 2s fade out
                0f at 300000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "radWarning"
    )

    // Derived states for the warning UI
    val showRadWarning = terminalMode && radWarning > 0.001f
    val homeAlphaDuringWarning = if (terminalMode) (1f - radWarning).coerceIn(0f, 1f) else 1f
    val radWarningAlpha = if (terminalMode) radWarning else 0f

    // Screen dim — flicker spikes lower brightness slightly
    val crackleDim = if (terminalMode) (1f - flickerVal * 0.12f) else 1f

    // Blinking cursor
    val cursor by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    // Phosphor tint — theme-colored
    val phosphorTint = accent.primary.copy(alpha = 0.28f)

    // Nav bar bottom inset
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Outer wrapper — radiation warning sits here, outside the faded home Box
    Box(modifier = Modifier.fillMaxSize().background(NearBlack)) {

        // Home screen Box — fades out independently during radiation warning
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = crackleDim * homeAlphaDuringWarning
                }
                .drawWithContent {
                    drawContent()
                    if (terminalMode) {
                        // === BACKGROUND TEXTURE — fine horizontal scanline grid ===
                        // Draws alternating dark/slightly-lighter 1px bands across the whole screen
                        // giving the phosphor-screen "rows of pixels" look
                        var scanY = 0f
                        while (scanY < size.height) {
                            drawRect(
                                color = Color.Black.copy(alpha = 0.38f),
                                topLeft = Offset(0f, scanY),
                                size = androidx.compose.ui.geometry.Size(size.width, 1f)
                            )
                            scanY += 3f
                        }

                        // === SCANLINE CRACKLE — two narrow bands that snap on/off briefly ===
                        if (scanCrackle > 0f) {
                            val band1Y = scanCracklePos1 * size.height
                            val band2Y = scanCracklePos2 * size.height
                            val bandH = 6f   // thin, not a big blur
                            listOf(band1Y, band2Y).forEach { bY ->
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            accent.light.copy(alpha = scanCrackle * 0.18f),
                                            accent.light.copy(alpha = scanCrackle * 0.28f),
                                            accent.light.copy(alpha = scanCrackle * 0.18f),
                                            Color.Transparent
                                        ),
                                        startY = bY - bandH,
                                        endY = bY + bandH
                                    )
                                )
                            }
                        }

                        // Theme-colored phosphor overlay — deeper saturation
                        drawRect(color = phosphorTint)

                        // Noise grain — theme-tinted dots
                        val random = java.util.Random(42)
                        repeat(400) {
                            drawCircle(
                                color = accent.primary.copy(alpha = random.nextFloat() * 0.05f),
                                radius = random.nextFloat() * 1.4f,
                                center = Offset(
                                    random.nextFloat() * size.width,
                                    random.nextFloat() * size.height
                                )
                            )
                        }

                        // === STATIC BURST — crosshatch pattern, fires on spike ===
                        if (staticA > 0f) {
                            val lineSpacing = 2f
                            val lineAlpha = accent.bright.copy(alpha = staticA * 0.40f)
                            var y = (staticSeed % lineSpacing)
                            while (y < size.height) {
                                drawLine(color = lineAlpha, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 0.8f)
                                y += lineSpacing * 2f
                            }
                            var x = (staticSeed % lineSpacing) + lineSpacing
                            while (x < size.width) {
                                drawLine(color = lineAlpha, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 0.8f)
                                x += lineSpacing * 2f
                            }
                            // Sparse pepper dots
                            val dotRng = java.util.Random(staticSeed.toLong())
                            repeat(140) {
                                drawCircle(
                                    color = accent.bright.copy(alpha = dotRng.nextFloat() * staticA * 0.60f),
                                    radius = 0.8f,
                                    center = Offset(dotRng.nextFloat() * size.width, dotRng.nextFloat() * size.height)
                                )
                            }
                        }

                        // === DISTORTION BAR ===
                        if (distortVal > 0f) {
                            val barY = distortPos * size.height
                            val barH = size.height * 0.08f
                            val barAlpha = distortVal * 0.20f
                            var bx = 0f
                            while (bx < size.width) {
                                drawRect(
                                    color = accent.light.copy(alpha = barAlpha),
                                    topLeft = Offset(bx, barY),
                                    size = androidx.compose.ui.geometry.Size(3f, barH)
                                )
                                bx += 6f
                            }
                            drawRect(
                                color = accent.primary.copy(alpha = barAlpha * 0.5f),
                                topLeft = Offset(0f, barY),
                                size = androidx.compose.ui.geometry.Size(size.width, barH)
                            )
                        }

                        // Vignette — more pronounced edges
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f)
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.4f),
                                radius = size.width * 0.88f
                            )
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp)
                    .padding(bottom = 96.dp + navBarPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                var showRenameDialog by remember { mutableStateOf(false) }

                if (showRenameDialog) {
                    var newName by remember { mutableStateOf(terminalHeaderName) }
                    AlertDialog(
                        onDismissRequest = { showRenameDialog = false },
                        containerColor = CardDark,
                        titleContentColor = accent.light,
                        textContentColor = accent.dim,
                        title = { Text("rename terminal header", fontSize = 18.sp) },
                        text = {
                            androidx.compose.material3.TextField(
                                value = newName,
                                onValueChange = { newName = it.lowercase() },
                                label = { Text("header name", color = TextHint) },
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = accent.bright,
                                    unfocusedTextColor = accent.bright,
                                    cursorColor = accent.primary
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                onUpdateTerminalHeader(newName)
                                showRenameDialog = false
                            }) {
                                Text("save", color = accent.bright)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRenameDialog = false }) {
                                Text("cancel", color = TextHint)
                            }
                        }
                    )
                }

                Text(
                    text = if (terminalMode)
                        "C:\\${terminalHeaderName.uppercase()}\\> void_phone${if (cursor > 0.5f) "_" else " "}"
                    else
                        "void phone  v$AppVersionName",
                    fontSize = if (terminalMode) 20.sp else 30.sp,
                    fontWeight = if (terminalMode) FontWeight.Normal else FontWeight.Light,
                    fontFamily = font,
                    textAlign = TextAlign.Center,
                    color = accent.light,
                    letterSpacing = if (terminalMode) 1.sp else 3.sp,
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { showRenameDialog = true }
                    )
                )
                Text(
                    text = if (terminalMode)
                        "v$AppVersionName :: build_$AppBuildNumber :: sys_ready"
                    else
                        "[ build_$AppBuildNumber :: essentials_only ]",
                    fontSize = if (terminalMode) 10.sp else 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = TextHint,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Delete confirmation dialog
                var appToDelete by remember { mutableStateOf<AppItem?>(null) }
                appToDelete?.let { app ->
                    AlertDialog(
                        onDismissRequest = { appToDelete = null },
                        containerColor = CardDark,
                        titleContentColor = accent.light,
                        textContentColor = accent.dim,
                        title = { Text(app.name.lowercase(), fontSize = 18.sp, letterSpacing = 1.sp) },
                        text = { Text("remove from home screen?", fontSize = 14.sp) },
                        confirmButton = {
                            TextButton(onClick = {
                                onRemoveFavourite(app)
                                appToDelete = null
                            }) {
                                Text("remove", color = accent.bright)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { appToDelete = null }) {
                                Text("cancel", color = TextHint)
                            }
                        }
                    )
                }

                if (favourites.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (terminalMode) "> no processes loaded\n> run 'all apps' to add" else "no apps yet.\ntap \"all apps\" and hold an app to add it here.",
                            fontSize = 14.sp,
                            fontFamily = font,
                            textAlign = TextAlign.Center,
                            color = accent.dim
                        )
                    }
                } else {
                    // Centred grid — items float in the middle of the available space
                    // rather than stacking from the top
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val colCount = gridColumnCount(gridColumns)
                        val itemSpacing = when (gridColumns) {
                            GridColumns.ONE   -> 16.dp
                            GridColumns.TWO   -> 20.dp
                            GridColumns.THREE -> 10.dp
                        }

                        if (gridColumns == GridColumns.ONE) {
                            // Single column — each item centred, large circle fills the row
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(favourites, key = { it.packageName }) { app ->
                                    HomeIconTile(
                                        app          = app,
                                        showLabel    = showLabels,
                                        iconShape    = iconShape,
                                        accent       = accent,
                                        terminalMode = terminalMode,
                                        showIcons    = showIcons,
                                        iconSize     = iconSize,
                                        gridColumns  = gridColumns,
                                        terminalHeaderName = terminalHeaderName,
                                        iconFor      = iconFor,
                                        onClick      = { onAppClick(app) },
                                        onLongClick  = { appToDelete = app }
                                    )
                                }
                            }
                        } else {
                            // 2 or 3 columns — grid centred in available space
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(colCount),
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                                horizontalArrangement = Arrangement.spacedBy(
                                    if (gridColumns == GridColumns.THREE) 6.dp else 12.dp
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = if (gridColumns == GridColumns.TWO) 16.dp else 4.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                items(favourites, key = { it.packageName }) { app ->
                                    HomeIconTile(
                                        app          = app,
                                        showLabel    = showLabels,
                                        iconShape    = iconShape,
                                        accent       = accent,
                                        terminalMode = terminalMode,
                                        showIcons    = showIcons,
                                        iconSize     = iconSize,
                                        gridColumns  = gridColumns,
                                        terminalHeaderName = terminalHeaderName,
                                        iconFor      = iconFor,
                                        onClick      = { onAppClick(app) },
                                        onLongClick  = { appToDelete = app }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // All Apps pill button
                OutlinedButton(
                    onClick = onOpenAppsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(
                            listOf(accent.primary.copy(alpha = 0.4f), accent.primary.copy(alpha = 0.7f))
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accent.bright
                    )
                ) {
                    Text(
                        text = if (terminalMode) "[ all apps ]" else "All Apps",
                        fontSize = 20.sp,
                        fontFamily = font,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }

            } // end Column

            // Bottom bar — 5 controls + three-dot menu, sits above system nav bar
            var menuExpanded by remember { mutableStateOf(false) }
            var showIconPackDialog by remember { mutableStateOf(false) }

            if (showIconPackDialog) {
                AlertDialog(
                    onDismissRequest = { showIconPackDialog = false },
                    containerColor = CardDark,
                    titleContentColor = accent.light,
                    textContentColor = accent.dim,
                    title = { Text("icon pack", fontSize = 18.sp) },
                    text = {
                        Column {
                            val options = listOf(IconPackInfo("", "stock icons")) + iconPacks
                            options.forEach { pack ->
                                val selected = pack.packageName == currentIconPack
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectIconPack(pack.packageName)
                                            showIconPackDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selected) "●  " else "○  ",
                                        color = if (selected) accent.light else TextHint,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = pack.label.lowercase(),
                                        color = if (selected) accent.light else accent.bright,
                                        fontSize = 15.sp,
                                        fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showIconPackDialog = false }) {
                            Text("close", color = accent.bright)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
                    .padding(bottom = navBarPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shape toggle
                BottomBarIcon(
                    text = when (iconShape) {
                        IconShape.CIRCLE         -> "◯"
                        IconShape.ROUNDED_SQUARE -> "▢"
                        IconShape.DIAMOND        -> "◇"
                    },
                    isActive = true,
                    accent = accent,
                    terminalMode = terminalMode,
                    onClick = onToggleShape
                )
                // Icons toggle
                BottomBarIcon(
                    text = "⊞",
                    isActive = showIcons,
                    accent = accent,
                    terminalMode = terminalMode,
                    onClick = onToggleIcons
                )
                // Icon size toggle — shows current size letter
                BottomBarIcon(
                    text = when (iconSize) {
                        IconSize.SMALL  -> "S"
                        IconSize.MEDIUM -> "M"
                        IconSize.LARGE  -> "L"
                    },
                    isActive = true,
                    accent = accent,
                    terminalMode = terminalMode,
                    onClick = onCycleIconSize
                )
                // Grid columns toggle
                BottomBarIcon(
                    text = when (gridColumns) {
                        GridColumns.ONE   -> "1"
                        GridColumns.TWO   -> "2"
                        GridColumns.THREE -> "3"
                    },
                    isActive = true,
                    accent = accent,
                    terminalMode = terminalMode,
                    onClick = onCycleColumns
                )
                // Terminal mode toggle
                BottomBarIcon(
                    text = ">_",
                    isActive = terminalMode,
                    accent = accent,
                    terminalMode = terminalMode,
                    onClick = onToggleTerminal
                )
                // Theme colour dot
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.dot.copy(alpha = 0.2f))
                        .border(1.dp, accent.faint, CircleShape)
                        .clickable(onClick = onCycleTheme),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accent.dot)
                    )
                }
                // Three-dot menu
                Box {
                    BottomBarIcon(
                        text = "⋮",
                        isActive = menuExpanded,
                        accent = accent,
                        terminalMode = terminalMode,
                        onClick = { menuExpanded = true }
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(CardDark)
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(if (showLabels) "hide labels" else "show labels", color = accent.bright, fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                            onClick = { menuExpanded = false; onToggleLabels() }
                        )
                        if (iconPacks.isNotEmpty()) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("icon pack", color = accent.bright, fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                                onClick = { menuExpanded = false; showIconPackDialog = true }
                            )
                        }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("help", color = accent.bright, fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                            onClick = { menuExpanded = false; onHelpClick() }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("set as default launcher", color = accent.bright, fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                            onClick = { menuExpanded = false; onSetAsDefault() }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("return to previous launcher", color = accent.bright, fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                            onClick = { menuExpanded = false; onReturnToSystemLauncher() }
                        )
                        HorizontalDivider(color = accent.faint.copy(alpha = 0.4f))
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("close app", color = Color(0xFFEF4444), fontFamily = if (terminalMode) FontFamily.Monospace else FontFamily.Default) },
                            onClick = { menuExpanded = false; android.os.Process.killProcess(android.os.Process.myPid()) }
                        )
                    }
                }
            }
        } // end inner home Box

        // === RADIATION WARNING — outside the faded home Box, always visible ===
        if (showRadWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NearBlack.copy(alpha = (1f - homeAlphaDuringWarning).coerceIn(0f, 1f)))
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(radWarningAlpha)
                ) {
                    Text(
                        text = "☢",
                        fontSize = 64.sp,
                        color = accent.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "WARNING",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp,
                        color = accent.bright,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "[ RADIATION DETECTED ]",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        color = accent.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "C:\\${terminalHeaderName.uppercase()}\\> sys_alert :: code_red",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = accent.dim,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    } // end outer wrapper Box
}

// ---------------------------------------------------------------------------
// Discreet bottom bar icon button
// ---------------------------------------------------------------------------
@Composable
private fun BottomBarIcon(
    text: String,
    isActive: Boolean,
    accent: AccentTheme,
    terminalMode: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(NearBlack)
            .border(
                if (isActive) 2.dp else 1.dp,
                accent.faint.copy(alpha = if (isActive) 0.8f else 0.4f),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            color = if (isActive) accent.light else TextHint,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------------------------
// Home icon tile — circle/square with app name or icon
// ---------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeIconTile(
    app: AppItem,
    showLabel: Boolean,
    iconShape: IconShape,
    accent: AccentTheme,
    terminalMode: Boolean = false,
    showIcons: Boolean = false,
    iconSize: IconSize = IconSize.MEDIUM,
    gridColumns: GridColumns = GridColumns.TWO,
    terminalHeaderName: String = "void",
    iconFor: (AppItem) -> Drawable = { it.icon },
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val circleSize = iconSizeDp(iconSize)
    val borderWidth = if (terminalMode) 2.dp else 1.5.dp
    val font = if (terminalMode) FontFamily.Monospace else FontFamily.Default

    val circleShape = when (iconShape) {
        IconShape.CIRCLE         -> CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(24.dp)
        IconShape.DIAMOND        -> DiamondShape
    }

    // Brighter border and background in terminal mode
    val borderColor = accent.primary.copy(alpha = if (terminalMode) 0.95f else 0.70f)
    val bgColor = if (terminalMode) accent.primary.copy(alpha = 0.08f) else Color.Transparent

    // Label: ALWAYS prefix in terminal mode
    val label = when {
        terminalMode -> "C:\\${terminalHeaderName.uppercase()}\\> ${app.name.lowercase()}"
        else -> app.name.lowercase()
    }

    // Font size scales with circle size and name length
    val baseFontSp = when (iconSize) {
        IconSize.SMALL  -> 11
        IconSize.MEDIUM -> 14
        IconSize.LARGE  -> 18
    }
    val textFontSp = when {
        app.name.length > 12 -> (baseFontSp - 2).coerceAtLeast(9)
        app.name.length > 7  -> (baseFontSp - 1).coerceAtLeast(10)
        else                 -> baseFontSp
    }

    // In 1-column mode the tile fills the full width; circle is centred inside it
    val tileModifier = if (gridColumns == GridColumns.ONE) {
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    }

    Column(
        modifier = tileModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .aspectRatio(1f)
                .clip(circleShape)
                .background(bgColor)
                .border(borderWidth, borderColor, circleShape),
            contentAlignment = Alignment.Center
        ) {
            if (showIcons) {
                // Show actual app icon inside the circle (icon-pack drawable if a pack is active)
                val iconPadding = (circleSize.value * 0.22f).dp
                AppIconImage(
                    drawable = iconFor(app),
                    sizeDp = circleSize - iconPadding * 2,
                    shape = iconShape
                )
            } else {
                // Text only — brighter, bolder
                Text(
                    text = label,
                    fontSize = textFontSp.sp,
                    fontFamily = font,
                    fontWeight = if (terminalMode) FontWeight.Normal else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = if (gridColumns == GridColumns.ONE) 1 else 3,
                    overflow = TextOverflow.Ellipsis,
                    color = accent.light,          // upgraded from accent.bright → accent.light
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
        // Show name below when in icon mode
        if (showIcons && showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.name.lowercase(),
                fontSize = (textFontSp - 2).coerceAtLeast(10).sp,
                fontFamily = font,
                color = accent.bright,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// All Apps screen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllAppsScreen(
    apps: List<AppItem>,
    favourites: List<AppItem>,
    iconShape: IconShape,
    accent: AccentTheme,
    iconFor: (AppItem) -> Drawable = { it.icon },
    onAppClick: (AppItem) -> Unit,
    onAddFavourite: (AppItem) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlack)
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.horizontalGradient(
                    listOf(accent.primary.copy(alpha = 0.3f), accent.primary.copy(alpha = 0.5f))
                )
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = accent.bright
            )
        ) {
            Text(
                text = "← Back to Home",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "All Apps",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = accent.light,
            letterSpacing = 1.sp
        )
        Text(
            text = "Hold an app to add it to your home screen",
            fontSize = 14.sp,
            color = TextHint,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                val isFavourite = favourites.any { it.packageName == app.packageName }
                AppListRow(
                    app         = app,
                    isFavourite = isFavourite,
                    iconShape   = iconShape,
                    accent      = accent,
                    iconFor     = iconFor,
                    onClick     = { onAppClick(app) },
                    onLongClick = {
                        if (!isFavourite) {
                            onAddFavourite(app)
                            Toast.makeText(
                                context,
                                "${app.name} added to home",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "${app.name} is already on home",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                HorizontalDivider(color = accent.faint.copy(alpha = 0.3f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// App list row
// ---------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppListRow(
    app: AppItem,
    isFavourite: Boolean,
    iconShape: IconShape,
    accent: AccentTheme,
    iconFor: (AppItem) -> Drawable = { it.icon },
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconImage(
            drawable = iconFor(app),
            sizeDp   = 44.dp,
            shape    = iconShape
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text      = app.name,
            fontSize  = 20.sp,
            fontWeight = FontWeight.Normal,
            color     = accent.bright,
            modifier  = Modifier.weight(1f),
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
        if (isFavourite) {
            Text(
                text     = "★",
                fontSize = 18.sp,
                color    = accent.primary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Help screen
// ---------------------------------------------------------------------------
@Composable
fun HelpScreen(
    accent: AccentTheme,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NearBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(50),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.horizontalGradient(
                    listOf(accent.primary.copy(alpha = 0.3f), accent.primary.copy(alpha = 0.5f))
                )
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = accent.bright
            )
        ) {
            Text(
                text = "← Back to Home",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "how to use",
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = accent.light,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        HelpItem(
            icon = "S M L",
            title = "icon size",
            description = "Tap the S/M/L button in the bottom bar to cycle through Small, Medium, and Large circles.",
            accent = accent
        )
        HelpItem(
            icon = "1 2 3",
            title = "grid columns",
            description = "Tap the 1/2/3 button to change the number of columns. 1 column = one big circle per row.",
            accent = accent
        )
        HelpItem(
            icon = "◯",
            title = "icon shape",
            description = "Switch between circle and rounded square. Tap the shape icon in the bottom bar.",
            accent = accent
        )
        HelpItem(
            icon = "●",
            title = "colour theme",
            description = "Tap the coloured dot to cycle through Blue, Green, Pink, and Amber themes.",
            accent = accent
        )
        HelpItem(
            icon = ">_",
            title = "terminal mode",
            description = "Toggles the CRT terminal aesthetic — phosphor tint, scanlines, static bursts, and radiation warnings.",
            accent = accent
        )
        HelpItem(
            icon = "▦",
            title = "icon pack",
            description = "If you have an icon pack installed (like VOID UI), open the ⋮ menu and tap \"icon pack\" to apply it. Picking a pack turns on icon display automatically; choose \"stock icons\" to revert.",
            accent = accent
        )
        HelpItem(
            icon = "⌂",
            title = "switch launcher",
            description = "Returns to your phone's previous home screen temporarily.",
            accent = accent
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "home screen",
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = accent.light,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        HelpItem(
            icon = "＋",
            title = "add an app",
            description = "Tap \"all apps\" then hold any app to add it to your home screen.",
            accent = accent
        )
        HelpItem(
            icon = "✕",
            title = "remove an app",
            description = "Hold any circle on the home screen — a confirmation dialog will appear.",
            accent = accent
        )
        HelpItem(
            icon = "⊙",
            title = "set as default launcher",
            description = "Tap \"set as default launcher\" on the home screen to make void phone your permanent launcher.",
            accent = accent
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "void phone v$AppVersionName",
            fontSize = 13.sp,
            color = TextHint,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HelpItem(
    icon: String,
    title: String,
    description: String,
    accent: AccentTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.faint.copy(alpha = 0.3f))
                .border(1.dp, accent.faint.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 12.sp,
                color = accent.bright,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent.bright
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = accent.dim,
                lineHeight = 22.sp
            )
        }
    }
}
