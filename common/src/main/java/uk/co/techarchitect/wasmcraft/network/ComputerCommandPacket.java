package uk.co.techarchitect.wasmcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.entity.DroneEntity;
import uk.co.techarchitect.wasmcraft.menu.ComputerProvider;

public record ComputerCommandPacket(BlockPos pos, String command, int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ComputerCommandPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "computer_command"));

    public static final StreamCodec<FriendlyByteBuf, ComputerCommandPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.pos != null);
                if (packet.pos != null) {
                    buf.writeBlockPos(packet.pos);
                }
                buf.writeUtf(packet.command);
                buf.writeInt(packet.entityId);
            },
            buf -> {
                boolean hasPos = buf.readBoolean();
                BlockPos pos = hasPos ? buf.readBlockPos() : null;
                String command = buf.readUtf();
                int entityId = buf.readInt();
                return new ComputerCommandPacket(pos, command, entityId);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComputerCommandPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                ComputerProvider provider = null;

                if (packet.pos != null) {
                    var blockEntity = serverPlayer.level().getBlockEntity(packet.pos);
                    if (blockEntity instanceof ComputerProvider) {
                        provider = (ComputerProvider) blockEntity;
                    }
                } else if (packet.entityId != -1) {
                    var entity = serverPlayer.level().getEntity(packet.entityId);
                    if (entity instanceof ComputerProvider) {
                        provider = (ComputerProvider) entity;
                    }
                }

                if (provider != null) {
                    provider.executeCommand(packet.command);
                    var history = provider.getOutputHistory();
                    var commandHistory = provider.getCommandHistory();
                    var fileNames = provider.getFileNames();
                    NetworkManager.sendToPlayer(serverPlayer,
                            new ComputerOutputSyncPacket(packet.pos, history, commandHistory, fileNames, packet.entityId));
                }
            }
        });
    }
}
