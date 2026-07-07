package net.earthmc.emcapi.object.optout;

/**
 * @param override If true, all data sharing is disabled
 * @param townyResident If true, a player's Towny resident data is not shared
 * @param onlineStatus If true, a player's online status (both in the online endpoint and Towny endpoint) is not shared
 * @param quickShops If false, any player with a valid API key may query this player's shops
 * @param mcmmo If false, any player with a valid API key may query this player's mcMMO data
 */
public record OptOutSettings(boolean override, boolean townyResident, boolean onlineStatus, boolean quickShops, boolean mcmmo) {
    public static final OptOutSettings DEFAULT = new OptOutSettings(false, false, false, true, true);

    public boolean optedOut(OptOutType type) {
        if (override) {
            return true;
        }
        return switch (type) {
            case TOWNY_RESIDENT -> townyResident;
            case ONLINE_STATUS -> onlineStatus;
            case QUICKSHOPS -> quickShops;
            case MCMMO -> mcmmo;
        };
    }

    public OptOutSettings override(boolean value) {
        if (override == value) {
            return this;
        }
        return new OptOutSettings(value, townyResident, onlineStatus, quickShops, mcmmo);
    }

    public OptOutSettings update(OptOutType type, boolean value) {
        return switch (type) {
            case TOWNY_RESIDENT -> new OptOutSettings(override, value, onlineStatus, quickShops, mcmmo);
            case ONLINE_STATUS -> new OptOutSettings(override, townyResident, value, quickShops, mcmmo);
            case QUICKSHOPS -> new OptOutSettings(override, townyResident, onlineStatus, value, mcmmo);
            case MCMMO -> new OptOutSettings(override, townyResident, onlineStatus, quickShops, value);
        };
    }

    /**
     * @return Whether this object is redundant and should not be saved, as it carries the default values
     */
    public boolean isRedundant() {
        if (override) {
            return false;
        }
        return !townyResident && !onlineStatus && quickShops && mcmmo;
    }
}
