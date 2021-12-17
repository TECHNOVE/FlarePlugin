package co.technove.flareplugin.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSHelper {

    private static Object server;
    private static Field recentTps;

    private static Object tickTimes5s;
    private static Method getTimes;

    public static void initialize() throws ReflectiveOperationException {
        Class<?> serverClass;
        try {
            serverClass = Class.forName("net.minecraft.server.MinecraftServer");
        } catch (ClassNotFoundException e) {
            final String ver = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
            if (!ver.startsWith("v")) {
                throw e;
            }

            serverClass = Class.forName("net.minecraft.server." + ver + ".MinecraftServer");
        }
        final Method getServer = serverClass.getMethod("getServer");

        server = getServer.invoke(null);
        recentTps = serverClass.getField("recentTps");

        tickTimes5s = serverClass.getField("tickTimes5s").get(server);
        getTimes = tickTimes5s.getClass().getMethod("getTimes");
    }

    public static long[] getTickTimes5s() {
        try {
            return (long[]) getTimes.invoke(tickTimes5s);
        } catch (final IllegalAccessException | InvocationTargetException | NullPointerException e) {
            return new long[0];
        }
    }

    public static double[] getTps() {
        try {
            return (double[]) recentTps.get(server);
        } catch (final IllegalAccessException | NullPointerException e) {
            return new double[0];
        }
    }

}
