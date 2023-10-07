package me.DMan16.TelePadtation.Managers.Database;

import com.zaxxer.hikari.HikariConfig;
import me.DMan16.TelePadtation.TelePadtationMain;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public final class LocalDatabase extends DatabaseConnection {
	private final File file;
	
	public LocalDatabase(@NotNull String table,int maxPoolSize,long connectionTimeout) throws IOException,ClassNotFoundException,SQLException {
		super(table,maxPoolSize,connectionTimeout);
		Class.forName("org.sqlite.JDBC");
		file = new File(TelePadtationMain.getInstance().getDataFolder(),"database.db");
		if (!file.exists()) if (!file.createNewFile()) throw new IOException("Couldn't create database file!");
		connect();
		createTable();
	}
	
	@NotNull
	protected String connectionAddress() {
		return "jdbc:sqlite:" + file;
	}
	
	protected void setExtraHikariConfigs(@NotNull HikariConfig hikariConfig) {}
	
	@Override
	protected void fixColumns(@NotNull DatabaseMetaData data,@NotNull Statement statement,@NotNull String table,@NotNull Collection<String> columns,String @NotNull ... indexes) throws SQLException {
		String[] split;
		for (String column : columns) {
			if (column == null) continue;
			split = column.split(" ",2);
			if (split.length > 1) if (!data.getColumns(null,null,table,split[0]).next()) statement.execute("ALTER TABLE " + table + " ADD " + split[0] + " " + split[1] + ";");
		}
		for (String index : indexes) {
			if (index == null) continue;
			split = index.split("\\(",2);
			if (split.length > 1) statement.execute("CREATE INDEX IF NOT EXISTS " + split[0].trim() + " ON " + table + " (" + split[1] + ";");
		}
	}
	
	@NotNull
	protected String triggerLogic() {
		return "CREATE TRIGGER " + TRIGGER_NAME +
				" BEFORE INSERT ON " + table +
				" BEGIN" +
				"    SELECT RAISE(FAIL, 'Insertion failed: nearby TelePads detected')" +
				"    WHERE EXISTS (" +
				"        SELECT 1" +
				"        FROM " + table +
				"        WHERE World = NEW.World" +
				"          AND X = NEW.X" +
				"          AND (Y = NEW.Y + 1 OR Y = NEW.Y + 2 OR Y = NEW.Y - 1 OR Y = NEW.Y - 2)" +
				"          AND Z = NEW.Z" +
				"    );" +
				" END;";
	}
}