package ru.allin.allinshop;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ShopListener implements Listener {
    private final ALLINShopPlugin p;
    public ShopListener(ALLINShopPlugin p){this.p=p;}

    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false)
    public void click(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND || e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
        Block b=e.getClickedBlock(); if(b==null || !(b.getState() instanceof Sign)) return;
        Shop s=p.get(b.getLocation()); if(s==null)return;
        e.setCancelled(true); // works in ADVENTURE: our listener handles interaction, vanilla sign editing is blocked
        Player pl=e.getPlayer(); if(!pl.hasPermission("allinshop.use")){pl.sendMessage(p.msg("no-permission"));return;}
        boolean shift=pl.isSneaking();
        if(s.type==ShopType.BOTH){ if(shift)sell(pl,s,true); else buy(pl,s,false); }
        else if(s.type==ShopType.SELL)sell(pl,s,shift); else buy(pl,s,shift);
    }

    private void sell(Player pl,Shop s,boolean max){
        int ops=count(pl,s.item)/s.amount; if(!max)ops=Math.min(ops,1);
        if(ops<=0){pl.sendMessage(p.msg("need-items").replace("%amount%",String.valueOf(s.amount)).replace("%item%",s.item.getType().name()));return;}
        int n=ops*s.amount; double money=ops*s.sellPrice; remove(pl,s.item,n);
        EconomyResponse r=p.economy().depositPlayer(pl,money); if(!r.transactionSuccess()){give(pl,s.item,n);pl.sendMessage(p.msg("economy-error"));return;}
        pl.sendMessage(p.msg("sold").replace("%amount%",String.valueOf(n)).replace("%item%",s.item.getType().name()).replace("%price%",ALLINShopPlugin.money(money)));
    }

    private void buy(Player pl,Shop s,boolean max){
        int moneyOps=(int)Math.floor(p.economy().getBalance(pl)/s.buyPrice), spaceOps=space(pl,s.item)/s.amount, ops=Math.min(moneyOps,spaceOps); if(!max)ops=Math.min(ops,1);
        if(ops<=0){ if(p.economy().getBalance(pl)<s.buyPrice)pl.sendMessage(p.msg("need-money").replace("%price%",ALLINShopPlugin.money(s.buyPrice))); else pl.sendMessage(p.msg("inventory-full")); return; }
        int n=ops*s.amount; double cost=ops*s.buyPrice; EconomyResponse r=p.economy().withdrawPlayer(pl,cost); if(!r.transactionSuccess()){pl.sendMessage(p.msg("economy-error"));return;} give(pl,s.item,n);
        pl.sendMessage(p.msg("bought").replace("%amount%",String.valueOf(n)).replace("%item%",s.item.getType().name()).replace("%price%",ALLINShopPlugin.money(cost)));
    }

    private int count(Player pl,ItemStack t){int n=0;for(ItemStack s:pl.getInventory().getStorageContents())if(s!=null&&s.isSimilar(t))n+=s.getAmount();return n;}
    private void remove(Player pl,ItemStack t,int n){ItemStack[] c=pl.getInventory().getStorageContents();for(int i=0;i<c.length&&n>0;i++){ItemStack s=c[i];if(s==null||!s.isSimilar(t))continue;int q=Math.min(n,s.getAmount());s.setAmount(s.getAmount()-q);n-=q;if(s.getAmount()<=0)c[i]=null;}pl.getInventory().setStorageContents(c);}
    private int space(Player pl,ItemStack t){int n=0,m=t.getMaxStackSize();for(ItemStack s:pl.getInventory().getStorageContents()){if(s==null||s.getType()==Material.AIR)n+=m;else if(s.isSimilar(t))n+=Math.max(0,m-s.getAmount());}return n;}
    private void give(Player pl,ItemStack t,int n){while(n>0){int q=Math.min(t.getMaxStackSize(),n);ItemStack x=t.clone();x.setAmount(q);pl.getInventory().addItem(x);n-=q;}}

    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void signChange(SignChangeEvent e){if(p.get(e.getBlock().getLocation())!=null)e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void breakBlock(BlockBreakEvent e){if(p.get(e.getBlock().getLocation())!=null){e.setCancelled(true);e.getPlayer().sendMessage(p.msg("protected"));}}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void piston1(BlockPistonExtendEvent e){if(e.getBlocks().stream().anyMatch(b->p.get(b.getLocation())!=null))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void piston2(BlockPistonRetractEvent e){if(e.getBlocks().stream().anyMatch(b->p.get(b.getLocation())!=null))e.setCancelled(true);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void explode(BlockExplodeEvent e){e.blockList().removeIf(b->p.get(b.getLocation())!=null);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=false) public void explode2(EntityExplodeEvent e){e.blockList().removeIf(b->p.get(b.getLocation())!=null);}
}
