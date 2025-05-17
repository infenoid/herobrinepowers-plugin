package org.example.test;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HerobrinePowers extends JavaPlugin implements Listener {
    private final HashSet<Player> herobrinePlayers = new HashSet<>();
    private final HashMap<Player, BukkitTask> herobrineTasks = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("herobrine")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                if (herobrinePlayers.contains(player)) {
                    herobrinePlayers.remove(player);
                    player.sendMessage("§cHerobrine mode disabled.");
                    resetPlayerAppearance(player);
                } else {
                    herobrinePlayers.add(player);
                    player.sendMessage("§aHerobrine mode enabled!");
                    setHerobrineAppearance(player);
                }
            }
            return true;
        });
    }

    private void setHerobrineAppearance(Player player) {
        player.setPlayerListName("§f");
        player.sendMessage("§cYou have become Herobrine!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1f, 0.5f);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !herobrinePlayers.contains(player)) {
                    cancel();
                    return;
                }
                Location eyeLocation = player.getEyeLocation();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.equals(player)) {
                        online.spawnParticle(Particle.END_ROD, eyeLocation, 2, new Particle.DustOptions(Color.WHITE, 1));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 10L);
        herobrineTasks.put(player, task);
    }

    private void resetPlayerAppearance(Player player) {
        if (herobrineTasks.containsKey(player)) {
            herobrineTasks.get(player).cancel();
            herobrineTasks.remove(player);
        }
        player.setPlayerListName(player.getName());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!herobrinePlayers.contains(player)) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
            Location loc = event.getClickedBlock().getLocation();
            loc.getWorld().strikeLightning(loc);
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR && player.getInventory().getItemInMainHand().getType() == Material.NETHERITE_SWORD) {
            Location loc = player.getLocation();
            World world = player.getWorld();
            Location targetLoc = loc.clone().add(loc.getDirection().multiply(5));
            targetLoc.setY(world.getHighestBlockYAt(targetLoc) + 1);

            if (targetLoc.getBlock().getType().isSolid()) {
                player.sendMessage("§cTeleportation failed! Unsafe landing.");
                return;
            }

            player.teleport(targetLoc);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (herobrinePlayers.contains(player)) {
            if (!player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1, false, false));
            } else {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }
}
