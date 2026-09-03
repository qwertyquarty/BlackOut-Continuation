package kassuk.addon.blackout.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;

@Mixin(ServerboundTeleportToEntityPacket.class)
public interface ISpectatorTeleportC2SPacket {
    @Accessor("uuid")
    UUID blackout$getID();
}
