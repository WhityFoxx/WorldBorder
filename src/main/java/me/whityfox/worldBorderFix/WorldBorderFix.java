package me.whityfox.worldBorderFix;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.scheduler.BukkitRunnable;




public class WorldBorderFix extends JavaPlugin implements Listener {
    private static final double LIMIT = 600;
    private static final double DAMAGE_LIMIT = 100;
    private static final double FIRST_LAYER = 600;
    private static final double SECOND_LAYER = 500;
    private static final double THIRD_LAYER = 400;
    private static final double FOURTH_LAYER = 300;
    private static final double FIFTH_LAYER = 200;

    @Override
    public void onEnable() {
        System.out.println("The plugin WORLDBORDER BY WHITYFOX has just started!");
        getServer().getPluginManager().registerEvents(this, this);
    }
    @EventHandler
    public void onPotionEffectEnd(EntityPotionEffectEvent event) {
        if (event.getCause() == EntityPotionEffectEvent.Cause.MILK) {

            Player player = (Player) event.getEntity();
            if (isNearBorder(player, LIMIT)){
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    dealallEffects(player);
                }, 1L);
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        PotionEffect player_effect = player.getPotionEffect(PotionEffectType.HUNGER);
        if (isNearBorder(player, DAMAGE_LIMIT)){
            dealDamage(player);
        }
        if (isNearBorder(player, LIMIT)){
            dealallEffects(player);
        }
        if (player_effect != null){
            if (!isNearBorder(player, FIRST_LAYER)){
                player.removePotionEffect(PotionEffectType.HUNGER);
            }
            if (!isNearBorder(player, SECOND_LAYER)){
                player.removePotionEffect(PotionEffectType.SLOW);
            }
            if (!isNearBorder(player, THIRD_LAYER)){
                player.removePotionEffect(PotionEffectType.CONFUSION);
            }
            if (!isNearBorder(player, FOURTH_LAYER)){
                player.removePotionEffect(PotionEffectType.WITHER);
            }
            if (!isNearBorder(player, FIFTH_LAYER)){
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }

        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();


        if (isNearBorder(player, 500)) {
            event.setDeathMessage(player.getName() + " погиб, пытаясь пересечь границу мира.");
        } else {
            event.setDeathMessage(player.getName() + " умер при неизвестных обстоятельствах.");
        }
    }
    private boolean isNearBorder(Player player, double cur_lim) {
        WorldBorder worldBorder = player.getWorld().getWorldBorder();
        Location centerCoords = worldBorder.getCenter();
        Location playerLocation = player.getLocation();
        double borderSize = worldBorder.getSize();
        double xDiff = Math.abs(centerCoords.getX() - playerLocation.getX());
        double zDiff = Math.abs(centerCoords.getZ() - playerLocation.getZ());
        return (((borderSize / 2) - xDiff) <= cur_lim) | (((borderSize / 2) - zDiff) <= cur_lim);

    }
    private void LayerEffect(Player player, PotionEffectType layer_effect_type, double layer_condition) {
        if (isNearBorder(player, layer_condition)) {
            player.addPotionEffect(new PotionEffect(layer_effect_type, -1, 1, true, false));
        }
    }

    private void dealallEffects(Player player){
        LayerEffect(player, PotionEffectType.HUNGER, FIRST_LAYER);
        LayerEffect(player, PotionEffectType.SLOW, SECOND_LAYER);
        LayerEffect(player, PotionEffectType.CONFUSION, THIRD_LAYER);
        LayerEffect(player, PotionEffectType.WITHER, FOURTH_LAYER);
        LayerEffect(player, PotionEffectType.BLINDNESS, FIFTH_LAYER);
    }

    private void dealDamage(Player player) {
        // Наносим урон игроку, если он за пределами границы
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isNearBorder(player, DAMAGE_LIMIT)) {
                    player.damage(3.0); // Наносим 1 сердечко урона каждые 20 тиков
                } else {
                    cancel(); // Останавливаем задачу, если игрок вернулся в безопасную зону
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Запускаем задачу с периодичностью 1 секунда (20 тиков)
    }
}
