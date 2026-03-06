package net.earthmc.emcapi.sse.listeners;

import net.earthmc.emcapi.sse.SSEManager;
import org.bukkit.event.Listener;

public abstract class AbstractSSEListener implements Listener {
    protected final SSEManager sse;

    protected AbstractSSEListener(SSEManager sseManager) {
        this.sse = sseManager;
    }
}
