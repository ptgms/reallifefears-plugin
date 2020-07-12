package ptgms.industries.fears;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerWrapper {

    private static final Fears thisPlugin = JavaPlugin.getPlugin(Fears.class);

    public PlayerWrapper(Player p)
    {
        basePlayer = p;
    }

    Player basePlayer;

    ScaryMobs mobFear = ScaryMobs.ZOMBIE;
    boolean heightFear = false;

    int warmBiomeTime = 0;
    boolean inWarmBiome = false;
    boolean beacon = false;
    boolean bleeding = false;

    boolean mobFearActive = false;
    boolean heightFearActive = false;

    public static PlayerWrapper findPlayer(Player p) {
        PlayerWrapper w = null;

        for(int i = 0; i < thisPlugin.players.size(); i++)
        {
            if(thisPlugin.players.get(i).basePlayer == p)
            {
                w = thisPlugin.players.get(i);
            }
        }
        return w;
    }
}
