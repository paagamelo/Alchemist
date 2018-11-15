/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.timedistributions;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import org.jetbrains.annotations.NotNull;

/**
 * A DiracComb is a sequence of events that happen every fixed time interval.
 * 
 * @param <T>
 */
public class DiracComb<T> extends AbstractMonotonicDistribution<T> {

    private static final long serialVersionUID = -5382454244629122722L;

    private final double timeInterval;
    private final Time timeIntervalAsTime;

    /**
     * @param start
     *            initial time
     * @param rate
     *            how many events should happen per time unit
     */
    public DiracComb(final Time start, final double rate) {
        // Tricks the superclass into starting exactly at start time, as it updates once before actual execution
        super(start.minus(new DoubleTime(1 / rate)));
        timeInterval = 1 / rate;
        timeIntervalAsTime = new DoubleTime(timeInterval);
    }

    /**
     * @param rate
     *            how many events should happen per time unit
     */
    public DiracComb(final double rate) {
        this(new DoubleTime(), rate);
    }

    @Override
    public final double getRate() {
        return 1 / timeInterval;
    }

    @NotNull
    @Override
    public final Time computeDeltaTime() {
        return timeIntervalAsTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractDistribution<T> cloneWithStartTime(final Node<T> node, final Time start) {
        return new DiracComb<>(start, 1 / timeInterval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + timeInterval + ")";
    }

}
