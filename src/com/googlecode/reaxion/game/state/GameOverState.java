package com.googlecode.reaxion.game.state;

import java.util.logging.Logger;

import com.googlecode.reaxion.game.Reaxion;
import com.googlecode.reaxion.game.input.bindings.GlobalBindings;
import com.googlecode.reaxion.game.input.bindings.MenuBindings;
import com.googlecode.reaxion.game.mission.MissionManager;
import com.googlecode.reaxion.game.overlay.GameOverOverlay;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.jme.app.AbstractGame;
import com.jme.image.Texture;
import com.jme.input.AbsoluteMouse;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.game.state.GameStateManager;

/**
 * {@code GameOverState} is a simple GameState that has a camera and an overlay
 * displaying continuation options for the previous {@code BattleGameState}.
 * 
 * @author Khoa
 */
public class GameOverState extends BaseGameState {

	public static final String NAME = "gameOverState";

	private static final Logger logger = Logger
			.getLogger(GameOverState.class.getName());

	public float tpf;

	private BattleGameState battle;

	private GameOverOverlay overNode;

	protected InputHandler input;

	protected AbstractGame game = null;

	private AbsoluteMouse mouse;
	
	private boolean retry = true;

	public GameOverState(BattleGameState b) {
		super(true);
		setEndsBGM(true);
		battle = b;
	}

	@Override
	protected void init() {
		rootNode = new Node("RootNode");

		// Prepare game over node
		overNode = new GameOverOverlay();
		rootNode.attachChild(overNode);

		// Fix up the camera, will not be needed for final camera controls
		Vector3f loc = new Vector3f(0.0f, 2.5f, 10.0f);
		Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
		cam.setFrame(loc, left, up, dir);
		cam.setFrustumPerspective(45f, (float) DisplaySystem.getDisplaySystem()
				.getWidth()
				/ DisplaySystem.getDisplaySystem().getHeight(), .01f, 1000);
		cam.update();

		// Initial InputHandler
		// input = new FirstPersonHandler(cam, 15.0f, 0.5f);
		input = new InputHandler();

		// Setup software mouse
		mouse = new AbsoluteMouse("Mouse Input", DisplaySystem
				.getDisplaySystem().getWidth(), DisplaySystem
				.getDisplaySystem().getHeight());
		mouse.registerWithInputHandler(input);
		TextureState cursor = DisplaySystem.getDisplaySystem().getRenderer()
				.createTextureState();
		cursor.setTexture(TextureManager.loadTexture(Reaxion.class
				.getClassLoader().getResource(
						"com/googlecode/reaxion/resources/cursors/cursor.png"),
				Texture.MinificationFilter.NearestNeighborNoMipMaps,
				Texture.MagnificationFilter.NearestNeighbor));
		mouse.setRenderState(cursor);
		BlendState as1 = DisplaySystem.getDisplaySystem().getRenderer()
				.createBlendState();
		as1.setBlendEnabled(true);
		as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		as1
				.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		as1.setTestEnabled(true);
		as1.setTestFunction(BlendState.TestFunction.GreaterThan);
		mouse.setRenderState(as1);
		rootNode.attachChild(mouse);

		// Finish up
		rootNode.updateRenderState();
		rootNode.updateWorldBound();
		rootNode.updateGeometricState(0.0f, true);
	}

	@Override
	public void stateUpdate(float _tpf) {
		super.stateUpdate(_tpf);
		
		tpf = _tpf;

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
		}

		// Update the overlay
		overNode.update(this);

		// Update the geometric state of the rootNode
		rootNode.updateGeometricState(tpf, true);

		if (input != null) {
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					MenuBindings.SELECT_FINAL.toString(), false)) {
				if (retry)
					returnRetry();
				else
					returnToHub();
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					MenuBindings.LEFT.toString(), false) ||
					KeyBindingManager.getKeyBindingManager().isValidCommand(
							MenuBindings.RIGHT.toString(), false)) {
				retry = !retry;
				System.out.println("Option changed to "+(retry?"retry":"exit")+".");
				overNode.toggleChoice(retry);
			}
			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
					GlobalBindings.SCREENSHOT.toString(), false)) {
				Reaxion.takeScreenshot("GameOver");
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
				MouseInput.get().setCursorVisible(
						!MouseInput.get().isCursorVisible());
				logger.info("Cursor Visibility set to "
						+ MouseInput.get().isCursorVisible());
			}
		}
	}
	
	private void returnRetry() {
		// TODO: Link to the last mission point
		
		// flush LoadingQueue
		LoadingQueue.resetQueue();
		
		setActive(false);
		GameStateManager.getInstance().detachChild(this);
		
		MissionManager.retrySection();
	}

	private void returnToHub() {
		// flush LoadingQueue
		LoadingQueue.resetQueue();		

		setActive(false);
		GameStateManager.getInstance().detachChild(this);
		
		if (MissionManager.hasCurrentMission())
			MissionManager.endMission();
		else
			MissionManager.startHubGameState();
	}

	public void cleanup() {
	}
}
