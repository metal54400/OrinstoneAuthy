package fr.orinstone.authy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
public class Orinstoneauthy extends JavaPlugin {
    private static Orinstoneauthy instance;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        File dataFolder = new File(getDataFolder(), "userdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        getCommand("register").setExecutor(new AuthCommands());
        getCommand("login").setExecutor(new AuthCommands());
        getCommand("unregister").setExecutor(new AuthCommands());
        getCommand("remember").setExecutor(new AuthCommands());
        getCommand("resetpassword").setExecutor(new AuthCommands());
        getCommand("orinstoneadmin").setExecutor(new AuthCommands());
        getServer().getPluginManager().registerEvents(new AuthEvents(), this);
    }
    public static String format(String text) {
        if (text == null) return "";
        String prefix = instance.getConfig().getString("messages.prefix");
        String normal    = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String smallCaps = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = normal.indexOf(c);
            sb.append(idx >= 0 ? smallCaps.charAt(idx) : c);
        }
        return ChatColor.translateAlternateColorCodes('&', prefix + sb.toString());
    }
    public static Orinstoneauthy getInstance() { return instance; }
}
