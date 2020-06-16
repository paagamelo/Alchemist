/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A pedestrian capable of orienting itself.
 *
 * @param T the concentration type.
 * @param V the [Vector] type for the space this pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks stored in the pedestrian's [cognitiveMap].
 * @param R the type of edges of the [cognitiveMap].
 */
interface OrientingPedestrian<
    T,
    V : Vector<V>,
    A : GeometricTransformation<V>,
    L : ConvexGeometricShape<V, A>,
    R
> : Pedestrian<T, V, A>, OrientingAgent<V, A, L, R>