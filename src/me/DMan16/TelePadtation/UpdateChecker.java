package me.DMan16.TelePadtation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.util.Consumer;

public class UpdateChecker {
	private int resourceId;
	
	UpdateChecker(int resourceId) {
		this.resourceId = resourceId;
	}
	
	void getVersion(final Consumer<String> consumer) {
		try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
				Scanner scanner = new Scanner(inputStream)) {
			if (scanner.hasNext()) {
				consumer.accept(scanner.next());
			}
		} catch (IOException exception) {
			Utils.chatColorsLogPlugin("Error while looking for updates: " + exception.getMessage());
		}
	}
}