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
        long last = System.nanoTime();
        double nsPerTick = 1000000000.0/60.0;
        double delta = 0;
        while (true) {
            long now = System.nanoTime();
            delta += (now - last)/nsPerTick;
            last = now;
            while (delta >= 1) {
                update();
                delta--;
            }
            repaint();
            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
    }

    private void update() {
        Player p = map.getPlayer();
        if (p != null && !p.isDead() && !p.isWon()) {
            p.applyInput(left, right, jump);
            p.update();

            // Update camera to follow player
            updateCamera(p);

            // collision with mushrooms
            for (Mushroom m : map.getItems()) {
                if (!m.isCollected() && p.getBounds().intersects(m.getBounds())) {
                    m.collect();
                    p.grow();
                }
            }

            // collision with heart mushrooms
            for (HeartMushroom hm : map.getHeartMushrooms()) {
                if (!hm.isCollected() && p.getBounds().intersects(hm.getBounds())) {
                    hm.collect();
                    p.gainLife();
                }
            }

            // Enhanced enemy interactions
            for (int i = map.getEnemies().size() - 1; i >= 0; i--) {
                Demon d = map.getEnemies().get(i);
                if (!d.isAlive()) continue;
                
                Rectangle playerBounds = p.getBounds();
                Rectangle enemyBounds = d.getBounds();
                Rectangle enemyTopBounds = d.getTopBounds();
                
                if (playerBounds.intersects(enemyBounds)) {
                    // Check if player is stomping on enemy (coming from above)
                    if (p.getVY() > 0 && playerBounds.intersects(enemyTopBounds) && playerBounds.y < enemyBounds.y + 10) {
                        // Player stomped on enemy!
                        d.stomp();
                        p.update(); // Give player a little bounce
                    } else if (!p.isInvulnerable()) {
                        // Enemy hit player from side or below
                        if (p.isBig()) {
                            p.shrink();
                        } else {
                            p.loseLife();
                            if (p.getLives() <= 0) {
                                p.setDead(true);
                            }
                        }
                    }
                }
            }

            // Update the map (removes dead enemies)
            map.update();

            // Check flag collision (near fort entrance)
            Flag flag = map.getFlag();
            if (flag != null && !flag.isReached()) {
                Rectangle playerBounds = p.getBounds();
                Rectangle flagBounds = flag.getBounds();
                if (playerBounds.intersects(flagBounds)) { //ตรวจสอบว่าคนชนธงยัง
                    flag.setReached(true); //ถ้าชนแล้ว
                    p.setWon(true); //คนเล่นจะชนะ
                }
            }

            // Check fort entrance (main win condition)
            Point end = map.getEndPosition();
            if (end != null && !p.isWon()) {
                Rectangle pr = p.getBounds();
                // Larger win area for the fort entrance
                Rectangle fortArea = new Rectangle(end.x - 50, end.y - 50, 100, 100);
                if (pr.intersects(fortArea)) {
                    p.setWon(true);
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
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother graphics
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Apply camera transform
        g2.translate(-cameraX, -cameraY);
        
        // Draw the game world (everything moves with the camera)
        map.draw(g2);
        
        // Reset transform for UI elements (UI should stay fixed on screen)
        g2.translate(cameraX, cameraY);

        // UI: hearts and messages (these stay fixed on screen)
        Player p = map.getPlayer();
        if (p != null) {
            // Draw lives as hearts
            for (int i = 0; i < p.getLives(); i++) {
                Image heart = loader.load("heart.png");
                if (heart != null) {
                    g.drawImage(heart, 10 + i * 30, 10, 24, 24, null);
                } else {
                    // Fallback if heart image not found
                    g.setColor(Color.RED);
                    g.fillOval(10 + i * 30, 10, 24, 24);
                }
            }
            
            // Score removed from UI
            
            // Draw game state messages
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            
                if (p.isDead()) {
                String msg = "GAME OVER - Press R to Restart";
                g.setColor(Color.RED);
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g.drawString(msg, x, y);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                // final score display removed
            } else if (p.isWon()) {
                String msg = "FORT REACHED! YOU WIN! - Press R to Restart";
                g.setColor(Color.GREEN);
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g.drawString(msg, x, y);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                // final score display removed
            }
            
            // Show power-up status
            if (p.isBig()) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("SUPER MARIO!", 10, HEIGHT - 40);
            }
            
            // Optional: Show player coordinates for debugging
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Mario: (" + p.getX() + ", " + p.getY() + ")", WIDTH - 150, 20);
            g.drawString("Camera: (" + cameraX + ", " + cameraY + ")", WIDTH - 150, 35);
            g.drawString("Enemies: " + map.getEnemies().size(), WIDTH - 150, 50);
        }
    }

    private void restartGame() {
        MapCreator creator = new MapCreator(loader, "background.png");
        map = creator.create();
        Player player = map.getPlayer();
        if (player != null) {
            player.setGameMap(map);
        }
    // Reset camera position
        cameraX = 0;
        cameraY = 0;
    }

    // KeyListener
    @Override 
    public void keyTyped(KeyEvent e) {}
    
    @Override 
    public void keyPressed(KeyEvent e) {
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
    
    @Override public void keyReleased(KeyEvent e) {
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
