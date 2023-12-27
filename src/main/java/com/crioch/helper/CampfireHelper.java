package com.crioch.helper;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class CampfireHelper {
    public static void onLitWithItem(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        if (!world.isClient && cir.getReturnValue().isAccepted() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
            BlockPos blockPos;
            BlockState blockState = world.getBlockState(blockPos = context.getBlockPos());
            Block block = blockState.getBlock();
            boolean isCampfire = block.equals(Blocks.CAMPFIRE) || block.equals(Blocks.SOUL_CAMPFIRE);

            if (isCampfire) {
                CampfireHelper.dropItems(blockPos, world);
            }
        }
    }

    public static void onLitWithEntity(World world, BlockPos blockPos) {
        if (!world.isClient && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
            CampfireHelper.dropItems(blockPos, world);
        }
    }

    private static void dropItems(BlockPos blockPos, World world) {
        CampfireBlockEntity campfire = world.getBlockEntity(blockPos, BlockEntityType.CAMPFIRE).get();
        DefaultedList<ItemStack> items = campfire.getItemsBeingCooked();
        for (int i = 0; i < items.size(); ++i) {
            ItemStack item = items.get(i);
            if (item.isEmpty()) {
                continue;
            }
            ItemScatterer.spawn(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), item);
            items.set(i, ItemStack.EMPTY);
            campfire.spawnItemsBeingCooked();
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(campfire.getCachedState()));
        }
    }
}
