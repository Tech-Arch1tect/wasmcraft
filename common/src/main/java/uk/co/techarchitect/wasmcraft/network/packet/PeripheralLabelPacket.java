package uk.co.techarchitect.wasmcraft.network.packet;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.peripheral.PeripheralBlockEntity;

public record PeripheralLabelPacket(BlockPos pos, String label) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PeripheralLabelPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "peripheral_label"));

    public static final StreamCodec<FriendlyByteBuf, PeripheralLabelPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeUtf(packet.label);
            },
            buf -> new PeripheralLabelPacket(buf.readBlockPos(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PeripheralLabelPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                if (blockEntity instanceof PeripheralBlockEntity peripheral) {
                    if (peripheral.getOwner() != null && peripheral.getOwner().equals(player.getUUID())) {
                        peripheral.setLabel(packet.label);
                    }
                }
            }
        });
    }
}
