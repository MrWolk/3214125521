package ru.allin.allinshop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class Shop {
    public final Location location;
    public ShopType type;
    public ItemStack item;
    public int amount;
    public double sellPrice;
    public double buyPrice;

    public Shop(Location location, ShopType type, ItemStack item, int amount, double sellPrice, double buyPrice) {
        this.location = location;
        this.type = type;
        this.item = item;
        this.amount = amount;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }
}
