/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/**
 * 
 */
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

import java.util.List;

/**
 * @param <T>
 *            Concentration type
 */
public final class Trigger<T> implements TimeDistribution<T> {

    private static final long serialVersionUID = 5207992119302525618L;
    private Time willOccurAt;
    private Reaction<T> hostReaction;

    /**
     * @param event
     *            the time at which the event will happen
     */
    public Trigger(final Time event) {
        willOccurAt = event;
    }

    @Override
    public TimeDistribution<T> clone(final Node<T> newNode, final Time currentTime) {
        return new Trigger<>(willOccurAt);
    }

    @Override
    public Time getNextOccurence() {
        return willOccurAt;
    }

    @Override
    public double getRate() {
        return 0;
    }

    @Override
    public void initializationComplete(final Reaction<T> reaction, final Time currentTime) {
        hostReaction = reaction;
    }

    @Override
    public void update(final Time currentTime, final boolean executed, final List<Condition<T>> conditions) {
        if (executed) {
            willOccurAt = DoubleTime.INFINITE_TIME;
            if (hostReaction != null) {
                hostReaction.getNode().removeReaction(hostReaction);
            }
        }
    }

}
