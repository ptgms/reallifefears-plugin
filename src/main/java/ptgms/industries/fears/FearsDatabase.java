package ptgms.industries.fears;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

public class FearsDatabase {

    private final JavaPlugin thisPlugin = JavaPlugin.getPlugin(Fears.class);
    private final Logger console = thisPlugin.getLogger();
    Connection conn = null;

    public FearsDatabase(String dbName)
    {
        try {
            File newDirectory = new File("plugins/RealLifeFears");
            if(!newDirectory.exists())
            {
                newDirectory.mkdir();
            }

            // db parameters
            String url = "jdbc:sqlite:plugins/RealLifeFears/" + dbName;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            console.info("Successfully connected to database");

            createTables();

        } catch (SQLException e) {
            console.warning(ChatColor.RED + "Error connecting to database! The plugin will function," +
                    "but nothing will be written to the database and exceptions may be thrown. This is not good.");
            e.printStackTrace();
        }
    }

    private void createTables()
    {
        //fears table
        String players = "CREATE TABLE IF NOT EXISTS players (\n"
                + "	uuid text PRIMARY KEY,\n" //access with uuid
                + "	mob integer NOT NULL,\n" //which mob player is scared of
                + "	heights bit NOT NULL,\n" //whether the player is scared of height
                + " warmbiometime integer NOT NULL,\n" //how long the playe has been in a warm biome
                + "	beacon bit NOT NULL,\n" //whether the player has an active beacon
                + "	bleeding bit NOT NULL\n" //whether the player is bleeding
                + ");";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(players);
        } catch (SQLException e) {
            console.warning("Error creating tables!");
            e.printStackTrace();
        }
    }

    public void writePlayer(PlayerWrapper player)
    {
        //identity insert must be on so we can insert with uuid
        try {
            String players = "INSERT INTO players(uuid, mob, heights, warmbiometime, beacon, bleeding) " +
                    "VALUES(?,?,?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET mob=?, heights=?, warmbiometime=?, " +
                    "beacon=?, bleeding=?";
            PreparedStatement prep = conn.prepareStatement(players);

            //each '?' corresponds to a value
            prep.setString(1, player.basePlayer.getUniqueId().toString());
            prep.setInt(2, player.mobFear.getValue());
            prep.setBoolean(3, player.heightFear);
            prep.setInt(4, player.warmBiomeTime);
            prep.setBoolean(5, player.beacon);
            prep.setBoolean(6, player.bleeding);

            //update
            prep.setInt(7, player.mobFear.getValue());
            prep.setBoolean(8, player.heightFear);
            prep.setInt(9, player.warmBiomeTime);
            prep.setBoolean(10, player.beacon);
            prep.setBoolean(11, player.bleeding);

            prep.executeUpdate();
        } catch (SQLException e) {
            console.warning("Error writing player!");
            e.printStackTrace();
        }
    }

    public PlayerWrapper readPlayer(Player p) {
        PlayerWrapper player = new PlayerWrapper(p);

        try {
            String get = "SELECT * FROM players WHERE uuid=?";
            PreparedStatement prep = conn.prepareStatement(get);

            prep.setString(1, p.getUniqueId().toString());
            ResultSet rs = prep.executeQuery();

            if(rs.next())
            {
                player.mobFear = ScaryMobs.fromInt(rs.getInt("mob"));
                player.heightFear = rs.getBoolean("heights");
                player.warmBiomeTime = rs.getInt("warmbiometime");
                player.beacon = rs.getBoolean("beacon");
                player.bleeding = rs.getBoolean("bleeding");
            }

        } catch (SQLException ignored) {}

        return player; //return new player if not successful
    }
}
