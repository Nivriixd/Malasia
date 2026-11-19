package com.irvin.casinovidas.slotmachine.sound;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CasinoVidasMod.MOD_ID);

    /** Looping reel-spinning sound played while the slot machine is active. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SPINNING = SOUND_EVENTS.register("spinning",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "spinning")));

    /** Short click/thunk played each time an individual reel stops. */
    public static final DeferredHolder<SoundEvent, SoundEvent> REEL_STOP = SOUND_EVENTS.register("reel_stop",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "reel_stop")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
