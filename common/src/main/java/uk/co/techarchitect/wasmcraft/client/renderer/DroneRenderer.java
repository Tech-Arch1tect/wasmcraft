package uk.co.techarchitect.wasmcraft.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import uk.co.techarchitect.wasmcraft.Wasmcraft;
import uk.co.techarchitect.wasmcraft.client.model.DroneModel;
import uk.co.techarchitect.wasmcraft.entity.DroneEntity;

public class DroneRenderer extends MobRenderer<DroneEntity, DroneModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Wasmcraft.MOD_ID, "textures/entity/drone.png");

    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context, new DroneModel(context.bakeLayer(ModModelLayers.DRONE)), 0.4f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(DroneEntity entity) {
        return TEXTURE;
    }
}
