package miyagimayhem.audio;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Plays the button click and the menu music. Either can be switched off
 * separately, and the music has a volume. If the computer has no audio
 * device (or a sound file can't be read) the game carries on silently and
 * isAvailable() reports false, so the GUI can grey the controls out.
 */
public class AudioManager {

    private static final String CLICK_FILE = "click.wav";
    private static final String MUSIC_FILE = "menu_music.wav";
    private static final int DEFAULT_VOLUME_PERCENT = 50;

    private final Clip clickClip;
    private final Clip musicClip;

    private boolean effectsEnabled = true;
    private boolean musicEnabled = true;
    private boolean musicRequested;
    private int musicVolumePercent = DEFAULT_VOLUME_PERCENT;

    public AudioManager() {
        clickClip = load(CLICK_FILE);
        musicClip = load(MUSIC_FILE);
        applyVolume();
    }

    /** True if at least one sound could be loaded and the computer can play it. */
    public boolean isAvailable() {
        return clickClip != null || musicClip != null;
    }

    // ---- button sound ----

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public void setEffectsEnabled(boolean enabled) {
        effectsEnabled = enabled;
    }

    public void playClick() {
        if (effectsEnabled && clickClip != null) {
            clickClip.stop();
            clickClip.setFramePosition(0);
            clickClip.start();
        }
    }

    // ---- menu music ----

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        updateMusic();
    }

    /** Called when the menu is shown. Music plays only if it isn't muted. */
    public void startMusic() {
        musicRequested = true;
        updateMusic();
    }

    /** Called when the menu is left, so there is no music during a game. */
    public void stopMusic() {
        musicRequested = false;
        updateMusic();
    }

    public boolean isMusicPlaying() {
        return musicClip != null && musicClip.isRunning();
    }

    public int getMusicVolume() {
        return musicVolumePercent;
    }

    /** Sets the music volume from 0 (silent) to 100 (the file's full volume). */
    public void setMusicVolume(int percent) {
        musicVolumePercent = Math.max(0, Math.min(100, percent));
        applyVolume();
    }

    /** Stops everything and frees the audio line. Call when the program quits. */
    public void close() {
        musicRequested = false;
        for (Clip clip : new Clip[] {clickClip, musicClip}) {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
        }
    }

    private void updateMusic() {
        if (musicClip == null) {
            return;
        }
        boolean shouldPlay = musicRequested && musicEnabled;
        if (shouldPlay && !musicClip.isRunning()) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (!shouldPlay && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    private void applyVolume() {
        if (musicClip == null || !musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
        float decibels = musicVolumePercent == 0
                ? gain.getMinimum()
                : (float) (20 * Math.log10(musicVolumePercent / 100.0));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
    }

    /** Loads a sound from this package, or returns null if it can't be played. */
    private static Clip load(String fileName) {
        URL url = AudioManager.class.getResource(fileName);
        if (url == null) {
            return null;
        }
        try (AudioInputStream source = AudioSystem.getAudioInputStream(url)) {
            // Some files are 8-bit, which many sound cards refuse, so convert to 16-bit.
            AudioFormat original = source.getFormat();
            AudioFormat sixteenBit = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    original.getSampleRate(), 16, original.getChannels(),
                    original.getChannels() * 2, original.getSampleRate(), false);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(sixteenBit, source));
            return clip;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException
                | IllegalArgumentException | SecurityException e) {
            return null;
        }
    }
}
