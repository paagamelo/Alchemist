package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Time;
import it.unibo.alchemist.model.interfaces.TimeDistribution;

public class ReactionLike<T> extends AbstractReaction<T> {

    private boolean executable = true;

    /**
     * Builds a new reaction, starting at time t.
     *
     * @param n  the node this reaction belongs to
     * @param pd
     */
    public ReactionLike(final Node<T> n, final TimeDistribution<T> pd) {
        super(n, pd);
    }

    @Override
    protected AbstractReaction<T> buildNewReaction(final Node<T> n, final TimeDistribution<T> distribution, final Time currentTime) {
        return new ReactionLike<>(n, distribution);
    }

    @Override
    public Time getTau() {
        return canExecute() ? getTimeDistribution().getNextOccurence() : DoubleTime.INFINITE_TIME;
    }

    @Override
    public void update(final Time curTime, final boolean executed) {
        /*
         * trick time distributions into a forced update if the reaction is scheduled for infinity but can now execute.
         */
        getTimeDistribution().update(curTime, executed || (!executable && canExecute()), getConditions());
        executable = canExecute();
    }
}
