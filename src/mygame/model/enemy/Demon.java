package mygame.model.enemy;

import java.awt.*;
import java.awt.image.BufferedImage;
import mygame.model.GameMap;

public class Demon {
    public enum Type { DEMON1, DEMON2 }
    private double x, y, w = 48, h = 48;
    private double vx, vy;
    private int dir = -1; 
    private double speed;
    private BufferedImage sprite;
    private Type type;
    private GameMap gameMap;
    private boolean alive = true;
    private boolean onGround = false;
    private boolean isStatic = false; 
    // กำหนดค่าคงที่
    private final double GRAVITY = 0.8;
    private final double MAX_FALL_SPEED = 15.0;
    private final double GROUND_LEVEL = 450;

    public Demon(int x, int y, BufferedImage sprite, Type type) { //เป็นการกำหนดการเคลื่อนที่ของสัตว์อันตราย
        this.x = x; //ตำแหน่งเริ่มต้น 
        this.y = y; 
        this.sprite = sprite; //รูปภาพของสัตว์อันตราย
        this.type = type; 
        this.vx = 0; //ความเร็วเริ่มต้น
        this.vy = 0;
    
        this.speed = (type == Type.DEMON1) ? 1.5 : 2.5; //ถ้าเป็น DEMON1 ความเร็ว=1.5 แต่ถ้าเป็น DEMON2 ความเร็ว=2.5
    }

    public Demon(int x, int y, BufferedImage sprite, Type type, boolean isStatic) { //เป็นการบอกให้รู้ว่าสัตว์อันตรายนั้นจะอยู่นิ่งหรือไม่
        this(x, y, sprite, type); 
        this.isStatic = isStatic; //กำหนดค่าสถานะให้อยู่นิ่ง
        if (isStatic) {
            this.speed = 0; //ไม่เคลื่อนที่
        }
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap; //เชื่อมโยงกับgameMap
    }

    public void update() { //สถานะของสัตว์อันตราย
        if (!alive) return; //ถ้าตายจะไม่ทำอะไร
        
        if (isStatic) { //ถ้าเป็นสัตว์อันตรายที่อยู่นิ่ง ปรับตามแรงโน้มถ่วง
            if (y + h < GROUND_LEVEL) {
                vy += GRAVITY;
                if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;
                
                double newY = y + vy;
                if (newY + h >= GROUND_LEVEL) {
                    y = GROUND_LEVEL - h;
                    vy = 0;
                    onGround = true;
                } else {
                    y = newY;
                }
            } else {
                y = GROUND_LEVEL - h;
                vy = 0;
                onGround = true;
            }
            return; 
        }
        
        // Original movement logic for non-static demons
        // Apply gravity
        vy += GRAVITY;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;
        
        // Horizontal movement
        vx = dir * speed;
        
        // Check horizontal collision and platform edges
        double newX = x + vx;
        Rectangle futureRectX = new Rectangle((int)newX, (int)y, (int)w, (int)h);
        
        boolean shouldTurn = false;
        
        // Check wall collision
        if (gameMap != null && gameMap.checkCollision(futureRectX)) {
            shouldTurn = true;
        }
        
        // Check if we're about to walk off a platform (smart AI)
        if (gameMap != null && onGround) {
            Rectangle edgeCheck = new Rectangle((int)(newX + (dir > 0 ? w : -5)), (int)(y + h), 5, 10);
            if (!gameMap.checkGroundCollision(edgeCheck) && y + h < GROUND_LEVEL - 10) {
                shouldTurn = true; // Don't walk off platforms
            }
        }
        
        // Turn around if needed
        if (shouldTurn) {
            dir *= -1;
            vx = dir * speed;
        } else {
            x = newX;
        }
        
        // Keep within reasonable world bounds
        if (x < -100) {
            dir = 1;
            x = -100;
        } else if (x > 3000) {
            dir = -1;
            x = 3000;
        }

        // Vertical movement with collision detection
        double newY = y + vy;
        Rectangle futureRectY = new Rectangle((int)x, (int)newY, (int)w, (int)h);

        if (gameMap != null) {
            // Check ground collision
            if (vy > 0) { // Falling
                if (gameMap.checkGroundCollision(futureRectY) || newY + h >= GROUND_LEVEL) {
                    // Land on ground or block
                    if (newY + h >= GROUND_LEVEL) {
                        y = GROUND_LEVEL - h;
                    } else {
                        // Find exact landing position
                        Rectangle testRect = new Rectangle((int)x, (int)y, (int)w, (int)h);
                        while (!gameMap.checkGroundCollision(testRect) && y + h < GROUND_LEVEL) {
                            y++;
                            testRect.y = (int)y;
                        }
                        y--; // Step back one pixel
                    }
                    vy = 0;
                    onGround = true;
                } else {
                    y = newY;
                    onGround = false;
                }
            } else if (vy < 0) { // Jumping up (if somehow launched)
                if (gameMap.checkCollision(futureRectY)) {
                    vy = 0;
                } else {
                    y = newY;
                    onGround = false;
                }
            }
        } else {
            // Fallback without collision detection
            y = newY;
            if (y >= GROUND_LEVEL - h) {
                y = GROUND_LEVEL - h;
                vy = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        }
        
        // Remove enemies that fall off the world
        if (y > 700) {
            alive = false;
        }
    }

    public void stomp() {
        alive = false;
        vy = -8; // Bounce effect when stomped
    }

    public boolean isAlive() {
        return alive;
    }

    public Rectangle getBounds() { 
        return new Rectangle((int)x, (int)y, (int)w, (int)h); 
    }

    public Rectangle getTopBounds() {
        // Smaller rectangle on top for stomping detection
        return new Rectangle((int)x + 5, (int)y, (int)w - 10, (int)h / 3);
    }

    public void draw(Graphics2D g) { //วาดสัตว์อันตราย
        if (!alive) { 
            g.setColor(Color.DARK_GRAY);
            g.fillOval((int)x, (int)(y + h - 10), (int)w, 10);
            return;
        }
        if (sprite != null) { //ถ้ามีรูปภาพ
            if (dir > 0) { //
                g.drawImage(sprite, (int)x, (int)y, (int)w, (int)h, null);
            } else {
                g.drawImage(sprite, (int)x + (int)w, (int)y, -(int)w, (int)h, null); 
            }
        } else { //ถ้าไม่มีรูปภาพ วาดเป็นวงกลมแทน
            g.setColor(type == Type.DEMON1 ? Color.CYAN : Color.MAGENTA); 
            g.fillOval((int)x, (int)y, (int)w, (int)h);
            g.setColor(Color.RED); //วาดตา
            int eyeSize = 6;
            int eyeY = (int)y + (int)h / 3;
            if (dir > 0) {
                g.fillOval((int)x + (int)w - 18, eyeY, eyeSize, eyeSize);
                g.fillOval((int)x + (int)w - 10, eyeY, eyeSize, eyeSize);
            } else {
                g.fillOval((int)x + 4, eyeY, eyeSize, eyeSize);
                g.fillOval((int)x + 12, eyeY, eyeSize, eyeSize);
            }
            g.setColor(Color.BLACK);
            int[] xPoints, yPoints;
            if (dir > 0) {
                xPoints = new int[]{(int)x + (int)w - 8, (int)x + (int)w - 8, (int)x + (int)w - 2};
                yPoints = new int[]{(int)y + (int)h/2 - 4, (int)y + (int)h/2 + 4, (int)y + (int)h/2};
            } else {
                xPoints = new int[]{(int)x + 8, (int)x + 8, (int)x + 2};
                yPoints = new int[]{(int)y + (int)h/2 - 4, (int)y + (int)h/2 + 4, (int)y + (int)h/2};
            }
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }

    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
    public Type getType() { return type; }
}
