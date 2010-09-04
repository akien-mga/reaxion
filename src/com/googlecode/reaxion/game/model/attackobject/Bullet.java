package com.googlecode.reaxion.game.model.attackobject;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.state.BattleGameState;

public class Bullet extends AttackObject {
	
	public static final String filename = "bullet";
	protected static final int span = 120;
	protected static final float dpf = 5;
	
	public Bullet(Model m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    	allowYaw = false;
    	allowPitch = false;
    }
	
	public Bullet(Model[] m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    	allowYaw = false;
    	allowPitch = false;
    }
	
	@Override
	public void hit(BattleGameState b, Character other) {
		//System.out.println("bullet hit "+other.model);
		b.removeModel(this);
    }
	
	/*
	@Override
	public void initialize() {
		super.initialize();

		model.setCullHint(CullHint.Never);
		model.setLightCombineMode(LightCombineMode.Off);
		model.setTextureCombineMode(TextureCombineMode.Replace);
		model.updateRenderState();
	}
	*/
	
}