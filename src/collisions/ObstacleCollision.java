/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collisions;

import com.golden.gamedev.object.Sprite;
import com.golden.gamedev.object.collision.BasicCollisionGroup;
import models.Particle;
import views.SpriteView;

/**
 *
 * @author 999
 */
public class ObstacleCollision extends BasicCollisionGroup {
        
    @Override
    public void collided(Sprite s1, Sprite s2) {
        // получаем бота или игрока
        Particle p1 = ((SpriteView)s1).getParticle();
        p1.setSpeed(0);
        p1.fireCharacteristicsIsChanged();
    }
}
