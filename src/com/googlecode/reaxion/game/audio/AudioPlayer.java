package com.googlecode.reaxion.game.audio;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJavaSound;

import com.googlecode.reaxion.game.Reaxion;
import com.googlecode.reaxion.game.state.StageGameState;
import com.jme.math.Vector3f;

/**
 * Controls all audio of the game.
 * 
 * @author Brian Clanton
 *
 */

public class AudioPlayer {

	private static String baseURL = "com/googlecode/reaxion/resources/audio/";
	private static String trackDir = "tracks/";
	private static String sfxDir = "sfx/";
	private static String loggerHeader = "Audio Player Logger: ";
	
	private static BackgroundMusic currentBGM;
	
	private static boolean playingBGM;
	
	private static float volume = 1f;
	
	private static SoundSystem sound;
	private static SoundSystemLogger logger;
	
	/**
	 * Triggers sound system initialization.
	 */
	public static void prepare() {
		try {
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setSoundFilesPackage(baseURL);
		} catch (SoundSystemException e) {
			e.printStackTrace();
		}
		
		sound = new SoundSystem();
		
		logger = new SoundSystemLogger();
		SoundSystemConfig.setLogger(logger);
	}
	
	/**
	 * Places background music in the audio queue.
	 * 
	 * @param filename Filename of background music to be queued
	 */
	public static void queueBGM(BackgroundMusic bgm) {
		if (!Reaxion.musicEnabled())
			return;
		
		String filename = bgm.getFilename();
		String bgmName = filename.substring(0, filename.indexOf("."));
		String ext = filename.substring(filename.indexOf("."));
		String intro = bgmName + "-intro";
		currentBGM = bgm;
		
		sound.newStreamingSource(true, currentBGM.getTitle(), trackDir + intro + ext, true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
		sound.queueSound(currentBGM.getTitle(), trackDir + filename);
		
		logger.message(loggerHeader + "Queued BGM " + filename, 0);
	}
	
	/**
	 * Starts background music.
	 */
	public static void startBGM() {
		if (!Reaxion.musicEnabled())
			return;
		
		playingBGM = true;
		sound.play(currentBGM.getTitle());
		logger.message(loggerHeader + "BGM " + currentBGM.getTitle() + " started.", 0);
	}

	/**
	 * Stops background music.
	 */
	public static void clearBGM() {
		if (!Reaxion.musicEnabled())
			return;
		
		playingBGM = false;
		sound.stop(currentBGM.getTitle());
		logger.message(loggerHeader + "BGM " + currentBGM.getTitle() + " cleared.", 0);
	}
	
	/**
	 * Updates the listener position and orientation.
	 * 
	 * @param b {@code StageGameState} object that is currently active
	 */
	public static void update(StageGameState b) {
		if (!Reaxion.sfxEnabled())
			return;
		
		Vector3f loc = b.getPlayer().model.getLocalTranslation();
		Vector3f lookAt = b.getPlayer().rotationVector.normalize();
		sound.setListenerPosition(loc.x, loc.y, loc.z);
		sound.setListenerOrientation(lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
	}

	/**
	 * Plays a sound effect at a given position with roll-off attenuation.
	 * 
	 * @param filename Filename of the sound effect to be played
	 * @param x X position of sound effect to be played
	 * @param y Y position of sound effect to be played
	 * @param z Z position of sound effect to be played
	 */
	public static void playSoundEffect(String filename, float x, float y, float z) {
		if (!Reaxion.sfxEnabled())
			return;
		
		sound.quickPlay(true, sfxDir + filename, false, x, y, z, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
		logger.message(loggerHeader + "Quick SFX " + filename + " played.", 0);
	}
	
	/**
	 * Plays a repeating sound effect at a given position with roll-off attenuation.
	 * 
	 * @param filename Filename of the sound effect to be played
	 * @param x X position of sound effect to be played
	 * @param y Y position of sound effect to be played
	 * @param z Z position of sound effect to be played
	 */
	public static void playRepeatingSoundEffect(String filename, float x, float y, float z) {
		if (!Reaxion.sfxEnabled())
			return;
		
		sound.newStreamingSource(true, filename, sfxDir + filename, true, x, y, z, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
		sound.play(filename);
		logger.message(loggerHeader + "Repeating SFX " + filename + " played.", 0);
	}
	
	/**
	 * Stops a repeating sound effect.
	 * 
	 * @param filename Filename of the sound effect to be stopped
	 */
	public static void stopRepeatingSoundEffect(String filename) {
		if (!Reaxion.sfxEnabled())
			return;
		
		sound.stop(filename);
		logger.message(loggerHeader + "Repeating SFX " + filename + " stopped.", 0);
	}
	
	/**
	 * Lowers master volume due to game pause.
	 */
	public static void gamePaused() {
		sound.setMasterVolume(volume * .5f);
		logger.message(loggerHeader + "Master Volume lowered.", 0);
	}
	
	/**
	 * Returns master volume to normal due to game unpause.
	 */
	public static void gameUnpaused() {
		sound.setMasterVolume(volume);
		logger.message(loggerHeader + "Master Volume returned to normal.", 0);
	}
	
	/**
	 * Checks for currently playing background music.
	 * 
	 * @return {@code true} if background music, {@code false} if no background music
	 */
	public static boolean hasCurrentBGM() {
		return playingBGM;
	}
	
	/**
	 * Calls cleanup method of {@code SoundSystem}.
	 */
	public static void cleanup() {
		sound.cleanup();
	}
	
}
