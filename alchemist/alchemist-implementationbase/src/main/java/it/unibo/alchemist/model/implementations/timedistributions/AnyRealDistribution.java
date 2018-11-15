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
import it.unibo.alchemist.model.implementations.utils.RealDistributionUtil;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * This class is able to use any distribution provided by Apache Math 3 as a
 * subclass of {@link RealDistribution}. Being generic, however, it does not
 * allow for dynamic rate tuning (namely, it can't be used to generate events
 * with varying frequency based on
 * {@link it.unibo.alchemist.model.interfaces.Condition#getPropensityContribution()}.
 * 
 * @param <T>
 *            concentration type
 */
public final class AnyRealDistribution<T> extends AbstractMonotonicDistribution<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All the implementations of RealDistribution also implement Serializable")
    private final RealDistribution distribution;

    /**
     * @param rng
     *            the {@link RandomGenerator}
     * @param distribution
     *            the distribution name (case insensitive). Must be mappable to an entity implementing {@link RealDistribution}
     * @param parameters
     *            the parameters for the distribution
     */
    public AnyRealDistribution(final RandomGenerator rng, final String distribution, final double... parameters) {
        this(DoubleTime.ZERO_TIME, rng, distribution, parameters);
    }

    /**
     * @param start
     *            the initial time
     * @param rng
     *            the {@link RandomGenerator}
     * @param distribution
     *            the distribution name (case insensitive). Must be mappable to an entity implementing {@link RealDistribution}
     * @param parameters
     *            the parameters for the distribution
     */
    public AnyRealDistribution(final Time start, final RandomGenerator rng, final String distribution, final double... parameters) {
        this(start, RealDistributionUtil.makeRealDistribution(rng, distribution, parameters));
    }

    /**
     * @param distribution
     *            the {@link AnyRealDistribution} to use. To ensure
     *            reproducibility, such distribution must get created using the
     *            simulation {@link RandomGenerator}.
     */
    public AnyRealDistribution(final RealDistribution distribution) {
        this(DoubleTime.ZERO_TIME, distribution);
    }

    /**
     * @param start
     *            distribution start time
     * @param distribution
     *            the {@link AnyRealDistribution} to use. To ensure
     *            reproducibility, such distribution must get created using the
     *            simulation {@link RandomGenerator}.
     */
    public AnyRealDistribution(final Time start, final RealDistribution distribution) {
        super(start.plus(new DoubleTime(distribution.sample())));
        this.distribution = distribution;
    }

    @Override
    public double getRate() {
        return distribution.getNumericalMean();
    }

    @Override
    protected AbstractDistribution<T> cloneWithStartTime(final Node<T> node, final Time currentTime) {
        if (distribution instanceof Serializable) {
            return new AnyRealDistribution<>(currentTime, (RealDistribution) SerializationUtils.clone((Serializable) distribution));
        }
        throw new UnsupportedOperationException("Could not clone this time distribution, as "
                + distribution.getClass().getSimpleName() + " cannot be cloned (it's not Serializable)");
    }

    @NotNull
    @Override
    public Time computeDeltaTime() {
        return new DoubleTime(distribution.sample());
    }

}
