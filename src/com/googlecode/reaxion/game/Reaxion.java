package com.googlecode.reaxion.game;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Callable;

import com.googlecode.reaxion.game.audio.AudioPlayer;
import com.googlecode.reaxion.game.mission.MissionManager;
import com.googlecode.reaxion.game.state.BattleGameState;
import com.googlecode.reaxion.game.state.CharacterSelectionState;
import com.googlecode.reaxion.game.state.KeyPreferencesState;
import com.googlecode.reaxion.game.state.StageSelectionState;
import com.googlecode.reaxion.game.state.TitleScreenState;
import com.googlecode.reaxion.game.util.FontUtils;
import com.googlecode.reaxion.game.util.KeyBindingUtils;
import com.googlecode.reaxion.game.util.PlayerInfoManager;
import com.jme.input.MouseInput;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.LoadingGameState;

/**
 * The main game. This should run everything, but for now it's just a luncher for {@code BattleGameState}.
 * @author Nilay, Khoa
 */
public class Reaxion {

	private static final String GAME_VERSION = "0.8a";
	private static final String screenshotDir = "screenshots/";

	private static int screenWidth;
	private static int screenHeight;
	
	private static boolean music;
	private static boolean sfx;
	
	/**
	 * Multithreaded game system that shows the state of GameStates
	 */
	private static ReaxionGame game;
	
	private StageSelectionState stageState;
	
	/**
	 * GameState that shows progress of resource loading
	 */
	private LoadingGameState loadState;
	
	/**
	 * GameState that allows basic WASD movement, mouse camera rotation,
	 * and placement of objects, lights, etc.
	 */
	private BattleGameState battleState;
	
	
	/**
	 * GameState that allows character selection.
	 */
	private CharacterSelectionState charState;
	//private StageSelectionState stageState;

	/**
	 * Initialize the system
	 */
	public Reaxion() {
		/* Allow collection and viewing of scene statistics */
		System.setProperty("jme.stats", "set");
		/* Create a new StandardGame object with the given title in the window */
		game = new ReaxionGame("Reaxion v" + GAME_VERSION);
	}

	public static void main(String[] args) {
		try {
			Reaxion main = new Reaxion();
			main.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start up the system
	 * @throws IOException when StandardGame's settings are unavailable (?)	
	 * @throws InterruptedException 
	 */
	public void start() throws InterruptedException {
		if(ReaxionSettingsPanel.prompt(game.getSettings()))
			game.start();
		GameTaskQueueManager.getManager().update(new GameInit());
        
		screenWidth = DisplaySystem.getDisplaySystem().getWidth();
		screenHeight = DisplaySystem.getDisplaySystem().getHeight();
		
		music = game.getSettings().isMusic();
		sfx = game.getSettings().isSFX();
	}
	
	/**
	 * Performs necessary cleanup, then closes application.
	 */
	public static void terminate() {
		game.shutdown();
	}
	
	public static void takeScreenshot(String descriptor) {
		File f = new File(screenshotDir);
		if (!f.exists())
			f.mkdir();
		
		Calendar c = Calendar.getInstance();
		String tag = "Reaxion_" + descriptor + "_" +
			c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR) +"_" + 
			c.get(Calendar.HOUR_OF_DAY) + "-" + c.get(Calendar.MINUTE) + "-" + 
			(c.get(Calendar.SECOND) < 10 ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND));
		DisplaySystem.getDisplaySystem().getRenderer().takeScreenShot(screenshotDir + tag);
	}
	
	public static int getScreenWidth() {
		return screenWidth;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}
	
	public static boolean musicEnabled() {
		return music;
	}
	
	public static boolean sfxEnabled() {
		return sfx;
	}

	/**
	 * Initializes the system.
	 * @author Nilay
	 *
	 */
	private class GameInit implements Callable<Void> {

		//@Override	
		public Void call() throws Exception {
			MouseInput.get().setCursorVisible(true);
			AudioPlayer.prepare();
			FontUtils.loadFonts();
			KeyBindingUtils.initKeyBindings();
			MissionManager.createMissions();
			
//			KeyPreferencesState keyState = new KeyPreferencesState();
//			GameStateManager.getInstance().attachChild(keyState);
//			keyState.setActive(true);
			
//			TitleScreenState titleState = new TitleScreenState();
//			GameStateManager.getInstance().attachChild(titleState);
//			titleState.setActive(true);
			
			PlayerInfoManager.init();
			
//			BurstGridGameState gridState = new BurstGridGameState(PlayerInfoManager.get("Monica"));
//			GameStateManager.getInstance().attachChild(gridState);
//			gridState.setActive(true);
			
//			charState = new CharacterSelectionState();
//			GameStateManager.getInstance().attachChild(charState);
//			charState.setActive(true);
//			
//			StageSelectionState s = new StageSelectionState();
//			GameStateManager.getInstance().attachChild(s);
			
			return null;
		}	
	}
}