package uk.co.techarchitect.wasmcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import uk.co.techarchitect.wasmcraft.computer.ComputerCore;
import uk.co.techarchitect.wasmcraft.computer.FileSystem;
import uk.co.techarchitect.wasmcraft.computer.Terminal;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.List;
import java.util.UUID;

public class DroneComputerAdapter {
    private final DroneEntity drone;

    public DroneComputerAdapter(DroneEntity drone) {
        this.drone = drone;
    }

    public List<String> getOutputHistory() {
        return drone.getComputerCore().getTerminal().getHistory();
    }

    public List<String> getCommandHistory() {
        return drone.getComputerCore().getCommandHistory();
    }

    public List<String> getFileNames() {
        return drone.getComputerCore().getFileSystem().listFiles();
    }

    public UUID getId() {
        return drone.getComputerCore().getId();
    }

    public Level getLevel() {
        return drone.level();
    }

    public BlockPos getPosition() {
        return drone.blockPosition();
    }
}
