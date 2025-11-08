package uk.co.techarchitect.wasmcraft.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import uk.co.techarchitect.wasmcraft.network.packet.MonitorUpdatePacket;
import uk.co.techarchitect.wasmcraft.network.packet.PeripheralLabelPacket;

public class ModNetworking {
    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ComputerCommandPacket.TYPE,
                ComputerCommandPacket.CODEC,
                ComputerCommandPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ComputerOutputSyncPacket.TYPE,
                ComputerOutputSyncPacket.CODEC,
                ComputerOutputSyncPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                PeripheralLabelPacket.TYPE,
                PeripheralLabelPacket.CODEC,
                PeripheralLabelPacket::handle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                MonitorUpdatePacket.TYPE,
                MonitorUpdatePacket.CODEC,
                MonitorUpdatePacket::handle
        );
    }

    public static void sendToAllTracking(ServerLevel level, BlockPos pos, CustomPacketPayload packet) {
        ChunkPos chunkPos = new ChunkPos(pos);
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            NetworkManager.sendToPlayer(player, packet);
        }
    }
}
