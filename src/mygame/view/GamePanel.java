package mygame.view;

import mygame.manager.MapCreator;
import mygame.model.GameMap;
import mygame.model.hero.Player;
import mygame.model.enemy.Demon;
import mygame.model.item.Mushroom;
import mygame.model.item.HeartMushroom;
import mygame.model.item.Flag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    private Thread gameThread;
    private GameMap map;
    private ImageLoader loader;
    private final int WIDTH = 1200;
    private final int HEIGHT = 900;

    // Camera system
    private int cameraX = 0;
    private int cameraY = 0;
    private final int CAMERA_SMOOTH_FACTOR = 8; // Lower = smoother, higher = more responsive
    
    // Game stats
    // (score removed) game no longer tracks score in GamePanel

    // input
    private boolean left, right, jump;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        setBackground(Color.CYAN);
        
        loader = new ImageLoader("resources");
        MapCreator creator = new MapCreator(loader, "background.png");
        map = creator.create();
        
        // Set the game map reference in the player for collision detection
        Player player = map.getPlayer();
        if (player != null) {
            player.setGameMap(map);
        }
        
        addKeyListener(this);
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() { 
        long last = System.nanoTime(); //บันทึกเวลาขณะเริ่มเกม ในหน่วยนาโนวินาที
        double nsPerTick = 1000000000.0/60.0; //60 FPS
        double delta = 0; //ตัวแปรสะสมความต่างของเวลา เอาไว้เช็คว่าถึงเวลาต้อง update เกมยัง
        while (true) {
            long now = System.nanoTime();
            delta += (now - last)/nsPerTick;
            last = now; //อัพเดทเวลาปัจจุบัน
            while (delta >= 1) {  //ถ้า delta ถึง 1 ต้องอัพเดทเกม
                update();
                delta--;
            }
            repaint();
            try { Thread.sleep(2); } catch (InterruptedException e) {} //ลดการใช้ CPU
        }
    }

    private void update() {
        Player p = map.getPlayer();
        if (p != null && !p.isDead() && !p.isWon()) {
            p.applyInput(left, right, jump);
            p.update();
            updateCamera(p); //อัพเดทตำแหน่งกล้องตามผู้เล่น

            for (Mushroom m : map.getItems()) { //อัพเดทการเก็บเห็ด
                if (!m.isCollected() && p.getBounds().intersects(m.getBounds())) { //ตรวจสอบการชนด้วยกรอบสี่เหลี่ยม
                    m.collect(); //เก็บเห็ด
                    p.grow(); //Player ตัวใหญ่ขึ้น
                }
            }

            for (HeartMushroom hm : map.getHeartMushrooms()) { //อัพเดทการเก็บหัวใจ
                if (!hm.isCollected() && p.getBounds().intersects(hm.getBounds())) { //ตรวจสอบการชนด้วยกรอบสี่เหลี่ยม
                    hm.collect(); //เก็บหัวใจ คลาส HeartMushroom
                    p.gainLife(); //ผู้เล่นจะได้หัวใจเพิ่มขึ้น
                }
            }

            for (int i = map.getEnemies().size() - 1; i >= 0; i--) { //อัพเดทการชนกับ Demon
                Demon d = map.getEnemies().get(i);
                if (!d.isAlive()) continue;
                Rectangle playerBounds = p.getBounds(); //กรอบของผู้เล่น
                Rectangle enemyBounds = d.getBounds(); //กรอบของDemon
                Rectangle enemyTopBounds = d.getTopBounds(); //กรอบด้านบนของDemon
                if (playerBounds.intersects(enemyBounds)) { 
                    //ตรวจสอบว่าผู้เล่นกำลังตกลงมาและชนด้านบน(ในแกนY)ของDemonไหม
                    if (p.getVY() > 0 && playerBounds.intersects(enemyTopBounds) && playerBounds.y < enemyBounds.y + 10) { 
                        d.stomp(); //ถ้าชนให้Demonตาย
                        p.update(); //อัพเดทสถานะผู้เล่น
                    } else if (!p.isInvulnerable()) { //ถ้าไม่ใช่การชนด้านบน(ในแกนY)
                        if (p.isBig()) { //ถ้าผู้เล่นตัวใหญ่
                            p.shrink(); //ผู้เล่นจะหดตัวเล็กลง
                        } else { //ถ้าผู้เล่นตัวเล็กอยู่แล้ว
                            p.loseLife(); //ผู้เล่นจะเสียหัวใจ 1 ดวง
                            if (p.getLives() <= 0) { //ถ้าหมดหัวใจ
                                p.setDead(true); //ผู้เล่นตาย
                            }
                        }
                    }
                }
            }

            map.update();

            Flag flag = map.getFlag(); //อัพเดทการชนกับธง
            if (flag != null && !flag.isReached()) { //ถ้ายังไม่ชน
                Rectangle playerBounds = p.getBounds(); //กรอบของผู้เล่น
                Rectangle flagBounds = flag.getBounds(); //กรอบของธง
                if (playerBounds.intersects(flagBounds)) { //ตรวจสอบว่าคนชนธงยัง
                    flag.setReached(true); //ถ้าชนแล้ว
                    p.setWon(true); //คนเล่นจะชนะ
                }
            }
            Point end = map.getEndPosition(); //อัพเดทการชนกับปแนวเส้นชนะ
            if (end != null && !p.isWon()) {  //ถ้ายังไม่ชนะ
                Rectangle pr = p.getBounds(); //กรอบของผู้เล่น
                Rectangle fortArea = new Rectangle(end.x - 1000, end.y - 50, 100, 100); //กรอบเส้นชนะ
                if (pr.intersects(fortArea)) { //ตรวจสอบว่าผู้เล่นชนเส้นชนะยัง
                    p.setWon(true); //ถ้าชนแล้วผู้เล่นจะชนะ
                }
            }
        }
    }

    private void updateCamera(Player player) {
        // Calculate target camera position (center player on screen)
        int targetX = player.getX() - WIDTH / 2;
        int targetY = player.getY() - HEIGHT / 2;

        // Smooth camera movement
        cameraX += (targetX - cameraX) / CAMERA_SMOOTH_FACTOR;
        cameraY += (targetY - cameraY) / CAMERA_SMOOTH_FACTOR;

        // Optional: Add camera boundaries to prevent showing empty areas
        // You can adjust these based on your level size
        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        
        // For a level that extends beyond screen, you might want to set max boundaries:
        // if (cameraX > LEVEL_WIDTH - WIDTH) cameraX = LEVEL_WIDTH - WIDTH;
        // if (cameraY > LEVEL_HEIGHT - HEIGHT) cameraY = LEVEL_HEIGHT - HEIGHT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g; //สร้างกราฟฟิค2ดี
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(-cameraX, -cameraY);//เลื่อนกล้องตามการวิ่ง
        map.draw(g2);
        g2.translate(cameraX, cameraY); 
        Player p = map.getPlayer();
        if (p != null) {
            for (int i = 0; i < p.getLives(); i++) { //วาดหัวใจตามจำนวนชีวิต
                Image heart = loader.load("heart.png"); //โหลดรูปหัวใจ
                if (heart != null) {
                    //วาดรูปหัวใจที่มุมซ้ายบน โดยเว้นระยะห่าง 30 กว้าง 24 สูง 24
                    g.drawImage(heart, 10 + i * 30, 10, 24, 24, null); 
                } else {
                    g.setColor(Color.RED); //ถ้าไม่มีรูปให้วาดวงกลมแดงแทน
                    g.fillOval(10 + i * 30, 10, 24, 24);
                }
            }
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
                if (p.isDead()) { //ถ้าแพ้ให้ขึ้นข้อความ "GAME OVER - Press R to Restart"
                String msg = "GAME OVER - Press R to Restart";
                g.setColor(Color.RED); //สีแดง
                int x = (getWidth() - fm.stringWidth(msg)) / 2; //กลางจอ
                int y = getHeight() / 2; 
                g.drawString(msg, x, y);
                g.setFont(new Font("Arial", Font.BOLD, 16));
            } else if (p.isWon()) { //ข้อความเมื่อชนะ
                String msg = "FORT REACHED! YOU WIN! - Press R to Restart";
                g.setColor(Color.GREEN);
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g.drawString(msg, x, y);
                g.setFont(new Font("Arial", Font.BOLD, 16));
            }
            //ข้อความแสดงตำแหน่องของผู้เล่น กล้อง และสัตว์อันตราย
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Player: (" + p.getX() + ", " + p.getY() + ")", WIDTH - 150, 20);
            g.drawString("Camera: (" + cameraX + ", " + cameraY + ")", WIDTH - 150, 35);
        }
    }

    private void restartGame() { //รีสตาร์ทเกม
        MapCreator creator = new MapCreator(loader, "background.png"); //สร้างเกมใหม่
        map = creator.create(); //สร้างแผนที่ใหม่
        Player player = map.getPlayer(); //ตั้งค่าGameMapให้Playerตัวใหม่
        if (player != null) { 
            player.setGameMap(map); //เชื่อมโยงกับGameMap
        }
    // Reset กล้อง
        cameraX = 0;
        cameraY = 0;
    }

    // KeyListener
    @Override 
    public void keyTyped(KeyEvent e) {} 
    
    @Override 
    public void keyPressed(KeyEvent e)  { //เป็นการตรวจสอบปุ่มที่กด
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            jump = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        }
    }
    
    @Override public void keyReleased(KeyEvent e) { //เป็นการตรวจสอบปุ่มที่ปล่อย
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            jump = false;
        }
    }
}
