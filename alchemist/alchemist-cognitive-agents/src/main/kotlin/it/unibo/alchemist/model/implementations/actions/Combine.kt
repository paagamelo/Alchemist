package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Combination of multiple steering actions.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param actions
 *          the list of actions to combine to determine the pedestrian movement.
 * @param steerStrategy
 *          the logic according to the steering actions are combined.
 */
class Combine<T, P, A : GeometricTransformation<P>>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where
        P : Position<P>,
        P : Vector<P> {

    @Suppress("UNCHECKED_CAST")
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Combine<T, P, A> =
        if (n is Pedestrian<*, *, *>) {
            Combine(env, r, n as Pedestrian<T, P, A>, actions, steerStrategy)
        } else {
            throw IllegalArgumentException("Node must be a Pedestrian, found ${n::class}")
        }

    override fun nextPosition(): P = steerStrategy.computeNextPosition(actions).resizedToMaxWalkIfGreater()
}
