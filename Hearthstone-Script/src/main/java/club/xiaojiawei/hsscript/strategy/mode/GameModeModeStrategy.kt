package club.xiaojiawei.hsscript.strategy.mode

import club.xiaojiawei.bean.LogRunnable
import club.xiaojiawei.config.EXTRA_THREAD_POOL
import club.xiaojiawei.config.log
import club.xiaojiawei.enums.ModeEnum
import club.xiaojiawei.enums.RunModeEnum
import club.xiaojiawei.hsscript.bean.GameRect
import club.xiaojiawei.hsscript.status.DeckStrategyManager
import club.xiaojiawei.hsscript.status.Mode
import club.xiaojiawei.hsscript.status.PauseStatus
import club.xiaojiawei.hsscript.strategy.AbstractModeStrategy
import club.xiaojiawei.hsscript.utils.SystemUtil
import java.util.concurrent.TimeUnit

/**
 * 其他
 * @author 肖嘉威
 * @date 2022/11/26 21:44
 */
object GameModeModeStrategy : AbstractModeStrategy<Any?>() {

    val ADVENTURE_RECT: GameRect = GameRect(-0.0725, 0.0519, -0.2398, -0.0702)
    val BACK_RECT: GameRect = GameRect(0.3975, 0.4558, 0.4058, 0.4376)
    val CHOOSE_RECT: GameRect = GameRect(0.2229, 0.3398, 0.1287, 0.2661)
    val OTHER_MODE_RECT: GameRect = GameRect(-0.0845, 0.0779, 0.0058, 0.0424)

    fun enterAdventureMode() {
//                    点击冒险模式
        ADVENTURE_RECT.lClick()
        SystemUtil.delayShort()
//                    点击选择按钮进入冒险模式
        CHOOSE_RECT.lClick()
    }

    override fun wantEnter() {
        addWantEnterTask(EXTRA_THREAD_POOL.scheduleWithFixedDelay(LogRunnable {
            if (PauseStatus.isPause) {
                cancelAllWantEnterTasks()
            } else if (Mode.currMode == ModeEnum.HUB) {
                OTHER_MODE_RECT.lClick()
            } else {
                cancelAllWantEnterTasks()
            }
        }, DELAY_TIME, INTERVAL_TIME, TimeUnit.MILLISECONDS))
    }

    override fun afterEnter(t: Any?) {
        DeckStrategyManager.currentDeckStrategy?.let {
            if (it.runModes.isEmpty()) {
                SystemUtil.notice("未配置卡组")
                log.error { "未配置卡组" }
                PauseStatus.isPause = false
                return
            }
            val modeEnum = it.runModes[0].modeEnum
//            modeEnum.modeStrategy?.wantEnter()
            when (it.runModes[0]) {
                RunModeEnum.PRACTICE -> {
                    enterAdventureMode()
                }

                RunModeEnum.WILD, RunModeEnum.STANDARD, RunModeEnum.CASUAL -> {
                    BACK_RECT.lClick()
                }

                else -> {

                }
            }
        }
    }

}
