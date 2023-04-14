package me.gethertv.getcashgenerator.task;

import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorData;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeTaskGenerator extends BukkitRunnable {
    @Override
    public void run() {

        for(GeneratorUser generatorUser : GetCashGenerator.getInstance().getGeneratorsUser().values())
        {
            if(!generatorUser.isOnline())
                continue;

            generatorUser.addSecond();
        }
    }
}
