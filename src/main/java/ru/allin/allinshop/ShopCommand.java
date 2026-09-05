package ru.allin.allinshop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public final class ShopCommand implements CommandExecutor, TabCompleter {
    private final ALLINShopPlugin p;
    public ShopCommand(ALLINShopPlugin p){ this.p=p; }

    @Override public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player pl)) { s.sendMessage(p.msg("player-only")); return true; }
        if (!pl.hasPermission("allinshop.admin")) { pl.sendMessage(p.msg("no-permission")); return true; }
        if (a.length==0) { help(pl); return true; }
        if (a[0].equalsIgnoreCase("reload")) { p.reloadConfig(); p.all().forEach(p::refreshSign); pl.sendMessage(p.msg("updated")); return true; }
        Block b = pl.getTargetBlockExact(6);
        if (b==null || !(b.getState() instanceof Sign)) { pl.sendMessage(p.msg("look-at-sign")); return true; }
        try {
            switch(a[0].toLowerCase()) {
                case "create" -> create(pl,b,a);
                case "remove" -> remove(pl,b);
                case "info" -> info(pl,b);
                case "setbuy" -> setBuy(pl,b,a);
                case "setsell" -> setSell(pl,b,a);
                case "setamount" -> setAmount(pl,b,a);
                case "setitem" -> setItem(pl,b);
                default -> help(pl);
            }
        } catch (Exception e) { pl.sendMessage(p.color("&cОшибка: "+e.getMessage())); }
        return true;
    }

    private void create(Player pl, Block b, String[] a) throws SQLException {
        if (a.length<4) { pl.sendMessage(p.color("&e/ashop create <sell|buy|both> <кол-во> <цена> [цена покупки]")); return; }
        ShopType t=ShopType.parse(a[1]); if(t==null){ pl.sendMessage(p.color("&cТип: sell, buy или both")); return; }
        int amount=Integer.parseInt(a[2]); if(amount<=0) throw new IllegalArgumentException("Количество должно быть > 0");
        ItemStack held=pl.getInventory().getItemInMainHand(); if(held.getType()==Material.AIR){ pl.sendMessage(p.msg("hold-item")); return; }
        double sell=0,buy=0;
        if(t==ShopType.SELL) sell=price(a[3]);
        else if(t==ShopType.BUY) buy=price(a[3]);
        else { if(a.length<5){ pl.sendMessage(p.color("&e/ashop create both <кол-во> <sell> <buy>")); return;} sell=price(a[3]); buy=price(a[4]); }
        ItemStack item=held.clone(); item.setAmount(1);
        p.save(new Shop(b.getLocation(),t,item,amount,sell,buy)); pl.sendMessage(p.msg("created"));
    }

    private void remove(Player pl,Block b)throws SQLException{ Shop s=p.get(b.getLocation()); if(s==null){pl.sendMessage(p.msg("not-shop"));return;} if(b.getState() instanceof Sign sign){ sign.update(true,false); } p.remove(b.getLocation()); pl.sendMessage(p.msg("removed")); }
    private void info(Player pl,Block b){ Shop s=p.get(b.getLocation()); if(s==null){pl.sendMessage(p.msg("not-shop"));return;} pl.sendMessage(p.color("&aALLINShop &7| &f"+s.item.getType()+" x"+s.amount+" &7| SELL $"+ALLINShopPlugin.money(s.sellPrice)+" | BUY $"+ALLINShopPlugin.money(s.buyPrice))); }
    private Shop shop(Player pl,Block b){ Shop s=p.get(b.getLocation()); if(s==null)pl.sendMessage(p.msg("not-shop")); return s; }
    private void setBuy(Player pl,Block b,String[]a)throws SQLException{Shop s=shop(pl,b);if(s==null||a.length<2)return;s.buyPrice=price(a[1]);if(s.type==ShopType.SELL)s.type=ShopType.BOTH;p.save(s);pl.sendMessage(p.msg("updated"));}
    private void setSell(Player pl,Block b,String[]a)throws SQLException{Shop s=shop(pl,b);if(s==null||a.length<2)return;s.sellPrice=price(a[1]);if(s.type==ShopType.BUY)s.type=ShopType.BOTH;p.save(s);pl.sendMessage(p.msg("updated"));}
    private void setAmount(Player pl,Block b,String[]a)throws SQLException{Shop s=shop(pl,b);if(s==null||a.length<2)return;s.amount=Integer.parseInt(a[1]);if(s.amount<=0)throw new IllegalArgumentException("Количество должно быть > 0");p.save(s);pl.sendMessage(p.msg("updated"));}
    private void setItem(Player pl,Block b)throws SQLException{Shop s=shop(pl,b);if(s==null)return;ItemStack h=pl.getInventory().getItemInMainHand();if(h.getType()==Material.AIR){pl.sendMessage(p.msg("hold-item"));return;}s.item=h.clone();s.item.setAmount(1);p.save(s);pl.sendMessage(p.msg("updated"));}
    private double price(String x){double v=Double.parseDouble(x.replace(',','.'));if(v<=0)throw new IllegalArgumentException("Цена должна быть > 0");return v;}
    private void help(Player pl){pl.sendMessage(p.color("&a/ashop create sell <кол-во> <цена>\n/ashop create buy <кол-во> <цена>\n/ashop create both <кол-во> <sell> <buy>\n/ashop info\n/ashop remove\n/ashop setbuy <цена>\n/ashop setsell <цена>\n/ashop setamount <кол-во>\n/ashop setitem\n/ashop reload"));}
    @Override public List<String> onTabComplete(CommandSender s,Command c,String a,String[]x){if(x.length==1)return List.of("create","info","remove","setbuy","setsell","setamount","setitem","reload");if(x.length==2&&x[0].equalsIgnoreCase("create"))return List.of("sell","buy","both");return List.of();}
}
