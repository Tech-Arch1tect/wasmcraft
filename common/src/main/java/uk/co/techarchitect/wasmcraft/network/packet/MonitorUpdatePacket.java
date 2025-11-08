package uk.co.techarchitect.wasmcraft.network.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.blockentity.MonitorBlockEntity;

public record MonitorUpdatePacket(BlockPos pos, int minX, int minY, int maxX, int maxY, byte[] pixelData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MonitorUpdatePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "monitor_update")
    );

    public static final StreamCodec<FriendlyByteBuf, MonitorUpdatePacket> CODEC = new StreamCodec<>() {
        @Override
        public MonitorUpdatePacket decode(FriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            int minX = buf.readInt();
            int minY = buf.readInt();
            int maxX = buf.readInt();
            int maxY = buf.readInt();
            int dataLength = buf.readInt();
            byte[] pixelData = new byte[dataLength];
            buf.readBytes(pixelData);
            return new MonitorUpdatePacket(pos, minX, minY, maxX, maxY, pixelData);
        }

        @Override
        public void encode(FriendlyByteBuf buf, MonitorUpdatePacket packet) {
            buf.writeBlockPos(packet.pos);
            buf.writeInt(packet.minX);
            buf.writeInt(packet.minY);
            buf.writeInt(packet.maxX);
            buf.writeInt(packet.maxY);
            buf.writeInt(packet.pixelData.length);
            buf.writeBytes(packet.pixelData);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MonitorUpdatePacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer().level().getBlockEntity(packet.pos) instanceof MonitorBlockEntity monitor) {
                monitor.applyUpdate(packet.minX, packet.minY, packet.maxX, packet.maxY, packet.pixelData);
            }
        });
    }
}
