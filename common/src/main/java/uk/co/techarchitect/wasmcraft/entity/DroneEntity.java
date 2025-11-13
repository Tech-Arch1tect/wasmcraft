package uk.co.techarchitect.wasmcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import uk.co.techarchitect.wasmcraft.computer.ComputerEntityBase;
import uk.co.techarchitect.wasmcraft.computer.command.builtin.*;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.context.ContextHelper;
import uk.co.techarchitect.wasmcraft.wasm.context.MonitorContext;
import uk.co.techarchitect.wasmcraft.wasm.context.PeripheralContext;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DroneEntity extends ComputerEntityBase {
    private static final int INVENTORY_SIZE = 27;
    private static final double PERIPHERAL_RANGE = 16.0;
    private static final EntityDataAccessor<Float> HOVER_HEIGHT = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);

    private final SimpleContainer inventory;
    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];
    private final Map<String, UUID> connectedPeripherals = new HashMap<>();
    private final ContextHelper contextHelper;

    private double targetHoverHeight;

    public DroneEntity(EntityType<? extends DroneEntity> entityType, Level level) {
        super(entityType, level, "Drone initialized. Type 'help' for commands.");
        this.inventory = new SimpleContainer(INVENTORY_SIZE);
        this.targetHoverHeight = 1.0;
        this.noCulling = true;
        this.contextHelper = new ContextHelper(
            connectedPeripherals,
            redstoneOutputs,
            redstoneInputs,
            new ContextHelper.PeripheralProvider() {
                @Override
                public BlockPos getPosition() {
                    return blockPosition();
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
                    return level() instanceof net.minecraft.server.level.ServerLevel sl ? sl : null;
                }
            }
        );
    }

    public ContextHelper getContextHelper() {
        return contextHelper;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.4);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOVER_HEIGHT, 1.0f);
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

    @Override
    protected String getComputerDisplayName() {
        return "Drone Computer";
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            maintainHoverHeight();
            updateRedstoneInputs();
        }
    }

    private void maintainHoverHeight() {
        BlockPos belowPos = this.blockPosition().below();
        double currentY = this.getY();
        double targetY = belowPos.getY() + targetHoverHeight;

        Vec3 motion = this.getDeltaMovement();
        double yMotion = motion.y;

        double diff = targetY - currentY;
        if (Math.abs(diff) > 0.1) {
            yMotion = diff * 0.1;
        } else {
            yMotion *= 0.8;
        }

        this.setDeltaMovement(motion.x * 0.9, yMotion, motion.z * 0.9);
    }

    private void updateRedstoneInputs() {
        BlockPos pos = this.blockPosition();
        for (int i = 0; i < 6; i++) {
            Direction dir = getDirectionFromIndex(i);
            BlockPos adjacent = pos.relative(dir);
            int power = level().getSignal(adjacent, dir);
            redstoneInputs[i] = power;
        }
    }

    private Direction getDirectionFromIndex(int index) {
        return switch (index) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public void prepareForExecution() {
        updateRedstoneInputs();
    }

    public int getRedstoneOutput(Direction direction) {
        int index = switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
        return redstoneOutputs[index];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        CompoundTag inventoryTag = new CompoundTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                inventory.getItem(i).save(level().registryAccess(), itemTag);
                inventoryTag.put("Slot" + i, itemTag);
            }
        }
        tag.put("Inventory", inventoryTag);

        tag.putIntArray("RedstoneOutputs", redstoneOutputs);
        tag.putDouble("TargetHoverHeight", targetHoverHeight);

        CompoundTag peripheralsTag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : connectedPeripherals.entrySet()) {
            peripheralsTag.putUUID(entry.getKey(), entry.getValue());
        }
        tag.put("ConnectedPeripherals", peripheralsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Inventory")) {
            CompoundTag inventoryTag = tag.getCompound("Inventory");
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (inventoryTag.contains("Slot" + i)) {
                    CompoundTag itemTag = inventoryTag.getCompound("Slot" + i);
                    inventory.setItem(i, net.minecraft.world.item.ItemStack.parseOptional(level().registryAccess(), itemTag));
                }
            }
        }

        if (tag.contains("RedstoneOutputs")) {
            int[] outputs = tag.getIntArray("RedstoneOutputs");
            System.arraycopy(outputs, 0, redstoneOutputs, 0, Math.min(outputs.length, 6));
        }

        if (tag.contains("TargetHoverHeight")) {
            targetHoverHeight = tag.getDouble("TargetHoverHeight");
        }

        if (tag.contains("ConnectedPeripherals")) {
            CompoundTag peripheralsTag = tag.getCompound("ConnectedPeripherals");
            connectedPeripherals.clear();
            for (String key : peripheralsTag.getAllKeys()) {
                if (peripheralsTag.hasUUID(key)) {
                    connectedPeripherals.put(key, peripheralsTag.getUUID(key));
                }
            }
        }
    }

}
