package me.melontini.andromeda.config;

import com.google.common.collect.Lists;
import me.melontini.andromeda.util.annotations.config.Environment;
import me.melontini.andromeda.util.annotations.config.FeatureEnvironment;
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
    @FeatureEnvironment(Environment.SERVER)
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
    @FeatureEnvironment(Environment.SERVER)
    public boolean canBeeNestsFall = true;

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean temperatureBasedCropGrowthSpeed = false;

    @ConfigEntry.Category("world")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean quickFire = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip()
    @FeatureEnvironment(Environment.SERVER)
    public boolean fallingPropagule = true;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.BOTH)
    public boolean cactusBottleFilling = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.BOTH)
    public IncubatorSettings incubator = new IncubatorSettings();

    public static class IncubatorSettings {
        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean enable = false;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean randomness = true;

        @ConfigEntry.Category("blocks")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean recipe = true; //Used in JSON
    }

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.BOTH)
    public boolean usefulFletching = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.SERVER)
    public boolean bedsExplodeEverywhere = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean enableBedExplosionPower = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public float bedExplosionPower = 5.0F;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean safeBeds = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.SERVER)
    public boolean leafSlowdown = false;

    @ConfigEntry.Category("blocks")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.SERVER)
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
        public List<Effect> effectList = Arrays.asList(new Effect());

        public static class Effect {

            public String identifier;
            public int amplifier;

            public Effect(String identifier, int amplifier) {
                this.identifier = identifier;
                this.amplifier = amplifier;
            }

            public Effect() {
                this.identifier = "minecraft:regeneration";
                this.amplifier = 0;
            }
        }
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @FeatureEnvironment(Environment.SERVER)
    public boolean zombiesPreventUselessItems = false;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean allZombiesCanPickUpItems = false;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.SERVER)
    public Snowballs snowballs = new Snowballs();

    public static class Snowballs {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean freeze = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean extinguish = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean melt = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public boolean layers = false;

        @ConfigEntry.Category("entities")
        public boolean enableCooldown = true;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        public int cooldown = 10;
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.SERVER)
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
    @FeatureEnvironment(Environment.SERVER)
    public boolean beeFlowerDuplication = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean beeTallFlowerDuplication = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.SERVER)
    public boolean villagersFollowEmeraldBlocks = false;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @FeatureEnvironment(Environment.SERVER)
    public boolean betterFurnaceMinecart = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip()
    @FeatureEnvironment(Environment.SERVER)
    public int maxFurnaceMinecartFuel = 45000;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @FeatureEnvironment(Environment.SERVER)
    public boolean furnaceMinecartTakeFuelWhenLow = true;

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public NewMinecarts newMinecarts = new NewMinecarts();

    public static class NewMinecarts {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isAnvilMinecartOn = false;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isNoteBlockMinecartOn = false;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isJukeboxMinecartOn = false;
    }

    @ConfigEntry.Category("entities")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public NewBoats newBoats = new NewBoats();

    public static class NewBoats {
        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isFurnaceBoatOn = false;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isTNTBoatOn = false;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isJukeboxBoatOn = false;

        @ConfigEntry.Category("entities")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean isHopperBoatOn = false;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.CLIENT)
    public Tooltips tooltips = new Tooltips();

    public static class Tooltips {
        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.CLIENT)
        public boolean clock = true;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.CLIENT)
        public boolean compass = true;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.CLIENT)
        public boolean recoveryCompass = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.BOTH)
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
    @FeatureEnvironment(Environment.CLIENT)
    public boolean slightlyBetterItemNames = false;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    public TotemSettings totemSettings = new TotemSettings();

    public static class TotemSettings {
        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.RequiresRestart
        @FeatureEnvironment(Environment.BOTH)
        public boolean enableInfiniteTotem = false;

        @ConfigEntry.Category("items")
        @ConfigEntry.Gui.Tooltip
        public boolean enableTotemAscension = true;
    }

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip(count = 4)
    @FeatureEnvironment(Environment.BOTH)
    public boolean balancedMending = false;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip
    public boolean minecartBlockPicking = true;

    @ConfigEntry.Category("items")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.BOTH)
    public boolean minecartSpawnerPicking = false;

    @ConfigEntry.Category("bugfixes")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @FeatureEnvironment(Environment.CLIENT)
    public boolean frameIndependentAdvancementShadow = true;

    @ConfigEntry.Category("bugfixes")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.CLIENT)
    public boolean properlyAlignedRecipeAlternatives = true;

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
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
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.BOTH)
    public ThrowableItems throwableItems = new ThrowableItems();

    public static class ThrowableItems {

        @ConfigEntry.Category("mechanics")
        public boolean enable = false;

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public List<String> blacklist = Lists.newArrayList();

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean canZombiesThrowItems = true;

        @ConfigEntry.Category("mechanics")
        @ConfigEntry.Gui.Tooltip
        public int zombieThrowInterval = 40;

    }

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.Tooltip(count = 3)
    @FeatureEnvironment(Environment.SERVER)
    public boolean tradingGoatHorn = true;

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.CLIENT)
    public boolean tooltipNotName = false;

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.CLIENT)
    public boolean noMoreAdventure = false;

    @ConfigEntry.Category("gui")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.CLIENT)
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
    @FeatureEnvironment(Environment.CLIENT)
    public boolean itemFrameTooltips = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.SERVER)
    public boolean damageBackport = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.CollapsibleObject
    @FeatureEnvironment(Environment.SERVER)
    public AutoGenRecipes recipeAdvancementsGeneration = new AutoGenRecipes();

    public static class AutoGenRecipes {
        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        @FeatureEnvironment(Environment.SERVER)
        public boolean enable = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public boolean requireAllItems = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public boolean ignoreRecipesHiddenInTheRecipeBook = true;

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public List<String> namespaceBlacklist = Arrays.asList("minecraft", "andromeda", "extshape");

        @ConfigEntry.Category("misc")
        @ConfigEntry.Gui.Tooltip
        public List<String> recipeBlacklist = Arrays.asList();
    }

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @FeatureEnvironment(Environment.BOTH)
    public boolean minorInconvenience = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    public boolean compatMode = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip()
    @FeatureEnvironment(Environment.CLIENT)
    public boolean autoUpdateTranslations = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 2)
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.CLIENT)
    public boolean sendOptionalData = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean sendCrashReports = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    public boolean debugMessages = false;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.RequiresRestart
    public boolean enableFeatureManager = true;

    @ConfigEntry.Category("misc")
    @ConfigEntry.Gui.RequiresRestart
    @FeatureEnvironment(Environment.BOTH)
    public boolean unknown = false;
}
