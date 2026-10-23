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
 * Plays the button click and the background music. Each can be switched off
 * separately. If the computer has no audio device (or a sound file can't be
 * read) the game carries on silently and isAvailable() reports false, so the
 * GUI can grey the controls out.
 */
public class AudioManager {

    private static final String CLICK_FILE = "click.wav";
    private static final String MUSIC_FILE = "menu_music.wav";
    /** The music file is loud, so it is played about 8 dB quieter (roughly 40% of full volume). */
    private static final float MUSIC_GAIN_DB = -7.9f;

    private final Clip clickClip;
    private final Clip musicClip;

    private boolean effectsEnabled = true;
    private boolean musicEnabled = true;
    private boolean musicStarted;

    public AudioManager() {
        clickClip = load(CLICK_FILE);
        musicClip = load(MUSIC_FILE);
        applyMusicGain();
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

    /** Starts the background music (unless it is muted). Call once when the program opens. */
    public void startMusic() {
        musicStarted = true;
        updateMusic();
    }

    public boolean isMusicPlaying() {
        return musicClip != null && musicClip.isRunning();
    }

    /** Stops everything and frees the audio line. Call when the program quits. */
    public void close() {
        musicStarted = false;
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
        boolean shouldPlay = musicStarted && musicEnabled;
        if (shouldPlay && !musicClip.isRunning()) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else if (!shouldPlay && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    private void applyMusicGain() {
        if (musicClip != null && musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), MUSIC_GAIN_DB)));
        }
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
