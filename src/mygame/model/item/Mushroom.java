package mygame.model.item;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Mushroom {
    private int x,y,w=48,h=48;
    private boolean collected = false;
    private BufferedImage sprite;

    public Mushroom(int x, int y, BufferedImage sprite) {
        this.x = x; this.y = y; this.sprite = sprite;
    }

    public void draw(Graphics2D g) {
        if (!collected) {
            if (sprite != null) g.drawImage(sprite, x, y, w, h, null);
            else { g.setColor(Color.PINK); g.fillOval(x,y,w,h); }
        }
    }

    public void collect() { collected = true; }
    public boolean isCollected() { return collected; }
    public Rectangle getBounds() { return new Rectangle(x,y,w,h); }
}
