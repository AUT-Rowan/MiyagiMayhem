package miyagimayhem.gui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.junit.Test;

public class MenuBackgroundTest {

    @Test
    public void bonsaiPictureIsBundledAndReadable() throws Exception {
        try (InputStream in = MenuPanel.class.getResourceAsStream("bonsai.png")) {
            assertNotNull("bonsai.png is missing from the gui package", in);
            BufferedImage image = ImageIO.read(in);
            assertNotNull(image);
            assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
        }
    }
}
