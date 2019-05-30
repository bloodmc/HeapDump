package me.bloodmc.heap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.management.MBeanServer;

public class HeapPlugin extends JavaPlugin implements CommandExecutor {

    private Logger logger = this.getLogger();
    private Path configPath = Paths.get(".", "plugins", "HeapDump");
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("heapdump").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
        if ((cmd.getName().equalsIgnoreCase("heapdump"))) {
            final Path path = configPath.resolve("dumps").resolve("heap-dump-" + this.formatter.format(LocalDateTime.now()) + "-server.hprof");
            sender.sendMessage("Writing JVM heap dump to: " + path.toFile().getAbsolutePath());
            dumpHeap(path.toFile());
            return true;
        }

        return false;
    }

    public void dumpHeap(File file) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            final Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final Object hotspot = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            final Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspot, file.getPath(), true);
        } catch (Throwable t) {
            this.logger.severe("Could not write heap to " + file.getAbsolutePath());
        }
    }
}
