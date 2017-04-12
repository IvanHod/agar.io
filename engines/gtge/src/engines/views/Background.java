/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engines.views;

import com.golden.gamedev.object.background.TileBackground;
import java.awt.image.BufferedImage;

/** 
 * Фон для игры
 */
public class Background extends TileBackground {

    public Background(BufferedImage[] bis, int[][] ints) {
        super(bis, ints);
    }
    
    public void render(Graphics2D g) {
        super.render(g.get());
    }

    /**
     * Возвращает позицию X смещения фона
     * @return 
     */
    @Override
    public double getX() {
        return super.getX();
    }

    /**
     * Возвращает позицию Y смещения фона
     * @return 
     */    
    @Override
    public double getY() {
        return super.getY();
    }
}