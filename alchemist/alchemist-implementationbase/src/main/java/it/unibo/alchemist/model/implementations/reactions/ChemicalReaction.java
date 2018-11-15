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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * 
 * 
 * @param <T>
 */
public class ChemicalReaction<T> extends AbstractReaction<T> {

    private static final long serialVersionUID = -5260452049415003046L;

    /**
     * @param node
     *            node
     * @param randomGenerator the simulation {@link RandomGenerator}
     * @param rate the markovian rate for this reaction (which will get multiplied by the propensity contribution of
     *             each condition as returned by
     *             {@link it.unibo.alchemist.model.interfaces.Condition#getPropensityContribution()}
     */
    public ChemicalReaction(final RandomGenerator randomGenerator, final Node<T> node, final double rate) {
        this(node, new ExponentialTime<>(rate, randomGenerator));
    }

    /**
     *
     * @param node the node
     * @param timeDistribution the {@link ExponentialTime} the time distribution this reaction should use. Note: you
     *                         can not share time distributions across reactions.
     */
    public ChemicalReaction(final Node<T> node, final ExponentialTime<T> timeDistribution) {
        super(node, timeDistribution);
    }

    @Override
    protected ChemicalReaction<T> buildNewReaction(final Node<T> n, final TimeDistribution<T> distribution, final Time currentTime) {
        if (distribution instanceof  ExponentialTime) {
            return new ChemicalReaction<>(n, (ExponentialTime<T>) distribution);
        }
        throw new IllegalArgumentException("Chemical reactions can only use " + ExponentialTime.class.getSimpleName()
                + ", attempted to clone one injecting a " + distribution.getClass().getSimpleName());
    }

}
