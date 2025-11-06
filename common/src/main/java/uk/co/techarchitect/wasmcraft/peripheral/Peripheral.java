package uk.co.techarchitect.wasmcraft.peripheral;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public interface Peripheral {
    UUID getId();

    String getType();

    String getLabel();

    void setLabel(String label);

    UUID getOwner();

    BlockPos getPosition();
}
