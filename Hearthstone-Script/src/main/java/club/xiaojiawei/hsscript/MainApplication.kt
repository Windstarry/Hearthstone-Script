package club.xiaojiawei.hsscript

import club.xiaojiawei.CardAction.Companion.commonActionFactory
import club.xiaojiawei.bean.LThread
import club.xiaojiawei.config.log
import club.xiaojiawei.config.submitExtra
import club.xiaojiawei.hsscript.bean.CommonCardAction.Companion.DEFAULT
import club.xiaojiawei.hsscript.bean.Release
import club.xiaojiawei.hsscript.config.InitializerConfig
import club.xiaojiawei.hsscript.core.Core
import club.xiaojiawei.hsscript.data.*
import club.xiaojiawei.hsscript.dll.CSystemDll
import club.xiaojiawei.hsscript.enums.ConfigEnum
import club.xiaojiawei.hsscript.enums.WindowEnum
import club.xiaojiawei.hsscript.listener.*
import club.xiaojiawei.hsscript.status.PauseStatus
import club.xiaojiawei.hsscript.status.TaskManager
import club.xiaojiawei.hsscript.utils.*
import club.xiaojiawei.hsscript.utils.SystemUtil.addTray
import club.xiaojiawei.hsscript.utils.SystemUtil.shutdown
import club.xiaojiawei.hsscript.utils.WindowUtil.buildStage
import club.xiaojiawei.hsscript.utils.WindowUtil.getStage
import club.xiaojiawei.hsscript.utils.WindowUtil.showStage
import club.xiaojiawei.util.isFalse
import club.xiaojiawei.util.isTrue
import com.sun.jna.Memory
import com.sun.jna.WString
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.stage.Screen
import javafx.stage.Stage
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.awt.MenuItem
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.io.File
import java.net.URLClassLoader
import java.util.function.Consumer
import java.util.function.Supplier
import javax.swing.AbstractAction

/**
 * javaFX启动器
 * @author 肖嘉威
 * @date 2023/7/6 9:46
 */
class MainApplication : Application() {

    private var stageShowingListener: ChangeListener<Boolean?>? = null

    fun testJava() {
        try {
            val classManager = DynamicClassManager()

            // 加载外部Java文件
            val testUtilClass =
                classManager.loadJavaFile("S:\\IdeaProjects\\fs32\\src\\main\\java\\com\\fs\\TestUtil1.java")
            println("成功加载类：${testUtilClass.name}")

            // 创建实例
            val testUtil = classManager.instantiate(testUtilClass)
            println("成功创建示例： $testUtil")

            // 调用方法 (使用反射)
            val getWarMethod = testUtilClass.getMethod("getWar")
            val result = getWarMethod.invoke(testUtil)
            println("调用getWar方法结果： $result")

        } catch (e: Exception) {
            println("发生错误: ${e.message}")
            e.printStackTrace()
        }
    }

    fun testKt() {
        try {
            val manager = DynamicClassManager()

            // 动态加载外部 Kotlin 文件
            val loadedClass = manager.loadKotlinFile("S:\\IdeaProjects\\fs32\\src\\main\\java\\com\\fs\\TestUtil.kt")

            // 创建实例
            val instance = loadedClass.getDeclaredConstructor().newInstance()

            // 调用方法 sayHello()
            val method = loadedClass.getMethod("getWar")
            method.invoke(instance)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun compileAndRunExternalKtFiles(filePaths: List<String>, classpath: String) {
        val ktFiles = filePaths.map { File(it) }.filter {
            val res = it.exists()
            res.isFalse {
                println(it.absolutePath + "不存在")
            }
            res
        }
        if (ktFiles.isEmpty()) return

        val tempDir = File(System.getProperty("java.io.tmpdir"), "kotlin-temp")
        tempDir.mkdirs()

        val classDir = File(tempDir, "classes")
        classDir.mkdirs()

        val compiler = K2JVMCompiler()
        val args = mutableListOf<String>()
        args.addAll(ktFiles.map { it.absolutePath }) // 添加所有文件路径
        args.addAll(
            arrayOf(
                "-d",
                classDir.absolutePath,
                "-classpath",
                classpath
            )
        )

        val exitCode = compiler.exec(System.out, *args.toTypedArray())
        if (exitCode.code != 0) {
            println("Compilation failed")
            return
        }

        val classLoader = URLClassLoader(arrayOf(classDir.toURI().toURL()))
        val className = ktFiles.first().nameWithoutExtension.capitalize() // 使用第一个文件的类名
        try {
            val tempClass = classLoader.loadClass(className)
            val newInstance = tempClass.getDeclaredConstructor().newInstance()
            val mainMethod = tempClass.getDeclaredMethod("getWar")
            println(mainMethod.invoke(newInstance))
        } catch (e: ClassNotFoundException) {
            println("Class not found: $className")
        } catch (e: NoSuchMethodException) {
            println("Main method not found in class: $className")
        } catch (e: Exception) {
            println("Error running compiled code: ${e.message}")
        }
    }

    override fun start(stage: Stage?) {
        preInit()
        InitializerConfig.initializer.init()
        showMainPage()
//        testJava()
//        testKt()
//        compileAndRunExternalKtFiles(listOf("S:\\IdeaProjects\\fs32\\src\\main\\java\\com\\fs\\TestUtil.kt"), System.getProperty("java.class.path"))
    }

    private fun preInit() {
        commonActionFactory = Supplier { DEFAULT.createNewInstance() }
        Platform.setImplicitExit(false)
        launchService()
        CardUtil.reloadCardWeight()
        Runtime.getRuntime()
            .addShutdownHook(
                LThread(
                    {
                        CSystemDll.INSTANCE.removeSystemTray()
                        GameUtil.findGameHWND()?.let {
                            CSystemDll.INSTANCE.uninstallInjectDll(it)
                        }
                        log.info { "软件已关闭" }
                    },
                    "ShutdownHook Thread"
                )
            )
    }


    private fun showMainPage() {
        if (PROGRAM_ARGS.stream().anyMatch {
                if (it.startsWith(ARG_PAGE)) {
                    WindowEnum.fromString(it.removePrefix(ARG_PAGE).uppercase())?.let { windowEnum ->
                        showStage(windowEnum)
                        WindowUtil.hideLaunchPage()
                        return@anyMatch true
                    }
                }
                return@anyMatch false
            }) return
        val stage = buildStage(WindowEnum.MAIN)
        stageShowingListener =
            ChangeListener { _, aBoolean: Boolean?, t1: Boolean? ->
                if (t1 != null && t1) {
                    stage.showingProperty().removeListener(stageShowingListener)
                    stageShowingListener = null
                    afterShowing()
                }
            }
        stage.showingProperty().addListener(stageShowingListener)
        stage.show()
    }

    @Deprecated("")
    private fun setSystemTray() {
        val isPauseItem = MenuItem("开始")
        isPauseItem.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                PauseStatus.asyncSetPause(!PauseStatus.isPause)
            }
        })
        PauseStatus.addListener { observableValue: ObservableValue<out Boolean?>?, aBoolean: Boolean?, isPause: Boolean ->
            if (isPause) {
                isPauseItem.label = "开始"
            } else {
                isPauseItem.label = "暂停"
            }
        }

        val settingsItem = MenuItem("设置")
        settingsItem.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                showStage(WindowEnum.SETTINGS, getStage(WindowEnum.MAIN))
            }
        })

        val quitItem = MenuItem("退出")
        quitItem.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                shutdown()
            }
        })

        addTray(Consumer { e: MouseEvent? ->
//            左键点击
            if (e?.button == 1) {
                (getStage(WindowEnum.MAIN)?.isShowing ?: false).isTrue {
                    WindowUtil.hideAllStage()
                }.isFalse {
                    showStage(WindowEnum.MAIN)
                }
            }
        }, isPauseItem, settingsItem, quitItem)
    }

    private var trayItemArr: CSystemDll.TrayItem.Reference? = null

    private val trayMenu: CSystemDll.TrayMenu.Reference by lazy {
        val textMemorySize = 50L
        val iconPathMemorySize = 255L
        CSystemDll.TrayMenu.Reference().apply {
            text = Memory(textMemorySize).apply {
                setWideString(0, SCRIPT_NAME)
            }
            iconPath = WString(SystemUtil.getTrayIconFile().absolutePath)
            clickCallback = object : CSystemDll.TrayCallback {
                override fun invoke() {
                    (getStage(WindowEnum.MAIN)?.isShowing ?: false).isTrue {
                        WindowUtil.hideAllStage()
                    }.isFalse {
                        showStage(WindowEnum.MAIN)
                    }
                }
            }
            itemCount = 5
            trayItem = CSystemDll.TrayItem.Reference()
            trayItemArr = trayItem
            val trayItemArr = trayItem!!.toArray(itemCount) as Array<CSystemDll.TrayItem>
            trayItemArr[0].apply {
                id = 1000
                type = CSystemDll.MF_STRING
                text = Memory(textMemorySize).apply {
                    setWideString(0, "开始")
                }
                iconPath = Memory(iconPathMemorySize).apply {
                    setWideString(0, SystemUtil.getResouceImgFile(TRAY_START_IMG_NAME).absolutePath)
                }
                callback = object : CSystemDll.TrayCallback {
                    override fun invoke() {
                        PauseStatus.asyncSetPause(!PauseStatus.isPause)
                    }
                }
            }
            trayItemArr[1].apply {
                id = 1001
                type = CSystemDll.MF_STRING
                text = Memory(textMemorySize).apply {
                    setWideString(0, "设置")
                }
                iconPath = Memory(iconPathMemorySize).apply {
                    setWideString(0, SystemUtil.getResouceImgFile(TRAY_SETTINGS_IMG_NAME).absolutePath)
                }
                callback = object : CSystemDll.TrayCallback {
                    override fun invoke() {
                        showStage(WindowEnum.SETTINGS, getStage(WindowEnum.MAIN))
                    }
                }
            }
            trayItemArr[2].apply {
                id = 1002
                type = CSystemDll.MF_STRING
                text = Memory(textMemorySize).apply {
                    setWideString(0, "统计")
                }
                iconPath = Memory(iconPathMemorySize).apply {
                    setWideString(0, SystemUtil.getResouceImgFile(TRAY_STATISTICS_IMG_NAME).absolutePath)
                }
                callback = object : CSystemDll.TrayCallback {
                    override fun invoke() {
                        showStage(WindowEnum.STATISTICS, getStage(WindowEnum.MAIN))
                    }
                }
            }
            trayItemArr[3].apply {
                id = 1003
                type = CSystemDll.MF_SEPARATOR
            }
            trayItemArr[4].apply {
                id = 1004
                type = CSystemDll.MF_STRING
                text = Memory(textMemorySize).apply {
                    setWideString(0, "退出")
                }
                iconPath = Memory(iconPathMemorySize).apply {
                    setWideString(0, SystemUtil.getResouceImgFile(TRAY_EXIT_IMG_NAME).absolutePath)
                }
                callback = object : CSystemDll.TrayCallback {
                    override fun invoke() {
                        shutdown()
                    }
                }
            }
            PauseStatus.addListener { _, _, isPause: Boolean ->
                if (isPause) {
                    trayItemArr[0].text?.setWideString(0, "开始")
                    trayItemArr[0].iconPath?.setWideString(0, SystemUtil.getResouceImgFile(TRAY_START_IMG_NAME).absolutePath)
                } else {
                    trayItemArr[0].text?.setWideString(0, "暂停")
                    trayItemArr[0].iconPath?.setWideString(0, SystemUtil.getResouceImgFile(TRAY_PAUSE_IMG_NAME).absolutePath)
                }
            }
            CSystemDll.INSTANCE.addSystemTray(this).isFalse {
                log.warn { "系统托盘创建失败" }
            }
        }
    }

    private fun launchService() {
        TaskManager.launch
        Core.launch
        GlobalHotkeyListener.launch
        VersionListener.launch
        WorkListener.launch
        SystemSleepListener.launch
        StatisticsListener.launch
    }

    private fun checkArg() {
        val args = this.parameters.raw
        var pause: String? = ""
        for (arg in args) {
            if (arg.startsWith(ARG_PAUSE)) {
                val split: Array<String?> = arg.split("=".toRegex(), limit = 2).toTypedArray()
                if (split.size > 1) {
                    pause = split[1]
                }
            }
        }
        if ("false" == pause) {
            log.info { "接收到开始参数，开始脚本" }
            Thread.sleep(1000)
            PauseStatus.isPause = false
        } else {
            val version = ConfigUtil.getString(ConfigEnum.CURRENT_VERSION)
            if (Release.compareVersion(VersionUtil.VERSION, version) > 0) {
                runUI {
                    showStage(WindowEnum.VERSION_MSG, getStage(WindowEnum.MAIN))
                    ConfigUtil.putString(ConfigEnum.CURRENT_VERSION, VersionUtil.VERSION)
                }
            }
        }
    }

    private fun checkSystem() {
        CSystemDll.INSTANCE.isRunAsAdministrator().isFalse {
            val text = "当前进程不是以管理员启动，功能可能受限"
            log.warn { text }
            SystemUtil.notice(text)
        }

        Screen.getScreens()?.let {
            if (it.size > 1) {
                log.info { "检测到多台显示器，开始运行后${GAME_CN_NAME}窗口不要移动到其他显示器" }
            }
        }
    }

    private fun afterShowing() {
        submitExtra {
            trayMenu
            WindowUtil.hideLaunchPage()
            checkSystem()
            checkArg()
        }
    }

}