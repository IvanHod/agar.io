/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import settings.PlayerSettings;
import collisions.*;
import com.golden.gamedev.Game;
import com.golden.gamedev.funbox.GameSettings;
import com.golden.gamedev.object.GameFont;
import com.golden.gamedev.object.PlayField;
import com.golden.gamedev.object.SpriteGroup;
import com.golden.gamedev.object.background.TileBackground;
import events.*;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import models.GameModel;
import models.Particle;

/**
 *
 * @author 999
 */
public class GameView extends Game {
    
    private final int WIDTH = 2000;
    private final int HEIGHT = 1000;
    
    private final int[][] tiles = new int[WIDTH][HEIGHT];
    
    private final PlayField field = new PlayField();
    
    /**
     * Фон игры
     */
    private TileBackground bg = null;
    
    
    private final GameModel game = new GameModel(WIDTH, HEIGHT);
    
    ArrayList<SpriteView> Sprites = new ArrayList<>();
    
    private SpriteGroup agarParticles = new SpriteGroup("agar");
    private SpriteGroup obstacleParticles = new SpriteGroup("obstacle");
    private ArrayList<SpriteGroup> enemies = new ArrayList<>();
    private SpriteGroup player = new SpriteGroup("player");
    
    private int countBot = 0;
    private int countAgar = 0;
    private int countObstacle = 0;
    private boolean isCreatePlayer = true;
    
    PlayerSettings settings = null;
    
    private GameFont font;
    private GameFont bigFont;
    
    private int AteParticles = 0;
    
    private String resultString = "";
    private String againString = "PRESS \"SPACE\" AND WILL START YOUR GAME!";

    public void addPlayer(String name, String picture) {
        Particle _player = game.createPlayer(name);
        PlayerView pl = new PlayerView(_player);
        pl.setPicture(picture);
        pl.particle.setGameListener(new GameObserver());
        player.add(pl);
    }
    
    public void setSettings(int cAgar, int cBot, int cObstacle, boolean playerCreate) {
        countAgar       = cAgar;
        countBot        = cBot;
        countObstacle   = cObstacle;
        isCreatePlayer  = playerCreate;
    }

    @Override
    public void initResources() {
        game.startGame(countAgar, countBot, countObstacle);
        game.setGameListener(new GameObserver());
        
        
        font  = fontManager.getFont(getImage("libs/font.fnt"));
        bigFont  = fontManager.getFont(getImage("libs/font.fnt"));

        loadAgars();
        loadBots();
        loadObstacle();
        
        field.addGroup(agarParticles);
        field.addGroup(obstacleParticles);
        field.addGroup(player);
        for(SpriteGroup enemy : enemies)
            field.addGroup(enemy);
        
        field.addCollisionGroup(player, agarParticles, new AgarCollision());
        field.addCollisionGroup(player, obstacleParticles, new ObstaclePlayerCollision());

        
        
        bg = new TileBackground(getImages("img/background.png", 1, 1), tiles);

        field.setBackground(bg);
        
        settings = new PlayerSettings();
        settings.setGameListener(new GameObserver());
        if(!isCreatePlayer) {
            settings.setVisible(true);
        }
    }

    @Override
    public void update(long l) {
        game.updateGame(mousePosition());
        bg.update(l);
        field.update(l);
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        System.out.println("" + keyCode);
        return false;
    }

    @Override
    public void render(Graphics2D g) {
        bg.render(g);
        field.render(g);
        PlayerView player = (PlayerView) this.player.getActiveSprite();
        if (player != null) {
            bg.setToCenter(player);
        }
        String draw = "YOU ATE " + AteParticles + " PARTICLES";
        String namePlayer = player == null ? "DO YOU WANT CONNETION?" : "YOUR NAME " 
                + player.particle.getName().toUpperCase();
        font.drawString(g, namePlayer, 9, 9);
        font.drawString(g, draw, 9, 30);
        
        if(player == null) {
            font.drawString(g, resultString, 240, 250);
            font.drawString(g, againString, 180, 300);
        } else {
            font.drawString(g, "", 180, 200);
            font.drawString(g, "", 180, 300);
        }
    }
    
    /**
     * Текущая позиция координат мыши
     * @return 
     */
    public Point mousePosition() {
        Point p = new Point(this.getMouseX(), this.getMouseY());
        p.x += bg.getX();
        p.y += bg.getY();
        return p;
    }
    
    private void loadBots() {
        for( Particle particle : game.get("bot") ) {
            AIView ai = new AIView( particle );
            ai.particle.setGameListener(new GameObserver());
            SpriteGroup aiGroup = new SpriteGroup(particle.getName());
            aiGroup.add(ai);
            field.addGroup(aiGroup);
            field.addCollisionGroup(aiGroup, agarParticles, new AgarCollision());
            field.addCollisionGroup(aiGroup, obstacleParticles, new ObstacleAICollision());
            field.addCollisionGroup(player, aiGroup, new PlayerBotCollision());
            for(SpriteGroup enemy : enemies ) {
                field.addCollisionGroup(enemy, aiGroup, new BotBotCollision());
            }
            enemies.add(aiGroup);
            Sprites.add(ai);
        }
    }
    
    private void loadPlayers() {
        for( Particle particle : game.get("player")) {
            PlayerView pl = new PlayerView( particle );
            pl.particle.setGameListener(new GameObserver());
            player.add(pl);
        }
    }
    
    private void loadAgars() {
        for( Particle particle : game.get("agar")) {
            AgarView agar = new AgarView( particle );
            agar.particle.setGameListener(new GameObserver());
            agarParticles.add(agar);
        }
    }
    
    private void loadObstacle() {
        for( Particle particle : game.get("obstacle")) {
            ObstacleView obstacle = new ObstacleView( particle );
            obstacle.particle.setGameListener(new GameObserver());
            obstacleParticles.add(obstacle);
        }
    }
    
    /**
     * Возвращает размеры окна для изображения
     * @return 
     */
    public Dimension dimensions() {
        return new Dimension(WIDTH, HEIGHT);
    }
    
    protected class GameObserver implements GameListener{

        @Override
        public void ParticleDied(GameEvent e) {
            Particle p = e.getParticle();
            game.removeParticle(p);
            agarParticles.removeInactiveSprites();
            SpriteGroup sg = field.getGroup(p.getName());
            if(sg != null) {
                field.removeGroup(sg);
                enemies.remove(sg);
            }
            if(p.getType().equals("player")) {
                player.removeInactiveSprites();
                resultString = "YOU LOSE WITH SCORE: " + AteParticles;
                AteParticles = 0;
                settings.setVisible(true);
            }
        }

        @Override
        public void generatedAgar(GameEvent e) {
            AgarView agar = new AgarView(e.getParticle());
            agar.particle.setGameListener(new GameObserver());
            agarParticles.add(agar);
        }

        @Override
        public void generatedBot(GameEvent e) {
            AIView ai = new AIView( e.getParticle() );
            ai.particle.setGameListener(new GameObserver());
            SpriteGroup aiGroup = new SpriteGroup(ai.particle.getName());
            aiGroup.add(ai);
            enemies.add(aiGroup);
            field.addGroup(aiGroup);
            field.addCollisionGroup(player, aiGroup, new PlayerBotCollision());
            field.addCollisionGroup(aiGroup, agarParticles, new AgarCollision());
            field.addCollisionGroup(aiGroup, obstacleParticles, new ObstacleAICollision());
            for(SpriteGroup group : enemies)
                field.addCollisionGroup(aiGroup, group, new BotBotCollision());
        }

        @Override
        public void generatedPlayer(GameEvent e) {
        }

        @Override
        public void AteParticle() {
            AteParticles = AteParticles == -1 ? 0 : (AteParticles + 1);
        }

        @Override
        public void createNewPlayer(String name, String ava) {
            addPlayer(name, ava);
        }
    }
}
