package ru.allin.allinshop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public final class ALLINShopPlugin extends JavaPlugin {
    private Economy economy;
    private ShopDatabase db;
    private final Map<String, Shop> shops = new HashMap<>();

    @Override public void onEnable() {
        saveDefaultConfig();
        RegisteredServiceProvider<Economy> r = getServer().getServicesManager().getRegistration(Economy.class);
        if (r == null) { getLogger().severe("Vault economy provider not found"); getServer().getPluginManager().disablePlugin(this); return; }
        economy = r.getProvider();
        try {
            getDataFolder().mkdirs(); db = new ShopDatabase(this); db.open(); shops.putAll(db.loadAll());
        } catch (SQLException e) { getLogger().severe(e.getMessage()); getServer().getPluginManager().disablePlugin(this); return; }
        ShopCommand cmd = new ShopCommand(this); getCommand("ashop").setExecutor(cmd); getCommand("ashop").setTabCompleter(cmd);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getScheduler().runTaskLater(this, () -> shops.values().forEach(this::refreshSign), 20L);
    }

    @Override public void onDisable() { try { if (db != null) db.close(); } catch (SQLException ignored) {} }
    public Economy economy(){ return economy; }
    public Shop get(Location l){ return shops.get(ShopDatabase.key(l)); }
    public Collection<Shop> all(){ return shops.values(); }
    public void save(Shop s) throws SQLException { shops.put(ShopDatabase.key(s.location), s); db.save(s); refreshSign(s); }
    public void remove(Location l) throws SQLException { shops.remove(ShopDatabase.key(l)); db.delete(l); }

    public String msg(String k) { return color(getConfig().getString("messages.prefix", "") + getConfig().getString("messages."+k, k)); }
    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    public static String money(double d) { return Math.rint(d)==d ? Long.toString((long)d) : String.format(Locale.US,"%.2f",d); }

    public void refreshSign(Shop s) {
        if (!(s.location.getBlock().getState() instanceof Sign sign)) return;
        sign.setLine(0, "[ALLIN SHOP]");
        sign.setLine(1, s.item.getType().name()+" x"+s.amount);
        sign.setLine(2, s.type==ShopType.BUY ? "BUY $"+money(s.buyPrice) : "SELL $"+money(s.sellPrice));
        sign.setLine(3, s.type==ShopType.BOTH ? "BUY $"+money(s.buyPrice) : "RIGHT CLICK");
        sign.update(true, false);
    }
}
