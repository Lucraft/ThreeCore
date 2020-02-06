package net.threetag.threecore.item;

import com.google.common.collect.Lists;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.threetag.threecore.ability.AbilityGenerator;
import net.threetag.threecore.ability.AbilityMap;
import net.threetag.threecore.ability.IAbilityProvider;
import net.threetag.threecore.capability.ItemAbilityContainerProvider;

import javax.annotation.Nullable;
import java.util.List;

public class AxeAbilityItem extends AxeItem implements IAbilityProvider {

    private List<AbilityGenerator> abilityGenerators;

    public AxeAbilityItem(IItemTier itemTier, int attackDamage, float attackSpeed, Properties properties) {
        super(itemTier, attackDamage, attackSpeed, properties);
    }

    public AxeAbilityItem setAbilities(List<AbilityGenerator> abilities) {
        this.abilityGenerators = abilities;
        return this;
    }

    public AxeAbilityItem addAbility(AbilityGenerator abilityGenerator) {
        if (this.abilityGenerators == null)
            this.abilityGenerators = Lists.newArrayList();
        this.abilityGenerators.add(abilityGenerator);
        return this;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ItemAbilityContainerProvider(stack);
    }

    @Override
    public AbilityMap getAbilities() {
        return new AbilityMap(this.abilityGenerators);
    }

}