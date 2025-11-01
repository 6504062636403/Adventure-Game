package mygame.manager;

import mygame.model.GameMap;
import mygame.model.hero.Player;
import mygame.model.enemy.Demon;
import mygame.model.item.Mushroom;
import mygame.model.item.HeartMushroom;
import mygame.model.item.Flag;
import mygame.model.block.Brick;
import mygame.view.ImageLoader;

import java.awt.image.BufferedImage;

public class MapCreator {

    private final ImageLoader loader;
    private final BufferedImage mapImg;
    
    public MapCreator(ImageLoader loader, String mapFile) { 
        this.loader = loader; //โหลดรูปภาพ
        this.mapImg = loader.load(mapFile); //เป็นการโหลดรูปภาพพื้นหลัง
    }
    

    public GameMap create() {
        BufferedImage backgroundImg = loader.load("background.png");
        GameMap map = new GameMap(backgroundImg);
        if (mapImg == null) {
            System.out.println("Map image not found, creating basic level");
            createBasicLevel(map);
            return map;
        }

        if (backgroundImg != null) {
            System.out.println("Background image size: " + backgroundImg.getWidth() + "x" + backgroundImg.getHeight());
        }
        int pixelMultiplier = 2; // Scaled down coordinates
        
        
        int demonCount = 0, mushroomCount = 0, heartMushroomCount = 0, pipeCount = 0;
        int rightmostContentX = 0;
        
        
        System.out.println("Total demons created from map: " + demonCount);
        System.out.println("Total mushrooms created from map: " + mushroomCount);
        System.out.println("Total heart mushrooms created from map: " + heartMushroomCount);
        System.out.println("Total pipes created from map: " + pipeCount);
        
        // Calculate level dimensions first
        int levelWidth = mapImg.getWidth() * pixelMultiplier;
        int backgroundWidth = (backgroundImg != null) ? backgroundImg.getWidth() : levelWidth;
        int actualLevelLength = Math.max(levelWidth, backgroundWidth / 2);
        
        createDefaultGround(map, actualLevelLength);
        
        
        int regularDemonSpacing = 400;
        int startX = 500;
        int additionalDemons = 0;
        
        System.out.println("Level width: " + levelWidth + ", Background width: " + backgroundWidth + ", Using: " + actualLevelLength);
        
        for (int x = startX; x < actualLevelLength; x += regularDemonSpacing) {
            // Check if there's already a demon nearby (within 250 pixels)
            // boolean demonNearby = false;
            // for (Demon existingDemon : map.getEnemies()) {
            //     if (Math.abs(existingDemon.getX() - x) < 250) {
            //         demonNearby = true;
            //         break;
            //     }
            // }
            
            // if (!demonNearby) {

                Demon.Type demonType;
                double r = Math.random();
                if (r < 0.5) {
                    demonType = Demon.Type.DEMON1;
                } else {
                    demonType = Demon.Type.DEMON2;
                }
                
                String spriteName = (demonType == Demon.Type.DEMON1) ? "demon1.png" : "demon2.png";
                
                map.addEnemy(new Demon(x, 402, loader.load(spriteName), demonType, true)); // true = static
                additionalDemons++;
                demonCount++;
                rightmostContentX = Math.max(rightmostContentX, x);
                
                if (additionalDemons <= 15) {
                    System.out.println("Created additional demon " + additionalDemons + " (" + demonType + ") at (" + x + ",402)");
                }

            // }
        }
        
        System.out.println("Total additional demons created: " + additionalDemons);
        System.out.println("Total demons in level: " + demonCount);
        
        // Add some heart mushrooms throughout the level for extra lives
        addHeartMushrooms(map, actualLevelLength);
        
        // Add random mushrooms for growing throughout the level
        addRandomMushrooms(map, actualLevelLength);
        
        // Position the win condition at the fort in the background
        // The fort is typically at the end of the background image
        int fortX, fortY;
        if (backgroundImg != null) {
            // Fort is usually at the right side of the background image
            fortX = backgroundImg.getWidth() - 200; // Near the end of background
            fortY = 350; // Ground level where fort entrance would be
        } else {
            // Fallback if no background
            fortX = Math.max(rightmostContentX + 300, mapImg.getWidth() * pixelMultiplier - 200);
            fortY = 350;
        }
        
        map.setEndPosition(fortX, fortY);
        
        // Create a flag at the fort entrance (optional visual indicator)
        Flag flag = new Flag(fortX - 1000, fortY - 96, loader.load
        ("flag.png")); // Position flag before fort entrance
        map.setFlag(flag);
        
        System.out.println("Rightmost content at: " + rightmostContentX);
        System.out.println("Fort win area positioned at (" + fortX + "," + fortY + ")");
        
        // Always create a default player at proper ground level
        Player player = new Player(100, 402, loader.load("player.png")); // 402 = 450 - 48 (ground level - player height)
        map.setPlayer(player);
        
        return map;
    }
    
    // Create default ground level across the entire level width
    private void createDefaultGround(GameMap map, int levelLength) {
        int groundY = 450; // Ground level
        
        // Extend ground to cover the entire level length including fort area
        BufferedImage backgroundImg = loader.load("background.png");
        int fullLevelWidth = levelLength;
        if (backgroundImg != null) {
            // Extend to at least cover the fort area
            fullLevelWidth = Math.max(levelLength, backgroundImg.getWidth() - 100);
        }
        
        System.out.println("Creating ground from 0 to " + fullLevelWidth + " pixels");
        
        // Create continuous ground across the entire level
        for (int x = 0; x < fullLevelWidth; x += 48) {
            map.addBlock(new Brick(x, groundY, loader.load("brick.png")));
        }
        
        // Add additional ground layers for thickness
        for (int x = 0; x < fullLevelWidth; x += 48) {
            map.addBlock(new Brick(x, groundY + 48, loader.load("brick.png")));
            map.addBlock(new Brick(x, groundY + 96, loader.load("brick.png")));
            map.addBlock(new Brick(x, groundY + 144, loader.load("brick.png")));
        }
        
        System.out.println("Created default ground from 0 to " + fullLevelWidth + " at level " + groundY);
    }
    
    // Add heart mushrooms throughout the level for gaining extra lives
    private void addHeartMushrooms(GameMap map, int levelLength) {
        int heartMushroomSpacing = 1200;
        int startX = 800; // Start after player gets used to the game
        int heartMushroomsAdded = 0;
        
        for (int x = startX; x < levelLength; x += heartMushroomSpacing) {
            // Place heart mushrooms at a reachable height
            int heartY = 350; 
            map.addHeartMushroom(new HeartMushroom(x, heartY, loader.load("heart.png")));
            heartMushroomsAdded++;
            
            if (heartMushroomsAdded <= 5) {
                System.out.println("Added heart mushroom " + heartMushroomsAdded + " at (" + x + "," + heartY + ")");
            }
        }

        System.out.println("Total heart mushrooms added to level: " + heartMushroomsAdded);
    }
    
    //สุ่มเห็ดเพิ่มขนาดPlayer
    private void addRandomMushrooms(GameMap map, int levelLength) { 
        int minSpacing = 800; //ระยะห่างขั้นต่ำระหว่างเห็ด
        int maxSpacing = 1300; //ระยะห่างสูงสุดระหว่างเห็ด
        int startX = 200; //จุดเริ่มต้นวางเห็ดในแกน X
        int mushroomsAdded = 0;
        
        int x = startX;
        while (x < levelLength - 200) { //เว้นที่ไว้สำหรับธง
            //สุ่มในแกน X
            int spacing = minSpacing + (int)(Math.random() * (maxSpacing - minSpacing));
            x += spacing; //เก็บค่าระยะห่างที่สุ่มได้ในแกน X
            //สุ่มตำแหน่งY
            int mushroomY;
            double heightChance = Math.random(); //สุ่มเลข
            if (heightChance < 0.2) { 
                mushroomY = 402; //บนพื้นดิน
            } else {
                mushroomY = 350; //แกน Y แบบลอย
            }
            map.addItem(new Mushroom(x, mushroomY, loader.load("mushroom.png"))); //เพิ่มเห็ด
            mushroomsAdded++;
            if (mushroomsAdded <= 8) {
                System.out.println("Added random mushroom " + mushroomsAdded + " at (" + x + "," + mushroomY + ")");
            }
        }
        System.out.println("Total random mushrooms added to level: " + mushroomsAdded);
    }
    
    private void createBasicLevel(GameMap map) {
        // Add player at starting position
        map.setPlayer(new Player(100, 350, loader.load("player.png")));
        
        // Create World 1-1 style level with multiple sections
        
        // Section 1: Starting area with basic platforms
        createGroundSection(map, 0, 48 * 10); // Ground blocks from start
        createFloatingPlatform(map, 350, 380, 4); // Small floating platform
        addMushroom(map, 400, 332);
        addEnemy(map, 450, 350, Demon.Type.DEMON1);
        
        // Section 2: Gap with pipe
        createGroundSection(map, 48 * 15, 48 * 5);
        addEnemy(map, 48 * 16, 350, Demon.Type.DEMON2);
        
        // Section 3: Multi-level platforms (pyramid style)
        createGroundSection(map, 48 * 22, 48 * 8);
        createStaircase(map, 48 * 25, 400, 4); // Ascending stairs
        addMushroom(map, 48 * 27, 300);
        addEnemy(map, 48 * 29, 350, Demon.Type.DEMON1);
        
        // Section 4: Long ground with obstacles
        createGroundSection(map, 48 * 35, 48 * 15);
        createFloatingPlatform(map, 48 * 38, 350, 3);
        createFloatingPlatform(map, 48 * 42, 300, 4);
        createFloatingPlatform(map, 48 * 47, 320, 3);
        addMushroom(map, 48 * 39, 302);
        addMushroom(map, 48 * 44, 252);
        addEnemy(map, 48 * 40, 300, Demon.Type.DEMON2);
        addEnemy(map, 48 * 45, 270, Demon.Type.DEMON1);
        
        // Section 5: Challenge area with gaps
        createGroundSection(map, 48 * 55, 48 * 4);
        // Gap here - player must jump!
        createGroundSection(map, 48 * 62, 48 * 6);
        addEnemy(map, 48 * 64, 350, Demon.Type.DEMON2);
        
        // Section 6: Final area leading to flag
        createGroundSection(map, 48 * 70, 48 * 10);
        createStaircase(map, 48 * 75, 400, 6); // Bigger staircase
        addMushroom(map, 48 * 77, 280);
        addEnemy(map, 48 * 72, 350, Demon.Type.DEMON1);
        addEnemy(map, 48 * 78, 250, Demon.Type.DEMON2);
        
        // Final platform before fort
        createFloatingPlatform(map, 48 * 82, 350, 5);
        
        // Set end position at the fort location (based on background image)
        BufferedImage backgroundImg = loader.load("background.png");
        int fortX, fortY;
        if (backgroundImg != null) {
            fortX = backgroundImg.getWidth() - 200; // Near the end of background where fort is
            fortY = 350; // Ground level
        } else {
            fortX = 48 * 85; // Fallback position
            fortY = 350;
        }
        map.setEndPosition(fortX, fortY);
        
        // Create and add the flag near the fort
        Flag flag = new Flag(fortX - 100, fortY - 96, loader.load("flag.png"));
        map.setFlag(flag);
    }
    
    private void createGroundSection(GameMap map, int startX, int width) {
        int groundY = 450;
        for (int x = startX; x < startX + width; x += 48) {
            map.addBlock(new Brick(x, groundY, loader.load("brick.png")));
        }
    }
    
    private void createFloatingPlatform(GameMap map, int startX, int y, int length) {
        for (int i = 0; i < length; i++) {
            map.addBlock(new Brick(startX + i * 48, y, loader.load("brick.png")));
        }
    }
    
    private void createStaircase(GameMap map, int startX, int baseY, int steps) {
        for (int step = 0; step < steps; step++) {
            int stepY = baseY - (step * 48);
            for (int block = 0; block <= step; block++) {
                map.addBlock(new Brick(startX + step * 48, stepY + block * 48, loader.load("brick.png")));
            }
        }
    }
    
    private void addMushroom(GameMap map, int x, int y) {
        map.addItem(new Mushroom(x, y, loader.load("mushroom.png")));
    }
    
    private void addEnemy(GameMap map, int x, int y, Demon.Type type) {
        String spriteName = (type == Demon.Type.DEMON1) ? "demon1.png" : "demon2.png";
        System.out.println("Adding enemy of type " + type + " at (" + x + "," + y + ")");
        map.addEnemy(new Demon(x, y, loader.load(spriteName), type));
    }
}
