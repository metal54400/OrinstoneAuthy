package fr.orinstone.authy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
public class AuthCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("orinstoneadmin")) {
            if (!sender.hasPermission("orinstoneauthy.admin")) {
                sender.sendMessage(Orinstoneauthy.format("Vous n'avez pas la permission."));
                return true;
            }
            if (args.length < 3 || !args[0].equalsIgnoreCase("reset")) {
                sender.sendMessage(msg("admin-reset-usage"));
                return true;
            }
            String targetName = args[1];
            String newPass = args[2];
            Player target = Bukkit.getPlayer(targetName);
            UUID uuid = (target != null) ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
            if (target != null) {
                target.sendMessage(Orinstoneauthy.format("Votre mot de passe a été réinitialisé par un admin."));
                target.sendMessage(Orinstoneauthy.format("Nouveau mot de passe: " + newPass));
            }
            saveUser(uuid, newPass, false);
            sender.sendMessage(msg("admin-success-reset").replace("{player}", targetName));
            return true;
        }
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (label.equalsIgnoreCase("register")) {
            if (args.length < 2) { p.sendMessage(msg("register-usage")); return true; }
            saveUser(p.getUniqueId(), args[0], false);
            p.sendMessage(msg("success-register"));
            AuthEvents.completeAuth(p, true);
            logToAdmins("register", p.getName());
            return true;
        }
        if (label.equalsIgnoreCase("login")) {
            File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", p.getUniqueId() + ".dat");
            if (!file.exists()) { p.sendMessage(msg("not-registered")); return true; }
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            if (cfg.getString("password").equals(args[0])) {
                p.sendMessage(msg("success-login"));
                AuthEvents.completeAuth(p, false);
                logToAdmins("login", p.getName());
            } else { p.sendMessage(msg("wrong-password")); }
            return true;
        }
        if (label.equalsIgnoreCase("remember")) {
            toggleRemember(p);
            logToAdmins("remember", p.getName());
            return true;
        }
        if (label.equalsIgnoreCase("resetpassword")) {
            if (args.length < 2) { p.sendMessage(msg("reset-usage")); return true; }
            saveUser(p.getUniqueId(), args[1], true);
            p.sendMessage(Orinstoneauthy.format("Votre nouveau mot de passe est: " + args[1]));
            p.sendMessage(msg("success-reset"));
            logToAdmins("reset", p.getName());
            return true;
        }
        if (label.equalsIgnoreCase("unregister")) {
            File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", p.getUniqueId() + ".dat");
            if(file.exists()) file.delete();
            AuthEvents.authenticated.remove(p.getUniqueId());
            p.sendMessage(Orinstoneauthy.format("Compte supprimé."));
            logToAdmins("unregister", p.getName());
            return true;
        }
        return true;
    }
    private String msg(String path) {
        String raw = Orinstoneauthy.getInstance().getConfig().getString("messages." + path);
        return Orinstoneauthy.format(raw);
    }
    private void logToAdmins(String type, String playerName) {
        String logMsg = ChatColor.translateAlternateColorCodes('&', Orinstoneauthy.getInstance().getConfig().getString("admin-logs." + type).replace("{player}", playerName));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("orinstoneauthy.admin")) {
                online.sendMessage(logMsg);
            }
        }
    }
    private void saveUser(UUID uuid, String pass, boolean keepRemember) {
        File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", uuid + ".dat");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.set("password", pass);
        if (!keepRemember) cfg.set("remember-me", false);
        cfg.set("last-login", System.currentTimeMillis());
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
    private void toggleRemember(Player p) {
        File file = new File(Orinstoneauthy.getInstance().getDataFolder() + "/userdata/", p.getUniqueId() + ".dat");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        boolean state = !cfg.getBoolean("remember-me", false);
        cfg.set("remember-me", state);
        try { cfg.save(file); } catch (IOException e) {}
        p.sendMessage(state ? msg("remember-on") : msg("remember-off"));
    }
}
