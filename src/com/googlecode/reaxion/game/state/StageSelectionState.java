package com.googlecode.reaxion.game.state;

import java.util.ArrayList;

import com.googlecode.reaxion.game.ability.Ability;
import com.googlecode.reaxion.game.ability.ActiveShielder;
import com.googlecode.reaxion.game.ability.AfterImage;
import com.googlecode.reaxion.game.ability.EvasiveStart;
import com.googlecode.reaxion.game.ability.HealingFactor;
import com.googlecode.reaxion.game.ability.PassiveHealer;
import com.googlecode.reaxion.game.ability.RandomInstantGauge;
import com.googlecode.reaxion.game.audio.BgmPlayer;
import com.googlecode.reaxion.game.input.ai.TestAI;
import com.googlecode.reaxion.game.model.character.Austin;
import com.googlecode.reaxion.game.model.character.Character;
import com.googlecode.reaxion.game.model.character.Cy;
import com.googlecode.reaxion.game.model.character.Khoa;
import com.googlecode.reaxion.game.model.character.MajorCharacter;
import com.googlecode.reaxion.game.model.character.Monica;
import com.googlecode.reaxion.game.model.character.Nilay;
import com.googlecode.reaxion.game.model.stage.Stage;
import com.googlecode.reaxion.game.overlay.StageSelectionOverlay;
import com.googlecode.reaxion.game.util.Battle;
import com.googlecode.reaxion.game.util.LoadingQueue;
import com.jme.app.AbstractGame;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.scene.Node;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameStateManager;

/**
 * {@code StageSelectionState} is designated to the stage selection menu. It
 * contains a instance of {@code StageSelectionOverlay}. This state adds key
 * functionality to the stage selection menu. The arrow keys are used to change
 * the selected stage name, and the enter key is used to load that stage and
 * create a new {@code BattleGameState}.
 * 
 * @author Brian
 * 
 */

public class StageSelectionState extends BasicGameState {

	public static final String NAME = "stageSelectionState";

	private static final String stageClassURL = "com.googlecode.reaxion.game.model.stage.";
	private static final String attackBaseLocation = "com.googlecode.reaxion.game.attack.";

	public float tpf;

	private StageSelectionOverlay stageSelectionNode;

	protected InputHandler input;

	private KeyBindingManager manager;

	protected AbstractGame game = null;
	
	public StageSelectionState() {
		super(NAME);
		init();
	}

	private void init() {
		rootNode = new Node("RootNode");

		stageSelectionNode = new StageSelectionOverlay();
		rootNode.attachChild(stageSelectionNode);

		input = new InputHandler();
		initKeyBindings();

		rootNode.updateRenderState();
		rootNode.updateWorldBound();
		rootNode.updateGeometricState(0.0f, true);
	}

	private void initKeyBindings() {
		manager = KeyBindingManager.getKeyBindingManager();
		manager.set("arrow_up", KeyInput.KEY_UP);
		manager.set("arrow_down", KeyInput.KEY_DOWN);
		manager.set("select", KeyInput.KEY_RETURN);
		manager.set("exit", KeyInput.KEY_ESCAPE);
	}

	@Override
	public void update(float _tpf) {
		tpf = _tpf;

		if (input != null) {
			input.update(tpf);

			if (manager.isValidCommand("exit", false)) {
				if (game != null)
					game.finish();
				else
					System.exit(0);
			}
		}

		rootNode.updateGeometricState(tpf, true);

		checkKeyInput();
	}
	
	public StageSelectionOverlay getstageOverlay()
	{
		return stageSelectionNode;
	}
	

	private void checkKeyInput() {
		if (input != null) {
			if (manager.isValidCommand("arrow_up", false))
				stageSelectionNode.updateDisplay(true);
			if (manager.isValidCommand("arrow_down", false))
				stageSelectionNode.updateDisplay(false);
			if (manager.isValidCommand("select", false)) {
				// switchToLoadingOverlay();
				gotoBattleState();
			}
		}

	}

	// private void switchToLoadingOverlay() {
	// Quad cover = new Quad("cover",
	// DisplaySystem.getDisplaySystem().getWidth(),
	// DisplaySystem.getDisplaySystem().getHeight());
	// cover.setSolidColor(new ColorRGBA(0, 0, 0, 1));
	// BitmapText loadingText = new BitmapText(stageSelectionNode.getTextFont(),
	// false);
	// loadingText.setText("Loading " +
	// stageSelectionNode.getSelectedStageName());
	// loadingText.setSize(48);
	// loadingText.setLocalTranslation(cover.getWidth() / 2, cover.getHeight() /
	// 2, 0);
	//		
	// Overlay loading = new Overlay();
	// loading.attachChild(cover);
	// loading.attachChild(loadingText);
	// loading.updateRenderState();
	//		
	// rootNode.detachChild(stageSelectionNode);
	// rootNode.attachChild(loading);
	// }

	/**
	 * Switches from {@code StageSelectionState} to {@code BattleGameState} and
	 * passes a {@code Stage} object corresponding to the stage the user selects
	 * in the menu.
	 */
	private void gotoBattleState() {
		Battle c = Battle.getCurrentBattle();
		c.setStage(stageSelectionNode.getSelectedStageClass());
		Battle.setCurrentBattle(c);
		
		BattleGameState battleState = Battle.createBattleGameState();
		GameStateManager.getInstance().attachChild(battleState);
		battleState.setActive(true);
		setActive(false);
	}

	@Override
	public void cleanup() {
	}

}