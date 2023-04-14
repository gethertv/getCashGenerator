package me.gethertv.getcashgenerator.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class ExtraPlaceholder extends PlaceholderExpansion {
    DecimalFormat formatter = new DecimalFormat("00");
    @Override
    public @NotNull String getIdentifier() {
        return "getcash";
    }

    @Override
    public @NotNull String getAuthor() {
        return "gethertv";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (offlinePlayer.getPlayer() == null) return null;
        Player player = offlinePlayer.getPlayer();
        if (identifier.startsWith("generator"))
        {
            String[] location = identifier.split("_");
            if(location.length<5) return null;
            Location loc = new Location(Bukkit.getWorld(location[1]), Integer.parseInt(location[2]), Integer.parseInt(location[3]), Integer.parseInt(location[4]));
            if(loc!=null)
            {
                GeneratorUser generatorUser = GetCashGenerator.getInstance().getGeneratorsUser().get(loc);
                if(generatorUser!=null)
                {
                    return String.valueOf(generatorUser.getSecond());
                }
            }
            return null;
        }
        return null;
    }
}
