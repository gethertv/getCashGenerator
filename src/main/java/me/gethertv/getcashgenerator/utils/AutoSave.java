package me.gethertv.getcashgenerator.utils;

import me.gethertv.getcashgenerator.GetCashGenerator;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSave extends BukkitRunnable {
    @Override
    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                GetCashGenerator.getInstance().getSql().updateList();
            }
        }.runTaskAsynchronously(GetCashGenerator.getInstance());
    }
}
