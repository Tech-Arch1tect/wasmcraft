package uk.co.techarchitect.wasmcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.menu.ComputerMenu;

import java.util.ArrayList;
import java.util.List;

public record ComputerOutputSyncPacket(BlockPos pos, List<String> output, List<String> commandHistory, List<String> fileNames, int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ComputerOutputSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "computer_output_sync"));

    public static final StreamCodec<FriendlyByteBuf, ComputerOutputSyncPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.pos != null);
                if (packet.pos != null) {
                    buf.writeBlockPos(packet.pos);
                }
                buf.writeInt(packet.output.size());
                for (String line : packet.output) {
                    buf.writeUtf(line);
                }
                buf.writeInt(packet.commandHistory.size());
                for (String cmd : packet.commandHistory) {
                    buf.writeUtf(cmd);
                }
                buf.writeInt(packet.fileNames.size());
                for (String file : packet.fileNames) {
                    buf.writeUtf(file);
                }
                buf.writeInt(packet.entityId);
            },
            buf -> {
                boolean hasPos = buf.readBoolean();
                BlockPos pos = hasPos ? buf.readBlockPos() : null;
                int size = buf.readInt();
                List<String> output = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    output.add(buf.readUtf());
                }
                int cmdSize = buf.readInt();
                List<String> commandHistory = new ArrayList<>();
                for (int i = 0; i < cmdSize; i++) {
                    commandHistory.add(buf.readUtf());
                }
                int fileSize = buf.readInt();
                List<String> fileNames = new ArrayList<>();
                for (int i = 0; i < fileSize; i++) {
                    fileNames.add(buf.readUtf());
                }
                int entityId = buf.readInt();
                return new ComputerOutputSyncPacket(pos, output, commandHistory, fileNames, entityId);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComputerOutputSyncPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            Minecraft.getInstance().execute(() -> {
                var player = Minecraft.getInstance().player;
                if (player != null && player.containerMenu instanceof ComputerMenu menu) {
                    menu.setClientOutputHistory(packet.output);
                    menu.setClientCommandHistory(packet.commandHistory);
                    menu.setClientFileNames(packet.fileNames);
                }
            });
        });
    }
}
