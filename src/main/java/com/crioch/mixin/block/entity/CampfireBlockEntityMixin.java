package com.crioch.mixin.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.world.GameRules;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity {

	@Shadow
	RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> matchGetter;

	public CampfireBlockEntityMixin(BlockPos pos, BlockState state) {
		super(BlockEntityType.CAMPFIRE, pos, state);
	}

	@Inject(at = @At("INVOKE"), method = "addItem(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;I)Z", cancellable = true)
	private void addItem(@Nullable Entity user, ItemStack stack, int cookTime, CallbackInfoReturnable<Boolean> info) {
		// Only filter if limited crafting is enabled and it is lit
		if (!world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || CampfireBlock.canBeLit(getCachedState())) {
			return;
		}
		if (user != null) {
			ServerPlayerEntity player = (ServerPlayerEntity)user;
			Optional<RecipeEntry<CampfireCookingRecipe>> recipe = this.matchGetter.getFirstMatch(new SimpleInventory(stack), world);
			if (!recipe.isPresent() || !player.getRecipeBook().contains(recipe.get())) {
				info.setReturnValue(false);
			}
		} else {
			info.setReturnValue(false);
		}
	}
}