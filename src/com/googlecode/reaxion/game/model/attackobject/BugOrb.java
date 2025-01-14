package com.googlecode.reaxion.game.model.attackobject;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.state.StageGameState;
import com.googlecode.reaxion.game.util.ListFilter;
import com.jme.math.Vector3f;

public class BugOrb extends AttackObject {
	
	public static final String filename = "bug-orb";
	protected static final int span = 540;
	
	protected static final float dpf = 32;
	
	public float speed = 1;
	private boolean changed = false;
	private Vector3f backPoint;
	
	public BugOrb(Model m, Vector3f bP) {
    	super(filename, dpf, m);
    	set();
    	backPoint = bP.clone();
    }
	
	public BugOrb(Model[] m, Vector3f bP) {
    	super(filename, dpf, m);
    	set();
    	backPoint = bP.clone();
    }
	
	private void set() {
		name = "Bug Orb";
    	trackable = true;
    	flinch = true;
    	lifespan = span;
    	gravitate = true;
    	gravity = -.06f;
	}
	
	@Override
	public void hit(StageGameState b, Character other) {
		finish(b);
    }
	
	@ Override
    public void act(StageGameState b) {
		
		// check if a hit by another attack with linear approximation
    	Model[] collisions = getLinearModelCollisions(velocity, .5f, ListFilter.filterUsers(b.getModels(), users, true));
        for (Model c : collisions) {
        	if (c instanceof AttackObject) {
        		// turn around
        		if (!changed) {
        			((AttackObject)c).hit(b, (Character)users.get(users.size()-1));
        			lifeCount = 0;
        			users = c.users;
        			changed = true;
        			velocity = backPoint.subtract(model.getLocalTranslation()).normalize().mult(speed*2);
        			gravitate = false;
        		}
        		
        	// check for character hits
        	} else if (c instanceof Character)
        		((Character)c).hit(b, this);
        }
		
		// apply gravity
        if (gravitate) {
        	velocity.y += gravVel;
        	if (model.getLocalTranslation().y > 0)
        		gravVel += gravity;
        	contactGround();
        }
		
		// actually move
        Vector3f loc = model.getLocalTranslation();
        loc.addLocal(velocity);
        model.setLocalTranslation(loc);
        
        //check lifespan
        if (lifeCount == lifespan)
        	finish(b);
        lifeCount++;
    }
	
}
