package uk.co.techarchitect.wasmcraft.client.renderer;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import uk.co.techarchitect.wasmcraft.Wasmcraft;

public class ModModelLayers {
    public static final ModelLayerLocation DRONE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "drone"), "main");
}
