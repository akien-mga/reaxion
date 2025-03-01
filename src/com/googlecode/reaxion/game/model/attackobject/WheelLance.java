package com.googlecode.reaxion.game.model.attackobject;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.state.StageGameState;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;

public class WheelLance extends AttackObject {
	
	public static final String filename = "lance";
	protected static final int span = 200;
	protected static final float dpf = 17;
	private static final float angleInc = .0044f; // approx. pi/720
	
	private float angle = 0;
	
	public WheelLance(Model m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    }
	
	public WheelLance(Model[] m) {
    	super(filename, dpf, m);
    	flinch = true;
    	lifespan = span;
    }
	
	@Override
	public void hit(StageGameState b, Character other) {
		finish(b);
    }
	
	@ Override
    public void act(StageGameState b) {
        // rotate proportional to life left
    	yaw += (float)(lifespan - lifeCount) * angleInc;
    	rotate();
        
    	super.act(b);
    }
	
}
