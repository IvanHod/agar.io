/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import collisions.*;
import com.golden.gamedev.Game;
import com.golden.gamedev.object.PlayField;
import com.golden.gamedev.object.SpriteGroup;
import com.golden.gamedev.object.background.TileBackground;
import com.golden.gamedev.object.collision.BasicCollisionGroup;
import events.*;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import static java.lang.Thread.yield;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.GameModel;
import models.Particle;

/**
 *
 * @author 999
 */
public class GameView extends Game {
    
    private final int WIDTH = 2000;
    private final int HEIGHT = 1000;
    
    private int[][] tiles = new int[WIDTH][HEIGHT];
    
    private final PlayField field = new PlayField();
    
    /**
     * Фон игры
     */
    private TileBackground bg = null;
    
    
    private GameModel game = new GameModel(WIDTH, HEIGHT);
    
    ArrayList<SpriteView> Sprites = new ArrayList<>();
    
    private SpriteGroup agarParticles = new SpriteGroup("agar");
    private SpriteGroup obstacleParticles = new SpriteGroup("obstacle");
    private ArrayList<SpriteGroup> enemies = new ArrayList<>();
    private SpriteGroup player = new SpriteGroup("player");

    @Override
    public void initResources() {
        
        game.startGame();
        game.setGameListener(new GameObserver());
        loadAgars();
        loadBots();
        loadObstacle();
        loadPlayers();
        
        field.addGroup(agarParticles);
        field.addGroup(obstacleParticles);
        field.addGroup(player);
        for(SpriteGroup enemy : enemies)
            field.addGroup(enemy);
        
        field.addCollisionGroup(player, agarParticles, new AgarCollision());
        field.addCollisionGroup(player, obstacleParticles, new ObstaclePlayerCollision());

        bg = new TileBackground(getImages("img/background.png", 1, 1), tiles);

        field.setBackground(bg);
    }

    @Override
    public void update(long l) {
        game.updateGame(mousePosition());
        bg.update(l);
        field.update(l);
    }

    @Override
    public void render(Graphics2D g) {
        bg.render(g);
        field.render(g);
        PlayerView player = (PlayerView) this.player.getActiveSprite();
        if (player != null) {
            bg.setToCenter(player);
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
            for(SpriteGroup enemy : enemies ) {
                field.addCollisionGroup(enemy, aiGroup, new BotBotCollision());
            }
            field.addCollisionGroup(aiGroup, agarParticles, new AgarCollision());
            field.addCollisionGroup(aiGroup, obstacleParticles, new ObstacleAICollision());
            field.addCollisionGroup(player, aiGroup, new PlayerBotCollision());
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
            game.removeParticle(e.getParticle());
            agarParticles.removeInactiveSprites();
            SpriteGroup sg = field.getGroup(e.getParticle().getName());
            if(sg != null) {
                field.removeGroup(sg);
                enemies.remove(sg);
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
    }
}
