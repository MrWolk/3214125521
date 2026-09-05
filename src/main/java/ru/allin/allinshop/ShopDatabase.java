package ru.allin.allinshop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.*;

public final class ShopDatabase implements AutoCloseable {
    private final ALLINShopPlugin plugin;
    private Connection connection;

    public ShopDatabase(ALLINShopPlugin plugin) { this.plugin = plugin; }

    public void open() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath().resolve("shops.db"));
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS shops (world TEXT NOT NULL,x INTEGER NOT NULL,y INTEGER NOT NULL,z INTEGER NOT NULL,type TEXT NOT NULL,item TEXT NOT NULL,amount INTEGER NOT NULL,sell_price REAL NOT NULL,buy_price REAL NOT NULL,PRIMARY KEY(world,x,y,z))");
        }
    }

    public Map<String, Shop> loadAll() throws SQLException {
        Map<String, Shop> map = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM shops"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                World w = Bukkit.getWorld(rs.getString("world"));
                if (w == null) continue;
                Location l = new Location(w, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
                Shop s = new Shop(l, ShopType.valueOf(rs.getString("type")), decode(rs.getString("item")), rs.getInt("amount"), rs.getDouble("sell_price"), rs.getDouble("buy_price"));
                map.put(key(l), s);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return map;
    }

    public void save(Shop s) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO shops(world,x,y,z,type,item,amount,sell_price,buy_price) VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(world,x,y,z) DO UPDATE SET type=excluded.type,item=excluded.item,amount=excluded.amount,sell_price=excluded.sell_price,buy_price=excluded.buy_price")) {
            ps.setString(1, s.location.getWorld().getName());
            ps.setInt(2, s.location.getBlockX()); ps.setInt(3, s.location.getBlockY()); ps.setInt(4, s.location.getBlockZ());
            ps.setString(5, s.type.name()); ps.setString(6, encode(s.item)); ps.setInt(7, s.amount);
            ps.setDouble(8, s.sellPrice); ps.setDouble(9, s.buyPrice); ps.executeUpdate();
        } catch (IOException e) { throw new SQLException(e); }
    }

    public void delete(Location l) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM shops WHERE world=? AND x=? AND y=? AND z=?")) {
            ps.setString(1, l.getWorld().getName()); ps.setInt(2, l.getBlockX()); ps.setInt(3, l.getBlockY()); ps.setInt(4, l.getBlockZ()); ps.executeUpdate();
        }
    }

    public static String key(Location l) { return l.getWorld().getUID()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ(); }

    private static String encode(ItemStack i) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream o = new BukkitObjectOutputStream(b)) { o.writeObject(i); }
        return Base64.getEncoder().encodeToString(b.toByteArray());
    }
    private static ItemStack decode(String s) throws IOException, ClassNotFoundException {
        try (BukkitObjectInputStream i = new BukkitObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(s)))) { return (ItemStack)i.readObject(); }
    }
    public void close() throws SQLException { if (connection != null && !connection.isClosed()) connection.close(); }
}
