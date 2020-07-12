package ptgms.industries.fears;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class PlayerController implements Listener {
    public PlayerController(Fears plugin) {
        getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static Fears thisPlugin = JavaPlugin.getPlugin(Fears.class);

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            PlayerWrapper w = PlayerWrapper.findPlayer((Player) e);
            if (e.getDamage() >= 8 && !e.getEntity().isDead()) {
                e.getEntity().sendMessage(ChatColor.RED + "Critical Damage!" + ChatColor.WHITE +
                        " You are losing blood! Patch up your Wound with Leather/Paper/Kelp as soon as possible," +
                        " otherwise you will die!");
                w.bleeding = true;
                w.basePlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100000000,
                        0, false, false));
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {

        PlayerWrapper w = PlayerWrapper.findPlayer(e.getPlayer());

        w.bleeding = false;
        e.getPlayer().sendMessage("You respawned. All your Fears have been randomized.");
        Random rand = new Random();
        w.mobFear = ScaryMobs.values()[rand.nextInt(10)]; //0 inclusive, 10 exclusive. This is to avoid NONE.
        w.heightFear = rand.nextBoolean();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        PlayerWrapper w = PlayerWrapper.findPlayer(e.getEntity());

        if (w.bleeding) {
            if (Objects.requireNonNull(e.getDeathMessage()).endsWith("withered away")) {
                e.setDeathMessage(w.basePlayer.getDisplayName() + " died of blood loss");
                w.bleeding = false;
            }
        }
        if (w.beacon) {
            for (ItemStack i : Objects.requireNonNull(e.getEntity().getPlayer()).getInventory()) {
                if (i == null) {
                    continue;
                }
                if (i.getType() == Material.NETHER_STAR) {
                    ItemStack star = new ItemStack(Material.NETHER_STAR, 1);
                    ItemMeta starMeta = star.getItemMeta();
                    i.setItemMeta(starMeta);
                }
            }
            for (PlayerWrapper p : thisPlugin.players) {
                if (p.beacon) {
                    if (!p.basePlayer.isDead()) {
                        p.basePlayer.setHealth(0);
                    }
                }
            }
            w.beacon = false;
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        PlayerWrapper w = PlayerWrapper.findPlayer(event.getPlayer());

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack main = w.basePlayer.getInventory().getItemInMainHand();

        if (w.bleeding) {
            if (main.getType() == Material.PAPER ||
                    main.getType() == Material.KELP ||
                    main.getType() == Material.DRIED_KELP ||
                    main.getType() == Material.LEATHER) {
                w.basePlayer.sendMessage("You fixed up your wound with " +
                        main.getType().name().replace("_", " ").toLowerCase());
                w.basePlayer.playSound(event.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                w.bleeding = false;
                w.basePlayer.removePotionEffect(PotionEffectType.WITHER);
                main.setAmount(main.getAmount() - 1);
            }
        }

        if (main.getType() == Material.NETHER_STAR) {
            if (Objects.requireNonNull(main.getItemMeta()).getLore() != null) {
                //System.out.println("Got here!");
                if (w.beacon) {
                    w.beacon = false;
                    event.getPlayer().sendMessage("You deactivated your beacon!");
                } else {
                    w.beacon = true;
                    event.getPlayer().sendMessage("You activated your beacon!");
                }
            }
        }
    }

    @EventHandler
    public void PlayerItemConsumeEvent(PlayerItemConsumeEvent event) {

        PlayerWrapper w = PlayerWrapper.findPlayer(event.getPlayer());

        if (event.getItem().getType() == Material.MILK_BUCKET) {
            if (w.basePlayer.hasPotionEffect(PotionEffectType.CONFUSION)) {
                w.basePlayer.sendMessage("You drank milk while having nausea. You threw up.");
                if (w.basePlayer.getHealth() <= 10) {
                    w.basePlayer.setHealth(1);
                } else {
                    w.basePlayer.setHealth(event.getPlayer().getHealth() - 10);
                }
            }
        }
        if (w.bleeding) {
            w.basePlayer.sendMessage("You drank milk while bleeding out! This made you throw up blood and you died!");
            w.basePlayer.setHealth(0);
        }

        ItemStack item = event.getItem();
        if (item.getItemMeta() != null && item.getItemMeta() instanceof PotionMeta) {
            PotionType potionType = ((PotionMeta) item.getItemMeta()).getBasePotionData().getType();
            if (potionType == PotionType.WATER) {
                if (w.inWarmBiome) {
                    if (w.warmBiomeTime <= 50) {
                        w.warmBiomeTime = 0;
                        w.basePlayer.removePotionEffect(PotionEffectType.CONFUSION);
                        w.basePlayer.removePotionEffect(PotionEffectType.SLOW);
                        w.basePlayer.removePotionEffect(PotionEffectType.WEAKNESS);
                    } else {
                        if (w.warmBiomeTime - 50 <= 99) {
                            w.basePlayer.removePotionEffect(PotionEffectType.CONFUSION);
                            w.basePlayer.removePotionEffect(PotionEffectType.SLOW);
                            w.basePlayer.removePotionEffect(PotionEffectType.WEAKNESS);
                        }
                        w.warmBiomeTime = w.warmBiomeTime - 50;
                    }
                } else {
                    w.basePlayer.removePotionEffect(PotionEffectType.WITHER);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {

        //try to read a player
        PlayerWrapper w = thisPlugin.db.readPlayer(event.getPlayer());
        thisPlugin.players.add(w);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        ArrayList<PlayerWrapper> players = thisPlugin.players;
        for(int i = 0; i < players.size(); i++)
        {
            if(players.get(i).basePlayer == event.getPlayer())
            {
                thisPlugin.db.writePlayer(players.get(i));
                players.remove(i);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        ArrayList<PlayerWrapper> players = thisPlugin.players;
        for(int i = 0; i < players.size(); i++)
        {
            if(players.get(i).basePlayer == event.getPlayer())
            {
                thisPlugin.db.writePlayer(players.get(i));
                players.remove(i);
                return;
            }
        }
    }
}
