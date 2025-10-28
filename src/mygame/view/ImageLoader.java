package mygame.view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class ImageLoader {
    private final String basePath;

    public ImageLoader(String basePath) {
        this.basePath = basePath;
    }

    public BufferedImage load(String name) {
        try {
            // First try to load from resources folder directly
            File file = new File(basePath + File.separator + name);
            if (file.exists()) {
                return ImageIO.read(file);
            }
            
            // Try to load from classpath
            InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
            if (stream != null) {
                BufferedImage img = ImageIO.read(stream);
                stream.close();
                return img;
            }
            
            // Try absolute path from project root
            File absoluteFile = new File("resources" + File.separator + name);
            if (absoluteFile.exists()) {
                return ImageIO.read(absoluteFile);
            }
            
            System.out.println("Failed to load: " + name + " (tried multiple paths)");
            return null;
        } catch (Exception e) {
            System.out.println("Failed to load: " + name + " -> " + e.getMessage());
            return null;
        }
    }
}
