package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPrimitiveComponentsStructureTest {

    @Test
    fun remainingLegacyPrimitiveApisStayAvailableDuringRendererMigration() {
        val source = loadSource()

        assertTrue(source.contains("fun AppButton("))
        assertTrue(source.contains(") = Button("))
        assertTrue(source.contains("colors: ButtonColors"))
        assertTrue(source.contains("fun AppIconButton("))
        assertTrue(source.contains(") = IconButton("))
        assertTrue(source.contains("fun AppFilledIconButton("))
        assertTrue(source.contains(") = FilledIconButton("))
        assertTrue(source.contains("fun AppTextButton("))
        assertTrue(source.contains(") = TextButton("))
        assertTrue(source.contains("fun AppOutlinedTextField("))
        assertTrue(source.contains("shouldUseMiuixOutlinedTextField("))
        assertTrue(source.contains("MiuixTextField("))
        assertTrue(source.contains("OutlinedTextField("))
        assertTrue(source.contains("fun AppDropdownMenu("))
        assertTrue(source.contains("fun AppModalNavigationDrawer("))
        assertTrue(source.contains("fun AppNavigationDrawerItem("))
        assertTrue(source.contains(") = NavigationDrawerItem("))
        assertTrue(source.contains("fun AppCircularProgressIndicator("))
        assertTrue(source.contains(") = CircularProgressIndicator("))
        assertTrue(source.contains("fun AppLinearProgressIndicator("))
        assertTrue(source.contains(") = LinearProgressIndicator("))
        assertTrue(source.contains("fun AppCheckbox("))
        assertTrue(source.contains(") = Checkbox("))
        assertTrue(source.contains("fun AppRadioButton("))
        assertTrue(source.contains(") = RadioButton("))
        assertTrue(source.contains("fun AppOutlinedButton("))
        assertTrue(source.contains(") = OutlinedButton("))
        assertTrue(source.contains("fun AppCard("))
        assertTrue(source.contains("content: @Composable ColumnScope.() -> Unit"))
        assertTrue(source.contains(") = Card("))
        assertTrue(source.contains("fun AppAssistChip("))
        assertTrue(source.contains(") = AssistChip("))
        assertTrue(source.contains("fun AppFilterChip("))
        assertTrue(source.contains(") = FilterChip("))
        assertTrue(source.contains("fun AppFloatingActionButton("))
        assertTrue(source.contains(") = FloatingActionButton("))
        assertTrue(source.contains("fun AppSmallFloatingActionButton("))
        assertTrue(source.contains(") = SmallFloatingActionButton("))
        assertTrue(source.contains("fun AppTab("))
        assertTrue(source.contains(") = Tab("))
        assertTrue(source.contains("fun AppPrimaryTabRow("))
        assertTrue(source.contains(") = PrimaryTabRow("))
        assertTrue(source.contains("fun AppPrimaryScrollableTabRow("))
        assertTrue(source.contains(") = PrimaryScrollableTabRow("))
        assertTrue(source.contains("fun AppSuggestionChip("))
        assertTrue(source.contains(") = SuggestionChip("))
    }

    @Test
    fun surfaceFacadeRoutesEachThemeToItsNativeRenderer() {
        val facade = loadSource("components/AppSurface.kt")
        val material = loadSource("renderer/material3/AppMaterial3Surface.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixSurface.kt")

        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Surface("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixSurface("))
        assertTrue(facade.contains("Color.Unspecified"))
        assertTrue(facade.contains("Dp.Unspecified"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Surface"))
        assertTrue(material.contains("import androidx.compose.material3.HorizontalDivider"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.Surface"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.HorizontalDivider"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun listItemFacadeRoutesEachThemeToItsNativeRenderer() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppListItem.kt")
        val material = loadSource("renderer/material3/AppMaterial3ListItem.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixListItem.kt")

        assertFalse(primitiveSource.contains("fun AppListItem("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3ListItem("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixListItem("))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.ListItem"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.BasicComponent"))
        assertTrue(miuix.contains("startAction = leadingContent"))
        assertTrue(miuix.contains("endActions = trailingContent?.let"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun badgeFacadeRoutesEachThemeToItsNativeRenderer() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppBadge.kt")
        val material = loadSource("renderer/material3/AppMaterial3Badge.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixBadge.kt")

        assertFalse(primitiveSource.contains("fun AppBadge("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Badge("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixBadge("))
        assertTrue(facade.contains("containerColor: Color = Color.Unspecified"))
        assertTrue(facade.contains("contentColor: Color = Color.Unspecified"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Badge"))
        assertTrue(material.contains("BadgeDefaults.containerColor"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.Badge"))
        assertTrue(miuix.contains("BadgeDefaults.containerColor"))
        assertTrue(miuix.contains("BadgeDefaults.contentColor"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun sliderFacadeMapsNeutralColorsToEachNativeRenderer() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppSlider.kt")
        val material = loadSource("renderer/material3/AppMaterial3Slider.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixSlider.kt")

        assertFalse(primitiveSource.contains("fun AppSlider("))
        assertTrue(facade.contains("data class AppSliderColors("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Slider("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixSlider("))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Slider"))
        assertTrue(material.contains("SliderDefaults.colors("))
        assertTrue(material.contains("modifier.appDesktopInteractionVisuals("))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.Slider"))
        assertTrue(miuix.contains("SliderDefaults.sliderColors("))
        assertTrue(miuix.contains("modifier.appDesktopFocusableItemVisuals(enabled)"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun switchFacadeRoutesEachThemeToNativeDefaults() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppSwitch.kt")
        val material = loadSource("renderer/material3/AppMaterial3Switch.kt")
        val miuix = loadSource("renderer/miuix/AppMiuixSwitch.kt")

        assertFalse(primitiveSource.contains("fun AppSwitch("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Switch("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppMiuixSwitch("))
        assertTrue(facade.contains("showThumbIcon: Boolean = true"))
        assertFalse(facade.contains("SwitchColors"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Switch"))
        assertTrue(material.contains("SwitchDefaults.colors()"))
        assertTrue(material.contains("Icons.Filled.Check"))
        assertTrue(material.contains("Icons.Filled.Close"))
        assertTrue(miuix.contains("import top.yukonga.miuix.kmp.basic.Switch"))
        assertTrue(miuix.contains("LocalAppThemeConfig.current.hapticFeedbackEnabled"))
        assertTrue(miuix.contains("modifier.appDesktopFocusableItemVisuals("))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun textAndIconFacadesRouteEachThemeToNativeRenderers() {
        val primitiveSource = loadSource()
        val textFacade = loadSource("components/AppText.kt")
        val iconFacade = loadSource("components/AppIcon.kt")
        val themeDefaults = loadSource("AppPrimitiveThemeDefaults.kt")
        val materialText = loadSource("renderer/material3/AppMaterial3Text.kt")
        val materialIcon = loadSource("renderer/material3/AppMaterial3Icon.kt")
        val miuixText = loadSource("renderer/miuix/AppMiuixText.kt")
        val miuixIcon = loadSource("renderer/miuix/AppMiuixIcon.kt")

        assertFalse(primitiveSource.contains("fun AppText("))
        assertFalse(primitiveSource.contains("fun AppIcon("))
        assertEquals(4, textFacade.lineSequence().count { it == "fun AppText(" })
        assertEquals(4, iconFacade.lineSequence().count { it == "fun AppIcon(" })
        assertTrue(textFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Text("))
        assertTrue(textFacade.contains("AppUiStyle.MIUIX -> AppMiuixText("))
        assertTrue(iconFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Icon("))
        assertTrue(iconFacade.contains("AppUiStyle.MIUIX -> AppMiuixIcon("))
        assertTrue(textFacade.contains("globalTextTapCopy"))
        assertFalse(textFacade.contains("import androidx.compose.material3"))
        assertFalse(textFacade.contains("import top.yukonga.miuix"))
        assertFalse(iconFacade.contains("import androidx.compose.material3"))
        assertFalse(iconFacade.contains("import top.yukonga.miuix"))

        assertTrue(themeDefaults.contains("MaterialLocalTextStyle.current"))
        assertTrue(themeDefaults.contains("MiuixTheme.textStyles.main"))
        assertFalse(themeDefaults.contains(".kmp.basic."))
        assertTrue(materialText.contains("import androidx.compose.material3.Text"))
        assertTrue(materialIcon.contains("import androidx.compose.material3.Icon"))
        assertTrue(miuixText.contains("import top.yukonga.miuix.kmp.basic.Text"))
        assertTrue(miuixIcon.contains("import top.yukonga.miuix.kmp.basic.Icon"))
        assertFalse(miuixText.contains("import androidx.compose.material3"))
        assertFalse(miuixIcon.contains("import androidx.compose.material3"))
    }

    private fun loadSource(): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/components/AppPrimitiveComponents.kt"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate AppPrimitiveComponents.kt from ${File(".").absolutePath}")
    }

    private fun loadSource(relativePath: String): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/$relativePath"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
