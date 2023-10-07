package me.DMan16.TelePadtation.Enums;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.DMan16.TelePadtation.Utils.Utils;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public enum Heads {
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/decoration/36295-nether-portal">View</a>
	 */
	PORTABLE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSJ9fX0="),
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/decoration/23202-globe">View</a>
	 */
	GLOBAL("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM0NjdhNTMxOTc4ZDBiOGZkMjRmNTYyODVjNzI3MzRkODRmNWVjODhlMGI0N2M0OTMyMzM2Mjk3OWIzMjNhZiJ9fX0="),
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/decoration/116-ender-pearl">View</a>
	 */
	ACTIVE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWNiN2MyMWNjNDNkYzE3Njc4ZWU2ZjE2NTkxZmZhYWIxZjYzN2MzN2Y0ZjZiYmQ4Y2VhNDk3NDUxZDc2ZGI2ZCJ9fX0="),
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/decoration/1159-ender-pearl-red">View</a>
	 */
	OBSTRUCTED("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJjMmY2ZmE3ZWM1MzA0MzVjNDMxNTcyOTM4YjlmZWI5NTljNDIyOThlNTU1NDM0MDI2M2M2NTI3MSJ9fX0="),
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/decoration/3937-pearl">View</a>
	 */
	INACTIVE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjdlNmFkNGI3OGJkMzUxODdhNjU3MDg5OTE4MTdlMjY2OTlmOTAyYzc3MzhjODFjMzc1ODU5ZDcyNzUzOSJ9fX0="),
	/**
	 * <a href="https://minecraft-heads.com/custom-heads/miscellaneous/27523-settings">View</a>
	 */
	EDIT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkNDliYWU5NWM3OTBjM2IxZmY1YjJmMDEwNTJhNzE0ZDYxODU0ODFkNWIxYzg1OTMwYjNmOTlkMjMyMTY3NCJ9fX0=");
	
	private static final @NotNull JSONParser PARSER = new JSONParser();
	private static final Base64.@NotNull Decoder DECODER = Base64.getDecoder();
	private static Method setProfileMethod = null;
	
	private final String skin;
	
	Heads(String skin) {
		this.skin = skin;
	}
	
	@NotNull
	public String skin() {
		return skin;
	}
	
	@NotNull
	public static SkullMeta setSkin(@NotNull SkullMeta meta,@NotNull String skin) {
		setSkinResult(meta,skin);
		return meta;
	}
	
	public static boolean setSkinResult(@NotNull SkullMeta meta,@NotNull String skin) {
		if (isSkin(skin)) try {
			if (setProfileMethod == null) setProfileMethod = meta.getClass().getDeclaredMethod("setProfile",GameProfile.class);
			setProfileMethod.setAccessible(true);
			UUID id = new UUID(skin.substring(skin.length() - 20).hashCode(),skin.substring(skin.length() - 10).hashCode());
			GameProfile profile = new GameProfile(id,"D");
			profile.getProperties().put("textures",new Property("textures",skin));
			setProfileMethod.invoke(meta,profile);
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	public static boolean isSkin(@NotNull String str) {
		try {
			Objects.requireNonNull((JSONObject) ((JSONObject) ((JSONObject) PARSER.parse(Utils.toLowercase(new String(DECODER.decode(str))))).get("textures")).get("skin"));
			return true;
		} catch (Exception e) {}
		return false;
	}
}