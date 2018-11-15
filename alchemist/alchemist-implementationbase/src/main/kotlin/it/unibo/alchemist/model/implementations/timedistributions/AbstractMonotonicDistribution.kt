package it.unibo.alchemist.model.implementations.timedistributions

import it.unibo.alchemist.model.interfaces.Condition
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.implementations.times.DoubleTime

/**
 * This class represents a [time distribution][it.unibo.alchemist.model.interfaces.TimeDistribution] whose putative time
 * can never get
 * updated in such a way that the reaction would be scheduled before it would have been before the update.
 *
 * More specifically, this class is made to simplify the creation of
 * [time distribution][it.unibo.alchemist.model.interfaces.TimeDistribution]s which only update their putative time
 * after the engine attempted an actual execution.
 */
abstract class AbstractMonotonicDistribution<T>(start: Time) : AbstractDistribution<T>(start) {

    protected var hostReaction: Reaction<T>? = null

    /**
     * This method gets called when the reaction has been executed in order to compute the shift forward in time.
     * @return the difference between
     */
    abstract fun computeDeltaTime(): Time

    final override fun computeNewTime(curTime: Time, executed: Boolean, conditions: MutableList<Condition<T>>?) =
        if (executed){
            curTime + computeDeltaTime()
                .takeIf { it >= DoubleTime.ZERO_TIME }
                ?: throw IllegalStateException("${javaClass.simpleName} tried to go back in time")
        } else {
            curTime
            // If the next time is infinity, there is no need to schedule the reaction anymore.
        }.also { if (it.isInfinite) hostReaction?.node?.removeReaction(hostReaction) }

    final override fun initializationComplete(reaction: Reaction<T>, currentTime: Time) {
        hostReaction = reaction
        update(currentTime, true, reaction.conditions)
    }

}