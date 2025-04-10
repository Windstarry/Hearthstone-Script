package club.xiaojiawei.hsscript.listener

import club.xiaojiawei.bean.LRunnable
import club.xiaojiawei.config.EXTRA_THREAD_POOL
import club.xiaojiawei.config.log
import club.xiaojiawei.hsscript.bean.WorkTimeRule
import club.xiaojiawei.hsscript.bean.single.WarEx
import club.xiaojiawei.hsscript.enums.WindowEnum
import club.xiaojiawei.hsscript.status.PauseStatus
import club.xiaojiawei.hsscript.status.WorkTimeStatus
import club.xiaojiawei.hsscript.utils.WindowUtil
import club.xiaojiawei.hsscript.utils.go
import club.xiaojiawei.hsscript.utils.runUI
import club.xiaojiawei.util.isFalse
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.stage.Stage
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * 工作状态
 *
 * @author 肖嘉威
 * @date 2023/9/10 22:04
 */
object WorkListener {

    private var checkVersionTask: ScheduledFuture<*>? = null

    val launch: Unit by lazy {
        checkVersionTask = EXTRA_THREAD_POOL.scheduleWithFixedDelay(LRunnable {
            checkWork()
        }, 0, 1000 * 60, TimeUnit.MILLISECONDS)
        log.info { "工作时段监听已启动" }
    }

    var isDuringWorkDate = false

    /**
     * 是否处于工作中
     */
    private val workingProperty = SimpleBooleanProperty(false)

    var working: Boolean
        get() {
            return workingProperty.get()
        }
        set(value) {
            workingProperty.set(value)
        }

    fun addChangeListener(listener: ChangeListener<Boolean>) {
        workingProperty.addListener(listener)
    }

    fun removeChangeListener(listener: ChangeListener<Boolean>) {
        workingProperty.removeListener(listener)
    }

    fun canWork(): Boolean {
        return isDuringWorkDate
    }

    @Synchronized
    fun checkWork() {
        judge()
    }

    private var prevWorkTimeRule: WorkTimeRule? = null

    private fun judge() {
        var canWork = false
        if (!PauseStatus.isPause) {
            val readOnlyWorkTimeSetting = WorkTimeStatus.readOnlyWorkTimeSetting()
            val dayIndex = LocalDate.now().dayOfWeek.value - 1
            if (dayIndex >= readOnlyWorkTimeSetting.size) return
            val id = readOnlyWorkTimeSetting[dayIndex]
            WorkTimeStatus.readOnlyWorkTimeRuleSet().toTypedArray().find { it.id == id }?.let {
                val timeRules = it.getTimeRules().toTypedArray()
                val nowTime = LocalTime.now()
                val nowSecondOfDay = nowTime.toSecondOfDay()

                var minDiffSec: Int = Int.MAX_VALUE
                var minWorkTimeRule: WorkTimeRule? = null
                for (rule in timeRules) {
                    if (!rule.isEnable()) continue
                    val workTime = rule.getWorkTime()
                    val startTime = workTime.parseStartTime() ?: continue
                    val endTime = workTime.parseEndTime() ?: continue
                    if (nowTime >= startTime && nowTime < endTime) {
                        canWork = true
                        break
                    } else {
                        val diffSec = nowSecondOfDay - endTime.toSecondOfDay()
                        if (diffSec > 0 && diffSec < minDiffSec) {
                            minDiffSec = diffSec
                            minWorkTimeRule = rule
                        }
                    }
                }
                if (canWork) {
                    workingProperty.set(true)
                } else if (prevWorkTimeRule != minWorkTimeRule && !WarEx.inWar) {
                    prevWorkTimeRule = minWorkTimeRule
                    minWorkTimeRule?.getOperate()?.let { operates ->
                        var alert: AtomicReference<Stage?> = AtomicReference<Stage?>()
                        val countdownTime = 10
                        val future = go {
                            for (i in 0 until countdownTime) {
                                Thread.sleep(1000)
                            }
                            runUI {
                                alert.get()?.hide()
                            }
                            for (operate in operates) {
                                operate.exec().isFalse {
                                    log.error {
                                        operate.value + "执行失败"
                                    }
                                }
                            }
                        }
                        val operationName = operates.map { it.name }
                        alert.set(
                            WindowUtil.createAlert(
                                "${countdownTime}秒执行${operationName}",
                                null,
                                {
                                    future.cancel(true)
                                    runUI {
                                        alert.get()?.hide()
                                    }
                                },
                                null,
                                WindowUtil.getStage(WindowEnum.MAIN),
                                "阻止"
                            )
                        )
                    }
                }

            }
        }
        isDuringWorkDate = canWork
    }

    /**
     * 获取下一次可工作的时间
     */
    fun getSecondsUntilNextWorkPeriod(): Long {
        if (working) return -1L

        val readOnlyWorkTimeSetting = WorkTimeStatus.readOnlyWorkTimeSetting()
        val dayIndex = LocalDate.now().dayOfWeek.value - 1
        if (dayIndex >= readOnlyWorkTimeSetting.size) return -1L

        var sec = -1L
        for (i in dayIndex until readOnlyWorkTimeSetting.size) {
            val id = readOnlyWorkTimeSetting[i]
            sec = getSecondsUntilNextWorkPeriod(id, (i - dayIndex) * 3600 * 24L)
            if (sec > 0) break
        }
        if (sec == -1L) {
            for (i in 0 until dayIndex) {
                val id = readOnlyWorkTimeSetting[i]
                sec = getSecondsUntilNextWorkPeriod(id, (i + readOnlyWorkTimeSetting.size - dayIndex) * 3600 * 24L)
                if (sec > 0) break
            }
        }

        return sec
    }

    fun getSecondsUntilNextWorkPeriod(workTimeRuleSetId: String, offsetSec: Long): Long {
        WorkTimeStatus.readOnlyWorkTimeRuleSet().toTypedArray().find { it.id == workTimeRuleSetId }?.let {
            val timeRules = it.getTimeRules().toTypedArray()
            val nowTime = LocalTime.now()
            val nowSecondOfDay = nowTime.toSecondOfDay() - offsetSec

            var minDiffSec: Long = Long.MAX_VALUE
            var minWorkTimeRule: WorkTimeRule? = null
            for (rule in timeRules) {
                if (!rule.isEnable()) continue
                val workTime = rule.getWorkTime()
                val startTime = workTime.parseStartTime() ?: continue
                val diffSec: Long = startTime.toSecondOfDay() - nowSecondOfDay
                if (diffSec > 0 && diffSec < minDiffSec) {
                    minDiffSec = diffSec
                    minWorkTimeRule = rule
                }
            }
            return if (minDiffSec == Long.MAX_VALUE) -1L else minDiffSec
        }
        return -1L
    }

}