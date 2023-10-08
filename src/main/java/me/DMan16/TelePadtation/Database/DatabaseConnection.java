package me.DMan16.TelePadtation.Database;

import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainAbortAction;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.DMan16.TelePadtation.Classes.BlockLocation;
import me.DMan16.TelePadtation.TelePads.TelePad;
import me.DMan16.TelePadtation.TelePadtationMain;
import me.DMan16.TelePadtation.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DatabaseConnection {
	protected static final String ERROR_MESSAGE = "Insertion failed: nearby TelePads detected";
	protected static final String TRIGGER_NAME = "prevent_nearby_vertical_telepads";
	
	protected final @NotNull String table;
	protected final int maxPoolSize;
	protected final long connectionTimeout;
	private HikariDataSource hikari;
	
	protected DatabaseConnection(@NotNull String table,int maxPoolSize,long connectionTimeout) {
		this.table = table;
		this.maxPoolSize = maxPoolSize;
		this.connectionTimeout = connectionTimeout;
	}
	
	public final void close() {
		if (hikari == null) return;
		hikari.close();
		hikari = null;
	}
	
	public final void connect() {
		if (hikari != null && !hikari.isClosed()) return;
		close();
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(connectionAddress());
		hikariConfig.setPoolName(TelePadtationMain.PLUGIN_NAME);
		hikariConfig.addDataSourceProperty("cachePrepStmts","true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize","250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit","2048");
		hikariConfig.addDataSourceProperty("useServerPrepStmts","true");
		hikariConfig.addDataSourceProperty("useLocalSessionState","true");
		hikariConfig.addDataSourceProperty("rewriteBatchedStatements","true");
		hikariConfig.addDataSourceProperty("cacheResultSetMetadata","true");
		hikariConfig.addDataSourceProperty("cacheServerConfiguration","true");
		hikariConfig.addDataSourceProperty("elideSetAutoCommits","true");
		hikariConfig.addDataSourceProperty("maintainTimeStats","false");
		hikariConfig.setMaximumPoolSize(maxPoolSize);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		setExtraHikariConfigs(hikariConfig);
		this.hikari = new HikariDataSource(hikariConfig);
	}
	
	@NotNull
	public final Connection getConnection() throws SQLException {
		return hikari.getConnection();
	}
	
	protected abstract @NotNull String connectionAddress();
	
	protected abstract void setExtraHikariConfigs(@NotNull HikariConfig hikariConfig);
	
	protected abstract String triggerLogic();
	
	private void addInsertConstraint(@NotNull Statement statement) throws SQLException {
		statement.executeUpdate("DROP TRIGGER IF EXISTS " + TRIGGER_NAME);
		statement.executeUpdate(triggerLogic());
	}
	
	protected final void createTable() throws SQLException {
		try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
			DatabaseMetaData data = connection.getMetaData();
			createTableAndFixColumns(data,statement,table,Arrays.asList("World VARCHAR(100) NOT NULL","X INT NOT NULL","Y INT NOT NULL","Z INT NOT NULL","OwnerID VARCHAR(36) NOT NULL","Type TEXT NOT NULL","Used INT UNSIGNED NOT NULL DEFAULT 0",
					"Fuel INT UNSIGNED NOT NULL DEFAULT 0","Global BIT(1) NOT NULL DEFAULT 0","Name TEXT"),Collections.singletonList("PRIMARY KEY(World,X,Y,Z)"),"World(World)","OwnerID_Global(OwnerID,Global)");
			addInsertConstraint(statement);
		}
	}
	
	protected void createTable(@NotNull Statement statement,@NotNull String table,@NotNull Collection<String> columns,@Nullable Collection<String> extra,String @NotNull ... indexes) throws SQLException {
		Stream<String> stream = columns.stream().filter(Objects::nonNull);
		if (extra != null) stream = Stream.concat(stream,extra.stream().filter(Objects::nonNull));
		if (indexes.length > 0) stream = Stream.concat(stream,Arrays.stream(indexes).filter(Objects::nonNull).map(index -> "INDEX " + index));
		statement.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + stream.collect(Collectors.joining(",")) + ");");
	}
	
	protected void fixColumns(@NotNull DatabaseMetaData data,@NotNull Statement statement,@NotNull String table,@NotNull Collection<String> columns,String @NotNull ... indexes) throws SQLException {
		Stream<String> stream = columns.stream().filter(Objects::nonNull).map(column -> "ADD COLUMN IF NOT EXISTS " + column);
		if (indexes.length > 0) stream = Stream.concat(stream,Arrays.stream(indexes).filter(Objects::nonNull).map(index -> "ADD INDEX IF NOT EXISTS " + index));
		statement.execute("ALTER TABLE " + table + " " + stream.collect(Collectors.joining(",")) + ";");
	}
	
	protected void createTableAndFixColumns(@NotNull DatabaseMetaData data,@NotNull Statement statement,@NotNull String table,@NotNull Collection<String> columns,@Nullable Collection<String> extra,String @NotNull ... indexes) throws SQLException {
		createTable(statement,table,columns,extra);
		fixColumns(data,statement,table,columns,indexes);
	}
	
	private static void setLocation(@NotNull PreparedStatement statement,@NotNull BlockLocation location,int start) throws SQLException {
		statement.setString(start++,location.world().getName());
		statement.setInt(start++,location.x());
		statement.setInt(start++,location.y());
		statement.setInt(start,location.z());
	}
	
	private static void setLocation(@NotNull PreparedStatement statement,@NotNull TelePad telePad,int start) throws SQLException {
		setLocation(statement,telePad.location(),start);
	}
	
	@NotNull
	private static TaskChainAbortAction<Object,Object,Object> failAbort(@Nullable Runnable onFail) {
		return new TaskChainAbortAction<Object,Object,Object>() {
			@Override
			public void onAbort(TaskChain<?> chain,Object arg1) {
				Utils.runNotNull(onFail);
			}
		};
	}
	
//	private static <V> void execute(@NotNull TaskChain<Supplier<@Nullable V>> chain,@Nullable Consumer<@Nullable V> onSuccess,@Nullable Runnable onFail) {
//		chain = chain.abortIfNull(failAbort(onFail));
//		if (onSuccess == null) chain.execute();
//		else chain.syncLast(supplier -> onSuccess.accept(supplier.get())).execute();
//	}
//
//	private static void execute(@NotNull TaskChain<@NotNull Boolean> chain,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
//		chain = chain.abortIf(success -> !success,failAbort(onFail));
//		if (onSuccess == null) chain.execute();
//		else chain.syncLast(b -> onSuccess.run()).execute();
//	}
	
	public final void add(@NotNull TelePad telePad,@Nullable Runnable onSuccess,@Nullable Runnable onFailConstraint,@Nullable Runnable onFailDatabase) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + " (World,X,Y,Z,OwnerID,Type) VALUES (?,?,?,?,?,?);")) {
				setLocation(statement,telePad,1);
				statement.setString(5,telePad.ownerID().toString());
				statement.setString(6,telePad.type());
				Utils.executeUpdateFailNoResults(statement);
				return true;
			} catch (SQLException e) {
				if (Utils.applyNotNull(e.getMessage(),msg -> Utils.toLowercase(msg).contains(Utils.toLowercase(ERROR_MESSAGE)))) return false;
				e.printStackTrace();
				return null;
			}
		}).abortIfNull(failAbort(onFailConstraint)).abortIfNot(Utils::self,failAbort(onFailDatabase)).syncLast(b -> Utils.runNotNull(onSuccess)).execute();
	}
	
	/**
	 * @param player if not null then the TelePad is owned by this player and is a private TelePad
	 */
	public final void remove(@NotNull TelePad telePad,@Nullable Player player,@Nullable Consumer<@Nullable Long> onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection()) {
				Long owned = player == null ? null : owned(connection,player);
				try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE World=? AND X=? AND Y=? AND Z=?;")) {
					setLocation(statement,telePad,1);
					Utils.executeUpdateFailNoResults(statement);
					telePad.setRemoved();
					return Utils.supplier(Utils.applyNotNull(owned,o -> o - 1));
				}
			} catch (SQLException e) {e.printStackTrace();}
			return null;
		}).abortIfNull(failAbort(onFail)).syncLast(supplier -> Utils.runNotNull(onSuccess,run -> run.accept(supplier.get()))).execute();
	}
	
	@Nullable
	protected final TelePad.TelePadPlaceable getTelePad(@NotNull ResultSet result,@NotNull BlockLocation location) throws SQLException {
		String type = result.getString("Type");
		if (type == null) return null;
		UUID ownerID = UUID.fromString(result.getString("OwnerID"));
		int used = result.getInt("Used");
		int fuel = result.getInt("Fuel");
		boolean isGlobal = result.getBoolean("Global");
		String name = result.getString("Name");
		return TelePadtationMain.TelePadsManager().getFromDatabase(type,ownerID,location,used,fuel,isGlobal,name);
	}
	
	@Nullable
	protected final TelePad.TelePadPlaceable getTelePad(@NotNull ResultSet result,@NotNull World world) throws SQLException {
		int x = result.getInt("X"),y = result.getInt("Y"),z = result.getInt("Z");
		return getTelePad(result,new BlockLocation(world,x,y,z));
	}
	
	@Nullable
	public final TelePad.TelePadPlaceable getTelePad(@NotNull BlockLocation location) throws SQLException {
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE World=? AND X=? AND Y=? AND Z=?;")) {
			setLocation(statement,location,1);
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					return getTelePad(result,location);
				}
				return null;
			}
		}
	}
	
	public final void getApplicableTelePads(@NotNull Player player,@NotNull Consumer<@Nullable List<TelePad.@NotNull TelePadPlaceable>> onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE OwnerID=? OR Global=?;")) {
				statement.setString(1,player.getUniqueId().toString());
				statement.setBoolean(2,true);
				try (ResultSet results = statement.executeQuery()) {
					List<TelePad.TelePadPlaceable> telePads = new ArrayList<>();
					while (results.next()) {
						String worldName = results.getString("World");
						World world = Bukkit.getWorld(worldName);
						if (world == null) continue;
						TelePad.TelePadPlaceable telePad = getTelePad(results,world);
						if (telePad == null || !telePad.canAccess(player)) continue;
						telePads.add(telePad);
					}
					return telePads;
				}
			} catch (SQLException e) {e.printStackTrace();}
			return null;
		}).abortIfNull(failAbort(onFail)).syncLast(telePads -> Utils.runNotNull(onSuccess,run -> run.accept(telePads))).execute();
	}
	
	private long owned(@NotNull Connection connection,@NotNull Player player) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE OwnerID=? AND Global=?;")) {
			statement.setString(1,player.getUniqueId().toString());
			statement.setBoolean(2,false);
			try (ResultSet results = statement.executeQuery()) {
				results.next();
				return Math.max(results.getLong(1),0);
			}
		}
	}
	
	public final long owned(@NotNull Player player) throws SQLException {
		try (Connection connection = getConnection()) {
			return owned(connection,player);
		}
	}
	
	public final void use(@NotNull TelePad telePad,int newUsed,@Nullable Runnable onSuccess) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET Used=? WHERE World=? AND X=? AND Y=? AND Z=?;")) {
				statement.setInt(1,newUsed);
				setLocation(statement,telePad,2);
				Utils.executeUpdateFailNoResults(statement);
				return onSuccess;
			} catch (SQLException e) {e.printStackTrace();}
			return null;
		}).abortIfNull().syncLast(Runnable::run).execute();
	}
	
	public final void setGlobal(@NotNull TelePad telePad,@NotNull Player player,boolean newIsGlobal,@Nullable Runnable onSuccess,@Nullable Consumer<@NotNull Long> onFailLimit,@Nullable Runnable onFailDatabase) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection()) {
				if (!newIsGlobal) {
					long owned = owned(connection,player);
					long limit = TelePadtationMain.configManager().limit(player);
					if (owned + 1 > limit) {
						if (onFailLimit != null) onFailLimit.accept(limit);
						return null;
					}
				}
				try (PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET Global=? WHERE World=? AND X=? AND Y=? AND Z=?;")) {
					statement.setBoolean(1,newIsGlobal);
					setLocation(statement,telePad,2);
					Utils.executeUpdateFailNoResults(statement);
					return onSuccess;
				}
			} catch (SQLException e) {
				Utils.runNotNull(onFailDatabase);
			}
			return null;
		}).abortIfNull().syncLast(Runnable::run).execute();
	}
	
	public final void setName(@NotNull TelePad telePad,@Nullable String newName,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET Name=? WHERE World=? AND X=? AND Y=? AND Z=?;")) {
				statement.setString(1,newName);
				setLocation(statement,telePad,2);
				Utils.executeUpdateFailNoResults(statement);
				return onSuccess;
			} catch (SQLException e) {
				Utils.runNotNull(onFail);
			}
			return null;
		}).abortIfNull().syncLast(Runnable::run).execute();
	}
	
	public final void setExtraFuel(@NotNull TelePad telePad,int newExtraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET Fuel=? WHERE World=? AND X=? AND Y=? AND Z=?;")) {
				statement.setInt(1,newExtraFuel);
				setLocation(statement,telePad,2);
				Utils.executeUpdateFailNoResults(statement);
				return onSuccess;
			} catch (SQLException e) {
				Utils.runNotNull(onFail);
			}
			return null;
		}).abortIfNull().syncLast(Runnable::run).execute();
	}
	
	public final void recharge(@NotNull TelePad telePad,int newUsed,int newExtraFuel,@Nullable Runnable onSuccess,@Nullable Runnable onFail) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE " + table + " SET Used=?,Fuel=? WHERE World=? AND X=? AND Y=? AND Z=?;")) {
				statement.setInt(1,newUsed);
				statement.setInt(2,newExtraFuel);
				setLocation(statement,telePad,3);
				Utils.executeUpdateFailNoResults(statement);
				return onSuccess;
			} catch (SQLException e) {
				e.printStackTrace();
				Utils.runNotNull(onFail);
			}
			return null;
		}).abortIfNull().syncLast(Runnable::run).execute();
	}
	
	public final void fixWorld(@NotNull World world) {
		TelePadtationMain.taskChainFactory().newChain().asyncFirst(() -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE World=?;")) {
				statement.setString(1,world.getName());
				try (ResultSet results = statement.executeQuery()) {
					List<TelePad.TelePadPlaceable> telePads = new ArrayList<>();
					while (results.next()) {
						try {
							TelePad.TelePadPlaceable telePad = getTelePad(results,world);
							if (telePad != null) telePads.add(telePad);
						} catch (SQLException e) {e.printStackTrace();}
					}
					return telePads;
				}
			} catch (SQLException e) {e.printStackTrace();}
			return null;
		}).abortIfNull().sync(telePads -> {
			telePads = TelePadtationMain.configManager().fixOrShouldRemove(telePads);
			return telePads.isEmpty() ? null : telePads;
		}).abortIfNull().asyncLast(telePads -> {
			try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE World=? AND X=? AND Y=? AND Z=?;")) {
				for (TelePad telePad : telePads) {
					setLocation(statement,telePad,1);
					statement.addBatch();
				}
				int removed = statement.executeBatch().length;
				if (removed != telePads.size()) throw new SQLException("While fixing world \"" + world.getName() + "\", " + removed + " TelePads were removed, but " + telePads.size() + " were supposed to be removed (" + removed + "/" + telePads.size() + ")");
			} catch (SQLException e) {e.printStackTrace();}
		}).execute();
	}
}