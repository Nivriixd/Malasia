package com.irvin.casinovidas.vidas.item;

import com.irvin.casinovidas.vidas.client.ClientVidasData;
import com.irvin.casinovidas.vidas.server.VidasManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Corazon consumible. La logica real corre solo en el servidor. */
public class HeartItem extends Item {
    private final boolean golden;

    public HeartItem(boolean golden, Properties props) {
        super(props);
        this.golden = golden;
    }

    // En 1.21.1 la firma es (ItemStack, LivingEntity). Se deja tambien la vieja por si tu version
    // de Minecraft/mappings la sigue usando; la que no exista simplemente no se llama.
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int lives = level.isClientSide ? ClientVidasData.getLives() : VidasManager.getLives((ServerPlayer) player);
        if (!VidasManager.canUse(golden, lives)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.vidas.cannot_use"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            if (VidasManager.consume(sp, golden)) {
                level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                if (!sp.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return stack;
    }
}
