/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.apache.commons.math3.util.MathArrays;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 
 * 
 * @param <T>
 */
public final class ChemicalReaction<T> extends ReactionLike<T> {

    private static final long serialVersionUID = -5260452049415003046L;
    private final double baseRate;
    private double currentRate = Double.NaN;

    /**
     * @param n
     *            node
     */
    public ChemicalReaction(final Node<T> n, double rate) {
        super(n);
        baseRate = rate;
    }

    public ChemicalReaction(final Node<T> node, TimeDistribution timeDistribution) {
        super(node, timeDistribution);
        this(node, timeDistribution.getRate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChemicalReaction<T> cloneOnNewNode(final Node<T> n, final Time currentTime) {
        return makeClone(() -> new ChemicalReaction<>(n, baseRate));
    }

    @Override
    public void initializationComplete(final Time t) {

    }

    /**
     * Subclasses must call super.updateInternalStatus for the rate to get updated in case of method override.
     */
    protected void updateInternalStatus(final Time curTime, final boolean executed, final Environment<T, ?> env) {
        currentRate = baseRate;
        for (final Condition<T> cond : getConditions()) {
            final double v = cond.getPropensityContribution();
            if (v == 0) {
                currentRate = 0;
                break;
            }
            if (v < 0) {
                throw new IllegalStateException("Condition " + cond + " returned a negative propensity conditioning value");
            }
            currentRate *= cond.getPropensityContribution();
        }
    }

    @Override
    public final double getRate() {
        return Double.isNaN(currentRate) ? getTimeDistribution().getRate() : currentRate;
    }

    @Override
    public Time getPutativeExecutionTime() {
        return null;
    }

    @Override
    public TimeDistribution getTimeDistribution() {
        return null;
    }

    @Override
    public void update(final Time curTime, final boolean executed) {

    }

    @Override
    protected Time generateTime(@NotNull final double[] propensityVector,
                                @NotNull final Time currentTime,
                                final boolean executed) {
        currentRate = getTimeDistribution().getRate();
        for (final double v : propensityVector) {
            currentRate *= v;
        }
        return getTimeDistribution();
    }
}
