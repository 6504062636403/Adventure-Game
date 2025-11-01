package mygame.model.block;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Brick {
    private int x,y,w=48,h=48;
    private BufferedImage sprite;

    public Brick(int x, int y, BufferedImage sprite) { this.x = x; this.y = y; this.sprite = sprite;}

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return w; }
    public int getHeight() { return h; }

    public void draw(Graphics2D g) { //ไว้สำหรับใส่รูปอิฐ
        if (sprite != null) g.drawImage(sprite, x, y, w, h, null);
        else { g.setColor(Color.GRAY); g.fillRect(x,y,w,h); }
    }
}
