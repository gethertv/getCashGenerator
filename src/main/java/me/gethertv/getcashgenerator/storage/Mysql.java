package me.gethertv.getcashgenerator.storage;
import me.gethertv.getcashgenerator.GetCashGenerator;
import me.gethertv.getcashgenerator.data.GeneratorData;
import me.gethertv.getcashgenerator.data.GeneratorUser;
import me.gethertv.getcashgenerator.listener.PlaceBlockListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;

public class Mysql {
    private String host;
    private String username;
    private String password;
    private String database;
    private String port;
    private boolean ssl;
    private boolean isFinished;
    private Connection connection;

    private List<String> sqlExecuteList = new ArrayList<>();

    public Mysql(String host, String username, String password, String database, String port, boolean ssl) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
        this.ssl = ssl;

        openConnection();

        createTable();
    }

    private String getUsername() {
        return this.username;
    }

    private String getPassword() {
        return this.password;
    }

    private String getHost() {
        return this.host;
    }

    private String getPort() {
        return this.port;
    }

    private String getDatabase() {
        return this.database;
    }

    private boolean useSSL() {
        return this.ssl;
    }

    public boolean isConnected() {
        return (getConnection() != null);
    }

    public Connection getConnection() {
        validateConnection();
        return this.connection;
    }

    private void openConnection() {
        try {
            long l1 = System.currentTimeMillis();
            long l2 = 0L;
            //Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", getUsername());
            properties.setProperty("password", getPassword());
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("useSSL", String.valueOf(useSSL()));
            properties.setProperty("requireSSL", String.valueOf(useSSL()));
            properties.setProperty("verifyServerCertificate", "false");
            String str = "jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getDatabase();
            this.connection = DriverManager.getConnection(str, properties);
            l2 = System.currentTimeMillis();
            this.isFinished = true;
            System.out.println("[mysql] Connected successfully");
        } catch (ClassNotFoundException classNotFoundException) {
            this.isFinished = false;
            System.out.println("[mysql] Check your configuration.");
            Bukkit.getPluginManager().disablePlugin(GetCashGenerator.getInstance());
        } catch (SQLException sQLException) {
            this.isFinished = false;
            System.out.println("[mysql] (" + sQLException.getLocalizedMessage() + "). Check your configuration.");
            Bukkit.getPluginManager().disablePlugin(GetCashGenerator.getInstance());
        }
    }

    private void validateConnection() {
        if (!this.isFinished)
            return;
        try {
            if (this.connection == null) {
                System.out.println("[mysql] aborted. Connecting again");
                reConnect();
            }
            if (!this.connection.isValid(4)) {
                System.out.println("[mysql] timeout.");
                reConnect();
            }
            if (this.connection.isClosed()) {
                System.out.println("[mysql] closed. Connecting again");
                reConnect();
            }
        } catch (Exception exception) {
        }
    }

    private void reConnect() {
        System.out.println("[mysql] connection again");
        openConnection();
    }

    public void closeConnection() {
        if (getConnection() != null) {
            try {
                getConnection().close();
                System.out.println("[mysql] connection closed");
            } catch (SQLException sQLException) {
                System.out.println("[mysql] error when try close connection");
            }
        }
    }

    public int checkExists(String str) {
        int i = 0;
        try {
            ResultSet resultSet = getResult(str);
            if (resultSet.next()) {
                i++;
                resultSet.close();
                return i;
            }
        } catch (SQLException sQLException) {
            return i;
        }
        return i;
    }


    public void update(String paramString) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = getConnection().createStatement();
                statement.executeUpdate(paramString);
            }
        } catch (SQLException sQLException) {
            System.out.println("[mysql] wrong update : '" + paramString + "'!");
        }
    }

    public void updateList() {
        GetCashGenerator.getInstance().getGeneratorsUser().forEach((location, generatorUser) -> {
            String loc = "";
            loc+=location.getWorld().getName();
            loc+=":"+location.getBlockX();
            loc+=":"+location.getBlockY();
            loc+=":"+location.getBlockZ();

            sqlExecuteList.add("UPDATE cash_generator SET time = '"+generatorUser.getSecond()+"' WHERE location = '"+loc+"'");
        });
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();

            for(String sqlString : sqlExecuteList)
            {
                statement.executeUpdate(sqlString);
            }
            connection.commit();
        } catch (SQLException sQLException) {
            System.out.println("[mysql] wrong update : " + sQLException.getMessage());
        }

        sqlExecuteList.clear();
    }

    public void loadGenerators() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String str = "SELECT * FROM cash_generator";
                try
                {
                    ResultSet resultSet = getResult(str);
                    while (resultSet.next()) {
                        UUID owner = UUID.fromString(resultSet.getString("uuid"));
                        String[] locString = resultSet.getString("location").split(":");
                        int second = resultSet.getInt("time");
                        String type = resultSet.getString("type");
                        ItemStack itemStack = GetCashGenerator.getInstance().getGeneratorByName().get(type);
                        if(itemStack==null)
                            continue;

                        Integer integer = GetCashGenerator.getInstance().getLimitGenerator().get(owner);
                        int active = 0;
                        if(integer!=null)
                            active=integer;

                        active++;
                        GetCashGenerator.getInstance().getLimitGenerator().put(owner, active);

                        GeneratorData generatorData = GetCashGenerator.getInstance().getGeneratorByItem().get(itemStack);
                        Location locBlock = new Location(Bukkit.getWorld(locString[0]), Integer.parseInt(locString[1]), Integer.parseInt(locString[2]), Integer.parseInt(locString[3]));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                locBlock.getBlock().setType(generatorData.getItemStack().getType());
                            }
                        }.runTask(GetCashGenerator.getInstance());

                        GetCashGenerator.getInstance().getGeneratorsUser().put(locBlock, new GeneratorUser(owner, PlaceBlockListener.createHologram(locBlock, generatorData), second, generatorData));
                    }
                } catch(SQLException | NullPointerException sQLException){ }

            }
        }.runTaskAsynchronously(GetCashGenerator.getInstance());
    }

    public ResultSet getResult(String paramString) {
        ResultSet resultSet = null;
        Connection connection = getConnection();
        try {
            if (connection != null) {
                Statement statement = getConnection().createStatement();
                resultSet = statement.executeQuery(paramString);
            }
        } catch (SQLException sQLException) {
            System.out.println("[mysql] wrong when want get result: '" + paramString + "'!");
        }
        return resultSet;
    }


    public void createTable() {
        String create = "CREATE TABLE IF NOT EXISTS cash_generator (id INT(10) AUTO_INCREMENT, PRIMARY KEY (id), uuid VARCHAR(100), location VARCHAR(100), type VARCHAR(100), time INT(11))";
        update(create);
    }


    private int getInt(String paramString1, String paramString2) {
        try {
            ResultSet resultSet = getResult(paramString2);
            if (resultSet.next()) {
                int i = resultSet.getInt(paramString1);
                resultSet.close();
                return i;
            }
        } catch (SQLException sQLException) {
            return 0;
        }
        return 0;
    }

    public void deleteGenertor(Location location) {
        String loc = "";
        loc+=location.getWorld().getName();
        loc+=":"+location.getBlockX();
        loc+=":"+location.getBlockY();
        loc+=":"+location.getBlockZ();

        String sqlQuery = "DELETE FROM cash_generator WHERE location = '"+loc+"'";
        sqlExecuteList.add(sqlQuery);
    }

    public void createGenerator(Location location, GeneratorUser generatorUser) {
        String loc = "";
        loc+=location.getWorld().getName();
        loc+=":"+location.getBlockX();
        loc+=":"+location.getBlockY();
        loc+=":"+location.getBlockZ();

        String sqlQuery = "INSERT INTO cash_generator (uuid, location, type, time) VALUES ('"+generatorUser.getOwner()+"', '"+loc+"', '"+generatorUser.getGeneratorData().getKey()+"', '"+generatorUser.getSecond()+"')";
        sqlExecuteList.add(sqlQuery);
    }

}
