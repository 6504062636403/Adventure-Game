package mygame.model.item;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Flag {
    private int x, y;
    private int width, height;
    private BufferedImage image;
    private boolean reached;
    
    public Flag(int x, int y, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.reached = false;
        
        if (image != null) {
            this.width = image.getWidth();
            this.height = image.getHeight();
        } else {
            this.width = 48;
            this.height = 96;
        }
    }
    
    public void draw(Graphics2D g) {
        if (image != null) { //เมื่อมีรูปภาพ
            g.drawImage(image, x, y, width, height, null);
        } else {
            // Fallback drawing if image not found
            g.setColor(new Color(139, 69, 19)); // Brown color
            g.fillRect(x + 20, y, 4, height); // Flag pole
            g.setColor(Color.RED);
            g.fillRect(x + 24, y, 30, 20); // Flag
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("FINISH", x - 5, y - 10);
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public void setReached(boolean reached) {
        this.reached = reached;
    }
    
    public boolean isReached() {
        return reached;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}