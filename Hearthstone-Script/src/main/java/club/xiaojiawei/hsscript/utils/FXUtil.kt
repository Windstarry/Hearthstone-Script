package club.xiaojiawei.hsscript.utils

import club.xiaojiawei.controls.ico.CopyIco
import club.xiaojiawei.controls.ico.OKIco
import club.xiaojiawei.util.isFalse
import club.xiaojiawei.util.isTrue
import javafx.animation.PauseTransition
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseEvent
import javafx.util.Duration

/**
 * @author 肖嘉威
 * @date 2024/9/28 14:22
 */

inline fun runUI(crossinline block: () -> Unit) {
    Platform.isFxApplicationThread().isTrue {
        block()
    }.isFalse {
        Platform.runLater {
            block()
        }
    }
}

object FXUtil {
    fun buildCopyNode(clickHandler: Runnable?, tooltip: String? = null, opacity: Double = 0.9): Node {
        val graphicLabel = Label()
        val copyIco = CopyIco()
        val icoColor = "#e4e4e4"
        copyIco.color = icoColor
        graphicLabel.graphic = copyIco
        tooltip?.let {
            graphicLabel.tooltip = Tooltip(it)
        }
        graphicLabel.style = """
                    -fx-cursor: hand;
                    -fx-alignment: CENTER;
                    -fx-pref-width: 22;
                    -fx-pref-height: 22;
                    -fx-background-radius: 3;
                    -fx-background-color: rgba(128,128,128,${opacity});
                    -fx-font-size: 10;
                    """.trimIndent()
        graphicLabel.onMouseClicked = EventHandler { actionEvent: MouseEvent? ->
            clickHandler?.run()
            val okIco = OKIco()
            okIco.color = icoColor
            graphicLabel.graphic = okIco
            val pauseTransition = PauseTransition(Duration.millis(1000.0))
            pauseTransition.onFinished = EventHandler { actionEvent1: ActionEvent? ->
                graphicLabel.graphic = copyIco
            }
            pauseTransition.play()
        }
        return graphicLabel
    }
}

