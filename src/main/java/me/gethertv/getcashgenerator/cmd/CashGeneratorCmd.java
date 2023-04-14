package me.gethertv.getcashgenerator.cmd;

import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.utils.ColorFixer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CashGeneratorCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("getgeneratorcash.admin"))
            return false;

        if(args.length==4)
        {
            if(args[0].equalsIgnoreCase("give"))
            {
                Player target = Bukkit.getPlayer(args[1]);
                if(target==null)
                {
                    sender.sendMessage(ColorFixer.addColor("&cPodany gracz nie jest online!"));
                    return false;
                }
                ItemStack item = GetCashGenerator.getInstance().getGeneratorByName().get(args[2].toLowerCase());
                if(item==null)
                {
                    sender.sendMessage(ColorFixer.addColor("&cPodany generator nie istnieje!"));
                    return false;
                }
                if(!isInteger(args[3]))
                {
                    sender.sendMessage(ColorFixer.addColor("&cMusisz podac ilosc! (Liczba calkowita)"));
                    return false;
                }
                item = item.clone();
                item.setAmount(Integer.parseInt(args[3]));
                target.getInventory().addItem(item);
                sender.sendMessage(ColorFixer.addColor("&aPomyslnie nadano generator!"));
                return true;


            }
        }
        return false;
    }

    private boolean isInteger(String input)
    {
        try {
            int a = Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {}

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length==1)
        {
            return Arrays.asList("give");
        }
        if(args.length==3)
        {
            List<String> data = new ArrayList<>();
            GetCashGenerator.getInstance().getGeneratorByName().keySet().forEach(key -> {
                data.add(key);
            });

            return data;
        }
        return null;
    }
}
