package com.googlecode.reaxion.game.ability;

import com.googlecode.reaxion.game.model.character.Character;

/**
 * Increases character's jumping height by double.
 */
public class HighJump extends Ability {
	
	public HighJump() {
		super("High Jump");
	}
	
	@Override
	public void set(Character c) {
		System.out.println(c.model+" can jump higher!");
		c.gravity /= 2;
	}
	
}