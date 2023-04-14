package me.gethertv.getcashgenerator;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.gethertv.getcashgenerator.cmd.CashGeneratorCmd;
import me.gethertv.getcashgenerator.data.GeneratorData;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import me.gethertv.getcashgenerator.listener.BlockExplodeListener;
import me.gethertv.getcashgenerator.listener.BreakBlockListener;
import me.gethertv.getcashgenerator.listener.PlaceBlockListener;
import me.gethertv.getcashgenerator.placeholder.ExtraPlaceholder;
import me.gethertv.getcashgenerator.storage.Mysql;
import me.gethertv.getcashgenerator.task.TimeTaskGenerator;
import me.gethertv.getcashgenerator.utils.AutoSave;
import me.gethertv.getcashgenerator.utils.ColorFixer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GetCashGenerator extends JavaPlugin {

    
    private static GetCashGenerator instance;

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    private HashMap<Location, GeneratorUser> generatorsUser = new HashMap<>();
    private HashMap<String, ItemStack> generatorByName = new HashMap<>();
    private HashMap<ItemStack, GeneratorData> generatorByItem = new HashMap<>();
    private HashMap<UUID, Integer> limitGenerator = new HashMap<>();

    private Mysql sql;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupSql();
        if (!sql.isConnected()) {
            getLogger().log (Level.WARNING, "Nie można połączyć sie z baza danych!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            (new ExtraPlaceholder()).register();
        }

        implementsItemGenerator();

        sql.loadGenerators();

        getServer().getPluginManager().registerEvents(new PlaceBlockListener(), this);
        getServer().getPluginManager().registerEvents(new BreakBlockListener(), this);
        getServer().getPluginManager().registerEvents(new BlockExplodeListener(), this);

        new TimeTaskGenerator().runTaskTimer(this, 20L, 20L);
        new AutoSave().runTaskTimer(this, 20L*120, 20L*120);

        getCommand("getcashgenerator").setExecutor(new CashGeneratorCmd());
        getCommand("getcashgenerator").setTabCompleter(new CashGeneratorCmd());

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        sql.updateList();

        for(GeneratorUser generatorUser : generatorsUser.values())
        {
            generatorUser.getHologram().delete();
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            (new ExtraPlaceholder()).unregister();
        }


        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);




    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void setupSql() {
        String host = getConfig().getString("mysql.host");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        String database = getConfig().getString("mysql.database");
        String port = getConfig().getString("mysql.port");

        boolean ssl = false;
        if (getConfig().get("mysql.ssl") != null) {
            ssl = getConfig().getBoolean("mysql.ssl");
        }
        this.sql = new Mysql(host, username, password, database, port, ssl);
    }
    private void implementsItemGenerator() {

        for(String key: getConfig().getConfigurationSection("generators").getKeys(false))
        {
            String id = getConfig().getString("generators."+key+".id");
            int second = getConfig().getInt("generators."+key+".time");
            boolean enable = getConfig().getBoolean("generators."+key+".enable");
            double earn = getConfig().getInt("generators."+key+".earn");
            double heightHologram = getConfig().getInt("generators."+key+".height");
            List<String> hologram = new ArrayList<>();
            hologram.addAll(GetCashGenerator.getInstance().getConfig().getStringList("generators."+key+".hologram"));
            List<String> cmds = new ArrayList<>();
            cmds.addAll(GetCashGenerator.getInstance().getConfig().getStringList("generators."+key+".cmds"));


            ItemStack itemStack = new ItemStack(Material.valueOf(getConfig().getString("generators."+key+".item.material").toUpperCase()));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ColorFixer.addColor(getConfig().getString("generators."+key+".item.displayname")));
            List<String> lore = new ArrayList<>();
            lore.addAll(getConfig().getStringList("generators."+key+".item.lore"));
            itemMeta.setLore(ColorFixer.addColor(lore));

            itemStack.setItemMeta(itemMeta);

            generatorByName.put(id, itemStack);

            generatorByItem.put(itemStack, new GeneratorData(id, itemStack, hologram,heightHologram, enable, earn, second, cmds));
        }


    }



    public static GetCashGenerator getInstance() {
        return instance;
    }

    public HashMap<Location, GeneratorUser> getGeneratorsUser() {
        return generatorsUser;
    }

    public HashMap<ItemStack, GeneratorData> getGeneratorByItem() {
        return generatorByItem;
    }

    public HashMap<String, ItemStack> getGeneratorByName() {
        return generatorByName;
    }

    public Mysql getSql() {
        return sql;
    }

    public static Economy getEcon() {
        return econ;
    }

    public HashMap<UUID, Integer> getLimitGenerator() {
        return limitGenerator;
    }
}
