package uk.co.techarchitect.wasmcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.blockentity.ComputerBlockEntity;

public record ComputerCommandPacket(BlockPos pos, String command) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComputerCommandPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "computer_command"));

    public static final StreamCodec<FriendlyByteBuf, ComputerCommandPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeUtf(packet.command);
            },
            buf -> new ComputerCommandPacket(buf.readBlockPos(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComputerCommandPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(packet.pos) instanceof ComputerBlockEntity computerBlockEntity) {
                    computerBlockEntity.executeCommand(packet.command);
                    NetworkManager.sendToPlayer(serverPlayer,
                            new ComputerOutputSyncPacket(packet.pos, computerBlockEntity.getOutputHistory()));
                }
            }
        });
    }
}
