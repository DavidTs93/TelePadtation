package me.DMan16.TelePadtation.Managers.Database;

import com.zaxxer.hikari.HikariConfig;
import me.DMan16.TelePadtation.Utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class SQLDatabase extends DatabaseConnection {
	private final @NotNull String address;
	private final @Nullable String username;
	private final @Nullable String password;
	
	public SQLDatabase(@NotNull String host,@NotNull String table,int maxPoolSize,long connectionTimeout,@NotNull String database,int port,@Nullable String username,@Nullable String password) throws SQLException {
		super(table,maxPoolSize,connectionTimeout);
		this.address = "jdbc:mysql://" + host + (port > 0 ? ":" + port : "") + ":" + database + "?autoReconnect=true&useSSL=false";
		this.username = Utils.isNullOrEmpty(username) ? null : username;
		this.password = Utils.isNullOrEmpty(password) ? null : password;
		connect();
		createTable();
	}
	
	@NotNull
	protected String connectionAddress() {
		return address;
	}
	
	protected void setExtraHikariConfigs(@NotNull HikariConfig hikariConfig) {
		if (username != null) hikariConfig.setUsername(username);
		if (password != null) hikariConfig.setPassword(password);
	}
	
	@NotNull
	protected String triggerLogic() {
		return "CREATE TRIGGER " + TRIGGER_NAME +
				" BEFORE INSERT ON " + table +
				" FOR EACH ROW " +
				" BEGIN" +
				"    IF EXISTS (" +
				"        SELECT 1" +
				"        FROM " + table +
				"        WHERE World = NEW.World" +
				"          AND X = NEW.X" +
				"          AND (Y = NEW.Y + 1 OR Y = NEW.Y + 2 OR Y = NEW.Y - 1 OR Y = NEW.Y - 2)" +
				"          AND Z = NEW.Z" +
				"    ) THEN" +
				"        SIGNAL SQLSTATE '45000'" +
				"        SET MESSAGE_TEXT = 'Insertion failed: nearby TelePads detected';" +
				"    END IF;" +
				" END;";
	}
}