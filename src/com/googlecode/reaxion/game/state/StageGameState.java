package com.googlecode.reaxion.game.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

import com.googlecode.reaxion.game.Reaxion;
import com.googlecode.reaxion.game.audio.AudioPlayer;
import com.googlecode.reaxion.game.input.PlayerInput;
import com.googlecode.reaxion.game.input.bindings.DebugBindings;
import com.googlecode.reaxion.game.input.bindings.GameBindings;
import com.googlecode.reaxion.game.input.bindings.GlobalBindings;
import com.googlecode.reaxion.game.mission.MissionManager;
import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.model.character.MajorCharacter;
import com.googlecode.reaxion.game.model.stage.Stage;
import com.googlecode.reaxion.game.overlay.HudOverlay;
import com.googlecode.reaxion.game.overlay.PauseOverlay;
import com.googlecode.reaxion.game.util.Battle;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.jme.app.AbstractGame;
import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.MouseInput;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.CullState.Face;
import com.jme.scene.state.LightState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.Debugger;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.StatisticsGameState;

/**
 * {@code StageGameState} is a heavily modified version {@code DebugGameState}, with
 * added functionality built around {@code Models}, movement, and the camera system.
 * @author Khoa
 */
public class StageGameState extends BaseGameState {
	
	public static final String NAME = "stageGameState";
	
    protected static final Logger logger = Logger.getLogger(StageGameState.class
            .getName());
    
    public float tpf;
    protected double totalTime = 0;
    protected boolean timing = true;
    
    protected HudOverlay hudNode;
    protected PauseOverlay pauseNode;
    
    protected Node containerNode;
    
    protected InputHandler input;
    protected WireframeState wireState;
    protected LightState lightState;
    
    protected BasicPassManager pManager;
    
    protected boolean pause;
    protected boolean frozen;
    protected boolean showBounds = false;
    protected boolean showDepth = false;
    protected boolean showNormals = false;
    protected boolean statisticsCreated = false;
    protected AbstractGame game = null;
    
    /**
     * Contains all scene model elements
     */
    protected ArrayList<Model> models;
    
    /**
     * Contains the stage used for the battle
     */
    protected Stage stage;
    
    protected AbsoluteMouse mouse;
	public String cameraMode = "lock"; //options are "lock" and "free"
	public static int cameraDistance = 15;
	protected Model currentTarget;
	protected float camRotXZ = 0;
	protected final static float camRotXZSpd = (float)Math.PI/12;
	protected float camRotY = .3927f; // pi/8
	protected final static float camRotYSpd = (float)Math.PI/24;
	protected final static float[] camRotXZLimit = {-0.5236f, 0.5236f}; //-pi/6 and pi/6
	protected final static float[] camRotYLimit = {-0.1325f, 1.5394f, 0.5236f}; //arctan(2/15), 49pi/100 (close to pi/2) and pi/6
	
	/*
	// test axes converge at point
	private Cylinder[] cyl = new Cylinder[3];
	*/
	
    protected PlayerInput playerInput;
    protected MajorCharacter player;
    protected Class[] playerAttacks;
    protected MajorCharacter partner;
    protected Class[] partnerAttacks;
    
    public StageGameState() {
    	super(true);
    }
    
    public StageGameState(Battle b) {
    	super(true);  	
    	
    	LoadingQueue.execute(this);
    	
    	bgm = getStage().getBgm(-1);
    	
    	b.assignPositions();
    	assignTeam(b.getP1(), b.getP1Attacks(), b.getP2(), b.getP2Attacks());    	
    	nextTarget(0);
    	
    	load();
    	initStage();
    	
    	rootNode.updateRenderState();
    }
    
    protected void init() {
    	setName(NAME);
    	
        rootNode = new Node("RootNode");
        models = new ArrayList<Model>();
        
        // Prepare HUD node
        hudNode = new HudOverlay();
        rootNode.attachChild(hudNode);
        
        // Prepare pause node
        pauseNode = new PauseOverlay();
        rootNode.attachChild(pauseNode);
        
        // Prepare container node (must contain anything being reflected)
        containerNode = new Node("ReflectionNode");
        containerNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        rootNode.attachChild(containerNode);
        
        // Prepare the pass manager
        pManager = new BasicPassManager();
        
        // Set up multipass
        //setUpPasses();

        // Create a wirestate to toggle on and off. Starts disabled with default
        // width of 1 pixel.
        wireState = DisplaySystem.getDisplaySystem().getRenderer()
                .createWireframeState();
        wireState.setEnabled(false);
        rootNode.setRenderState(wireState);

        // Create cull states
//    	CullState cs = DisplaySystem.getDisplaySystem().getRenderer().createCullState();
//    	cs.setCullFace(Face.Back);
//    	cs.setEnabled(true);
//    	rootNode.setRenderState(cs);
    	
    	// Create blend states
//    	BlendState alphaState = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
//        alphaState.setBlendEnabled(true);
//        alphaState.setSourceFunction(BlendState.SourceFunction.ConstantAlpha);
//        alphaState.setDestinationFunction(BlendState.DestinationFunction.DestinationAlpha);
//        alphaState.setTestEnabled(true);
//        alphaState.setTestFunction(BlendState.TestFunction.Never);
//        alphaState.setEnabled(true);
//        //alphaState.setReference(1);
//        containerNode.setRenderState(alphaState);
//        containerNode.updateRenderState();

    	DisplaySystem.getDisplaySystem().getRenderer().getQueue().setTwoPassTransparency(true);
    	
        // Create ZBuffer for depth
        ZBufferState zbs = DisplaySystem.getDisplaySystem().getRenderer()
                .createZBufferState();
        zbs.setEnabled(true);
        zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        rootNode.setRenderState(zbs);
        
        // Fix up the camera, will not be needed for final camera controls
        Vector3f loc = new Vector3f( 0.0f, 2.5f, 10.0f );
        Vector3f left = new Vector3f( -1.0f, 0.0f, 0.0f );
        Vector3f up = new Vector3f( 0.0f, 1.0f, 0.0f );
        Vector3f dir = new Vector3f( 0.0f, 0f, -1.0f );
        cam.setFrame( loc, left, up, dir );
        cam.setFrustumPerspective(45f, (float) DisplaySystem.getDisplaySystem().getWidth()/DisplaySystem.getDisplaySystem().getHeight(), .01f, 2000);
        cam.update();

        // Initial InputHandler
	    //input = new FirstPersonHandler(cam, 15.0f, 0.5f);
        input = new InputHandler();	    
	    
	    //Setup software mouse
	    // Commented out to get rid of that annoying bs where it would nom the mouse
	    MouseInput.get().setCursorVisible(true);
	 /* mouse = new AbsoluteMouse("Mouse Input", DisplaySystem.getDisplaySystem().getWidth(), DisplaySystem.getDisplaySystem().getHeight());
		mouse.registerWithInputHandler(input);
		TextureState cursor = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
		cursor.setTexture(TextureManager.loadTexture(
				Reaxion.class.getClassLoader().getResource("com/googlecode/reaxion/resources/cursors/cursor.png"),
				Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor));
		mouse.setRenderState(cursor);
		BlendState as1 = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
		as1.setBlendEnabled(true);
		as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		as1.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		as1.setTestEnabled(true);
		as1.setTestFunction(BlendState.TestFunction.GreaterThan);
		mouse.setRenderState(as1);
		rootNode.attachChild(mouse);*/
		
		
		
		/*
        // make test cylinders
        for (int i=0; i<cyl.length; i++) {
        	cyl[i] = new Cylinder("cyl"+i, 3, 3, .025f, 100f);
        	rootNode.attachChild(cyl[i]);
        	cyl[i].setLocalRotation(new Quaternion().fromAngleAxis((float)Math.PI/2, new Vector3f((i==0)?1:0,(i==1)?1:0,(i==2)?1:0)));
        }
        */

        // Finish up
        rootNode.updateRenderState();
        rootNode.updateWorldBound();
        rootNode.updateGeometricState(0.0f, true);
    }
    
    /**
     * Preloads all elements at start to reduce on-the-fly loading.
     */
    protected void load() { 	
    	try {
    		// try to preload player characters' attacks
    		for (int i=0; i<playerAttacks.length; i++) {
    			if (playerAttacks[i] != null)
    				playerAttacks[i].getMethod("load").invoke(null);
    			if (partnerAttacks[i] != null)
    				partnerAttacks[i].getMethod("load").invoke(null);
    		}
    		
    		// try to preload common resources
    		LoadingQueue.push(new Model("glow-ring"));
    		
    		LoadingQueue.execute(null);
    	} catch (Exception e) {
			System.out.println("Error occured during preloading.");
			e.printStackTrace();
		}
    }
    
    /**
     * Hides the mouse and HUD.
     */
    protected void hideOverlays() {
    	rootNode.detachChild(hudNode);
    	rootNode.detachChild(mouse);
    }
    
    /**
     * Displays the mouse and HUD.
     */
    protected void showOverlays() {
    	rootNode.attachChild(hudNode);
    	rootNode.attachChild(mouse);
    }
    
	/**
     * Returns the {@code BasicPassManager}.
     * @author Khoa
     *
     */
    public BasicPassManager getPassManager() {
    	return pManager;
    }
    
    /**
     * Specifies the stage for this game state.
     * @param s Stage to be designated
     * @author Khoa
     *
     */
    public void setStage(Stage s) {
    	stage = s;
    	containerNode.attachChild(s.model);
    }
    
    /**
     * Initialize stage components.
     * @author Khoa
     */
    public void initStage() {
    	stage.loadComponents(this);
    	// attach stage's lighting to rootNode
    	lightState = stage.createLights();
    	rootNode.setRenderState(lightState);
    }
    
    /**
     * Returns pointer to the stage.
     * @author Khoa
     *
     */
    public Stage getStage() {
    	return stage;
    }
    
    /**
     * Specifies the tag team for this game state.
     * @param p1 Character to be designated as the player
     * @param q1 Array of the attack classes for the player
     * @param p2 Character to be designated as the partner
     * @param q2 Array of the attack classes for the partner
     * @author Khoa
     *
     */
    public void assignTeam(MajorCharacter p1, Class[] q1, MajorCharacter p2, Class[] q2) {
    	// Assign players to local objects
    	player = p1;
    	playerAttacks = q1;
    	partner = p2;
    	partnerAttacks = q2;
    	
    	// Update player stats
    	player.updateStats();
    	partner.updateStats();
    	
    	// Create input system
    	playerInput = new PlayerInput(this);
    	
    	// Pass attack reference to HUD
    	hudNode.passCharacterInfo(playerAttacks, player.minGauge);
    	
    	// Remove the inactive character
    	removeModel(partner);
    }
    
    /**
     * Specifies the player character for this game state.
     * @param p Character to be designated as the player
     * @param q Array of the attack classes for the character
     * @author Khoa
     *
     */
    public void assignPlayer(MajorCharacter p, Class[] q) {
    	// Assign players to local objects
    	player = p;
    	player.updateStats();
    	playerAttacks = q;
    	
    	// Create input system
    	playerInput = new PlayerInput(this);
    	// Pass attack reference to HUD
    	hudNode.passCharacterInfo(playerAttacks, player.minGauge);
    }
    
    /**
     * Switches player with partner
     * @author Khoa
     *
     */
    public void tagSwitch() {
    	if (partner != null && partner.hp > 0) {
    		// Tell the current attack a swap is in progress
    		if (player.currentAttack != null)
    			player.currentAttack.switchOut(this);
    		MajorCharacter p = player;
    		player = partner;
    		partner = p;
    		Class[] a = playerAttacks;
    		playerAttacks = partnerAttacks;
    		partnerAttacks = a;
    		// Pass attack reference to HUD
    		hudNode.passCharacterInfo(playerAttacks, player.minGauge);
    		// Attach the active character
    		addModel(player);
    		// Synchronize position
    		player.model.setLocalTranslation(partner.model.getLocalTranslation().clone());
    		player.model.setLocalRotation(partner.model.getLocalRotation().clone());
    		player.rotationVector = partner.rotationVector;
    		player.gravVel = partner.gravVel;
    		// Remove the inactive character
    		removeModel(partner);

    		rootNode.updateRenderState();
    	}
    }
      
    /**
     * Returns total time in {@code BattleGameState}.
     * @author Khoa
     *
     */
    public double getTotalTime() {
    	return totalTime;
    }
    
    /**
     * Returns pointer to main character.
     * @author Khoa
     *
     */
    public MajorCharacter getPlayer() {
    	return player;
    }
    
    /**
     * Returns pointer to partner character.
     * @author Khoa
     *
     */
    public MajorCharacter getPartner() {
    	return partner;
    }
    
    /**
     * Returns pointer to current target.
     * @author Khoa
     *
     */
    public Model getTarget() {
    	if (currentTarget == null)
    		nextTarget(0);
    	return currentTarget;
    }
    
    /**
     * Returns player's attacks
     * @author Khoa
     *
     */
    public Class[] getPlayerAttacks() {
    	return playerAttacks;
    }
    
    /**
     * Returns an {@code ArrayList} of models.
     */
    public ArrayList<Model> getModels() {
    	return models;
    }
    
    @ Override
    protected void onActivate() {
    	super.onActivate();
    }
    
    public LightState getLightState() {
    	return lightState;
    }

    @ Override
    public void stateUpdate(float _tpf) {
    	super.stateUpdate(_tpf);
    	
        // Update the InputHandler
    	if (input != null) {
    		input.update(tpf);
    		
    		/** If exit is a valid command (via key Esc), exit game */
    		if (KeyBindingManager.getKeyBindingManager().isValidCommand(GlobalBindings.EXIT.toString(),
	                false)) {
	        	if (game != null) {
	        		game.finish();
	        	} else {
	        		Reaxion.terminate();
	        	}  
        	}
    		
    		// check pausing
    	    checkPause();
    	}
    	
    	if (frozen)
            return;
    	
    	// Update the PlayerInput
    	if (playerInput != null) {
    		playerInput.checkKeys();
    	}
    	
    	// Update time
    	tpf = _tpf;
    	if (timing)
    		totalTime += tpf;
    	
    	// Make the stage act
    	stage.act(this);
    	
    	// Traverse list of models and call act() method
    	updateModels();
    	
    	// Billboard the models
    	billboardModels();
    	
    	// Check targets
    	if (models.indexOf(currentTarget) == -1 || currentTarget.model.getParent() == null)
    		nextTarget(0);
    	
    	// Update the camera
    	if (cameraMode == "lock" && player != null && models.size() > 0) {
    		Vector3f p = player.getTrackPoint();
    		//System.out.println(models+" "+currentTarget);
    		Vector3f t = currentTarget.getTrackPoint();
    		if (!p.equals(t)) {
    			//Vector3f camOffset = new Vector3f(t.x-p.x, t.y-p.y, t.z-p.z);
    			float angle = FastMath.atan2(p.z-t.z, p.x-t.x);
    			//camOffset = camOffset.normalize().mult(cameraDistance);
    			//System.out.println((p.x-camOffset.x)+", "+(p.y-camOffset.y)+", "+(p.z-camOffset.z));
    			cam.setLocation(new Vector3f(p.x+cameraDistance*FastMath.cos(angle-camRotXZ), 
    					p.y+cameraDistance*FastMath.sin(camRotY), 
    					p.z+cameraDistance*FastMath.sin(angle-camRotXZ)));
    			cam.lookAt(t, new Vector3f(0, 1, 0));
    		}
    	} else if (cameraMode == "free" && player != null) {
    		Vector3f p = player.getTrackPoint();
    		float x, y, z, minor;
    		y = cameraDistance*(float)Math.sin(camRotY);
    		minor = cameraDistance*(float)Math.cos(camRotY);
    		x = minor*(float)Math.sin(camRotXZ);
    		z = minor*(float)Math.cos(camRotXZ);
    		cam.setLocation(new Vector3f(p.x+x, p.y+y, p.z+z));
    		cam.lookAt(p, new Vector3f(0, 1, 0));
    	}
    	
    	// Update the audio system
    	AudioPlayer.update(this);
//        AudioSystem.getSystem().update();
//        SfxPlayer.update(this);
    	
    	// Update the HUD
    	hudNode.update(this);

        // Update the geometric state of the rootNode
        rootNode.updateGeometricState(tpf, true);
        
        // Update the pass manager
        pManager.updatePasses(tpf);

        if (input != null) {
        	/** If camera_mode is a valid command (via key TAB), switch camera modes. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.CAM_MODE.toString(), false)) {
	            swapCameraMode();
	        }
        	/** If camera controls are valid commands (via WASD keys), change camera angle. */
        	if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.CAM_LEFT.toString(), true)) {
        		camRotXZ -= camRotXZSpd;
        		if (cameraMode != "free")
        			camRotXZ = Math.max(camRotXZ, camRotXZLimit[0]);
	        }
        	if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.CAM_RIGHT.toString(), true)) {
	            camRotXZ += camRotXZSpd;
	            if (cameraMode != "free")
        			camRotXZ = Math.min(camRotXZ, camRotXZLimit[1]);
	        }
        	if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.CAM_UP.toString(), true)) {
        		if (cameraMode == "free")
        			camRotY = Math.min(camRotY + camRotYSpd, camRotYLimit[1]);
        		else
        			camRotY = Math.min(camRotY + camRotYSpd, camRotYLimit[2]);
	        }
        	if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.CAM_DOWN.toString(), true)) {
        		camRotY = Math.max(camRotY - camRotYSpd, camRotYLimit[0]);
	        }
        	/** If target_near is a valid command (via key 1), switch to next closest target. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.TARGET_NEAR.toString(), false) && cameraMode == "lock") {
	            nextTarget(-1);
	            rootNode.updateRenderState();
	        }
	        /** If target_far is a valid command (via key 2), switch to next furthest target. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GameBindings.TARGET_FAR.toString(), false) && cameraMode == "lock") {
	            nextTarget(1);
	            rootNode.updateRenderState();
	        }
	        /** If toggle_wire is a valid command (via key T), change wirestates. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                DebugBindings.TOGGLE_WIRE.toString(), false)) {
	            wireState.setEnabled(!wireState.isEnabled());
	            rootNode.updateRenderState();
	        }
	        /** If toggle_lights is a valid command (via key L), change lightstate. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                DebugBindings.TOGGLE_LIGHTS.toString(), false)) {
	            lightState.setEnabled(!lightState.isEnabled());
	            rootNode.updateRenderState();
	        }
	        /** If toggle_bounds is a valid command (via key B), change bounds. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                DebugBindings.TOGGLE_BOUNDS.toString(), false)) {
	            showBounds = !showBounds;
	        }
	        /** If toggle_depth is a valid command (via key F3), change depth. */
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                DebugBindings.TOGGLE_DEPTH.toString(), false)) {
	            showDepth = !showDepth;
	        }
	        /** If toggle_stats is a valid command (via key F4), change depth. */
            if (KeyBindingManager.getKeyBindingManager().isValidCommand(
                    DebugBindings.TOGGLE_STATS.toString(), false)) {
            	if (statisticsCreated == false) {
	                // create a statistics game state
	                GameStateManager.getInstance().attachChild(
	                		new StatisticsGameState("stats", 1f, 0.25f, 0.75f, true));
	                statisticsCreated = true;
            	}
                GameStateManager.getInstance().getChild("stats").setActive(
                        !GameStateManager.getInstance().getChild("stats").isActive());
            }
            
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                DebugBindings.TOGGLE_NORMALS.toString(), false)) {
	            showNormals = !showNormals;
	        }
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GlobalBindings.SCREENSHOT.toString(), false)) {
	        	Reaxion.takeScreenshot(MissionManager.hasCurrentMission() ? MissionManager.getCurrentMissionTitle() : "Hub");
	        }
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                GlobalBindings.MEM_REPORT.toString(), false)) {
	            long totMem = Runtime.getRuntime().totalMemory();
	            long freeMem = Runtime.getRuntime().freeMemory();
	            long maxMem = Runtime.getRuntime().maxMemory();
	
                logger.info("|*|*|  Memory Stats  |*|*|");
                logger.info("Total memory: " + (totMem >> 10) + " kb");
                logger.info("Free memory: " + (freeMem >> 10) + " kb");
                logger.info("Max memory: " + (maxMem >> 10) + " kb");
	        }
	        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
	                        GlobalBindings.TOGGLE_MOUSE.toString(), false)) {
	                    MouseInput.get().setCursorVisible(!MouseInput.get().isCursorVisible());
	                    logger.info("Cursor Visibility set to " + MouseInput.get().isCursorVisible());
	                }
        }
        
        act();
    }
    
    /**
     * If toggle_pause is a valid command (via key P), change pause.
     */
    protected void checkPause() {
        if (KeyBindingManager.getKeyBindingManager().isValidCommand(
                GameBindings.TOGGLE_PAUSE.toString(), false) && timing) {
        	togglePaused();
        	// toggle the overlay
        	if (pause) {
        		AudioPlayer.gamePaused();
        		pauseNode.pause();
        	}
        	else {
        		AudioPlayer.gameUnpaused();
        		pauseNode.unpause();
        	}
        	System.out.println("Paused: "+pause);
        }
    }
    
    /**
     * Calls the {@code act()} method for all objects in the models list.
     */
    protected void updateModels() {
    	for (int i=0; i<models.size(); i++)
    		models.get(i).act(this);
    }
    
    /**
     * Rotates models to face the camera based on their billboarding properties.
     */
    protected void billboardModels() {
    	for (int i=0; i<models.size(); i++) {
    		Model m = models.get(i);
    		if (m.billboarding != Model.Billboard.None) {
    			Vector3f face = cam.getDirection().negate();
    			if (m.billboarding == Model.Billboard.YLocked)
    				face.y = 0;
    			m.rotate(face);
    		}
    	}
    }
    
	/**
     * Called by {@code stateUpdate()}. Override to add extra functionality.
     */
    protected void act() {
    	
    }
    
	/**
     * Toggles camera mode, maintaining viewpoint when switching to free, and auto-locking
     * closest target when switching to lock
     */
    protected void swapCameraMode() {
    	if (cameraMode == "lock") {
    		cameraMode = "free";
    		Vector3f c = cam.getLocation();
    		Vector3f p = player.getTrackPoint();
    		//camRotY = (float)Math.asin((double)((c.y-p.y)/cameraDistance));
    		camRotXZ = (float)Math.atan2((double)(c.x-p.x), (double)(c.z-p.z));
    	} else if (cameraMode == "free") {
    		// make sure there are trackable objects
    		boolean noTargets = true;
    		for (int i=0; i<models.size(); i++) {
        		if (models.get(i) != player && models.get(i).trackable) {
        			noTargets = false;
        			break;
        		}
        	}
    		if (!noTargets) {
    			cameraMode = "lock";
    			camRotXZ = 0;
    			nextTarget(0);
    		}
    	}
    	System.out.println("Camera switch to "+cameraMode);
    }
    
    /**
     * Sets the target to the specified model and returns whether it was in the model list
     */
    public Boolean setTarget(Model m) {
    	int i = models.indexOf(m);
    	if (i != -1)
    		currentTarget = m;
    	cam.update();
    	return (i != -1);
    }
    
    /**
     * Sets the currentTarget to another model, according to the value of {@code k}
     * @param k -1 for next closest from current target, 1 for next further,
     * and 0 for closest to player
     */
    public void nextTarget(int k) {
    	// do nothing if there are no models
    	if (models.size() == 0)
    		return;
    	
    	ArrayList<Object[]> o = new ArrayList<Object[]>();
    	
    	// Add models and distances to 2D ArrayList
    	for (int i=0; i<models.size(); i++) {
    		Model m = models.get(i);
    		if (m != player && m.trackable) {
    			Object[] a = new Object[2];
    			a[0] = new Float(player.model.getWorldTranslation().distance(m.model.getWorldTranslation()));
    			a[1] = m;
    			o.add(a);
    		}
    	}
    	
    	// do nothing if there's nothing to sort
    	if (o.size() == 0)
    		return;
    	
    	// Make it an array
    	Object[] t = o.toArray();
    	
    	// Sort 2D array by distances
    	Arrays.sort(t, new Comparator<Object>() {
        	public int compare(Object one, Object two){
        		Object[] first = (Object[]) one;
        		Object[] secnd = (Object[]) two;
        		//System.out.println((Float)(first[0])+" - "+(Float)(secnd[0]));
        		return (int)((Float)(first[0]) - (Float)(secnd[0]));
        	}
        });
    	/*
    	System.out.print("[ ");
    	for (Object f : t) {System.out.print(Arrays.toString((Object[])f));}
    	System.out.println(" ]");
    	*/
    	
        // Locate the currentTarget's index
        int ind = -1;
        for (int i=0; i<t.length; i++) {
    		if (((Object[])t[i])[1] == currentTarget) {
    			ind = i;
    			break;
    		}
    	}
        
    	// Set the new target
        switch (k) {
        	case 0: currentTarget = (Model)(((Object[])t[0])[1]); break;
        	case -1: currentTarget = (Model)(((Object[])t[(t.length+ind-1)%t.length])[1]); break;
        	case 1: currentTarget = (Model)(((Object[])t[(ind+1)%t.length])[1]);
        }
        
        /*
        // move test cylinders to lock point
        Vector3f tp = currentTarget.getTrackPoint();
        for (int i=0; i<cyl.length; i++) {
        	cyl[i].setLocalTranslation(tp.x, tp.y, tp.z);
        }
        */
        
        // Update camera
        cam.update();
    }
    
    /**
     * Adds m to the child node that is reflected by water in this stage.
     * @param m
     */
    public void addModel(Model m) {
    	models.add(m);
    	containerNode.attachChild(m.model);
    }
    
    public boolean removeModel(Model m) {
    	containerNode.detachChild(m.model);
    	return models.remove(m);
    }
    
    public Node getContainerNode() {
    	return containerNode;
    }
    
    public void toggleZPressed(boolean b) {
    	hudNode.zPressed = b;
    }
    
    public void togglePaused() {
    	pause = !pause;
    	frozen = !frozen;
    }

	public void stateRender(float tpf) {
    	
        // Render the rootNode
        DisplaySystem.getDisplaySystem().getRenderer().draw(rootNode);

        if (showBounds) {
            Debugger.drawBounds(rootNode, DisplaySystem.getDisplaySystem()
                    .getRenderer(), true);
        }

        if (showNormals) {
            Debugger.drawNormals(rootNode, DisplaySystem.getDisplaySystem()
                    .getRenderer());
        }

        if (showDepth) {
            DisplaySystem.getDisplaySystem().getRenderer().renderQueue();
            Debugger.drawBuffer(Texture.RenderToTextureType.Depth, Debugger.NORTHEAST,
                    DisplaySystem.getDisplaySystem().getRenderer());
        }
        
        // Have the PassManager render
        pManager.renderPasses(DisplaySystem.getDisplaySystem().getRenderer());
    }

    public void cleanup() {
    }
    
}