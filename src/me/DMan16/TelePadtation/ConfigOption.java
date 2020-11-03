package me.DMan16.TelePadtation;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

@SuppressWarnings("unchecked")
public class ConfigOption<V> {
	private final FileConfiguration config;
	private final String optionName;
	private V value;
	private final V defaultValue;
	private final String[] comment;

	public ConfigOption(FileConfiguration config, String optionName, V defaultValue, String[] comment) {
		this.config = config;
		this.optionName = optionName;
		this.defaultValue = defaultValue;
		this.comment = comment;
		setValue();
	}

	private void setValue() {
		try {
			value = (V) config.get(optionName,defaultValue);
			if (value.getClass() == Integer.class) {
				if ((Integer) value <= 0) {
					throw new Exception();
				}
			}
			if (value.getClass() == Double.class) {
				if ((Double) value < 0.0) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			TelePadtation.getLog().log(Level.WARNING,"Config: Failed to read value of: \"" + optionName + "\"! Using default value instead!");
			value = defaultValue;
		}
	}
	

	public void setValue(V v) {
		try {
			value = (V) config.get(optionName,v);
			if (value.getClass() == Integer.class) {
				if ((Integer) value <= 0) {
					throw new Exception();
				}
			}
			if (value.getClass() == Double.class) {
				if ((Double) value < 0.0) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			TelePadtation.getLog().log(Level.WARNING,"Config: Failed to read value of: \"" + optionName + "\"! Using default value instead!");
			value = v;
		}
	}

	public V getValue() {
		return (V) value;
	}

	public String[] getComment() {
		return comment;
	}

	public String toString() {
		String string = "";
		if (comment != null) {
			for (String comLine : comment) {
				if (!comLine.isEmpty()) {
					string = string + "# " + comLine + "\n";
				}
			}
		}
		string = string + optionName + ": ";
		if (value.getClass().isAssignableFrom(String.class)) {
			string = string + "'" + value.toString() + "'";
		} else if ((value instanceof List)) {
			StringBuilder builder = new StringBuilder();
			builder.append("\n");
			int listSize = ((List<?>) value).size();
			for (int index = 0; index < listSize; index++) {
				builder.append("  - " + ((List<?>) value).get(index) + (index == listSize - 1 ? "" : "\n"));
			}
			string = string + builder.toString();
		} else {
			string = string + value.toString();
		}
		return string;
	}
}