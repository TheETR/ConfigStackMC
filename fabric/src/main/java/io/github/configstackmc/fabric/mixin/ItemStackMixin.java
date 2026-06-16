package io.github.configstackmc.fabric.mixin;

import io.github.configstackmc.common.StackRule;
import io.github.configstackmc.fabric.ConfigStackMcMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void configstackmc$getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack)(Object)this;
        String id = BuiltInRegistries.ITEM.getKey(self.getItem()).toString();
        StackRule rule = ConfigStackMcMod.config().rules().get(id);
        if (rule != null) cir.setReturnValue(rule.maxStackSize());
    }
}
