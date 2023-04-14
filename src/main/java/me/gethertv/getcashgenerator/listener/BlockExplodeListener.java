package me.gethertv.getcashgenerator.listener;

import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class BlockExplodeListener implements Listener {

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> destroyedBlocks = event.blockList();

        for(Block block : destroyedBlocks) {
            GeneratorUser generatorUser = GetCashGenerator.getInstance().getGeneratorsUser().get(block.getLocation());
            if(generatorUser!=null)
            {
                event.setCancelled(true);
            }
        }
    }

}
