package net.earthmc.emcapi.service.mysterymaster;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MysteryPlayer {
    @NotNull
    String username();

    @NotNull
    UUID uuid();

    int openedCrates();

    int indexChange();
}
