/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * This interface represents a temporal distribution for any event.
 * 
 * @param <T>
 *            concentration type
 */
public interface TimeDistribution extends Serializable {

    /**
     * @return a sample from the time distribution
     */
    Time computeNextOccurrence(Time currentTime);

    /**
     * @return how many times per time unit the event will happen on average
     */
    double getRate();

    /**
     * @param currentTime
     *            the time at which the cloning operation happened
     * @return an exact copy of this {@link TimeDistribution}
     */
    TimeDistribution clone(Time currentTime);

}
