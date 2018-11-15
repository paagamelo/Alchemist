/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.interfaces;

import org.danilopianini.util.ListSet;

import java.io.Serializable;
import java.util.List;

/**
 * This interface represents a temporal distribution for any event.
 * 
 * @param <T>
 *            concentration type
 */
public interface TimeDistribution<T> extends Cloneable, Serializable {

    /**
     * @param newNode the node where the clone of this time distribution will be inserted
     * @param currentTime
     *            the time at which the cloning operation happened
     * @return an exact copy of this {@link TimeDistribution}
     */
    TimeDistribution<T> clone(Node<T> newNode, Time currentTime);

    /**
     * @return the next time at which the event will occur
     */
    Time getNextOccurence();

    /**
     * @return how many times per time unit the event will happen
     */
    double getRate();

    /**
     * This method is called when the environment has completed its
     * initialization. Can be used by the time distribution to compute its next
     * execution time - in case such computation requires an inspection of the
     * environment.
     *
     * @param reaction the reaction which is hosting this {@link TimeDistribution}
     *
     * @param currentTime
     *            the time at which the initialization of the hosting reaction was
     *            accomplished
     */
    void initializationComplete(Reaction<T> reaction, Time currentTime);

    /**
     * Updates the internal status.
     *
     * @param currentTime
     *            current time
     * @param executed
     *            true if the reaction has just been executed
     * @param conditions
     *            the current list of conditions in the reaction hosting this time distribution. The typical usage is
     *            to get from them data which may influence the next execution time, typically through
     *            {@link Condition#getPropensityContribution()}
     */
    void update(Time currentTime, boolean executed, List<Condition<T>> conditions);

}
