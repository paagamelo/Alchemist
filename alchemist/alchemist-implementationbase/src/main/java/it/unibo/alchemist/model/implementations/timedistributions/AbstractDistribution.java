/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

import java.util.List;

/**
 * This class provides, through a template method pattern, an utility that
 * ensures that the distribution does not trigger events before its initial
 * scheduling time.
 * 
 * @param <T>
 */
public abstract class AbstractDistribution<T> implements TimeDistribution<T> {
    //TODO: rename class in AbstractDistributionWithStartTime
    private static final long serialVersionUID = -8906648194668569179L;
    private Time tau;
    private boolean schedulable;
    private final Time startTime;

    /**
     * @param start
     *            initial time
     */
    public AbstractDistribution(final Time start) {
        tau = start;
        startTime = start;
    }

    /**
     * Implement this method to change the way .
     * 
     * @param curTime
     *            current time
     * @param executed
     *            true if the reaction whose this distribution has been
     *            associated has just been executed
     * @param conditions
     *            the list of conditions for this reaction. See {@link TimeDistribution#update(Time, boolean, List)}
     * @return the time **difference** between the current putative time (obtainable via {@link #getNextOccurence()})
     * and the new time. The returned result will be summed to the internal time and provide a new execution time.
     */
    protected abstract Time computeNewTime(Time curTime, boolean executed, List<Condition<T>> conditions);

    @Override
    public final AbstractDistribution<T> clone(Node<T> node, Time currentTime) {
        return cloneWithStartTime(node, currentTime.compareTo(startTime) > 0 ? currentTime : startTime);
    }

    protected abstract AbstractDistribution<T> cloneWithStartTime(Node<T> node, Time startTime);

    @Override
    public final Time getNextOccurence() {
        return tau;
    }

    protected final Time getStartTime() {
        return startTime;
    }

    @Override
    public final void update(final Time currentTime,
                             final boolean executed,
                             final List<Condition<T>> conditions) {
        if (!schedulable && currentTime.compareTo(startTime) >= 0) {
            /*
             * If the simulation time is beyond the startTime for this reaction,
             * it can start being scheduled normally.
             */
            schedulable = true;
        }
        /*
         * If the current time is not past the starting time for this reaction,
         * it should not be used.
         */
        tau = computeNewTime(schedulable ? currentTime : startTime, executed, conditions);
        if (tau.compareTo(currentTime) < 0) {
            throw new IllegalStateException(getClass().getSimpleName() + " tried to schedule a reaction in the past:"
                    + "currentTime is " + currentTime + ", new scheduled time is " + tau);
        }
    }

}
