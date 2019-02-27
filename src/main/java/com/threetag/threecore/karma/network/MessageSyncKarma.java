package com.threetag.threecore.karma.network;

import com.threetag.threecore.karma.capability.CapabilityKarma;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSyncKarma {

    public int entityId;
    public int karma;

    public MessageSyncKarma(int entityId, int karma) {
        this.entityId = entityId;
        this.karma = karma;
    }

    public MessageSyncKarma(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.karma = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.karma);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(this.entityId);

            if (entity != null) {
                entity.getCapability(CapabilityKarma.KARMA).ifPresent((k) -> k.setKarma(this.karma));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
