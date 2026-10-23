package miyagimayhem.audio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.Test;

/**
 * These tests don't need a sound card. Without one the manager simply
 * stays silent, so everything below must still behave.
 */
public class AudioManagerTest {

    @Test
    public void effectsAndMusicStartOnAndCanBeSwitchedOff() {
        AudioManager audio = new AudioManager();
        assertTrue(audio.isEffectsEnabled());
        assertTrue(audio.isMusicEnabled());
        audio.setEffectsEnabled(false);
        audio.setMusicEnabled(false);
        assertFalse(audio.isEffectsEnabled());
        assertFalse(audio.isMusicEnabled());
        audio.close();
    }

    @Test
    public void volumeIsKeptBetweenZeroAndOneHundred() {
        AudioManager audio = new AudioManager();
        audio.setMusicVolume(250);
        assertEquals(100, audio.getMusicVolume());
        audio.setMusicVolume(-5);
        assertEquals(0, audio.getMusicVolume());
        audio.setMusicVolume(40);
        assertEquals(40, audio.getMusicVolume());
        audio.close();
    }

    @Test
    public void playingAndStoppingNeverThrowsEvenWithoutAnAudioDevice() {
        AudioManager audio = new AudioManager();
        audio.playClick();
        audio.startMusic();
        audio.setMusicEnabled(false);
        audio.setMusicEnabled(true);
        audio.stopMusic();
        assertFalse(audio.isMusicPlaying());
        audio.close();
    }

    @Test
    public void mutedMusicDoesNotPlayWhenTheMenuIsShown() {
        AudioManager audio = new AudioManager();
        audio.setMusicEnabled(false);
        audio.startMusic();
        assertFalse(audio.isMusicPlaying());
        audio.close();
    }

    @Test
    public void bundledSoundsExistAndConvertTo16BitForPlayback() throws Exception {
        for (String file : new String[] {"click.wav", "menu_music.wav"}) {
            URL url = AudioManager.class.getResource(file);
            assertNotNull(file + " is missing", url);
            try (AudioInputStream source = AudioSystem.getAudioInputStream(url)) {
                AudioFormat original = source.getFormat();
                AudioFormat sixteenBit = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        original.getSampleRate(), 16, original.getChannels(),
                        original.getChannels() * 2, original.getSampleRate(), false);
                try (AudioInputStream converted = AudioSystem.getAudioInputStream(sixteenBit, source)) {
                    assertTrue(converted.read(new byte[1024]) > 0);
                }
            }
        }
    }
}
