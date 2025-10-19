/**
 * Contains a loader framework, used to bootstrap PySpigot into its own class loader at plugin load time. Used on platforms
 * that do not support adding JARs to the plugin class path at runtime.
 */
package dev.magicmq.pyspigot.loader;