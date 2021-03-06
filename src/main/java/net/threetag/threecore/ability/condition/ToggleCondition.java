package net.threetag.threecore.ability.condition;

import net.threetag.threecore.ability.Ability;

/**
 * Created by Nictogen on 2019-06-08.
 */
public class ToggleCondition extends KeyboundCondition
{
	public ToggleCondition(Ability ability)
	{
		super(ConditionType.TOGGLE, ability);
	}

	@Override void onKeyPressed()
	{
		this.dataManager.set(ENABLED, !this.dataManager.get(ENABLED));
	}

	@Override void onKeyReleased()
	{

	}
}
