/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.bukkit.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A utility class to simplify reflection for working with CraftBukkit and NMS classes.
 */
public final class ReflectionUtils {

    private static final String MC_VERSION;

    static {
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        MC_VERSION = split.length >= 4 ? split[3] : "";
    }

    private ReflectionUtils() {}

    public static Class<?> getNMSClass(String packageName, String className) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft." + packageName + "." + className);
        } catch (ClassNotFoundException ignored) {
            return Class.forName("net.minecraft.server." + MC_VERSION + "." + className);
        }
    }

    public static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName("org.bukkit.craftbukkit." + className);
        } catch (ClassNotFoundException ignored) {
            return Class.forName("org.bukkit.craftbukkit." + MC_VERSION + "." + className);
        }
    }

    public static Class<?> getCraftBukkitClass(String packageName, String className) throws ClassNotFoundException {
        try {
            return Class.forName("org.bukkit.craftbukkit." + packageName + "." + className);
        } catch (ClassNotFoundException ignored) {
            return Class.forName("org.bukkit.craftbukkit." + MC_VERSION + "." + packageName + "." + className);
        }
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
