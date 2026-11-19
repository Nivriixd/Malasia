package com.irvin.casinovidas.slotmachine.network.packet;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.currency.ClientBalance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor -> cliente: balance de monedas del jugador. */
public record BalanceS2CPayload(long balance) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BalanceS2CPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "balance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BalanceS2CPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_LONG, BalanceS2CPayload::balance, BalanceS2CPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BalanceS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBalance.set(payload.balance()));
    }
}
