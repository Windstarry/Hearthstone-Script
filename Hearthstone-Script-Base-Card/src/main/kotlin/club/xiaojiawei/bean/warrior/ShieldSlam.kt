package club.xiaojiawei.bean.warrior

import club.xiaojiawei.CardAction
import club.xiaojiawei.bean.PlayAction
import club.xiaojiawei.bean.Player
import club.xiaojiawei.enums.CardTypeEnum
import club.xiaojiawei.status.War

/**
 * [盾牌猛击](https://hearthstone.huijiwiki.com/wiki/Card/69641)
 * @author 肖嘉威
 * @date 2025/1/18 16:01
 */

private val cardIds = arrayOf<String>(
    "%EX1_410",
)

class ShieldSlam : CardAction.DefaultCardAction() {

    override fun generatePlayActions(war: War, player: Player): List<PlayAction> {
        val result = mutableListOf<PlayAction>()
        war.rival.playArea.cards.forEach { rivalCard ->
            if (rivalCard.cardType === CardTypeEnum.MINION && rivalCard.canBeTargetedByRivalSpells()) {
                result.add(
                    PlayAction({ newWar ->
                        findSelf(newWar)?.action?.power(rivalCard.action.findSelf(newWar))
                    }, { newWar ->
                        spendSelfCost(newWar)
                        removeSelf(newWar)?.let {
                            rivalCard.action.findSelf(newWar)?.let { rCard->
                                newWar.me.playArea.hero?.let { hero ->
                                    rCard.damage += hero.armor + newWar.me.getSpellPower()
                                }
                            }
                        }
                    })
                )
            }
        }
        return result
    }

    override fun createNewInstance(): CardAction {
        return ShieldSlam()
    }

    override fun getCardId(): Array<String> {
        return cardIds
    }
}