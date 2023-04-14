package me.gethertv.getcashgenerator.data;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class GeneratorData {
    private String key;
    private ItemStack itemStack;

    private List<String> hologram;

    private double heightHologram;
    private double money;
    private boolean enableMoney;
    private int second;

    private List<String> cmds;

    public GeneratorData(String key, ItemStack itemStack, List<String> hologram,double heightHologram, boolean enableMoney, double money, int second, List<String> cmds) {
        this.key = key;
        this.itemStack = itemStack;
        this.hologram = hologram;
        this.heightHologram = heightHologram;
        this.enableMoney = enableMoney;
        this.money = money;
        this.second = second;
        this.cmds = cmds;
    }


    public boolean isEnableMoney() {
        return enableMoney;
    }

    public double getHeightHologram() {
        return heightHologram;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<String> getHologram() {
        return hologram;
    }

    public void setHologram(List<String> hologram) {
        this.hologram = hologram;
    }

    public List<String> getCmds() {
        return cmds;
    }

    public String getKey() {
        return key;
    }

    public double getMoney() {
        return money;
    }

    public int getSecond() {
        return second;
    }
}
