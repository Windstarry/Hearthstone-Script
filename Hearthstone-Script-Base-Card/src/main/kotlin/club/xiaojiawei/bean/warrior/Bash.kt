package club.xiaojiawei.bean.warrior

import club.xiaojiawei.CardAction
import club.xiaojiawei.bean.PlayAction
import club.xiaojiawei.bean.Player
import club.xiaojiawei.enums.CardTypeEnum
import club.xiaojiawei.status.War

/**
 * [怒袭](https://hearthstone.huijiwiki.com/wiki/Card/2729)
 * @author 肖嘉威
 * @date 2025/1/18 17:36
 */
private val cardIds = arrayOf<String>(
    "%AT_064",
)

class Bash : CardAction.DefaultCardAction() {

    override fun generatePlayActions(war: War, player: Player): List<PlayAction> {
        val result = mutableListOf<PlayAction>()
        war.rival.playArea.cards.forEach { rivalCard ->
            if (rivalCard.cardType === CardTypeEnum.MINION && rivalCard.canBeTargetedByRivalSpells()) {
                result.add(
                    PlayAction({ newWar ->
                        findSelf(newWar)?.action?.power(rivalCard)
                    }, { newWar ->
                        removeSelf(newWar)?.let {
                            spendSelfCost(newWar)
                            rivalCard.action.findSelf(newWar)?.let { newRivalCard ->
                                newWar.me.playArea.hero?.let { hero ->
                                    hero.armor += 3
                                }
                                newRivalCard.damage += 3 + newWar.me.getSpellPower()
                            }
                        }
                    })
                )
            }
        }
        return result
    }

    override fun createNewInstance(): CardAction {
        return Bash()
    }

    override fun getCardId(): Array<String> {
        return cardIds
    }

}