package club.xiaojiawei.hsscript.controller.javafx.view

import club.xiaojiawei.controls.NotificationManager
import club.xiaojiawei.controls.Switch
import club.xiaojiawei.hsscript.enums.MouseControlModeEnum
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

/**
 * @author 肖嘉威
 * @date 2025/3/7 16:11
 */
open class AdvancedSettingsView {

    @FXML
    protected lateinit var preventAntiCheat: Switch

    @FXML
    protected lateinit var systemTitled: TitledPane

    @FXML
    protected lateinit var behaviorTitled: TitledPane

    @FXML
    protected lateinit var versionTitled: TitledPane

    @FXML
    protected lateinit var titledRootPane: VBox

    @FXML
    protected lateinit var scrollPane: ScrollPane

    @FXML
    protected lateinit var versionPane: Group

    @FXML
    protected lateinit var behaviorPane: Group

    @FXML
    protected lateinit var systemPane: Group

    @FXML
    protected lateinit var systemNavigation: ToggleButton

    @FXML
    protected lateinit var behaviorNavigation: ToggleButton

    @FXML
    protected lateinit var versionNavigation: ToggleButton

    @FXML
    protected lateinit var navigationBarToggle: ToggleGroup

    @FXML
    protected lateinit var mouseControlModeComboBox: ComboBox<MouseControlModeEnum>

    @FXML
    protected lateinit var githubUpdateSource: RadioButton

    @FXML
    protected lateinit var giteeUpdateSource: RadioButton

    @FXML
    protected lateinit var updateSourceToggle: ToggleGroup

    @FXML
    protected lateinit var pauseHotKey: TextField

    @FXML
    protected lateinit var exitHotKey: TextField

    @FXML
    protected lateinit var notificationManager: NotificationManager<Any>

    @FXML
    protected lateinit var updateDev: Switch

    @FXML
    protected lateinit var autoUpdate: Switch

    @FXML
    protected lateinit var runningMinimize: Switch

    @FXML
    protected lateinit var topGameWindow: Switch

    @FXML
    protected lateinit var sendNotice: Switch

    @FXML
    protected lateinit var useProxy: Switch

    @FXML
    protected lateinit var rootPane: StackPane

    @FXML
    protected lateinit var autoOffScreen: Switch

    @FXML
    protected lateinit var autoWake: Switch

    @FXML
    protected lateinit var autoSleep: Switch

}