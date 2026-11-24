package com.irvin.casinovidas.network;

import com.irvin.casinovidas.slotmachine.network.packet.BalanceS2CPayload;
import com.irvin.casinovidas.vidas.network.SyncLivesPayload;
import com.irvin.casinovidas.niveles.NivelCirculoPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModPayloads {
    private ModPayloads() {}

    private static final String PROTOCOL = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);

        registrar.playToClient(SyncLivesPayload.TYPE, SyncLivesPayload.STREAM_CODEC, SyncLivesPayload::handle);

        registrar.playToClient(BalanceS2CPayload.TYPE, BalanceS2CPayload.STREAM_CODEC, BalanceS2CPayload::handle);
    }
}
