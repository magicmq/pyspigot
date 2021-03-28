package dev.magicmq.pyspigot.utils;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getMinecraftVersionString() + "." + name);
    }

    private static String getMinecraftVersionString() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
