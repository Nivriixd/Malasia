package com.irvin.casinovidas.slotmachine.block;

import com.irvin.casinovidas.slotmachine.currency.CoinItems;
import com.irvin.casinovidas.slotmachine.currency.CoinsManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Digitalizador: clic derecho con monedas en la mano y todo el stack se suma al balance.
 * Con la mano vacia (o con otro item) muestra el balance actual.
 */
public class DigitizerBlock extends Block {
    public static final MapCodec<DigitizerBlock> CODEC = simpleCodec(DigitizerBlock::new);

    public DigitizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(CoinItems.COIN.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        if (player instanceof ServerPlayer sp) {
            int count = stack.getCount();
            CoinsManager.add(sp.server, sp.getUUID(), count);
            stack.shrink(count);
            sp.displayClientMessage(Component.translatable("message.slotmachinemod.coins_added", count), true);
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer sp) {
            long balance = CoinsManager.get(sp.server, sp.getUUID());
            sp.displayClientMessage(Component.translatable("message.casinovidas.balance", balance), true);
        }
        return InteractionResult.CONSUME;
    }
}
