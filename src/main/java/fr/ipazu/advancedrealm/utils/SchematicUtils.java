package fr.ipazu.advancedrealm.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.io.File;
import java.io.FileInputStream;

public class SchematicUtils {
    private Location location;
    private File file;

    public SchematicUtils(Location location, File file) {
        this.location = location;
        this.file = file;
    }

    public void paste() throws Exception {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new Exception("No clipboard format found for file: " + file.getName());
        }
        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        }

        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();
        org.bukkit.World world = location.getWorld();
        int baseX = location.getBlockX();
        int baseY = location.getBlockY();
        int baseZ = location.getBlockZ();

        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    BlockState state = clipboard.getBlock(BlockVector3.at(x, y, z));
                    if (state.getBlockType().getMaterial().isAir()) continue;
                    BlockData data = BukkitAdapter.adapt(state);
                    int wx = baseX + (x - origin.x());
                    int wy = baseY + (y - origin.y());
                    int wz = baseZ + (z - origin.z());
                    world.getBlockAt(wx, wy, wz).setBlockData(data, false);
                }
            }
        }
    }
}
