package uk.co.techarchitect.wasmcraft.peripheral;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public abstract class PeripheralBlockEntity extends BlockEntity implements Peripheral {
    private UUID id;
    private String label;
    private UUID owner;

    public PeripheralBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.id = UUID.randomUUID();
        this.label = generateDefaultLabel();
    }

    protected abstract String generateDefaultLabel();

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        String oldLabel = this.label;
        this.label = label;

        if (level != null && !level.isClientSide) {
            PeripheralManager.getInstance().updateLabel(id, oldLabel, label);
            setChanged();
        }
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public BlockPos getPosition() {
        return worldPosition;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("PeripheralId", id);
        tag.putString("PeripheralLabel", label);
        if (owner != null) {
            tag.putUUID("PeripheralOwner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("PeripheralId")) {
            UUID oldId = this.id;
            this.id = tag.getUUID("PeripheralId");
        }
        if (tag.contains("PeripheralLabel")) {
            this.label = tag.getString("PeripheralLabel");
        }
        if (tag.hasUUID("PeripheralOwner")) {
            this.owner = tag.getUUID("PeripheralOwner");
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && !level.isClientSide) {
            PeripheralManager.getInstance().registerPeripheral(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            PeripheralManager.getInstance().unregisterPeripheral(id);
        }
    }

    public void onPlaced(UUID placerUUID) {
        this.owner = placerUUID;
        if (level != null && !level.isClientSide) {
            PeripheralManager.getInstance().registerPeripheral(this);
        }
    }
}
