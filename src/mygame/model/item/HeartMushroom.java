package mygame.model.item;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HeartMushroom {
    private int x, y;
    private int width = 48, height = 48;
    private BufferedImage image;
    private boolean collected = false;
    
    public HeartMushroom(int x, int y, BufferedImage image) { //กำหนดตำแหน่งและรูปภาพของเห็ด
        this.x = x; //กำหนดตำแหน่ง x
        this.y = y;
        this.image = image;
    }
    
    public void draw(Graphics2D g) {
        if (!collected) {
            if (image != null) { //ใส่รูปภาพ
                g.drawImage(image, x, y, width, height, null);
            } else { //สร้างเมื่อไม่มีรูปภาพ
                g.setColor(Color.RED);
                g.fillOval(x + 5, y + 10, 15, 15);
                g.fillOval(x + 23, y + 10, 15, 15);
                g.fillRect(x + 5, y + 18, 33, 15);
                int[] xPoints = {x + 22, x + 5, x + 38};
                int[] yPoints = {y + 40, y + 33, y + 33};
                g.fillPolygon(xPoints, yPoints, 3);
                g.setColor(Color.WHITE);
                g.fillRect(x + 20, y + 15, 8, 3);
                g.fillRect(x + 22, y + 13, 4, 7);
            }
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public void collect() { //เก็บหัวใจ
        collected = true;
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}