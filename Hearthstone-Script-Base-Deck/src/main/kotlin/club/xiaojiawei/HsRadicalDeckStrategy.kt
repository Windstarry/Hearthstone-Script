package club.xiaojiawei

import club.xiaojiawei.bean.Card
import club.xiaojiawei.bean.isValid
import club.xiaojiawei.config.log
import club.xiaojiawei.enums.CardTypeEnum
import club.xiaojiawei.enums.RunModeEnum
import club.xiaojiawei.status.War
import club.xiaojiawei.util.DeckStrategyUtil
import club.xiaojiawei.util.isFalse
import club.xiaojiawei.util.isTrue

/**
 * @author 肖嘉威
 * @date 2024/10/17 17:58
 */
class HsRadicalDeckStrategy : DeckStrategy() {

    private val commonDeckStrategy = HsCommonDeckStrategy()

    override fun name(): String {
        return "激进策略"
    }

    override fun getRunMode(): Array<RunModeEnum> {
        return arrayOf(RunModeEnum.CASUAL, RunModeEnum.STANDARD, RunModeEnum.WILD, RunModeEnum.PRACTICE)
    }

    override fun deckCode(): String {
        return ""
    }

    override fun id(): String {
        return "e71234fa-radical-deck-97e9-1f4e126cd33b"
    }

    override fun executeChangeCard(cards: HashSet<Card>) {
        commonDeckStrategy.executeChangeCard(cards)
    }

    override fun executeOutCard() {
        if (War.me.isValid()){
            val me = War.me
            var plays = me.playArea.cards.toList()
//            使用地标
            plays.forEach {card->
                if (card.cardType === CardTypeEnum.LOCATION && !card.isLocationActionCooldown){
                    card.action.lClick()
                }
            }
            var hands = me.handArea.cards.toList()
            val (_, resultCards) = DeckStrategyUtil.calcPowerOrder(hands, me.usableResource)
            if (resultCards.isNotEmpty()) {
                log.info { "待出牌：$resultCards" }
                for (card in resultCards) {
                    if (me.usableResource >= card.cost){
                        if (card.cardType === CardTypeEnum.SPELL){
                            me.playArea.cards.find { card-> card.canBeTargetedByMe() }?.let {
                                card.action.power(it)
                            }?:let {
                                card.action.power()
                            }
                        }else{
                            if (me.playArea.isFull) break
                            card.isBattlecry.isTrue {
                                me.playArea.cards.find { card-> card.canAttack() }?.let {
                                    card.action.power(it)
                                }?:let {
                                    card.action.power()
                                }
                            }.isFalse {
                                card.action.power()
                            }
                        }
                    }
                }
            }
            plays = me.playArea.cards.toList()
//            使用地标
            plays.forEach {card->
                if (card.cardType === CardTypeEnum.LOCATION && !card.isLocationActionCooldown){
                    card.action.lClick()
                }
            }
            commonDeckStrategy.executeOutCard()
        }
    }

    override fun executeDiscoverChooseCard(vararg cards: Card): Int {
        return commonDeckStrategy.executeDiscoverChooseCard(*cards)
    }
}