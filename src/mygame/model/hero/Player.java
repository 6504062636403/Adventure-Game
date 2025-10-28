package mygame.model.hero;

import java.awt.*;
import java.awt.image.BufferedImage;
import mygame.model.GameMap;

public class Player {
    private double x, y, width, height; //ตำแหน่งและขนาดของplayer
    private double vx, vy; //ความเร็วในแกนxและy
    private boolean onGround = false; 
    private boolean big = false;
    private int lives = 3; //จำนวนชีวิตเริ่มต้น มีค่าเริ่มต้นเป็น3
    private boolean dead = false; //ตาย
    private boolean won = false; //ชนะ
    private BufferedImage sprite; //รูปภาพของplayer
    private GameMap gameMap;
    
    //กำหนดค่าคงที่
    private final double MOVE_SPEED = 1.0; //ความเร็วในการเคลื่อนที่ต่อครั้ง
    private final double MAX_SPEED = 3.0; //ความเร็วสูงสุดที่ผู้เล่นสามารถไปถึงได้
    private final double JUMP_STRENGTH = -16.0; //ความสูงของการกระโดด ร่องลบเพราะแกนYลงด้านล่าง
    private final double GRAVITY = 0.8;
    private final double FRICTION = 0.85;
    private final double MAX_FALL_SPEED = 18.0; 
    private final double GROUND_LEVEL = 450;
    
    
    private boolean facingRight = true;
    private int animationFrame = 0;
    private int animationTimer = 0;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;

    public Player(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite; //spriteจะเป็นรูปplaye 
        this.width = 68; 
        this.height = 68;
        this.vx = 0; 
        this.vy = 0; 
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap; //เชื่อมโยงกับแผนที่เกม
    }

    public void applyInput(boolean left, boolean right, boolean jump) {
        //กำหนดการเคลื่อนที่ในแกนX
        if (left) {
            vx -= MOVE_SPEED * 0.15; //เป็นการทำให้ความเร็วเพิ่มขึ้นอย่างช้าๆ
            if (vx < -MAX_SPEED) vx = -MAX_SPEED; //จำกัดความเร็วสูงสุด
            facingRight = false;
        } else if (right) {
            vx += MOVE_SPEED * 0.15;
            if (vx > MAX_SPEED) vx = MAX_SPEED;
            facingRight = true;
        } else {
            //เป็นการลดความเร็วเมื่อไม่มีการกดปุ่ม
            vx *= FRICTION;
            if (Math.abs(vx) < 0.1) vx = 0;
        }
        
        //กระโดด
        if (jump && onGround) {
            vy = JUMP_STRENGTH;
            onGround = false;
        }
    }

    public void update() {
        
        if (invulnerable) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
            }
        }
        
        //เป็นกำหนดแรงโน้มถ่วง
        vy += GRAVITY;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        //เป็นการเดิน
        animationTimer++;
        if (animationTimer > 8) {
            animationFrame = (animationFrame + 1) % 4;
            animationTimer = 0;
        }

        // การเคลื่อนที่ในแกนXพร้อมการตรวจสอบการชน
        double newX = x + vx;
        Rectangle futureRectX = new Rectangle((int)newX, (int)y, (int)width, (int)height);
        
        // ตรวจสอบการชนในแกนX
        boolean canMove = true;
        
        //ตรวจสอบการชน
        if (gameMap != null && gameMap.checkCollision(futureRectX)) {
            canMove = false;
        }
        
        //ตรวจสอบการชนกับธงที่เป็นจุดสิ้นสุดของแผนที่
        if (gameMap != null && gameMap.getEndPosition() != null) {
            Point flagPos = gameMap.getEndPosition();
            if (newX > flagPos.x + 48) { 
                canMove = false;
                //ชนธงแล้วชนะเกม
                if (x >= flagPos.x - width && !won) {
                    won = true;
                }
            }
        }
        
        if (canMove) {
            x = newX;
            //ตรวจสอบขอบเขตซ้ายของหน้าจอ
            if (x < 0) {
                x = 0;
                vx = 0;
            }
        } else {
            
            vx = 0;
        }

        //แกนY
        double newY = y + vy;
        Rectangle futureRectY = new Rectangle((int)x, (int)newY, (int)width, (int)height);

        if (gameMap != null) {
            //ตรวจสอบการชน
            if (vy > 0) { //ตก
                if (gameMap.checkGroundCollision(futureRectY) || newY + height >= GROUND_LEVEL) {
                    //ลงถึงพื้น
                    if (newY + height >= GROUND_LEVEL) {
                        y = GROUND_LEVEL - height;
                    } else {
                        //ชนบล็อกจากด้านบน ไม่ได้ทำ ไม่สำเร็จ
                        Rectangle testRect = new Rectangle((int)x, (int)y, (int)width, (int)height);
                        while (!gameMap.checkGroundCollision(testRect) && y + height < GROUND_LEVEL) {
                            y++;
                            testRect.y = (int)y;
                        }
                        y--; 
                    }
                    vy = 0;
                    onGround = true;
                } else {
                    y = newY;
                    onGround = false;
                }
            } else if (vy < 0) { //กระโดดขึ้น
                if (gameMap.checkCollision(futureRectY)) {
                    
                    vy = 0;
                } else {
                    y = newY;
                    onGround = false;
                }
            }
        } else {
            //
            y = newY;
            if (y >= GROUND_LEVEL - height) {
                y = GROUND_LEVEL - height;
                vy = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        }

        //ตรวจสอบถ้าตกนอกหน้าจอ
        if (y > 600) {
            loseLife();
            respawn();
        }
    }

    private void respawn() {
        x = 100; // ตำแหน่งxเริ่มต้น
        y = GROUND_LEVEL - height;
        vx = 0;
        vy = 0;
        onGround = true;
        invulnerable = true;
        invulnerabilityTimer = 120; //เกิดจะกระพริบ 2 วินาที
    }

    public Rectangle getBounds() { 
        return new Rectangle((int)x, (int)y, (int)width, (int)height); 
    }

    public void grow() {
        if (!big) { 
            big = true; 
            int oldHeight = (int)height;
            width = 72; 
            height = 72;
            y -= (height - oldHeight); //ปรับตำแหน่ง
        }
    } //อันนี้เป็นกินเห็ดแล้วเพิ่มขนาด
    
    public void shrink() {
        if (big) {
            int oldHeight = (int)height;
            big = false; 
            width = 48; 
            height = 48;
            y += (oldHeight - height); 
            invulnerable = true;
            invulnerabilityTimer = 120; 
        }
    } //อันนี้เป็นกินเห็ดแล้วเพิ่มขนาด
    
    public boolean isBig() { return big; }
    public boolean isInvulnerable() { return invulnerable; }

    public void loseLife() { 
        lives--; 
        if (big) {
            shrink(); //ถ้าชนแล้วจะไม่เสียชีวิต
            lives++; //ไม่เสียชีวิต
        } else {
            // Actually lose a life
            if (lives <= 0) {
                dead = true;
            } else {
                respawn();
            }
        }
    }//อันนี้เป็นกินเห็ดแล้วเพิ่มขนาด
    
    public void gainLife() {
        if (lives < 9) { //จำกัดจำนวนชีวิตสูงสุดที่9 
            lives++;
        }
    }
    
    public int getLives() { return lives; } 
    public void setDead(boolean d) { dead = d; }
    public boolean isDead() { return dead; }
    public void setWon(boolean w) { won = w; }
    public boolean isWon() { return won; }

    public int getX() { return (int)x; }
    public int getY() { return (int)y; }
    public double getVX() { return vx; }
    public double getVY() { return vy; }

    public void draw(Graphics2D g) {
        
        if (invulnerable && (invulnerabilityTimer / 4) % 2 == 0) {
            return; 
        }
        
        if (sprite != null) {
            // Flip sprite based on direction
            if (facingRight) {
                g.drawImage(sprite, (int)x, (int)y, (int)width, (int)height, null);
            } else {
                g.drawImage(sprite, (int)x + (int)width, (int)y, -(int)width, (int)height, null);
            }
        } else {
            // Colored rectangle fallback
            g.setColor(big ? Color.BLUE : Color.CYAN);
            g.fillRect((int)x, (int)y, (int)width, (int)height);
            
            //
            g.setColor(Color.WHITE);
            int eyeSize = 4;
            int eyeY = (int)y + (int)height / 3;
            if (facingRight) {
                g.fillOval((int)x + (int)width - 15, eyeY, eyeSize, eyeSize);
                g.fillOval((int)x + (int)width - 8, eyeY, eyeSize, eyeSize);
            } else {
                g.fillOval((int)x + 4, eyeY, eyeSize, eyeSize);
                g.fillOval((int)x + 11, eyeY, eyeSize, eyeSize);
            }
        }
        
    }
}
