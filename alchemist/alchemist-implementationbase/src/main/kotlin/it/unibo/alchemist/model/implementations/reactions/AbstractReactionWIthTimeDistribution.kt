package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.TimeDistribution

abstract class AbstractReactionWIthTimeDistribution<T>(
    node: Node<T>,
    private val timeDist: TimeDistribution) : AbstractReaction<T>(node) {

    final override fun getTimeDistribution() = timeDist
}