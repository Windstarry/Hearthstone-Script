package club.xiaojiawei.bean

import club.xiaojiawei.CardAction
import club.xiaojiawei.bean.area.Area
import club.xiaojiawei.enums.CardTypeEnum
import club.xiaojiawei.enums.TargetEnum
import club.xiaojiawei.mapper.BaseCardMapper

/**
 * @author 肖嘉威
 * @date 2022/11/27 14:56
 */
class Card(var action: CardAction) : BaseCard(), Cloneable {


    /**
     * 模拟用
     */
    var attackCount: Int = 0

    /**
     * 卡牌所在区域：手牌区、战场区等
     */
    var area: Area = Area.UNKNOWN_AREA

    fun resetExhausted() {
        isExhausted = false
        attackCount = 0
    }

    fun minusHealth(health: Int) {
        this.health -= health
    }

    override var damage
        get() = super.damage
        set(value) {
            super.damage = value
            if (!isAlive()) {
                action.triggerDeath(area.player.war, area.player)
            }
        }

    /**
     * 是否包含cardId
     */
    fun cardContains(baseCard: BaseCard): Boolean {
        return cardContains(baseCard.cardId)
    }

    fun cardContains(cardId: String): Boolean {
        return this.cardId.contains(cardId)
    }

    /**
     * 判断卡牌是否相同，指的是cardId相同
     */
    fun cardEquals(baseCard: BaseCard): Boolean {
        return cardEquals(baseCard.cardId)
    }

    fun cardEquals(cardId: String): Boolean {
        return this.cardId == cardId
    }

    /**
     * 能被敌方法术指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByRivalSpells(): Boolean {
        return !(isElusive || isCantBeTargetedBySpells || !canBeTargetedByRival())
    }

    /**
     * 能被我方法术指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByMySpells(): Boolean {
        return !(isElusive || isCantBeTargetedBySpells || !canBeTargetedByMe())
    }

    /**
     * 能被敌方英雄技能指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByRivalHeroPowers(): Boolean {
        return canBeTargetedByRivalSpells()
    }

    /**
     * 能被我方英雄技能指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByMyHeroPowers(): Boolean {
        return canBeTargetedByMySpells()
    }

    /**
     * 能被敌方指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByRival(): Boolean {
        return !(isStealth || isImmune || isDormantAwakenConditionEnchant || isUntouchable)
    }

    /**
     * 能被我方指向
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeTargetedByMe(): Boolean {
        return (cardType === CardTypeEnum.MINION || cardType === CardTypeEnum.HERO) && !(isImmune || isDormantAwakenConditionEnchant || isUntouchable)
    }

    /**
     * 能被攻击
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO]
     */
    fun canBeAttacked(): Boolean {
        return (cardType === CardTypeEnum.MINION || cardType === CardTypeEnum.HERO) && canBeTargetedByRival()
    }

    /**
     * 能攻击
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON]
     * 对于地标和技能，参见[club.xiaojiawei.bean.Card.canPower]
     */
    fun canAttack(ignoreExhausted: Boolean = false, ignoreAtc: Boolean = false): Boolean {
        return getAttackTarget(ignoreExhausted, ignoreAtc) !== TargetEnum.NONE
    }

    /**
     * 无法攻击
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON]
     * 对于地标和技能，参见[club.xiaojiawei.bean.Card.canPower]
     */
    fun cantAttack(ignoreExhausted: Boolean = false, ignoreAtc: Boolean = false): Boolean {
        return getAttackTarget(ignoreExhausted, ignoreAtc) === TargetEnum.NONE
    }

    /**
     * 获取能攻击的目标
     * 比如刚下场的突袭随从只能解场，此时返回[club.xiaojiawei.enums.TargetEnum.MINION]
     */
    fun getAttackTarget(ignoreExhausted: Boolean = false, ignoreAtc: Boolean = false): TargetEnum {
        if (!(cardType === CardTypeEnum.MINION || cardType === CardTypeEnum.HERO || cardType === CardTypeEnum.WEAPON) && isAlive()) return TargetEnum.NONE

        if (((isExhausted && !ignoreExhausted) || isCantAttack || isFrozen || isDormantAwakenConditionEnchant || (!ignoreAtc && atc <= 0))) return TargetEnum.NONE

        if (isAttackableByRush) return TargetEnum.MINION

        return TargetEnum.HERO_MINION
    }

    /**
     * 能使用/激活
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.LOCATION],[club.xiaojiawei.enums.CardTypeEnum.HERO_POWER]
     */
    fun canPower(): Boolean {
        return (cardType === CardTypeEnum.LOCATION && !isLocationActionCooldown && isAlive()) || (cardType === CardTypeEnum.HERO_POWER && !isExhausted)
    }

    /**
     * 是否受伤
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON],[club.xiaojiawei.enums.CardTypeEnum.LOCATION]
     */
    fun isInjured(): Boolean {
        return damage > armor
    }

    /**
     * 是不是魔免
     */
    fun isImmunityMagic(): Boolean {
        return (isCantBeTargetedByHeroPowers && isCantBeTargetedBySpells) || isElusive
    }

    /**
     * 获取血量（就是你在游戏中看到的血量）
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON],[club.xiaojiawei.enums.CardTypeEnum.LOCATION]
     */
    fun blood(): Int {
        return bloodLimit() - damage
    }

    /**
     * 判断是否存活
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON],[club.xiaojiawei.enums.CardTypeEnum.LOCATION]
     */
    fun isAlive(): Boolean {
        return blood() > 0
    }

    /**
     * 判断是否死亡
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON],[club.xiaojiawei.enums.CardTypeEnum.LOCATION]
     */
    fun isDead(): Boolean {
        return blood() <= 0
    }

    @Override
    public override fun clone(): Card {
        try {
            val card = Card(this.action.createNewInstance())
            BaseCardMapper.INSTANCE.update(this, card)
            card.attackCount = attackCount
            card.action.belongCard = card
            return card
        } catch (e: CloneNotSupportedException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 获取血量上限
     * 适用的卡牌类型：[club.xiaojiawei.enums.CardTypeEnum.MINION],[club.xiaojiawei.enums.CardTypeEnum.HERO],[club.xiaojiawei.enums.CardTypeEnum.WEAPON],[club.xiaojiawei.enums.CardTypeEnum.LOCATION]
     */
    fun bloodLimit(): Int {
        return (if (cardType === CardTypeEnum.WEAPON) durability else health) + armor
    }
}
