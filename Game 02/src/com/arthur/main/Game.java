package com.arthur.main;

import com.arthur.entities.*;
import com.arthur.graficos.Spritesheet;
import com.arthur.graficos.UI;
import com.arthur.world.Camera;
import com.arthur.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends Canvas implements Runnable, KeyListener {

    public static JFrame frame;
    private Thread thread;
    private boolean isRunning = true;
    public static final int WIDTH = 260;
    public static final int HEIGHT = 180;
    public static final int SCALE = 3;

    private int Cur_level = 1, max_level = 4;
    private BufferedImage image;
    private BufferedImage bullet;

    public static List<Entitie> entities;
    public static List<Enemy> enemies;
    public static List<BulletShoot> bullets;

    public static Spritesheet spritesheet;
    public static Player player;
    public static World world;
    public static Random rand;
    public static UI ui;

    public static String gameState = "menu";
    public static boolean gameover = true;
    public static int framesgameover = 0;
    private boolean restart = false;

    public Menu menu;


    public Game(){
        Sound.music.loop();
        rand = new Random();
        addKeyListener(this);
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        initFrame();
        /*Iniciando objetos*/
        ui = new UI();
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        entities = new ArrayList<Entitie>();
        enemies = new ArrayList<Enemy>();
        bullets = new ArrayList<BulletShoot>();

        spritesheet = new Spritesheet("/spritesheet.png");
        player = new Player(0, 0, 16, 16, spritesheet.getSprite(32, 0, 16, 16));
        entities.add(player);
        bullet = spritesheet.getSprite(96, 16, 16, 16);

        /*Iniciando mundo*/
        world = new World("/level1.png");
        /* *** */

        menu = new Menu();

    }

    public void initFrame() {
        frame = new JFrame();
        frame.add(this);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public synchronized void start() {
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    public synchronized void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }

    public void tick() {
        if(gameState == "normal") {
            restart=false;
            for (int i = 0; i < entities.size(); i++) {
                Entitie e = entities.get(i);
                e.tick();
            }
            for (int i = 0; i < bullets.size(); i++) {
                bullets.get(i).tick();
            }
            if (enemies.size() == 0) {
                Cur_level++;
                if (Cur_level > max_level) {
                    Cur_level = 1;
                }
                String newWorld = "level" + Cur_level + ".png";
                World.Restart(newWorld);
            }
        }else if (gameState == "Game over"){
            framesgameover++;
            if(framesgameover == 30){
                framesgameover = 0;
                if(gameover){
                    gameover = false;
                }else{
                    gameover = true;
                }
            }
        }else if (gameState == "menu"){
            menu.tick();
        }
        if (restart && gameState == "Game over"){
            Sound.menu.play();
            restart = false;
            gameState = "normal";
            Cur_level = 1;
        String newWorld = "level" + Cur_level + ".png";
        World.Restart(newWorld);

        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = image.getGraphics();
        g.setColor(new Color(0, 0, 0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        /*renderizando o mundo*/
        world.render(g);
        /* *** */

        /*renderização dos objetos*/
        for (int i = 0; i < entities.size(); i++) {
            Entitie e = entities.get(i);
            e.render(g);
        }
        for(int i = 0; i<bullets.size(); i++){
            bullets.get(i).render(g);
        }
        /* *** */
        ui.render(g);
        g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);

        g.drawImage(bullet, 700, 6, 12*SCALE, 12*SCALE, null);
        g.setColor(Color.black);
        g.setFont(new Font("arial", Font.BOLD, 16));
        g.drawString("x "+player.bullet, 730, 33);
        if(gameState == "Game over"){
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0,0,0,170));
            g2.fillRect(0, 0, WIDTH*SCALE, HEIGHT*SCALE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("arial", Font.BOLD, 40));
            g.drawString("Game Over!", (WIDTH*SCALE)/2 - 100, (HEIGHT*SCALE)/2);
            if(gameover) {
                g.setFont(new Font("arial", Font.BOLD, 15));
                g.drawString("Press ENTER to restart.", (WIDTH * SCALE) / 2 - 70, (HEIGHT * SCALE) / 2 + 35);
            }
        }else if(gameState == "menu"){
            menu.render(g);
        }
        bs.show();
    }

    public void run() {
        long lastTime = System.nanoTime();
        double amoutOfTicks = 60.0;
        double ns = 1000000000 / amoutOfTicks;
        double delta = 0;
        int frames = 0;
        double timer = System.currentTimeMillis();
        requestFocus();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1) {
                tick();
                render();
                frames++;
                delta--;
            }
            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames);
                frames = 0;
                timer += 1000;
            }
        }
        stop();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if(gameState == "normal"){
            player.right = true;
            Sound.move.loop();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
            if(gameState == "normal") {
                player.left = true;
                Sound.move.loop();
            }

        }
        if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
            if(gameState == "normal") {
                player.up = true;
                Sound.move.loop();
            }
            if(gameState == "menu"){
                menu.up = true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
            if(gameState == "normal"){
                player.down = true;
                Sound.move.loop();
            }
            if(gameState == "menu"){
                menu.down = true;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            player.shoot = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            restart = true;
            if(gameState == "menu"){
                menu.enter = true;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            gameState = "menu";
            menu.pause = true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT){
            player.right = false;
            Sound.move.stop();
        }
        else if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT){
            player.left = false;
            Sound.move.stop();
        }
        if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP){
            player.up = false;
            Sound.move.stop();
        }
        else if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN){
            player.down = false;
            Sound.move.stop();
        }
    }
}
