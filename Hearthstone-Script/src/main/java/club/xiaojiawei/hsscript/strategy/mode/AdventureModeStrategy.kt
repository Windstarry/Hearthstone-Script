package club.xiaojiawei.hsscript.strategy.mode

import club.xiaojiawei.hsscript.bean.GameRect
import club.xiaojiawei.bean.LogRunnable
import club.xiaojiawei.config.EXTRA_THREAD_POOL
import club.xiaojiawei.config.log
import club.xiaojiawei.enums.ModeEnum
import club.xiaojiawei.hsscript.listener.WorkListener
import club.xiaojiawei.hsscript.status.DeckStrategyManager
import club.xiaojiawei.hsscript.status.Mode
import club.xiaojiawei.hsscript.status.PauseStatus
import club.xiaojiawei.hsscript.strategy.AbstractModeStrategy
import club.xiaojiawei.hsscript.strategy.mode.TournamentModeStrategy.selectDeck
import club.xiaojiawei.hsscript.utils.SystemUtil
import java.util.concurrent.TimeUnit

/**
 * 冒险模式
 * @author 肖嘉威
 * @date 2022/11/25 12:41
 */
object AdventureModeStrategy : AbstractModeStrategy<Any?>() {

    val CHOOSE_RECT: GameRect = GameRect(0.2467, 0.3441, 0.2778, 0.3772)
    val PRACTICE_RECT: GameRect = GameRect(0.1655, 0.4198, -0.4079, -0.3187)
    val START_RECT: GameRect = GameRect(0.2564, 0.3452, 0.2690, 0.3728)
    val FIRST_HERO_RECT: GameRect = GameRect(0.1769, 0.4162, -0.4103, -0.3551)

    override fun wantEnter() {
        addWantEnterTask(EXTRA_THREAD_POOL.scheduleWithFixedDelay(LogRunnable {
            if (PauseStatus.isPause) {
                cancelAllWantEnterTasks()
            } else if (Mode.currMode == ModeEnum.HUB) {
                cancelAllWantEnterTasks()
                ModeEnum.GAME_MODE.modeStrategy?.wantEnter()
            } else if (Mode.currMode == ModeEnum.GAME_MODE) {
                GameModeModeStrategy.enterAdventureMode()
            } else {
                cancelAllWantEnterTasks()
            }
        }, DELAY_TIME, INTERVAL_TIME, TimeUnit.MILLISECONDS))
    }

    override fun afterEnter(t: Any?) {
        if (WorkListener.isDuringWorkDate()) {
            PRACTICE_RECT.lClick()
            SystemUtil.delayTiny()
            CHOOSE_RECT.lClick()
            SystemUtil.delayTiny()
            TournamentModeStrategy.FIRST_DECK_RECT.lClick()
            SystemUtil.delayTiny()
            START_RECT.lClick()
            SystemUtil.delayTiny()
            FIRST_HERO_RECT.lClick()
            SystemUtil.delayTiny()
            START_RECT.lClick()
        } else {
            WorkListener.stopWork()
        }
    }

}
