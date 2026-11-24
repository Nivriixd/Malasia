package com.irvin.casinovidas.niveles;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NivelCirculoPayload(int color) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NivelCirculoPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "nivel_circulo"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NivelCirculoPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, NivelCirculoPayload::color, NivelCirculoPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NivelCirculoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientCirculo.show(payload.color()));
    }
}
