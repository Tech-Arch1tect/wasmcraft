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
import uk.co.techarchitect.wasmcraft.menu.InventoryProvider;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.context.ContextHelper;
import uk.co.techarchitect.wasmcraft.wasm.context.InventoryContext;
import uk.co.techarchitect.wasmcraft.wasm.context.MonitorContext;
import uk.co.techarchitect.wasmcraft.wasm.context.MovementContext;
import uk.co.techarchitect.wasmcraft.wasm.context.PeripheralContext;
import uk.co.techarchitect.wasmcraft.wasm.context.RedstoneContext;
import uk.co.techarchitect.wasmcraft.wasm.context.WorldInteractionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.*;

public class DroneEntity extends ComputerEntityBase implements MovementContext, InventoryProvider, InventoryContext, WorldInteractionContext {
    private static final int INVENTORY_SIZE = 27;
    private static final double PERIPHERAL_RANGE = 16.0;
    private static final EntityDataAccessor<Float> HOVER_HEIGHT = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SELECTED_SLOT = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.INT);

    private final SimpleContainer inventory;
    private final int[] redstoneOutputs = new int[6];
    private final int[] redstoneInputs = new int[6];
    private final Map<String, UUID> connectedPeripherals = new HashMap<>();
    private final ContextHelper contextHelper;

    private boolean isMovementInProgress = false;
    private Vec3 targetPosition = null;
    private Vec3 movementStartPosition = null;
    private int movementTicksElapsed = 0;
    private static final int MAX_MOVEMENT_TICKS = 600;
    private float targetYaw = 0.0f;
    private static final double MOVEMENT_SPEED = 0.05;
    private static final double POSITION_THRESHOLD = 0.05;
    private final Object movementLock = new Object();

    public DroneEntity(EntityType<? extends DroneEntity> entityType, Level level) {
        super(entityType, level, "Drone initialized. Type 'help' for commands.");
        this.inventory = new SimpleContainer(INVENTORY_SIZE);
        this.noCulling = true;
        this.setNoGravity(true);
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
            },
            new ContextHelper.YawProvider() {
                @Override
                public float getYaw() {
                    return getYRot();
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
        builder.define(SELECTED_SLOT, 0);
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
        return new WasmContext[] { contextHelper, this };
    }

    @Override
    public com.dylibso.chicory.runtime.HostFunction[] toHostFunctions() {
        com.dylibso.chicory.runtime.HostFunction[] movement = MovementContext.super.toHostFunctions();
        com.dylibso.chicory.runtime.HostFunction[] inventory = InventoryContext.super.toHostFunctions();
        com.dylibso.chicory.runtime.HostFunction[] worldInteraction = WorldInteractionContext.super.toHostFunctions();

        com.dylibso.chicory.runtime.HostFunction[] combined = new com.dylibso.chicory.runtime.HostFunction[
            movement.length + inventory.length + worldInteraction.length];
        System.arraycopy(movement, 0, combined, 0, movement.length);
        System.arraycopy(inventory, 0, combined, movement.length, inventory.length);
        System.arraycopy(worldInteraction, 0, combined, movement.length + inventory.length, worldInteraction.length);

        return combined;
    }

    @Override
    protected String getComputerDisplayName() {
        return "Drone Computer";
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            if (isMovementInProgress && targetPosition != null) {
                updateProgrammaticMovement();
            } else {
                setDeltaMovement(Vec3.ZERO);
            }
            updateRedstoneInputs();
        }
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

    @Override
    public SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public int getInventorySize() {
        return INVENTORY_SIZE;
    }

    public int getSelectedSlot() {
        return this.entityData.get(SELECTED_SLOT);
    }

    @Override
    public int getSelectedSlot(int[] outSlot) {
        outSlot[0] = this.entityData.get(SELECTED_SLOT);
        return SUCCESS;
    }

    @Override
    public int setSelectedSlot(int slot) {
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            return ERR_INVALID_PARAMETER;
        }
        this.entityData.set(SELECTED_SLOT, slot);
        return SUCCESS;
    }

    @Override
    public int getInventorySize(int[] outSize) {
        outSize[0] = INVENTORY_SIZE;
        return SUCCESS;
    }

    @Override
    public int getItem(int slot, StringBuilder outItemId, int[] outCount) {
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            return ERR_INVALID_PARAMETER;
        }

        net.minecraft.world.item.ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty()) {
            outItemId.append("minecraft:air");
            outCount[0] = 0;
        } else {
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            outItemId.append(itemId);
            outCount[0] = stack.getCount();
        }

        return SUCCESS;
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

        tag.putInt("SelectedSlot", this.entityData.get(SELECTED_SLOT));
        tag.putIntArray("RedstoneOutputs", redstoneOutputs);

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

        if (tag.contains("SelectedSlot")) {
            this.entityData.set(SELECTED_SLOT, tag.getInt("SelectedSlot"));
        }

        if (tag.contains("RedstoneOutputs")) {
            int[] outputs = tag.getIntArray("RedstoneOutputs");
            System.arraycopy(outputs, 0, redstoneOutputs, 0, Math.min(outputs.length, 6));
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

    @Override
    public int moveRelative(float forward, float strafe, float vertical, float[] outActualMovement) {
        if (isMovementInProgress) {
            return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.ERR_MOVEMENT_IN_PROGRESS;
        }

        float yawRad = (float) Math.toRadians(getYRot());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);

        double dx = -forward * sin - strafe * cos;
        double dz = forward * cos - strafe * sin;
        double dy = vertical;

        Vec3 currentPos = position();
        Vec3 targetPos = new Vec3(
            currentPos.x + dx,
            currentPos.y + dy,
            currentPos.z + dz
        );

        if (targetPos.y < level().getMinBuildHeight() || targetPos.y > level().getMaxBuildHeight()) {
            return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.ERR_MOVEMENT_OUT_OF_WORLD;
        }

        this.movementStartPosition = currentPos;
        this.targetPosition = targetPos;
        this.isMovementInProgress = true;
        this.movementTicksElapsed = 0;

        synchronized (movementLock) {
            while (isMovementInProgress && movementTicksElapsed < MAX_MOVEMENT_TICKS) {
                try {
                    movementLock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isMovementInProgress = false;
                    targetPosition = null;
                    break;
                }
            }
        }

        Vec3 finalPos = position();
        outActualMovement[0] = (float) (finalPos.x - currentPos.x);
        outActualMovement[1] = (float) (finalPos.y - currentPos.y);
        outActualMovement[2] = (float) (finalPos.z - currentPos.z);

        return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.SUCCESS;
    }

    @Override
    public int rotate(float yawDegrees, float[] outActualYaw) {
        float currentYaw = getYRot();
        float newYaw = (currentYaw + yawDegrees) % 360;
        if (newYaw < 0) newYaw += 360;

        setYRot(newYaw);
        setYHeadRot(newYaw);
        yRotO = newYaw;

        outActualYaw[0] = yawDegrees;
        return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.SUCCESS;
    }

    @Override
    public int getPosition(double[] outPosition) {
        Vec3 pos = position();
        outPosition[0] = pos.x;
        outPosition[1] = pos.y;
        outPosition[2] = pos.z;
        return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.SUCCESS;
    }

    @Override
    public int getYaw(float[] outYaw) {
        float yaw = getYRot();
        if (yaw < 0) yaw += 360;
        outYaw[0] = yaw % 360;
        return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.SUCCESS;
    }

    @Override
    public int setYaw(float yawDegrees) {
        float normalizedYaw = yawDegrees % 360;
        if (normalizedYaw < 0) normalizedYaw += 360;

        setYRot(normalizedYaw);
        setYHeadRot(normalizedYaw);
        yRotO = normalizedYaw;

        return uk.co.techarchitect.wasmcraft.wasm.WasmErrorCodes.SUCCESS;
    }

    private void updateProgrammaticMovement() {
        if (targetPosition == null) {
            isMovementInProgress = false;
            return;
        }

        movementTicksElapsed++;

        Vec3 currentPos = position();
        Vec3 delta = targetPosition.subtract(currentPos);
        double distanceSquared = delta.lengthSqr();

        if (distanceSquared < POSITION_THRESHOLD * POSITION_THRESHOLD) {
            setPos(targetPosition);
            setDeltaMovement(Vec3.ZERO);
            isMovementInProgress = false;
            targetPosition = null;
            movementStartPosition = null;
            synchronized (movementLock) {
                movementLock.notifyAll();
            }
            return;
        }

        if (movementTicksElapsed >= MAX_MOVEMENT_TICKS) {
            setDeltaMovement(Vec3.ZERO);
            isMovementInProgress = false;
            targetPosition = null;
            movementStartPosition = null;
            synchronized (movementLock) {
                movementLock.notifyAll();
            }
            return;
        }

        double distance = Math.sqrt(distanceSquared);
        Vec3 direction = delta.scale(1.0 / distance);
        double stepSize = Math.min(MOVEMENT_SPEED, distance);
        Vec3 motion = direction.scale(stepSize);

        Vec3 newPos = currentPos.add(motion);

        BlockPos blockPos = BlockPos.containing(newPos);
        if (!level().getBlockState(blockPos).isAir() &&
            !level().getBlockState(blockPos).canBeReplaced()) {
            setDeltaMovement(Vec3.ZERO);
            isMovementInProgress = false;
            targetPosition = null;
            movementStartPosition = null;
            synchronized (movementLock) {
                movementLock.notifyAll();
            }
            return;
        }

        setPos(newPos);
        setDeltaMovement(motion);

        synchronized (movementLock) {
            movementLock.notifyAll();
        }
    }

    @Override
    public int canBreak(int relativeSide, int[] outCanBreak) {
        outCanBreak[0] = 0;

        if (relativeSide < 0 || relativeSide > 2) {
            return ERR_WORLD_INVALID_SIDE;
        }

        net.minecraft.server.level.ServerLevel serverLevel = level() instanceof net.minecraft.server.level.ServerLevel sl ? sl : null;
        if (serverLevel == null || serverLevel.isClientSide) {
            return ERR_INVALID_PARAMETER;
        }

        BlockPos entityPos = blockPosition();
        Direction absoluteDir = getAbsoluteDirectionFromRelative(relativeSide);
        BlockPos targetPos = entityPos.relative(absoluteDir);

        int selectedSlot = getSelectedSlot();
        net.minecraft.world.item.ItemStack toolStack = inventory.getItem(selectedSlot);

        net.minecraft.world.level.block.state.BlockState blockState = serverLevel.getBlockState(targetPos);
        if (blockState.isAir()) {
            outCanBreak[0] = 1;
            return SUCCESS;
        }

        if (blockState.getDestroySpeed(serverLevel, targetPos) < 0) {
            outCanBreak[0] = 0;
            return SUCCESS;
        }

        if (blockState.requiresCorrectToolForDrops()) {
            net.minecraft.world.item.component.Tool tool = toolStack.get(net.minecraft.core.component.DataComponents.TOOL);
            if (tool == null || !tool.isCorrectForDrops(blockState)) {
                outCanBreak[0] = 0;
                return SUCCESS;
            }
        }

        outCanBreak[0] = 1;
        return SUCCESS;
    }

    @Override
    public int breakBlock(int relativeSide) {
        if (relativeSide < 0 || relativeSide > 2) {
            return ERR_WORLD_INVALID_SIDE;
        }

        net.minecraft.server.level.ServerLevel serverLevel = level() instanceof net.minecraft.server.level.ServerLevel sl ? sl : null;
        if (serverLevel == null || serverLevel.isClientSide) {
            return ERR_INVALID_PARAMETER;
        }

        BlockPos entityPos = blockPosition();
        Direction absoluteDir = getAbsoluteDirectionFromRelative(relativeSide);
        BlockPos targetPos = entityPos.relative(absoluteDir);

        int selectedSlot = getSelectedSlot();
        net.minecraft.world.item.ItemStack toolStack = inventory.getItem(selectedSlot);

        net.minecraft.world.level.block.state.BlockState blockState = serverLevel.getBlockState(targetPos);
        if (blockState.isAir()) {
            return SUCCESS;
        }

        if (blockState.getDestroySpeed(serverLevel, targetPos) < 0) {
            return ERR_WORLD_UNBREAKABLE;
        }

        if (blockState.requiresCorrectToolForDrops()) {
            net.minecraft.world.item.component.Tool tool = toolStack.get(net.minecraft.core.component.DataComponents.TOOL);
            if (tool == null || !tool.isCorrectForDrops(blockState)) {
                return ERR_WORLD_WRONG_TOOL;
            }
        }

        boolean broken = serverLevel.destroyBlock(targetPos, true, this);
        if (!broken) {
            return ERR_WORLD_PROTECTED;
        }

        if (!toolStack.isEmpty()) {
            toolStack.hurtAndBreak(1, serverLevel, null,
                item -> inventory.setItem(selectedSlot, net.minecraft.world.item.ItemStack.EMPTY));
        }

        return SUCCESS;
    }

    private Direction getAbsoluteDirectionFromRelative(int relativeSide) {
        float yaw = getYRot();
        Direction facing = Direction.fromYRot(yaw);

        return switch (relativeSide) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> facing;
            case 3 -> facing.getOpposite();
            case 4 -> facing.getCounterClockWise();
            case 5 -> facing.getClockWise();
            default -> Direction.NORTH;
        };
    }

}
