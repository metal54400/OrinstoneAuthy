package fr.orinstone.authy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
public class AuthEvents implements Listener {
    public static final HashSet<UUID> authenticated = new HashSet<>();
    private static final Map<UUID, Location> previousLocations = new HashMap<>();
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        previousLocations.put(uuid, player.getLocation());
        teleportToAuth(player);
        File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", uuid + ".dat");
        if (file.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            boolean remember = cfg.getBoolean("remember-me", false);
            long last = cfg.getLong("last-login", 0);
            if (remember && (System.currentTimeMillis() - last) < (3L * 24 * 60 * 60 * 1000)) {
                player.sendMessage(Orinstoneauthy.format("Bon retour ! Connexion auto."));
                completeAuth(player, false);
                return;
            }
            player.sendMessage(Orinstoneauthy.format("Veuillez vous connecter avec /login."));
        } else {
            player.sendMessage(Orinstoneauthy.format("Veuillez vous enregistrer avec /register."));
        }
    }
    public static void completeAuth(Player player, boolean isNew) {
        UUID uuid = player.getUniqueId();
        authenticated.add(uuid);
        Location dest;
        if (isNew) {
            dest = getNewPlayerSpawn();
        } else {
            dest = previousLocations.getOrDefault(uuid, player.getWorld().getSpawnLocation());
        }
        player.teleport(dest);
        previousLocations.remove(uuid);
        File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", uuid + ".dat");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("last-login", System.currentTimeMillis());
        try { cfg.save(file); } catch (IOException e) {}
    }
    private void teleportToAuth(Player player) {
        FileConfiguration config = Orinstoneauthy.getInstance().getConfig();
        World world = Bukkit.getWorld(config.getString("auth-location.world", "world"));
        player.teleport(new Location(world, config.getDouble("auth-location.x"), config.getDouble("auth-location.y"), config.getDouble("auth-location.z")));
    }
    private static Location getNewPlayerSpawn() {
        FileConfiguration config = Orinstoneauthy.getInstance().getConfig();
        World world = Bukkit.getWorld(config.getString("new-player-spawn.world", "world"));
        return new Location(world, config.getDouble("new-player-spawn.x"), config.getDouble("new-player-spawn.y"), config.getDouble("new-player-spawn.z"));
    }
    @EventHandler public void onMove(PlayerMoveEvent e) { if (!authenticated.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onChat(AsyncPlayerChatEvent e) { if (!authenticated.contains(e.getPlayer().getUniqueId())) e.setCancelled(true); }
    @EventHandler public void onDamage(EntityDamageEvent e) { if (e.getEntity() instanceof Player && !authenticated.contains(e.getEntity().getUniqueId())) e.setCancelled(true); }
}
