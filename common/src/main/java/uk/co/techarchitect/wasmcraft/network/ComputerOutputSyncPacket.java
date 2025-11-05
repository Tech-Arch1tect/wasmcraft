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

public record ComputerOutputSyncPacket(BlockPos pos, List<String> output) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComputerOutputSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "computer_output_sync"));

    public static final StreamCodec<FriendlyByteBuf, ComputerOutputSyncPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeInt(packet.output.size());
                for (String line : packet.output) {
                    buf.writeUtf(line);
                }
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                int size = buf.readInt();
                List<String> output = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    output.add(buf.readUtf());
                }
                return new ComputerOutputSyncPacket(pos, output);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ComputerOutputSyncPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            if (player != null && player.containerMenu instanceof ComputerMenu menu) {
                menu.setClientOutputHistory(packet.output);
            }
        });
    }
}
