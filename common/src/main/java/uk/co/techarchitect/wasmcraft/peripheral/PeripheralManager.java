package uk.co.techarchitect.wasmcraft.peripheral;

import net.minecraft.core.BlockPos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PeripheralManager {
    private static final PeripheralManager INSTANCE = new PeripheralManager();

    private final Map<UUID, WeakReference<Peripheral>> peripheralsById = new ConcurrentHashMap<>();
    private final Map<String, WeakReference<Peripheral>> peripheralsByLabel = new ConcurrentHashMap<>();

    private PeripheralManager() {}

    public static PeripheralManager getInstance() {
        return INSTANCE;
    }

    public void registerPeripheral(Peripheral peripheral) {
        UUID id = peripheral.getId();
        String label = peripheral.getLabel();

        peripheralsById.put(id, new WeakReference<>(peripheral));
        if (label != null && !label.isEmpty()) {
            peripheralsByLabel.put(label, new WeakReference<>(peripheral));
        }
    }

    public void unregisterPeripheral(UUID id) {
        WeakReference<Peripheral> ref = peripheralsById.remove(id);
        if (ref != null) {
            Peripheral peripheral = ref.get();
            if (peripheral != null) {
                String label = peripheral.getLabel();
                if (label != null) {
                    peripheralsByLabel.remove(label);
                }
            }
        }
    }

    public void updateLabel(UUID id, String oldLabel, String newLabel) {
        if (oldLabel != null) {
            peripheralsByLabel.remove(oldLabel);
        }
        if (newLabel != null && !newLabel.isEmpty()) {
            WeakReference<Peripheral> ref = peripheralsById.get(id);
            if (ref != null) {
                peripheralsByLabel.put(newLabel, ref);
            }
        }
    }

    public Peripheral findByLabel(String label, UUID playerUUID) {
        WeakReference<Peripheral> ref = peripheralsByLabel.get(label);
        if (ref == null) {
            return null;
        }

        Peripheral peripheral = ref.get();
        if (peripheral == null) {
            peripheralsByLabel.remove(label);
            return null;
        }

        if (!peripheral.getOwner().equals(playerUUID)) {
            return null;
        }

        return peripheral;
    }

    public Peripheral findById(UUID id) {
        WeakReference<Peripheral> ref = peripheralsById.get(id);
        if (ref == null) {
            return null;
        }

        Peripheral peripheral = ref.get();
        if (peripheral == null) {
            peripheralsById.remove(id);
            return null;
        }

        return peripheral;
    }

    public List<Peripheral> findInRange(BlockPos center, double range, UUID playerUUID) {
        List<Peripheral> result = new ArrayList<>();

        for (WeakReference<Peripheral> ref : peripheralsById.values()) {
            Peripheral peripheral = ref.get();
            if (peripheral == null) {
                continue;
            }

            if (!peripheral.getOwner().equals(playerUUID)) {
                continue;
            }

            BlockPos pos = peripheral.getPosition();
            double distance = Math.sqrt(center.distSqr(pos));
            if (distance <= range) {
                result.add(peripheral);
            }
        }

        return result;
    }

    public void cleanup() {
        peripheralsById.entrySet().removeIf(entry -> entry.getValue().get() == null);
        peripheralsByLabel.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }
}
