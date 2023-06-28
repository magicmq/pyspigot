package dev.magicmq.pyspigot.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    private static final String MC_VERSION;

    static {
        MC_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static Class<?> getNMSClass(String packageName, String className) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft." + packageName + "." + className);
        } catch (ClassNotFoundException ignored) {
            return Class.forName("net.minecraft.server." + MC_VERSION + "." + className);
        }
    }

    public static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + MC_VERSION + "." + className);
    }

    public static Class<?> getCraftBukkitClass(String packageName, String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + MC_VERSION + "." + packageName + "." + className);
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName))
                return method;
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName))
                return field;
        }
        return null;
    }
}
