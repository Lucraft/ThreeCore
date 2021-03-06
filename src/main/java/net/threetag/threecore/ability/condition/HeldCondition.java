package net.threetag.threecore.ability.condition;

import net.threetag.threecore.ability.Ability;

public class HeldCondition extends KeyboundCondition
{
	public HeldCondition(Ability ability)
	{
		super(ConditionType.HELD, ability);
	}

	@Override void onKeyPressed()
	{
		this.dataManager.set(ENABLED, true);
	}

	@Override void onKeyReleased()
	{
		this.dataManager.set(ENABLED, false);
	}
}
