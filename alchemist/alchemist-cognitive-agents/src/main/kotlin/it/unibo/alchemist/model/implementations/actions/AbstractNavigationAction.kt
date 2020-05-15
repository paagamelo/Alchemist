/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.NavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import java.lang.IllegalStateException
import java.util.Optional

/**
 * An abstract [NavigationAction], taking care of properly moving the pedestrian in the
 * environment while delegating the decision on where to move it to a [NavigationStrategy].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is into.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the [environment].
 * @param F the type of edges of the navigation graph provided by the [environment].
 */
abstract class AbstractNavigationAction<T, P, A, N, E, M, F>(
    override val environment: EnvironmentWithGraph<*, T, P, A, M, F>,
    override val reaction: Reaction<T>,
    final override val pedestrian: OrientingPedestrian<T, P, A, N, E>
) : AbstractSteeringAction<T, P>(environment, reaction, pedestrian),
    NavigationAction<T, P, A, N, E, M, F>
    where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A> {

    /**
     * The strategy used to navigate the environment.
     */
    protected open lateinit var strategy: NavigationStrategy<T, P, A, N, E, M, F>

    /**
     * The position of the [pedestrian] in the [environment], this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override lateinit var pedestrianPosition: P

    /**
     * The room (= environment's area) the [pedestrian] is into, this is cached and updated
     * every time [update] is called so as to avoid potentially costly re-computations.
     */
    override var currentRoom: Optional<M> = Optional.empty()

    /**
     * Minimum distance to consider a target reached. Using zero (even with fuzzy equals) may lead to some
     * boundary cases in which the pedestrian remains blocked due to how the environment manage collisions
     * at present. This workaround allows to specify a minimum distance which is dependent on the pedestrian
     * shape. In the future, something better could be done.
     */
    protected val minDistance: Double = pedestrian.shape.diameter

    /**
     * @returns true if the distance to [pedestrianPosition] is smaller than or equal to [minDistance].
     */
    protected open fun P.isReached(): Boolean = distanceTo(pedestrianPosition) <= minDistance

    protected enum class State {
        START,
        NEW_ROOM,
        /**
         * Moving towards the first crossing point (see [crossDoor]).
         */
        MOVING_TO_CROSSING_POINT_1,
        /**
         * Moving towards the second crossing point (see [crossDoor]).
         */
        MOVING_TO_CROSSING_POINT_2,
        /**
         * When the second crossing point [isReached] (see [crossDoor]), the pedestrian may still be outside
         * any room. In such case it moves towards [expectedNewRoom] centroid until he/she enters a room.
         */
        CROSSING_DOOR,
        /**
         * Moving to the final destination, which is inside [currentRoom] (this means it can be directly
         * approached as no obstacle is placed in between).
         */
        MOVING_TO_FINAL,
        ARRIVED
    }
    protected var state: State = State.START
    /**
     * Caches the room the pedestrian is into when he/she starts moving.
     * When the pedestrian is crossing a door, it contains the room being left. When in [State.MOVING_TO_FINAL],
     * it contains the room the pedestrian was (and should be) into. It's used to detect if the pedestrian ended
     * up in an unexpected room while moving.
     */
    protected var previousRoom: Optional<M> = Optional.empty()
    /**
     * Defined when crossing a door. See [crossDoor].
     */
    protected var crossingPoints: Optional<Pair<P, P>> = Optional.empty()
    /**
     * Defined when crossing a door.
     */
    protected var expectedNewRoom: Optional<M> = Optional.empty()
    /**
     * Defined in [State.MOVING_TO_FINAL].
     */
    protected var finalDestination: Optional<P> = Optional.empty()

    /**
     * @returns the value if present or throws an [IllegalStateException] with a meaningful message.
     */
    protected fun <T> Optional<T>.orFail(): T = orElseThrow {
        IllegalStateException("internal error: variable must be defined in $state")
    }

    /**
     * Updates [pedestrianPosition] and [currentRoom], this can be costly.
     * Depending on how [ConvexGeometricShape.contains] manage points on the boundary, the pedestrian could
     * be inside two (adjacent) rooms at once. This can happen in two cases:
     * - when in [State.MOVING_TO_CROSSING_POINT_1] or [State.MOVING_TO_FINAL] and the pedestrian is moving
     * on [previousRoom]'s boundary. In such case [previousRoom] is used.
     * - when crossing a door or in [State.NEW_ROOM] and [expectedNewRoom] is adjacent to [previousRoom].
     * In such case [expectedNewRoom] is used.
     * Otherwise the first room containing [pedestrianPosition] is used.
     */
    protected open fun updateCachedVariables() {
        pedestrianPosition = environment.getPosition(pedestrian)
        currentRoom = when {
            (state == State.MOVING_TO_CROSSING_POINT_1 || state == State.MOVING_TO_FINAL) &&
                previousRoom.orFail().contains(pedestrianPosition) -> previousRoom
            (state == State.MOVING_TO_CROSSING_POINT_2 || state == State.CROSSING_DOOR || state == State.NEW_ROOM) &&
                expectedNewRoom.isPresent &&
                expectedNewRoom.get().contains(pedestrianPosition) -> expectedNewRoom
            else -> Optional.ofNullable(
                environment.graph.vertexSet().firstOrNull { it.contains(pedestrianPosition) }
            )
        }
    }

    protected open fun onStart() {
        state = when {
            currentRoom.isPresent -> State.NEW_ROOM
            /*
             * If the pedestrian cannot locate itself inside any room on start, it simply won't move.
             */
            else -> State.ARRIVED
        }
    }

    /**
     * @returns all the doors (= passages/edges) outgoing from the current room.
     */
    override fun doorsInSight(): List<F> = emptyList<F>()
        .takeUnless { currentRoom.isPresent }
        ?: environment.graph.outgoingEdgesOf(currentRoom.get()).toList()

    /**
     * The target of a directed edge of the environment's graph.
     */
    protected open val F.target: M get() = environment.graph.getEdgeTarget(this)

    /**
     * Moves the pedestrian across the provided [door], which must be among [doorsInSight].
     * Since connected rooms may be non-adjacent, a pair of [crossingPoints] has to be provided:
     * - the first point must belong to the current room's boundary and will be reached first,
     * - the second point must belong to the next room's boundary and will be pursued after
     * reaching the former one. [crossingPoints] may coincide if the two rooms are adjacent.
     */
    protected open fun crossDoor(door: F, crossingPoints: Pair<P, P>) {
        require(doorsInSight().contains(door)) { "$door is not in sight" }
        state = State.MOVING_TO_CROSSING_POINT_1
        this.previousRoom = Optional.of(currentRoom.orFail())
        this.crossingPoints = Optional.of(crossingPoints)
        this.expectedNewRoom = Optional.of(door.target)
    }

    override fun moveToFinal(destination: P) {
        require(currentRoom.orFail().contains(destination)) { "$destination is not in $currentRoom" }
        state = State.MOVING_TO_FINAL
        this.previousRoom = Optional.of(currentRoom.orFail())
        this.finalDestination = Optional.of(destination)
    }

    protected open fun moving() {
        currentRoom.takeIf { it.isPresent && it.get() != previousRoom.orFail() }?.get()?.let { newRoom ->
            return when (newRoom) {
                expectedNewRoom.orFail() -> state = State.NEW_ROOM
                else -> strategy.inUnexpectedNewRoom(previousRoom.orFail(), expectedNewRoom.orFail(), newRoom)
            }
        }
        if (desiredPosition.isReached()) {
            state = when (state) {
                State.MOVING_TO_CROSSING_POINT_1 -> State.MOVING_TO_CROSSING_POINT_2
                    /*
                     * Short-cut to save time.
                     */
                    .takeUnless { crossingPoints.orFail().run { first == second } } ?: State.CROSSING_DOOR
                State.MOVING_TO_CROSSING_POINT_2 -> State.CROSSING_DOOR
                State.MOVING_TO_FINAL -> State.ARRIVED
                else -> state
            }
        }
    }

    /**
     * The position the pedestrian wants to reach.
     */
    val desiredPosition: P get() = when (state) {
        State.MOVING_TO_CROSSING_POINT_1 -> crossingPoints.orFail().first
        State.MOVING_TO_CROSSING_POINT_2 -> crossingPoints.orFail().second
        State.CROSSING_DOOR -> expectedNewRoom.orFail().centroid
        State.MOVING_TO_FINAL -> finalDestination.orFail()
        /*
         * Always up to date current position.
         */
        else -> environment.getPosition(pedestrian)
    }

    /**
     * Updates the internal state but does not move the pedestrian.
     */
    open fun update() {
        updateCachedVariables()
        when (state) {
            State.START -> onStart()
            State.NEW_ROOM -> currentRoom.orFail().let {
                pedestrian.registerVisit(it)
                strategy.inNewRoom(it)
            }
            in State.MOVING_TO_CROSSING_POINT_1..State.MOVING_TO_FINAL -> moving()
            /*
             * Arrived.
             */
            else -> Unit
        }
    }
}