package net.earthmc.emcapi.util;

import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiName;
import io.javalin.openapi.OpenApiNullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContentTypes {
    // Common
    public record NameUUID(String name, UUID uuid) {}

    // Queries
    public record UUIDKey(String query, String key) {}
    public record StringKey(String query, String key) {}

    public record Advancements(@OpenApiName("minecraft:advancement/path") AdvancementData data) {}
    public record AdvancementData(UUID player, String date) {}

    public record McMMOTopQuery(String skill, String key) {}

    public record NearbyQuery(@OpenApiName("target_type") String targetType, String target,
                              @OpenApiName("search_type") String searchType, int radius, @OpenApiNullable Boolean strict) {}

    // Responses
    public record Nation(String name, UUID uuid, @OpenApiNullable String board,
                         @OpenApiNullable String dynmapColour, @OpenApiNullable String dynmapOutline, @OpenApiNullable String wiki,
                         @OpenApiNullable String discord, NameUUID king, NameUUID capital, NationTimestamps timestamps,
                         NationStatus status, NationStats stats, NationCoordinates coordinates, NameUUID[] residentsArray,
                         NameUUID[] towns, NameUUID[] outlaws, NameUUID[] allies, NameUUID[] enemies, NameUUID[] sanctioned,
                         Map<String, List<NameUUID>> ranks, NationEmbargoes embargoes, NationPacts pacts,
                         @OpenApiDescription("Only visible when queried with a nation leader/chancellor/treasurer's API key") BankHistory[] bankhistory) {}
    public record NationTimestamps(long registered) {}
    public record NationStatus(boolean isOpen, boolean isPublic, boolean isNeutral) {}
    public record NationStats(int nationBonus, int numTownBlocks, int numResidents, int numTowns, int numOutlaws,
                              int numAllies, int numEnemies, double balance) {}
    public record NationCoordinates(SpawnCoordinates spawn) {}
    public record NationEmbargoes(NameUUID[] own, NameUUID[] against) {}
    public record NationPacts(PactEntry[] active, PactEntry[] pending) {}
    public record PactEntry(@OpenApiName("NATION") Pact nation) {}
    public record Pact(String sender, String receiver, String status, PactStats stats) {}
    public record PactStats(long createdAt, long expiresAt, int duration) {}

    public record Town(String name, UUID uuid, @OpenApiNullable String board, String founder, @OpenApiNullable String wiki,
                       @OpenApiNullable String discord, NameUUID mayor, NameUUID nation, TownTimestamps timestamps,
                       TownStatus status, TownStats stats, TownyPerms perms, TownCoordinates coordinates,
                       NameUUID[] residents, NameUUID[] trusted, NameUUID[] outlaws, NameUUID[] quarters,
                       Map<String, List<NameUUID>> ranks, TownWarp[] warps,
                       @OpenApiDescription("Only visible when queried with a nation leader/chancellor/treasurer's API key") BankHistory[] bankhistory) {}
    public record TownTimestamps(long registered, @OpenApiNullable Long joinedNationAt, @OpenApiNullable Long ruinedAt) {}
    public record TownStatus(boolean isPublic, boolean isOpen, boolean isNeutral, boolean isCapital,
                             boolean isOverclaimed, boolean isRuined, boolean isForSale, boolean hasNation,
                             boolean canOutsiderSpawn, boolean canPassiveMobsSpawn,
                             boolean hasSnowAccumulation, boolean hasFriendlyFire) {}
    public record TownStats(int numTownBlocks, int maxTownBlocks, int numResidents, int numTrusted, int numOutlaws,
                            double balance, @OpenApiNullable Double forSalePrice) {}
    public record BankHistory(long time, String type, int amount, int balance, String reason) {}
    public record TownCoordinates(SpawnCoordinates spawn, int[] homeBlock, int[][] townBlocks) {}
    public record TownWarp(String name, UUID uuid, long createdAt, String createdBy, String access, WarpLocation location) {}
    public record WarpLocation(int x, int y, int z) {}

    public record SpawnCoordinates(String world, double x, double y, double z, float yaw, float pitch) {}

    public record Player(String name, UUID uuid, @OpenApiNullable String title, @OpenApiNullable String surname,
                         @OpenApiNullable String formattedName, @OpenApiNullable String about, NameUUID town,
                         NameUUID nation, PlayerTimestamps timestamps, PlayerStatus status, PlayerStats stats,
                         TownyPerms perms, PlayerRanks ranks, NameUUID[] friends) {}
    public record PlayerTimestamps(long registered, @OpenApiNullable Long joinedTownAt, @OpenApiNullable @OpenApiDescription("Only null if this resident is an NPC") Long lastOnline) {}
    public record PlayerStatus(boolean isOnline, boolean isNPC, boolean isMayor, boolean isKing, boolean hasTown, boolean hasNation) {}
    public record PlayerStats(double balance, int numFriends) {}
    public record PlayerRanks(String[] townRanks, String[] nationRanks) {}

    public record Quarter(UUID uuid, String type, NameUUID owner, NameUUID town,
                          QuarterTimestamps timestamps, QuarterStatus status, QuarterStats stats,
                          int[] colour, NameUUID[] trusted, QuarterCuboid[] cuboids) {}
    public record QuarterTimestamps(long registered, @OpenApiNullable Long claimedAt) {}
    public record QuarterStatus(boolean isEmbassy) {}
    public record QuarterStats(@OpenApiNullable Integer price, int volume, int numCuboids) {}
    public record QuarterCuboid(int[] cornerOne, int[] cornerTwo) {}

    public record TownyPerms(boolean[] build, boolean[] destroy, @OpenApiName("switch") boolean[] switchPerm, boolean[] itemUse,
                             TownyFlags flags) {}
    public record TownyFlags(boolean pvp, boolean explosions, boolean fire, boolean mobs) {}

    public record Location(Loc location, boolean isWilderness, NameUUID town, NameUUID nation) {}
    public record Loc(int x, int z) {}

    public record McMMO(String name,
                        @OpenApiName("ACROBATICS") int acrobatics, @OpenApiName("ALCHEMY") int alchemy,
                        @OpenApiName("ARCHERY") int ARCHERY, @OpenApiName("AXES") int axes,
                        @OpenApiName("CROSSBOWS") int crossbows, @OpenApiName("EXCAVATION") int excavation,
                        @OpenApiName("FISHING") int fishing, @OpenApiName("HERBALISM") int herbalism,
                        @OpenApiName("MACES") int maces, @OpenApiName("MINING") int mining,
                        @OpenApiName("REPAIR") int repair, @OpenApiName("SALVAGE") int salvage,
                        @OpenApiName("SMELTING") int smelting, @OpenApiName("SPEARS") int spears,
                        @OpenApiName("SWORDS") int swords, @OpenApiName("TAMING") int taming,
                        @OpenApiName("TRIDENTS") int tridents, @OpenApiName("UNARMED") int unarmed,
                        @OpenApiName("WOODCUTTING") int woodcutting) {}

    public record McMMOTop(String skill, @OpenApiName("1") McMMOTopEntry one, @OpenApiName("2") McMMOTopEntry two,
                           @OpenApiName("3") McMMOTopEntry three, long lastUpdated) {}
    public record McMMOTopEntry(String player, int level) {}

    public record MysteryMasterEntry(String name, UUID uuid, String change) {}

    public record Online(int count, List<NameUUID> players) {}

    public record Pursuits(@OpenApiName("PLAYER") PlayerPursuitLeaderboard player,
                           @OpenApiName("TOWN") TownPursuitLeaderboard town,
                           @OpenApiName("NATION") NationPursuitLeaderboard nation) {}
    public record PlayerPursuitLeaderboard(String name, boolean isActive, Map<String, PlayerPursuitEntry> top) {}
    public record TownPursuitLeaderboard(String name, boolean isActive, Map<String, TownPursuitEntry> top) {}
    public record NationPursuitLeaderboard(String name, boolean isActive, Map<String, NationPursuitEntry> top) {}
    public record PlayerPursuitEntry(UUID player, double score) {}
    public record TownPursuitEntry(UUID town, double score) {}
    public record NationPursuitEntry(UUID nation, double score) {}

    public record Server(String version, String moonPhase, ServerTimestamps timestamps, ServerWeather status, ServerStats stats, VoteParty voteParty) {}
    public record ServerTimestamps(int newDayTime, int serverTimeOfDay) {}
    public record ServerWeather(boolean hasStorm, boolean isThundering) {}
    public record ServerStats(int time, long fullTime, int maxPlayers, int numOnlinePlayers, int numOnlineNomads,
                              int numResidents, int numNomads, int numTowns, int numTownBlocks, int numNations,
                              int numQuarters, int numCuboids) {}
    public record VoteParty(int target, int numRemaining) {}

    public record Shops(@OpenApiName("1") Shop one, @OpenApiName("2") Shop two, @OpenApiName("3") Shop three) {}
    public record Shop(int id, String item, int price, int amount, String type, int stock) {}
}
