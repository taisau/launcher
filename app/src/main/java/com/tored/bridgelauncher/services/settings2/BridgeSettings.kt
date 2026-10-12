package com.tored.bridgelauncher.services.settings2

object BridgeSettings
{
    const val DEFAULT_PROJECT_DIR = "/storage/emulated/0/sync/5 computadors/56 software/56.39 bridge-launcher/phone-sync"

    val isQSTileAdded = BridgeSetting.systemBool("isQSTileAdded")
    val isDeviceAdminEnabled = BridgeSetting.systemBool("isDeviceAdminEnabled")
    val isAccessibilityServiceEnabled = BridgeSetting.systemBool("isAccessibilityServiceEnabled")
    val isExternalStorageManager = BridgeSetting.systemBool("isExternalStorageManager")

    val currentProjDir = BridgeSetting.file("currentProjDir", DEFAULT_PROJECT_DIR)
    val lastMockExportDir = BridgeSetting.file("lastMockExportDir")

    val theme = BridgeSetting.enum(
        key = "theme",
        defaultValue = BridgeThemeOptions.System,
    )

    val allowProjectsToTurnScreenOff = BridgeSetting.bool(
        key = "allowProjectsToTurnScreenOff",
        displayName = "Allow projects to turn the screen off",
    )

    val drawSystemWallpaperBehindWebView = BridgeSetting.bool(
        key = "Draw system wallpaper behind WebView",
        defaultValue = true,
        displayName = "Draw system wallpaper behind WebView",
    )

    val statusBarAppearance = BridgeSetting.enum(
        key = "statusBarAppearance",
        defaultValue = SystemBarAppearanceOptions.Hide,
        displayName = "Status bar",
    )

    val navigationBarAppearance = BridgeSetting.enum(
        key = "navigationBarAppearance",
        defaultValue = SystemBarAppearanceOptions.Hide,
        displayName = "Navigation bar",
    )

    val drawWebViewOverscrollEffects = BridgeSetting.bool(
        key = "drawWebViewOverscrollEffects",
        defaultValue = true,
        displayName = "Draw WebView overscroll effects",
    )

    val showBridgeButton = BridgeSetting.bool(
        key = "showBridgeButton",
        defaultValue = false,
        displayName = "Show Bridge button",
    )

    val showLaunchAppsWhenBridgeButtonCollapsed = BridgeSetting.bool(
        key = "showLaunchAppsWhenBridgeButtonCollapsed",
        displayName = "Show \"Launch apps\" button when the Bridge menu is collapsed",
    )
}

val ResettableBridgeSettings = with(BridgeSettings)
{
    listOf(
        currentProjDir,
        lastMockExportDir,
        theme,
        allowProjectsToTurnScreenOff,
        drawSystemWallpaperBehindWebView,
        statusBarAppearance,
        navigationBarAppearance,
        drawWebViewOverscrollEffects,
        showBridgeButton,
        showLaunchAppsWhenBridgeButtonCollapsed,
    )
}