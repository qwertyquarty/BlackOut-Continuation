package kassuk.addon.blackout.mixins;

import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundMoveEntityPacket.class)
public interface IEntityS2CPacket {
    @Accessor("entityId")
    int blackout$getId();
}
