package uk.co.techarchitect.wasmcraft.blockentity;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;
import uk.co.techarchitect.wasmcraft.network.ComputerOutputSyncPacket;
import uk.co.techarchitect.wasmcraft.wasm.WasmExecutor;

import java.util.ArrayList;
import java.util.List;

public class ComputerBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private final List<String> outputHistory = new ArrayList<>();

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        outputHistory.add("Computer initialized. Type 'help' for commands.");
    }

    public List<String> getOutputHistory() {
        return new ArrayList<>(outputHistory);
    }

    public void executeCommand(String command) {
        outputHistory.add("> " + command);

        String[] parts = command.toLowerCase().trim().split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "help" -> {
                outputHistory.add("Available commands:");
                outputHistory.add("  help        - Show this help message");
                outputHistory.add("  clear       - Clear the terminal");
                outputHistory.add("  run <file>  - Execute WASM file");
            }
            case "clear" -> {
                outputHistory.clear();
            }
            case "run" -> {
                if (parts.length < 2) {
                    outputHistory.add("Usage: run <filename>");
                    outputHistory.add("Example: run hello.wasm");
                } else {
                    String filename = parts[1];
                    if (!filename.endsWith(".wasm")) {
                        filename = filename + ".wasm";
                    }

                    String resourcePath = "/wasm/" + filename;
                    outputHistory.add("Executing " + filename + "...");

                    WasmExecutor.ExecutionResult result = WasmExecutor.executeFromResource(resourcePath);
                    if (result.isSuccess()) {
                        for (String line : result.getOutput().split("\n")) {
                            outputHistory.add(line);
                        }
                    } else {
                        outputHistory.add("ERROR: " + result.getError());
                    }
                }
            }
            default -> {
                outputHistory.add("Unknown command: " + cmd);
                outputHistory.add("Type 'help' for available commands");
            }
        }

        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag historyList = new ListTag();
        for (String line : outputHistory) {
            historyList.add(StringTag.valueOf(line));
        }
        tag.put("OutputHistory", historyList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        outputHistory.clear();
        if (tag.contains("OutputHistory", Tag.TAG_LIST)) {
            ListTag historyList = tag.getList("OutputHistory", Tag.TAG_STRING);
            for (int i = 0; i < historyList.size(); i++) {
                outputHistory.add(historyList.getString(i));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Computer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ComputerMenu(containerId, playerInventory, this);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    public void syncToPlayer(ServerPlayer player) {
        NetworkManager.sendToPlayer(player, new ComputerOutputSyncPacket(worldPosition, getOutputHistory()));
    }
}
