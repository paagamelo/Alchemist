/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.timedistributions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.List;

/**
 * Markovian events.
 * 
 * @param <T>
 */
public class ExponentialTime<T> extends AbstractDistribution<T> {

    private static final long serialVersionUID = 5216987069271114818L;
    private double propensity;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the random engines provided by Apache are Serializable")
    private final RandomGenerator rand;
    private final double rate;

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public ExponentialTime(final double markovianRate, final RandomGenerator random) {
        this(markovianRate, DoubleTime.ZERO_TIME, random);
    }

    /**
     * @param markovianRate
     *            Markovian rate for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    public ExponentialTime(final double markovianRate, final Time start, final RandomGenerator random) {
        super(start);
        if (markovianRate < 0 || Double.isNaN(markovianRate)) {
            throw new IllegalArgumentException("Rate must be positive (provided: " + markovianRate + ')');
        }
        rate = markovianRate;
        rand = random;
    }

    @Override
    protected ExponentialTime<T> cloneWithStartTime(final Node<T> node, final Time startTime) {
        return new ExponentialTime<>(rate, startTime, rand);
    }

    private double computePropensity(final List<Condition<T>> conditions) {
        double propensity = getMarkovianRate();
        for (final Condition<T> cond : conditions) {
            final double v = cond.getPropensityContribution();
            if (v < 0 || Double.isNaN(v)) {
                throw new IllegalStateException("Condition " + cond + " returned an invalid propensity contribution value: " + v);
            }
            if (v == 0.0) {
                return 0;
            }
            if (Double.isInfinite(v)) {
                return Double.POSITIVE_INFINITY;
            }
            propensity *= v;
        }
        return propensity;
    }

    @Override
    protected final Time computeNewTime(final Time curTime, final boolean executed, final List<Condition<T>> conditions) {
        /*
         * Compute the current reaction propensity
         */
        double oldPropensity = propensity;
        propensity = computePropensity(conditions);
        if (propensity == 0.0) {
            // Reaction cannot get scheduled.
            return DoubleTime.INFINITE_TIME;
        }
        if (Double.isInfinite(propensity)) {
            // Reaction must be scheduled immediately
            return curTime;
        }
        if (executed || oldPropensity == 0.0) {
            // Random re-use not applicable
            return curTime.plus(new DoubleTime(uniformToExponential(propensity)));
        }
        // Random reuse applicable
        // 1. Compute the time left before the occurrence with the previous generated time
        final Time timeLeft = getNextOccurence().minus(curTime);
        // 2. Emulate a new generation of time by shifting the previous one as if it were generated with the new propensity
        return curTime.plus(timeLeft.times(oldPropensity / propensity));
    }

    @Override
    public void initializationComplete(Reaction<T> reaction, final Time t) {
        update(t, true, reaction.getConditions());
    }

    protected double getMarkovianRate() {
        return rate;
    }

    @Override
    public final double getRate() {
        return propensity;
    }

    private double uniformToExponential(final double lambda) {
        return -FastMath.log1p(-rand.nextDouble()) / lambda;
    }
}
