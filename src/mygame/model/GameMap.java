package mygame.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import mygame.model.hero.Player;
import mygame.model.block.Brick;
import mygame.model.item.Mushroom;
import mygame.model.item.HeartMushroom;
import mygame.model.item.Flag;
import mygame.model.enemy.Demon;

public class GameMap {
    private BufferedImage background;
    private Player player;
    private List<Brick> bricks = new ArrayList<>();
    private List<Demon> enemies = new ArrayList<>();
    private List<Mushroom> items = new ArrayList<>();
    private List<HeartMushroom> heartMushrooms = new ArrayList<>();
    private Point endPosition;
    private Flag flag;

    public GameMap(BufferedImage bg) { this.background = bg; } 

    public void setPlayer(Player p) { this.player = p; }
    public Player getPlayer() { return this.player; }

    public void addBlock(Brick b) { bricks.add(b); }
    public void addEnemy(Demon d) { 
        d.setGameMap(this); //เชื่อมโยงกับแผนที่เกม
        enemies.add(d); 
    }
    public void addItem(Mushroom m) { items.add(m); }
    public void addHeartMushroom(HeartMushroom hm) { heartMushrooms.add(hm); }

    public List<Demon> getEnemies() { return enemies; }
    public List<Mushroom> getItems() { return items; }
    public List<HeartMushroom> getHeartMushrooms() { return heartMushrooms; }
    public List<Brick> getBricks() { return bricks; }

    public void setEndPosition(int x, int y) { this.endPosition = new Point(x,y); }
    public Point getEndPosition() { return endPosition; }
    
    public void setFlag(Flag flag) { this.flag = flag; }
    public Flag getFlag() { return flag; }

    
    public boolean checkCollision(Rectangle rect) {
        for (Brick b : bricks) {
            Rectangle brickRect = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            if (rect.intersects(brickRect)) {
                return true;
            }
        }
        return false;
    }

    // Check if player is standing on ground or a block
    public boolean checkGroundCollision(Rectangle rect) {
        // Ground level collision - player bottom should be at 450 (ground blocks are at y=450)
        if (rect.y + rect.height >= 450) {
            return true;
        }
        
        // Check collision with blocks from above (player standing on blocks)
        Rectangle groundCheckRect = new Rectangle(rect.x + 5, rect.y + rect.height, rect.width - 10, 5);
        return checkCollision(groundCheckRect);
    }

    public void update() {
        if (player != null) player.update();
        
        // Update enemies and remove dead ones
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Demon d = enemies.get(i);
            d.update();
            if (!d.isAlive()) {
                // Remove dead enemies after a brief delay
                enemies.remove(i);
            }
        }
    }

    public void draw(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        if (background != null) g2.drawImage(background, 0, 0, null);
        for (Brick b : bricks) b.draw(g2);
        for (Mushroom m : items) m.draw(g2);
        for (HeartMushroom hm : heartMushrooms) hm.draw(g2);
        for (Demon d : enemies) d.draw(g2);
        if (player != null) player.draw(g2);
        
        // Draw the flag
        if (flag != null) {
            flag.draw(g2);
        }
    }
}
