package uk.co.techarchitect.wasmcraft.computer;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystem {
    private final Map<String, byte[]> files = new HashMap<>();

    public void put(String filename, byte[] data) {
        files.put(filename, data);
    }

    public byte[] get(String filename) {
        return files.get(filename);
    }

    public boolean contains(String filename) {
        return files.containsKey(filename);
    }

    public boolean remove(String filename) {
        return files.remove(filename) != null;
    }

    public List<String> listFiles() {
        return new ArrayList<>(files.keySet());
    }

    public Map<String, byte[]> getFiles() {
        return new HashMap<>(files);
    }

    public boolean isEmpty() {
        return files.isEmpty();
    }

    public void saveToNbt(CompoundTag tag) {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            tag.putByteArray(entry.getKey(), entry.getValue());
        }
    }

    public void loadFromNbt(CompoundTag tag) {
        files.clear();
        for (String key : tag.getAllKeys()) {
            files.put(key, tag.getByteArray(key));
        }
    }
}
