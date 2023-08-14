package me.melontini.andromeda.config;

import com.google.common.collect.Lists;
import me.melontini.andromeda.util.SharedConstants;
import me.melontini.andromeda.util.annotations.config.Excluded;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"ArraysAsListWithZeroOrOneArgument"})
@Config(name = "andromeda")
@Config.Gui.Background("minecraft:textures/block/amethyst_block.png")
public class AndromedaConfig implements ConfigData {

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.CollapsibleObject
    public SelfPlanting autoPlanting = new SelfPlanting();

    public static class SelfPlanting {
        @ConfigEntry.Category("world")
        public boolean enabled = true;

        @ConfigEntry.Category("world")
        @ConfigEntry.Gui.Tooltip
        public boolean blacklistMode = true;

        @ConfigEntry.Category("world")
        @ConfigEntry.Gui.Tooltip
        public List<String> idList = Lists.newArrayList();
    }

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean canBeeNestsFall = true;

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    public boolean temperatureBasedCropGrowthSpeed = false;

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @Excluded.IfPlatform(SharedConstants.Platform.CONNECTOR)
    public boolean quickFire = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public boolean cactusBottleFilling = true;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.Gui.CollapsibleObject
    public IncubatorSettings incubatorSettings = new IncubatorSettings();

    public static class IncubatorSettings {
        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableIncubator = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public boolean incubatorRandomness = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public boolean incubatorRecipe = true; //Used in JSON
    }

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.Gui.RequiresRestart
    public boolean usefulFletching = true;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean bedsExplodeEverywhere = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip
    public float bedExplosionPower = 5.0F;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean safeBeds = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean leafSlowdown = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public CampfireTweaks campfireTweaks = new CampfireTweaks();

    public static class CampfireTweaks {
        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public boolean campfireEffects = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public boolean campfireEffectsPassive = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public int campfireEffectsRange = 10;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> campfireEffectsList = Arrays.asList("minecraft:regeneration");

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        public List<Integer> campfireEffectsAmplifierList = Arrays.asList(0);
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.CollapsibleObject
    public Snowballs snowballs = new Snowballs();

    public static class Snowballs {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean freeze = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean extinguish = true;

        @ConfigEntry.Category("entities")
        public boolean enableCooldown = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public int cooldown = 10;
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.CollapsibleObject
    public Slimes slimes = new Slimes();

    public static class Slimes {

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean flee = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean merge = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public int maxMerge = 4;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean slowness = false;
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean beeFlowerDuplication = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    public boolean beeTallFlowerDuplication = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean villagersFollowEmeraldBlocks = false;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean betterFurnaceMinecart = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip()
    public int maxFurnaceMinecartFuel = 45000;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean furnaceMinecartTakeFuelWhenLow = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public NewMinecarts newMinecarts = new NewMinecarts();

    public static class NewMinecarts {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isAnvilMinecartOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isNoteBlockMinecartOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isJukeboxMinecartOn = true;
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public NewBoats newBoats = new NewBoats();

    public static class NewBoats {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isFurnaceBoatOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isTNTBoatOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isJukeboxBoatOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isHopperBoatOn = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        public boolean isChestBoatOn = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.CollapsibleObject
    public Tooltips tooltips = new Tooltips();

    public static class Tooltips {
        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public boolean clock = true;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public boolean compass = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.RequiresRestart
    public boolean lockpickEnabled = false;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public Lockpick lockpick = new Lockpick();

    public static class Lockpick {
        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public int chance = 3;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public boolean breakAfterUse = true;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public boolean villagerInventory = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    public boolean slightlyBetterItemNames = false;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    @Excluded.IfPlatform(SharedConstants.Platform.CONNECTOR)
    public TotemSettings totemSettings = new TotemSettings();

    public static class TotemSettings {
        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @Excluded.IfPlatform(SharedConstants.Platform.CONNECTOR)
        public boolean enableInfiniteTotem = false;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @Excluded.IfPlatform(SharedConstants.Platform.CONNECTOR)
        public boolean enableTotemAscension = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip(count = 4)
    public boolean balancedMending = false;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    public boolean minecartBlockPicking = true;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean minecartSpawnerPicking = true;

    @ConfigEntry.Category("bugfixes")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean frameIndependentAdvancementShadow = true;

    @ConfigEntry.Category("bugfixes")
    @ConfigEntry.Gui.Tooltip
    public boolean properlyAlignedRecipeAlternatives = true;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public boolean villagerGifting = false;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.CollapsibleObject
    public DragonFight dragonFight = new DragonFight();

    public static class DragonFight {
        @ConfigEntry.Category("mechanics")
        public boolean fightTweaks = true;
        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean respawnCrystals = true;
        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean scaleHealthByMaxPlayers = false;
        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean shorterCrystalTrackRange = true;
        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public boolean shorterSpikes = false;
    }

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public boolean throwableItems = true;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    public List<String> throwableItemsBlacklist = Lists.newArrayList();

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    public boolean tooltipNotName = false;

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    public boolean noMoreAdventure = false;

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public GuiParticles guiParticles = new GuiParticles();

    public static class GuiParticles {
        @ConfigEntry.Category("gui")
        public boolean anvilScreenParticles = true;

        @ConfigEntry.Category("gui")
        public boolean enchantmentScreenParticles = true;

        @ConfigEntry.Category("gui")
        public boolean furnaceScreenParticles = true;

        @ConfigEntry.Category("gui")
        public boolean creativeScreenParticles = true;

        @ConfigEntry.Category("gui")
        public double creativeScreenParticlesVelX = 0.7d;

        @ConfigEntry.Category("gui")
        public boolean gameModeSwitcherParticles = true;
    }

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    public boolean itemFrameTooltips = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean damageBackport = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.CollapsibleObject
    public AutoGenRecipes autogenRecipeAdvancements = new AutoGenRecipes();

    public static class AutoGenRecipes {
        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public boolean autogenRecipeAdvancements = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public boolean requireAllItems = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public boolean ignoreRecipesHiddenInTheRecipeBook = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public List<String> blacklistedRecipeNamespaces = Arrays.asList("minecraft", "andromeda", "extshape");

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public List<String> blacklistedRecipeIds = Arrays.asList();
    }

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean minorInconvenience = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public boolean compatMode = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip()
    public boolean autoUpdateTranslations = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public boolean sendOptionalData = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean sendCrashReports = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean debugMessages = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.RequiresRestart
    public boolean unknown = false;
}
