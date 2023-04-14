package me.gethertv.getcashgenerator.listener;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.clip.placeholderapi.PlaceholderAPI;
import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorData;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import me.gethertv.getcashgenerator.utils.ColorFixer;
import me.gethertv.getcashgenerator.utils.Timer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

public class PlaceBlockListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceBlock(BlockPlaceEvent event)
    {
        if(event.isCancelled())
            return;

        Player player = event.getPlayer();

        ItemStack temp = player.getInventory().getItemInMainHand().clone();
        temp.setAmount(1);
        GeneratorData mainHand = GetCashGenerator.getInstance().getGeneratorByItem().get(temp);
        if(mainHand!=null)
        {
            if(hasLimit(player))
            {

                player.sendMessage(ColorFixer.addColor(GetCashGenerator.getInstance().getConfig().getString("lang.limit")));
                event.setCancelled(true);
                return;
            }
            createGenerator(player, mainHand, event.getBlock().getLocation(), EquipmentSlot.HAND);
            return;
        }
        temp = player.getInventory().getItemInOffHand().clone();
        temp.setAmount(1);
        GeneratorData offHand = GetCashGenerator.getInstance().getGeneratorByItem().get(temp);
        if(offHand!=null)
        {
            if(hasLimit(player))
            {
                player.sendMessage(ColorFixer.addColor(GetCashGenerator.getInstance().getConfig().getString("lang.limit")));
                event.setCancelled(true);
                return;
            }
            createGenerator(player, offHand, event.getBlock().getLocation(), EquipmentSlot.OFF_HAND);
            return;
        }
    }

    public boolean hasLimit(Player player)
    {
        Set<PermissionAttachmentInfo> permissionsSet = player.getEffectivePermissions();
        List<PermissionAttachmentInfo> permissions = new ArrayList<>(permissionsSet);
        Collections.reverse(permissions);
        for (PermissionAttachmentInfo permission : permissions) {
            if (permission.getPermission().startsWith("getgenerator.limit.")) {
                try {
                    int limit = Integer.parseInt(permission.getPermission().substring("getgenerator.limit.".length()));
                    if(limit==0)
                        return false;

                    Integer amountGenerator = GetCashGenerator.getInstance().getLimitGenerator().get(player.getUniqueId());
                    if(amountGenerator!=null)
                        return (limit-amountGenerator<=0);

                } catch (NumberFormatException e) {
                }
            }
        }
        return false;
    }
    public void createGenerator(Player player, GeneratorData generatorData, Location location, EquipmentSlot equipmentSlot)
    {
        if(equipmentSlot==EquipmentSlot.OFF_HAND)
            player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount()-1);
        if(equipmentSlot==EquipmentSlot.HAND)
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);



        GeneratorUser generatorUser = new GeneratorUser(player.getUniqueId(), createHologram(location, generatorData), 0, generatorData);
        GetCashGenerator.getInstance().getGeneratorsUser().put(location, generatorUser);
        GetCashGenerator.getInstance().getSql().createGenerator(location, generatorUser);
        //String holo = "%getcash_generator_"+location.getWorld().getName()+"_"+location.getBlockX()+"_"+location.getBlockY()+"_"+location.getBlockZ()+"%";
        //holo = PlaceholderAPI.setPlaceholders(player, holo);

        Integer integer = GetCashGenerator.getInstance().getLimitGenerator().get(player.getUniqueId());
        int active = 0;
        if(integer!=null)
            active=integer;

        active++;
        GetCashGenerator.getInstance().getLimitGenerator().put(player.getUniqueId(), active);

        player.sendMessage(ColorFixer.addColor(GetCashGenerator.getInstance().getConfig().getString("lang.success-create").replace("{generator}", generatorData.getItemStack().getItemMeta().getDisplayName())));
    }

    public static Hologram createHologram(Location location, GeneratorData generatorData)
    {
        List<String> lines = new ArrayList<>();
        lines.addAll(generatorData.getHologram());

        Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location.clone().add(0.5, generatorData.getHeightHologram(), 0.5), ColorFixer.addTime(lines, Timer.getTimeBySec(generatorData.getSecond())));

        return hologram;
    }
}
