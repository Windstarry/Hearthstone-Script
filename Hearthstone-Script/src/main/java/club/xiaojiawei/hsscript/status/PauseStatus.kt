package club.xiaojiawei.hsscript.status

import club.xiaojiawei.config.EXTRA_THREAD_POOL
import club.xiaojiawei.hsscript.enums.MouseControlModeEnum
import club.xiaojiawei.hsscript.enums.WindowEnum
import club.xiaojiawei.hsscript.utils.ConfigExUtil
import club.xiaojiawei.hsscript.utils.SystemUtil
import club.xiaojiawei.hsscript.utils.WindowUtil
import club.xiaojiawei.hsscript.utils.runUI
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.value.ChangeListener

/**
 * 脚本暂停状态
 * @author 肖嘉威
 * @date 2023/7/5 15:04
 */
object PauseStatus {

    private val isPauseProperty: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(true)

    var isPause: Boolean
        get() {
            return isPauseProperty.get()
        }
        set(value) {
            if (!value && ConfigExUtil.getMouseControlMode() !== MouseControlModeEnum.DRIVE) {
                SystemUtil.messageInfoOk("当前版本仅支持[${MouseControlModeEnum.DRIVE}]鼠标控制模式，请于高级设置中切换")
                runUI {
                    WindowUtil.showStage(WindowEnum.SETTINGS)
                }
                return
            }
            isPauseProperty.set(value)
        }

    fun asyncSetPause(isPaused: Boolean) {
        EXTRA_THREAD_POOL.submit {
            this.isPause = isPaused
        }
    }

    fun addListener(listener: ChangeListener<Boolean>) {
        isPauseProperty.addListener(listener)
    }

    fun removeListener(listener: ChangeListener<Boolean>) {
        isPauseProperty.removeListener(listener)
    }

}
