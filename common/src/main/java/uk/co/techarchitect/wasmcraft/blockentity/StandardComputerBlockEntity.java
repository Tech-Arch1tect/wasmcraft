package uk.co.techarchitect.wasmcraft.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import uk.co.techarchitect.wasmcraft.chunkloading.ChunkLoadingManager;
import uk.co.techarchitect.wasmcraft.computer.ComputerBlockEntityBase;
import uk.co.techarchitect.wasmcraft.computer.command.builtin.*;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.peripheral.Peripheral;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralManager;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.context.ContextHelper;
import uk.co.techarchitect.wasmcraft.wasm.context.MonitorContext;
import uk.co.techarchitect.wasmcraft.wasm.context.PeripheralContext;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StandardComputerBlockEntity extends ComputerBlockEntityBase {
    private static final double PERIPHERAL_RANGE = 16.0;

    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];
    private final Map<String, UUID> connectedPeripherals = new HashMap<>();
    private final ContextHelper contextHelper;

    public StandardComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        this.contextHelper = new ContextHelper(
            connectedPeripherals,
            redstoneOutputs,
            redstoneInputs,
            new ContextHelper.PeripheralProvider() {
                @Override
                public BlockPos getPosition() {
                    return worldPosition;
                }
                @Override
                public UUID getOwner() {
                    return computerCore.getOwner();
                }
                @Override
                public double getPeripheralRange() {
                    return PERIPHERAL_RANGE;
                }
            },
            new ContextHelper.LevelProvider() {
                @Override
                public net.minecraft.server.level.ServerLevel getLevel() {
                    return level instanceof net.minecraft.server.level.ServerLevel sl ? sl : null;
                }
            },
            new ContextHelper.YawProvider() {
                @Override
                public float getYaw() {
                    Direction facing = getBlockState().getValue(uk.co.techarchitect.wasmcraft.block.ComputerBlock.FACING);
                    return facing.toYRot();
                }
            }
        );
    }

    public ContextHelper getContextHelper() {
        return contextHelper;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ChunkLoadingManager.getInstance().registerChunkLoader(getId(), serverLevel, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    public void onBlockBroken() {
        if (level != null && !level.isClientSide) {
            ChunkLoadingManager.getInstance().unregisterChunkLoader(getId());
        }
    }

    @Override
    protected void registerCommands() {
        computerCore.getCommandRegistry().register(new HelpCommand(computerCore.getCommandRegistry()));
        computerCore.getCommandRegistry().register(new ClearCommand());
        computerCore.getCommandRegistry().register(new LsCommand());
        computerCore.getCommandRegistry().register(new RmCommand());
        computerCore.getCommandRegistry().register(new DownloadCommand());
        computerCore.getCommandRegistry().register(new RunCommand(this::getWasmContext));
        computerCore.getCommandRegistry().register(new StopCommand());
    }

    @Override
    protected WasmContext[] getContexts() {
        return new WasmContext[] { contextHelper };
    }

    private Direction relativeToWorldDirection(int relativeSide) {
        if (level == null) return Direction.NORTH;
        Direction facing = getBlockState().getValue(uk.co.techarchitect.wasmcraft.block.ComputerBlock.FACING);

        return switch (relativeSide) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> facing;
            case 3 -> facing.getOpposite();
            case 4 -> facing.getClockWise();
            case 5 -> facing.getCounterClockWise();
            default -> Direction.NORTH;
        };
    }

    public void updateRedstoneInputs() {
        if (level == null || level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            int index = direction.get3DDataValue();
            redstoneInputs[index] = level.getSignal(worldPosition.relative(direction), direction);
        }
    }

    public int getRedstoneOutput(Direction worldDirection) {
        int index = worldDirection.get3DDataValue();
        if (index >= 0 && index < 6) {
            return redstoneOutputs[index];
        }
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putIntArray("RedstoneOutputs", redstoneOutputs);
        tag.putIntArray("RedstoneInputs", redstoneInputs);

        CompoundTag peripheralsTag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : connectedPeripherals.entrySet()) {
            peripheralsTag.putUUID(entry.getKey(), entry.getValue());
        }
        tag.put("ConnectedPeripherals", peripheralsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("RedstoneOutputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneOutputs");
            System.arraycopy(loaded, 0, redstoneOutputs, 0, Math.min(loaded.length, redstoneOutputs.length));
        }

        if (tag.contains("RedstoneInputs", Tag.TAG_INT_ARRAY)) {
            int[] loaded = tag.getIntArray("RedstoneInputs");
            System.arraycopy(loaded, 0, redstoneInputs, 0, Math.min(loaded.length, redstoneInputs.length));
        }

        connectedPeripherals.clear();
        if (tag.contains("ConnectedPeripherals", Tag.TAG_COMPOUND)) {
            CompoundTag peripheralsTag = tag.getCompound("ConnectedPeripherals");
            for (String key : peripheralsTag.getAllKeys()) {
                connectedPeripherals.put(key, peripheralsTag.getUUID(key));
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Computer");
    }

    @Override
    public void prepareForExecution() {
        updateRedstoneInputs();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComputerMenu(containerId, playerInventory, this, this.worldPosition, -1);
    }
}
