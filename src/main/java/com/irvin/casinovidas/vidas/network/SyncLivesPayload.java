package com.irvin.casinovidas.vidas.network;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.vidas.client.ClientVidasData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor -> cliente: cuantas vidas tiene el jugador. Es todo lo que necesita el cliente. */
public record SyncLivesPayload(int lives) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncLivesPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "sync_lives"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncLivesPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SyncLivesPayload::lives, SyncLivesPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncLivesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientVidasData.setLives(payload.lives()));
    }
}
