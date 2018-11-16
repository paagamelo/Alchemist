package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution

open class ReactionLike<T>
/**
 * Builds a new [ReactionLike].
 *
 * @param n  the node this reaction belongs to
 * @param pd
 */
    (n: Node<T>, pd: TimeDistribution) : AbstractReactionWIthTimeDistribution<T>(n, pd) {

    private var nextOccurrence: Time? = null

    override fun cloneOnNewNode(n: Node<T>, currentTime: Time): Reaction<T> {
        return makeClone { ReactionLike(n, timeDistribution.clone(currentTime)) }
    }

    override fun initializationComplete(t: Time) = update(t, true)

    override fun getRate(): Double {
        return timeDistribution.rate
    }

    override final fun getPutativeExecutionTime(): Time {
        return nextOccurrence ?: throw IllegalStateException("The reaction has not been initialized yet")
    }

    override final fun update(currentTime: Time, executed: Boolean) {
        val propensities = propensityVector()
        nextOccurrence = if (propensities.contains(0.0)) {
            DoubleTime.INFINITE_TIME
        } else if (propensities.contains(Double.POSITIVE_INFINITY)) {
            currentTime
        } else if (propensities.any { it < 0 }) {
            throw IllegalStateException("Negative propensities are not meaningful")
        } else {
            generateTime(propensities, currentTime, executed);
        }
    }

    protected fun propensityVector(): DoubleArray = conditions.map{ it.getPropensityContribution() }.toDoubleArray()

    protected open fun generateTime(propensityVector: DoubleArray, currentTime: Time, executed: Boolean) =
        timeDistribution.computeNextOccurrence(currentTime)
}
