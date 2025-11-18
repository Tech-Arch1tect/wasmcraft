package uk.co.techarchitect.wasmcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;

public class ContainerHelper {

    public static BlockEntity getBlockEntityViaChunk(ServerLevel serverLevel, BlockPos pos) {
        LevelChunk chunk = serverLevel.getChunkAt(pos);
        return chunk.getBlockEntity(pos);
    }

    public static Container getContainerAt(ServerLevel serverLevel, BlockPos pos) {
        BlockState blockState = serverLevel.getBlockState(pos);

        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            Container container = ChestBlock.getContainer(
                chestBlock,
                blockState,
                serverLevel,
                pos,
                true
            );

            if (container != null) {
                return container;
            }

            ChestType chestType = blockState.getValue(ChestBlock.TYPE);

            if (chestType != ChestType.SINGLE) {
                Direction connectionDir = ChestBlock.getConnectedDirection(blockState);
                BlockPos otherPos = pos.relative(connectionDir);

                BlockEntity blockEntity1 = getBlockEntityViaChunk(serverLevel, pos);
                BlockEntity blockEntity2 = getBlockEntityViaChunk(serverLevel, otherPos);

                if (blockEntity1 instanceof Container container1 &&
                    blockEntity2 instanceof Container container2) {
                    if (chestType == ChestType.LEFT) {
                        return new CompoundContainer(container1, container2);
                    } else {
                        return new CompoundContainer(container2, container1);
                    }
                }
            }
        }

        BlockEntity blockEntity = getBlockEntityViaChunk(serverLevel, pos);
        if (blockEntity instanceof Container container) {
            return container;
        }

        return null;
    }

    public static class CompoundContainer implements Container {
        private final Container container1;
        private final Container container2;
        private final int size1;

        public CompoundContainer(Container container1, Container container2) {
            this.container1 = container1;
            this.container2 = container2;
            this.size1 = container1.getContainerSize();
        }

        @Override
        public int getContainerSize() {
            return container1.getContainerSize() + container2.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return container1.isEmpty() && container2.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot < size1 ? container1.getItem(slot) : container2.getItem(slot - size1);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return slot < size1 ? container1.removeItem(slot, amount) : container2.removeItem(slot - size1, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return slot < size1 ? container1.removeItemNoUpdate(slot) : container2.removeItemNoUpdate(slot - size1);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot < size1) {
                container1.setItem(slot, stack);
            } else {
                container2.setItem(slot - size1, stack);
            }
        }

        @Override
        public void setChanged() {
            container1.setChanged();
            container2.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return container1.stillValid(player) && container2.stillValid(player);
        }

        @Override
        public void clearContent() {
            container1.clearContent();
            container2.clearContent();
        }
    }
}
