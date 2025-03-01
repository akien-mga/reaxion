package com.googlecode.reaxion.game.audio;

import java.util.ArrayList;

import com.googlecode.reaxion.game.model.Model;
import com.googlecode.reaxion.game.state.StageGameState;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.AudioTrack.TrackType;

/**
 * Utility used to play global/positional and looping/non-looping sound effects.
 * @author Khoa Ha
 *
 */
public class SfxPlayer {
	
	private static String baseURL = "com/googlecode/reaxion/resources/audio/sfx/";
	
	private static final int clearTime = 100;
	private static int clearCount = 0;
	
	private static ArrayList<Object[]> tracker = new ArrayList<Object[]>();
	private static ArrayList<AudioTrack> global = new ArrayList<AudioTrack>();
	
	/**
	 * Adds and plays a sound effect.
	 * @param file Filename of sound
	 * @param loop Whether sound loops or not
	 * @return Reference to the new {@code AudioTrack}
	 */
	public static AudioTrack addSfx(String file, boolean loop) {
		AudioTrack sfx = AudioSystem.getSystem().createAudioTrack(SfxPlayer.class.getClassLoader().getResource(baseURL+file), false);
		sfx.setType(TrackType.ENVIRONMENT);
		sfx.setRelative(false);
		sfx.setLooping(loop);
		sfx.play();
		
		global.add(sfx);
		
        return sfx;
	}
	
	/**
	 * Adds and plays a positional sound effect.
	 * @param file Filename of sound
	 * @param emitter Model emitting sound
	 * @param range Audible range from emitter
	 * @param loop Whether sound loops or not
	 * @return Reference to the new {@code AudioTrack}
	 */
	public static AudioTrack addSfx(String file, Model emitter, int range, boolean loop) {
		range = Math.max(range, 0);
		AudioTrack sfx = AudioSystem.getSystem().createAudioTrack(SfxPlayer.class.getClassLoader().getResource(baseURL+file), false);
		
		if (sfx != null) {
			sfx.setType(TrackType.POSITIONAL);
			sfx.setRelative(false);
			sfx.setLooping(loop);
			sfx.play();
			
			Object[] data = new Object[] {sfx, emitter, range};			
			tracker.add(data);
		}
        return sfx;
	}
	
	/**
	 * Stops and removes a loaded {@code AudioTrack} from the player.
	 * @param sfx {@code AudioTrack} to remove
	 * @return Whether the {@code AudioTrack} was removed or not
	 */
	public static boolean removeSfx(AudioTrack sfx) {
		// check positional sounds
		for (int x = tracker.size(); --x >= 0; ) {
			if (((AudioTrack) tracker.get(x)[0]) == sfx) {
				((AudioTrack) tracker.get(x)[0]).stop();
				((AudioTrack) tracker.get(x)[0]).release();
				tracker.remove(x);
				return true;
			}
		}
		// check global sounds
		for (int x = global.size(); --x >= 0; ) {
			if (global.get(x) == sfx) {
				global.get(x).stop();
				global.get(x).release();
				global.remove(x);
				return true;
			}
		}
		// not here
		return false;
	}
	
	/**
	 * Called by {@code BattleGameState} to tweak positional sounds based
	 * on relative spatial location.
	 * @param b current {@code BattleGameState}
	 */
	public static void update(StageGameState b) {    
		// fade all sounds being tracked by distance to player
        for (int x = tracker.size(); --x >= 0; ) {
        	if (!((AudioTrack) tracker.get(x)[0]).isActive()) {
        		((AudioTrack) tracker.get(x)[0]).release();
        		tracker.remove(x);
        	} else {
        		float dist = b.getPlayer().model.getWorldTranslation().distance(
        				((Model) tracker.get(x)[1]).model.getWorldTranslation());
        		((AudioTrack) tracker.get(x)[0]).setVolume(1 - Math.min(dist/(Integer.parseInt(tracker.get(x)[2].toString())), 1));
        	}
        }
        
        // release unused resources on occasion
        if (clearCount >= clearTime) {
        	for (int x = global.size(); --x >= 0; ) {
            	if (!global.get(x).isActive()) {
            		global.get(x).release();
            		global.remove(x);
            	}
        	}
        	clearCount = 0;
        } else {
        	clearCount++;
        }
    }
	
}
