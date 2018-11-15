package it.unibo.alchemist.model.implementations.timedistributions

import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

object Immediately : AbstractDistribution<Nothing>(DoubleTime.ZERO_TIME) {
    override fun initializationComplete(reaction: Reaction<Nothing>?, currentTime: Time?) = Unit

    override fun cloneWithStartTime(node: Node<Nothing>?, startTime: Time?) = this

    override fun getRate() = Double.POSITIVE_INFINITY

    override fun computeNewTime(curTime: Time?, executed: Boolean, conditions: MutableList<Condition<Nothing>>?) = curTime
}