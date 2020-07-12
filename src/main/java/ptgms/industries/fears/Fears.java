package ptgms.industries.fears;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class Fears extends JavaPlugin {

    FearsDatabase db;
    ArrayList<PlayerWrapper> players;

    @Override
    public void onEnable() {
        new PlayerController(this);
        addCustomRecipes();
        db = new FearsDatabase("fears.db");
        players = new ArrayList<>();
        addOnlinePlayers();
        scheduleTasks();
    }

    @Override
    public void onDisable() {
        for(PlayerWrapper player : players)
        {
            db.writePlayer(player);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("setfear")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Sorry, you can't run this command!");
                return false;
            }
            if (args.length != 3) {
                sender.sendMessage(ChatColor.YELLOW +"Not enough arguments!");
                return false;
            }

            PlayerWrapper w = PlayerWrapper.findPlayer(Bukkit.getPlayer(args[0]));
            if(w == null)
            {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return false;
            }
            if (args[1].equals("heights")) {
                w.heightFear = "true".equals(args[2]);
                sender.sendMessage(ChatColor.GREEN + "Set fear of heights to " + w.heightFear);
                return true;

            } else if (args[1].equals("mob")) {
                try
                {
                    w.mobFear = ScaryMobs.valueOf(args[2]);
                    return true;
                }
                catch (IllegalArgumentException e)
                {
                    StringBuilder sb = new StringBuilder();
                    for(ScaryMobs mob : ScaryMobs.values())
                    {
                        sb.append(mob.name() + "\n");
                    }
                    sender.sendMessage( ChatColor.YELLOW + "Mob \"" + args[2] + "\" not found, " +
                            "the options are \n" + sb.toString());
                }
                return false;
            }
        }

        if (label.equalsIgnoreCase("getfear")) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Sorry, you can't run this command!");
                return false;
            }
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.YELLOW + "Not enough arguments!");
                return false;
            }

            PlayerWrapper w = PlayerWrapper.findPlayer(Bukkit.getPlayer(args[0]));
            if(w == null)
            {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return false;
            }

            switch (args[1]) {
                case "heights":
                    sender.sendMessage(ChatColor.GREEN + w.basePlayer.getDisplayName() +
                            "'s fear of heights is set to " + w.heightFear);
                    return true;
                case "mobs":
                    sender.sendMessage(ChatColor.GREEN + w.basePlayer.getDisplayName() +
                            " is scared of " + w.mobFear.name() + "s!");
                    return true;
            }
        }

        if (label.equalsIgnoreCase("localbeacon")) {
            if (args.length != 1) {
                sender.sendMessage("This command will activate the " + ChatColor.LIGHT_PURPLE + "PORTABLE BEACON" +
                        ChatColor.WHITE + ". You need an Nether Star for this. The effect you get is randomized and if" +
                        " other players also having the local beacon die, you have a chance of dying as well. When you" +
                        " die, the buff gets removed and you have to randomize it again. Make sure you only have " +
                        ChatColor.RED + "ONE" + ChatColor.WHITE + " Nether Star in your inventory while you confirm.");
                sender.sendMessage("Type in \"/localbeacon confirm\" to activate.");
                return false;
            }
            if (args[0].equals("confirm")) {

                PlayerWrapper w = PlayerWrapper.findPlayer(Bukkit.getPlayer(args[0]));
                if(w == null)
                {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return false;
                }

                ItemStack netherstar = new ItemStack(Material.NETHER_STAR);
                ItemMeta netherstarMeta = netherstar.getItemMeta();
                netherstar.setItemMeta(netherstarMeta);
                Player player = (Player) sender;
                if (player.getInventory().contains(netherstar, 1)) {
                    player.getInventory().remove(netherstar);
                    player.getInventory().addItem(makeBeacon(player.getName()));
                    sender.sendMessage("You now own an " + ChatColor.LIGHT_PURPLE + "PORTABLE BEACON"
                            + ChatColor.WHITE + ". Interact with it to activate it.");
                    w.beacon = true;
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You aren't carrying a netherstar!");
                    return false;
                }
            }
        }
        return false;
    }

    public ItemStack makeBeacon(String name) {
        ItemStack star = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta starMeta = star.getItemMeta();
        assert starMeta != null;
        starMeta.setDisplayName(name + "'s portable Beacon");
        starMeta.setLocalizedName("Portable Beacon");
        Random rand = new Random();
        int int_random = rand.nextInt(7);
        switch (int_random) {
            case 0:
                starMeta.setLore(Collections.singletonList("Regeneration"));
                break;
            case 1:
                starMeta.setLore(Collections.singletonList("Haste"));
                break;
            case 2:
                starMeta.setLore(Collections.singletonList("Speed"));
                break;
            case 3:
                starMeta.setLore(Collections.singletonList("Strength"));
                break;
            case 4:
                starMeta.setLore(Collections.singletonList("Health Boost"));
                break;
            case 5:
                starMeta.setLore(Collections.singletonList("Resistance"));
                break;
            case 6:
                starMeta.setLore(Collections.singletonList("Jump Boost"));
                break;
        }
        star.setItemMeta(starMeta);
        return star;
    }

    public void addCustomRecipes() {
        //region Nether Star
        ItemStack nStar = new ItemStack(Material.NETHER_STAR, 1);
        ShapedRecipe netherStar = new ShapedRecipe(new NamespacedKey(this, UUID.randomUUID().toString()), nStar);
        netherStar.shape("% %", " * ", "% %");
        netherStar.setIngredient('*', Material.GOLD_BLOCK);
        netherStar.setIngredient('%', Material.DIAMOND_BLOCK);
        getServer().addRecipe(netherStar);
        //endregion

        //region Notch Apple
        ItemStack nApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        ItemMeta nAppleMeta = nApple.getItemMeta();
        assert nAppleMeta != null;
        nAppleMeta.setDisplayName("Notch Apple");
        nAppleMeta.setLocalizedName("Notch Apple");
        nAppleMeta.setLore(Collections.singletonList("...or enchanted Apple if you're young."));
        nApple.setItemMeta(nAppleMeta);
        ShapedRecipe notchApple = new ShapedRecipe(new NamespacedKey(this, UUID.randomUUID().toString()), nApple);
        notchApple.shape("%%%", "%*%", "%%%");
        notchApple.setIngredient('*', Material.APPLE);
        notchApple.setIngredient('%', Material.GOLD_BLOCK);
        getServer().addRecipe(notchApple);
        //endregion
    }

    public void scheduleTasks() {
        //apply portable beacon
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (PlayerWrapper player : players) {
                String potiontype = "";
                PotionEffectType type;
                if (player.beacon) {
                    for (ItemStack i : player.basePlayer.getInventory()) {
                        if (i == null) {
                            continue;
                        }
                        if (i.getType() == Material.NETHER_STAR) {
                            if (Objects.requireNonNull(i.getItemMeta()).getLocalizedName().equals("Portable Beacon")) {
                                potiontype = Objects.requireNonNull(i.getItemMeta().getLore()).get(0);
                                break;
                            }
                        }
                    }
                    switch (potiontype) {
                        case "Regeneration":
                            type = PotionEffectType.REGENERATION;
                            break;
                        case "Haste":
                            type = PotionEffectType.FAST_DIGGING;
                            break;
                        case "Speed":
                            type = PotionEffectType.SPEED;
                            break;
                        case "Strength":
                            type = PotionEffectType.INCREASE_DAMAGE;
                            break;
                        case "Health Boost":
                            type = PotionEffectType.HEALTH_BOOST;
                            break;
                        case "Resistance":
                            type = PotionEffectType.DAMAGE_RESISTANCE;
                            break;
                        case "Jump Boost":
                            type = PotionEffectType.JUMP;
                            break;
                        default:
                            return;
                    }
                    player.basePlayer.addPotionEffect(new PotionEffect(type, 100, 0,
                            false, false));
                }
            }
        }, 20, 20);

        //apply warm biome
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (PlayerWrapper player : players) {

                Location loc = player.basePlayer.getLocation();
                World world = player.basePlayer.getWorld();
                Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                if (biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.DESERT_LAKES) {
                    player.inWarmBiome = true;
                } else {
                    player.inWarmBiome = false;
                    player.warmBiomeTime = 0;
                }

                if(player.inWarmBiome && player.basePlayer.getLocation().getY() >= 50)
                {
                    player.warmBiomeTime++;
                    if(player.warmBiomeTime >= 100)
                    {
                        if (player.warmBiomeTime == 100) {
                            player.basePlayer.sendMessage("You were in a warm biome for too long without " +
                                    "drinking water, you got a sunstroke!");
                        }
                        player.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 2000,
                                1, false, false));
                        player.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2000,
                                0, false, false));
                        player.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2000,
                                0, false, false));
                    }
                    if (player.warmBiomeTime >= 500) {
                        player.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100,
                                1, false, false));
                    }
                }
            }

        }, 20, 20);

        //apply fears
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(PlayerWrapper w : players) {
                if (w.heightFear) {
                    if (w.basePlayer.getLocation().getY() >= 120 && !w.heightFearActive) {
                        w.heightFearActive = true;
                        w.basePlayer.sendMessage(ChatColor.RED + "You are scared of heights! " +
                                "Get below Y-Level 120 as soon as possible!");
                        w.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100000000,
                                0, false, false));
                    } else if (w.basePlayer.getLocation().getY() <= 119 && w.heightFearActive) {
                        w.heightFearActive = false;
                        w.basePlayer.removePotionEffect(PotionEffectType.CONFUSION);
                    }
                }
                if (w.mobFear != ScaryMobs.NONE) {
                    List<Entity> entities = w.basePlayer.getNearbyEntities(5, 5, 5);
                    if (!w.mobFearActive) {
                        for (Entity entity : entities) {
                            if (entity.getType().ordinal() == w.mobFear.getValue()) {
                                w.basePlayer.sendMessage(ChatColor.RED + "You are scared of " +
                                        w.mobFear.toString().replace("_", " ") +
                                        "S! Kill it or get away as soon as possible!");
                                w.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,
                                        1000000, 0, false, false));
                                w.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
                                        1000000, 0, false, false));
                                w.mobFearActive = true;
                            }
                        }
                    } else {
                        for (Entity entity : entities) {
                            if (entity.getType().ordinal() == w.mobFear.getValue()) {
                                return;
                            }
                        }
                        w.basePlayer.removePotionEffect(PotionEffectType.CONFUSION);
                        w.basePlayer.removePotionEffect(PotionEffectType.WEAKNESS);
                        w.mobFearActive = false;
                    }
                }
            }
        }, 20, 20);
    }

    public void addOnlinePlayers() {
        for (Player p : getServer().getOnlinePlayers())
        {
            players.add(db.readPlayer(p));
        }
    }
}
