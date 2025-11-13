package uk.co.techarchitect.wasmcraft.computer;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.menu.ComputerProvider;
import uk.co.techarchitect.wasmcraft.network.ComputerOutputSyncPacket;
import uk.co.techarchitect.wasmcraft.wasm.WasmContext;
import uk.co.techarchitect.wasmcraft.wasm.WasmContextComposer;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.List;
import java.util.UUID;

public abstract class ComputerEntityBase extends PathfinderMob implements CommandContext, ComputerProvider {
    protected final ComputerCore computerCore;

    public ComputerEntityBase(EntityType<? extends PathfinderMob> entityType, Level level, String initMessage) {
        super(entityType, level);
        this.computerCore = new ComputerCore();
        this.computerCore.setSyncCallback((player, output, cmdHistory, files) -> {
            NetworkManager.sendToPlayer(player,
                new ComputerOutputSyncPacket(null, output, cmdHistory, files, this.getId()));
        });
        registerCommands();
        computerCore.getTerminal().addLine(initMessage);
    }

    public UUID getComputerId() {
        return computerCore.getId();
    }

    protected abstract void registerCommands();

    protected abstract WasmContext[] getContexts();

    protected abstract String getComputerDisplayName();

    protected WasmContext buildWasmContext() {
        WasmContextComposer composer = new WasmContextComposer();
        for (WasmContext context : getContexts()) {
            composer.add(context);
        }
        return composer;
    }

    public WasmContext getWasmContext() {
        return buildWasmContext();
    }

    public void setOwner(UUID owner) {
        computerCore.setOwner(owner);
    }

    public UUID getOwner() {
        return computerCore.getOwner();
    }

    public ComputerCore getComputerCore() {
        return computerCore;
    }

    @Override
    public List<String> getOutputHistory() {
        return computerCore.getTerminal().getHistory();
    }

    @Override
    public List<String> getCommandHistory() {
        return computerCore.getCommandHistory();
    }

    @Override
    public List<String> getFileNames() {
        return computerCore.getFileSystem().listFiles();
    }

    @Override
    public void executeCommand(String command) {
        computerCore.getTerminal().addLine("> " + command);
        computerCore.addToCommandHistory(command);

        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return;
        }

        String cmd = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command commandObj = computerCore.getCommandRegistry().get(cmd);
        if (commandObj != null) {
            commandObj.execute(this, args);
        } else {
            computerCore.getTerminal().addLine("Unknown command: " + cmd);
            computerCore.getTerminal().addLine("Type 'help' for available commands");
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            computerCore.tick(() -> {
                computerCore.syncToAllViewers();
            });
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (computerCore.getOwner() == null) {
                computerCore.setOwner(serverPlayer.getUUID());
            }
            final int entityId = this.getId();
            computerCore.addViewer(serverPlayer);
            MenuRegistry.openExtendedMenu(serverPlayer,
                new dev.architectury.registry.menu.ExtendedMenuProvider() {
                    @Override
                    public void saveExtraData(net.minecraft.network.FriendlyByteBuf buf) {
                        buf.writeBoolean(false);
                        buf.writeInt(entityId);
                    }

                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return net.minecraft.network.chat.Component.literal(ComputerEntityBase.this.getComputerDisplayName());
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                        return new ComputerMenu(id, playerInventory, ComputerEntityBase.this, null, entityId);
                    }
                });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag computerTag = new CompoundTag();
        computerCore.saveToNbt(computerTag);
        tag.put("Computer", computerTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Computer")) {
            CompoundTag computerTag = tag.getCompound("Computer");
            computerCore.loadFromNbt(computerTag);
        }
    }

    @Override
    public void addOutput(String line) {
        computerCore.getTerminal().addLine(line);
    }

    @Override
    public Terminal getTerminal() {
        return computerCore.getTerminal();
    }

    @Override
    public FileSystem getFileSystem() {
        return computerCore.getFileSystem();
    }

    @Override
    public BlockEntity getBlockEntity() {
        return null;
    }

    @Override
    public Level getLevel() {
        return level();
    }

    @Override
    public WasmExecutor.ExecutionHandle getActiveExecution() {
        return computerCore.getActiveExecution();
    }

    @Override
    public void setActiveExecution(WasmExecutor.ExecutionHandle handle) {
        computerCore.setActiveExecution(handle);
    }

    @Override
    public void markChanged() {
    }
}
