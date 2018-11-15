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
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

/**
 * This reaction completely ignores the propensity conditioning of the
 * conditions, and tries to run every time the {@link TimeDistribution} wants
 * to.
 * 
 * @param <T>
 */
public final class Event<T> extends AbstractReaction<T> {

    private static final long serialVersionUID = -1640973841645383193L;

    /**
     * @param node the node this {@link Event} belongs to
     * @param timeDistribution the {@link TimeDistribution} this event should use
     */
    public Event(final Node<T> node, final TimeDistribution<T> timeDistribution) {
        super(node, timeDistribution);
    }

    @Override
    protected AbstractReaction<T> buildNewReaction(final Node<T> n, final TimeDistribution<T> distribution, final Time currentTime) {
        return new Event<>(n, distribution);
    }

}
