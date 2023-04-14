package me.gethertv.getcashgenerator.listener;

import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorData;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import me.gethertv.getcashgenerator.utils.ColorFixer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakBlockListener implements Listener {

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event)
    {
        Player player =event.getPlayer();
        GeneratorUser generatorUser = GetCashGenerator.getInstance().getGeneratorsUser().get(event.getBlock().getLocation());
        if(generatorUser!=null)
        {
            event.setCancelled(true);
            if(player.hasPermission("getcashgenerator.break") || generatorUser.isOwner(player))
            {
                Integer integer = GetCashGenerator.getInstance().getLimitGenerator().get(player.getUniqueId());
                if(integer==1)
                    GetCashGenerator.getInstance().getLimitGenerator().remove(player.getUniqueId());
                else {
                    integer--;
                    GetCashGenerator.getInstance().getLimitGenerator().put(player.getUniqueId(), integer);
                }

                event.getBlock().setType(Material.AIR);
                generatorUser.getHologram().delete();
                player.getInventory().addItem(generatorUser.getGeneratorData().getItemStack());
                player.sendMessage(ColorFixer.addColor(GetCashGenerator.getInstance().getConfig().getString("lang.success-take").replace("{generator}", generatorUser.getGeneratorData().getItemStack().getItemMeta().getDisplayName())));
                GetCashGenerator.getInstance().getGeneratorsUser().remove(event.getBlock().getLocation());
                GetCashGenerator.getInstance().getSql().deleteGenertor(event.getBlock().getLocation());
            }
        }
    }
}
