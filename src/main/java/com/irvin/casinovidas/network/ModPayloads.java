package com.irvin.casinovidas.network;

import com.irvin.casinovidas.slotmachine.network.packet.BalanceS2CPayload;
import com.irvin.casinovidas.slotmachine.network.packet.ServerClaimPayoutC2SPayload;
import com.irvin.casinovidas.slotmachine.network.packet.ServerSlotsC2SPayload;
import com.irvin.casinovidas.slotmachine.network.packet.ServerSlotsS2CPayload;
import com.irvin.casinovidas.vidas.network.SyncLivesPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registro de TODOS los paquetes del mod (vidas + tragamonedas). */
public final class ModPayloads {
    private ModPayloads() {}

    private static final String PROTOCOL = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);

        // Vidas: servidor -> cliente
        registrar.playToClient(SyncLivesPayload.TYPE, SyncLivesPayload.STREAM_CODEC, SyncLivesPayload::handle);

        // Monedas / tragamonedas
        registrar.playToClient(BalanceS2CPayload.TYPE, BalanceS2CPayload.STREAM_CODEC, BalanceS2CPayload::handle);
        registrar.playToClient(ServerSlotsS2CPayload.TYPE, ServerSlotsS2CPayload.STREAM_CODEC, ServerSlotsS2CPayload::handle);
        registrar.playToServer(ServerSlotsC2SPayload.TYPE, ServerSlotsC2SPayload.STREAM_CODEC, ServerSlotsC2SPayload::handle);
        registrar.playToServer(ServerClaimPayoutC2SPayload.TYPE, ServerClaimPayoutC2SPayload.STREAM_CODEC, ServerClaimPayoutC2SPayload::handle);
    }
}
