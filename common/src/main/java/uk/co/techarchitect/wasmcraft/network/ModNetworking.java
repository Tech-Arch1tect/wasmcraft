package uk.co.techarchitect.wasmcraft.network;

import dev.architectury.networking.NetworkManager;

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
    }
}
