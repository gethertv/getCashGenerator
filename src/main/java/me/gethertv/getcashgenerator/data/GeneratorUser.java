package me.gethertv.getcashgenerator.data;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.utils.ColorFixer;
import me.gethertv.getcashgenerator.utils.Timer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GeneratorUser {
    private UUID owner;
    private int second;
    private GeneratorData generatorData;
    private Hologram hologram;

    public GeneratorUser(UUID uuid, Hologram hologram, int second, GeneratorData generatorData) {
        this.owner = uuid;
        this.hologram =hologram;
        this.second = second;
        this.generatorData = generatorData;
    }
    public void addSecond() {
        second++;
        if(second>generatorData.getSecond())
        {
            second=0;
            giveRewards();
        }
        updateHologram();
    }

    public String getOwnerName()
    {
        String name = "";
        Player p = Bukkit.getPlayer(owner);
        if(p!=null)
            name = p.getName();
        else
            name = Bukkit.getOfflinePlayer(owner).getName();

        return name;
    }
    private void giveRewards() {

        if(isOnline())
        {
            if(generatorData.isEnableMoney()) {
                Player player = Bukkit.getPlayer(owner);
                GetCashGenerator.getEcon().depositPlayer(player, generatorData.getMoney());
            }
        }
        getGeneratorData().getCmds().forEach(cmd -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", getOwnerName()));
        });
    }

    public boolean isOwner(Player player)
    {
        if(owner==player.getUniqueId())
            return true;

        return false;
    }
    private void updateHologram()
    {
        for (int i = 0; i < generatorData.getHologram().size(); i++) {
            String line = generatorData.getHologram().get(i);
            DHAPI.setHologramLine(hologram, i, ColorFixer.addTime(line, Timer.getTimeBySec(generatorData.getSecond()-second)));
        }
    }
    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }


    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public GeneratorData getGeneratorData() {
        return generatorData;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setGeneratorData(GeneratorData generatorData) {
        this.generatorData = generatorData;
    }

    public boolean isOnline() {
        Player p = Bukkit.getPlayer(owner);
        if(p!=null)
            return true;

        return false;
    }
}
