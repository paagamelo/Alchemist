package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Implementation of a generic [SteeringActionWithTarget] in a vector space.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param target
 *          the strategy used to compute the next target.
 * @param speed
 *          the speed selection strategy.
 * @param routing
 *          the routing strategy.
 */
open class SteeringActionWithTargetImpl<T, P> @JvmOverloads constructor(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    private val target: TargetSelectionStrategy<P>,
    private val speed: SpeedSelectionStrategy<P> = SpeedSelectionStrategy { pedestrian.speed() / reaction.rate },
    private val routing: RoutingStrategy<P> = RoutingStrategy { p1, p2 -> PolygonalChain(p1, p2) }
) : AbstractConfigurableMoveNode<T, P>(
    env,
    pedestrian,
    routing,
    target,
    speed
), SteeringActionWithTarget<T, P> where P : Position<P>, P : Vector<P> {

    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        SteeringActionWithTargetImpl(env, r, n as Pedestrian<T>, target, speed, routing)

    /**
     * Next relative position.
     */
    override fun interpolatePositions(current: P, target: P, maxWalk: Double): P =
        (target - current).coerceAtMost(maxWalk)

    override fun nextPosition(): P = nextPosition

    override fun target(): P = target.target
}
