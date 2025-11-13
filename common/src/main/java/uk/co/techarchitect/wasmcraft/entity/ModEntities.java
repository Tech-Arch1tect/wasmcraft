package uk.co.techarchitect.wasmcraft.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import uk.co.techarchitect.wasmcraft.Wasmcraft;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Wasmcraft.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<DroneEntity>> DRONE =
            ENTITIES.register("drone", () ->
                    EntityType.Builder.of(DroneEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(8)
                            .build("drone"));

    public static void register() {
        ENTITIES.register();
    }
}
