package com.focess.dropitem.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NMSManager {
	// public static Constructor<?> ConstructorEntityPlayer;
	public static Constructor<?> ConstructorPlayerInteractManager;
	public final static Class<?> CraftServer;
	public final static Class<?> CraftWorld;
	public final static Class<?> EntityPlayer;
	private final static Map<Class<?>, Map<String, Field>> loadedFields = new HashMap<>();
	private final static Map<Class<?>, Map<String, Method>> loadedMethods = new HashMap<>();
	private final static Map<String, Class<?>> loadedNMSClasses = new HashMap<>();
	public final static Class<?> MinecraftServer;

	// public final static Class<?> PlayerInteractManager;

	private static String versionString;
	public final static Class<?> World;

	public final static Class<?> WorldServer;

	static {
		World = NMSManager.getNMSClass("World");
		MinecraftServer = NMSManager.getNMSClass("MinecraftServer");
		WorldServer = NMSManager.getNMSClass("WorldServer");
		CraftWorld = NMSManager.getCraftClass("CraftWorld");
		CraftServer = NMSManager.getCraftClass("CraftServer");
		EntityPlayer = NMSManager.getNMSClass("EntityPlayer");
		// PlayerInteractManager =
		// NMSManager.getNMSClass("PlayerInteractManager");
		try {
			// NMSManager.ConstructorEntityPlayer = NMSManager.EntityPlayer
			// .getConstructor(new Class[] { NMSManager.MinecraftServer,
			// NMSManager.WorldServer, GameProfile.class,
			// NMSManager.PlayerInteractManager });
			// NMSManager.ConstructorPlayerInteractManager =
			// NMSManager.PlayerInteractManager
			// .getConstructor(new Class[] { NMSManager.World });
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static Object getConnection(final Player player) {
		final Method getHandleMethod = NMSManager.getMethod(player.getClass(), "getHandle", new Class[0]);

		if (getHandleMethod != null)
			try {
				final Object nmsPlayer = getHandleMethod.invoke(player, new Object[0]);
				final Field playerConField = NMSManager.getField(nmsPlayer.getClass(), "playerConnection");
				return playerConField.get(nmsPlayer);
			} catch (final Exception e) {
				e.printStackTrace();
			}

		return null;
	}

	public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>[] params) {
		try {
			return clazz.getConstructor(params);
		} catch (final NoSuchMethodException e) {
		}
		return null;
	}

	public static Class<?> getCraftClass(final String nmsClassName) {
		if (NMSManager.loadedNMSClasses.containsKey(nmsClassName))
			return NMSManager.loadedNMSClasses.get(nmsClassName);
		final String clazzName = "org.bukkit.craftbukkit." + NMSManager.getVersion() + nmsClassName;
		Class<?> clazz;
		try {
			clazz = Class.forName(clazzName);
		} catch (final Throwable t) {
			t.printStackTrace();
			return NMSManager.loadedNMSClasses.put(nmsClassName, null);
		}
		NMSManager.loadedNMSClasses.put(nmsClassName, clazz);
		return clazz;
	}

	public static Field getField(final Class<?> clazz, final String fieldName) {
		if (!NMSManager.loadedFields.containsKey(clazz))
			NMSManager.loadedFields.put(clazz, new HashMap<String, Field>());

		final Map<String, Field> fields = NMSManager.loadedFields.get(clazz);

		if (fields.containsKey(fieldName))
			return fields.get(fieldName);
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			fields.put(fieldName, field);
			NMSManager.loadedFields.put(clazz, fields);
			return field;
		} catch (final Exception e) {
			e.printStackTrace();
			fields.put(fieldName, null);
			NMSManager.loadedFields.put(clazz, fields);
		}
		return null;
	}

	public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>[] params) {
		if (!NMSManager.loadedMethods.containsKey(clazz))
			NMSManager.loadedMethods.put(clazz, new HashMap<String, Method>());
		final Map<String, Method> methods = NMSManager.loadedMethods.get(clazz);
		if (methods.containsKey(methodName))
			return methods.get(methodName);
		try {
			final Method method = clazz.getDeclaredMethod(methodName, params);
			method.setAccessible(true);
			methods.put(methodName, method);
			NMSManager.loadedMethods.put(clazz, methods);
			return method;
		} catch (final Exception e) {
			e.printStackTrace();
			methods.put(methodName, null);
			NMSManager.loadedMethods.put(clazz, methods);
		}
		return null;
	}

	public static Class<?> getNMSClass(final String nmsClassName) {
		if (NMSManager.loadedNMSClasses.containsKey(nmsClassName))
			return NMSManager.loadedNMSClasses.get(nmsClassName);

		final String clazzName = "net.minecraft.server." + NMSManager.getVersion() + nmsClassName;
		Class<?> clazz;
		try {
			clazz = Class.forName(clazzName);
		} catch (final Throwable t) {

			t.printStackTrace();
			return NMSManager.loadedNMSClasses.put(nmsClassName, null);
		}
		NMSManager.loadedNMSClasses.put(nmsClassName, clazz);
		return clazz;
	}

	public static String getVersion() {
		if (NMSManager.versionString == null) {
			final String name = Bukkit.getServer().getClass().getPackage().getName();
			NMSManager.versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
		}
		return NMSManager.versionString;
	}
}