package kassuk.addon.blackout.mixins;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public interface IRaycastContext {
    @Mutable
    @Accessor("from")
    void blackout$setStart(Vec3 start);

    @Mutable
    @Accessor("to")
    void blackout$setEnd(Vec3 end);
}
