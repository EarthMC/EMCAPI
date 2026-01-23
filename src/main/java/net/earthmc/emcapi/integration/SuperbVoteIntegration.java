package net.earthmc.emcapi.integration;

import io.minimum.minecraft.superbvote.SuperbVote;

/**
 * An integration with superbvote to read vote party values.
 */
public class SuperbVoteIntegration extends Integration {
    public SuperbVoteIntegration() {
        super("SuperbVote");
    }

    public int currentVotes() {
        return SuperbVote.getPlugin().getVoteParty().getCurrentVotes();
    }

    public int votesNeeded() {
        return SuperbVote.getPlugin().getVoteParty().votesNeeded();
    }
}
