/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import models.GameMath;
import models.GameModel;
import models.Particle;

/**
 *
 * @author 999
 */
public class AIController extends Controller {
    
    public AIController(GameModel game, Particle particle) {
        super(game, particle);
    }
    private int stepCount = 0;
    private int stepObstacleCount = 0;
    private int angleForStep;
    public void setSteps(int _stepCount){
        stepObstacleCount = _stepCount;
    }
    public int getObstacleSteps(){
        return stepObstacleCount;
    }
     public int getSteps(){
        return stepCount;
    }
    public void setAngleForStep(int _angleForStep){
        angleForStep = _angleForStep;
    }
    /**
     * Базовая реализация лишь проверяет, что спрайт не вышел за поля
     * @param mousePosition 
     */
    @Override
    public void update(Point mousePosition) {
        if(stepObstacleCount==0){
            if(stepCount==0){
                int angle = checkGoOutBorder();
                if(angle!=-1){
                    particle.setSpeed(0.1);
                    particle.setAngle(angle);
                    particle.fireCharacteristicsIsChanged();
                    angleForStep = angle;
                    stepCount = (int)(200 + Math.random()*(300));
                    return;
                }
                //найдем самую близкую к нам бактерию больше нас
                Particle bigOne = findNearestBiggerParticle();
                //найдем самую близкую к нам бактерию меньше нас
                Particle smallOne = findNearestSmallerParticle();
                //найдем самую близкую к нам агарину
                Particle agar = findNearestAgar();
                angle = 0;
                //выберем что приоритетнее в данной ситуации
                String particleStr = chooseParticle(smallOne,bigOne,agar);
                //если оказалось приоритетнее убежать от большой бактерии
                if("bigOne".equals(particleStr)){
                   //Расчитываем угол до нее
                   angle = GameMath.angle(particle.getPosition(), bigOne.getPosition());
                   //Разворачиваемся
                   angle = 360 - angle;
                }
                //если оказалось приоритетнее охотится за маленькой бактерией
                if("smallOne".equals(particleStr)){
                    //Расчитываем угол до маленькой бактерии
                    angle = GameMath.angle(particle.getPosition(), smallOne.getPosition());
                }
                //Если оказалось приоритетнее есть агар
                if("agar".equals(particleStr)){
                    //Расчитываем угол до агарины
                     angle = GameMath.angle(particle.getPosition(), agar.getPosition());

                }
                //Если все равно куда двигаться 
                if("random".equals(particleStr)){
                    //Двигаемся рандомно TODO
                    Random r = new Random();
                    angle = r.nextInt(360);
                }
                //Сообщаем частице выбранный угол
                particle.setAngle(angle);
                //Сообщаем частице выбранную скорость
                particle.setSpeed(3.0/particle.getSize());

                particle.fireCharacteristicsIsChanged();
                angleForStep = angle;
                stepCount = (int)(50 + Math.random()*(500));
            }
            else {
                 stepCount--; 
                 particle.setAngle(angleForStep);
                 particle.fireCharacteristicsIsChanged();

            }
        }
        else{
            System.out.print("obs\n");
            stepObstacleCount--;
            particle.setAngle(angleForStep);
            particle.fireCharacteristicsIsChanged();
        }
        
        
    }
    
    public String chooseParticle(Particle smallOne, Particle bigOne, Particle agar){
        if(bigOne == null) { //Если на поле вообще нет больше меня
            if(smallOne == null){ //Если на поле нет больше меня и меньше меня тоже нет
                if(agar == null){ // Если на поле только я
                    //Будем двигаться рандомно
                    return "random";
                }
                else{ //Если на поле кроме меня только агар
                    //Едим агар
                    return "agar";
                }
            }
            else{//Если на поле только агар и меньше меня
                //Гонимся за маленькой бактерией
                return "smallOne";
            }
        }
        else{//Если на поле есть бактерии больше меня
            if(smallOne == null){//Если на поле есть бактерии больше меня, а меньше меня нет
                if(agar == null){//Если на поле только большая бактерия и я
                    //Будем убегать от большой бактерии
                    return "bigOne";
                }
                else{//Если на поле есть бактерия больше меня и агар
                    //Если до агара ближе чем до большой бактерии
                    if(Math.abs(GameMath.distance(particle.getPosition(),bigOne.getPosition())-bigOne.getSize()/2)>GameMath.distance(particle.getPosition(),agar.getPosition())){
                        //Будем есть агар
                        return "agar";
                    }
                    else{ //Если до большой бактерии ближе чем до агара
                        //Будем убегать от большой бактерии
                        return "bigOne";
                    }
                }
            }
            else{//Если на поле есть бактерии больше и меньше меня
                if(agar == null){//Если есть бактерии больше и меньше меня, но нет агара
                    //Если до маленькойбактерии ближе чем до большой
                    if(GameMath.distance(particle.getPosition(),bigOne.getPosition())>GameMath.distance(particle.getPosition(),smallOne.getPosition())){
                        //Будем гнаться за маленькой бактерией
                        return "smallOne";
                    }
                    else{//Если большая бактерия ближе маленькой
                        //Убегаем от большой бактерии
                        return "bigOne";
                    }
                }
                else{//Если на поле есть и агар,и большие бактерии и маленкие
                    //Если маленькая бактерия ближе чем большая 
                    if((GameMath.distance(particle.getPosition(),bigOne.getPosition()))>GameMath.distance(particle.getPosition(),smallOne.getPosition())){
                        //Гонимся за маленькой бактерией
                        return "smallOne";
                    }
                    else{//Если агар ближе чем большая бактерия
                        if((GameMath.distance(particle.getPosition(),bigOne.getPosition()))>GameMath.distance(particle.getPosition(),agar.getPosition())){
                            //Едим агар
                            return "agar";
                        }
                        else{//Если еда дальше чем большая бактерия
                           //Убегаем от большой бактерии
                           return "bigOne";
                        }
                    }
                }
            }
        }
    }
    
    // !TODO Вынести получение ботов и игроков в общий файл, и передавать как параметр
    // что бы он по три раза не перебирал и не копировал массив
    public Particle findNearestSmallerParticle(){
        ArrayList<Particle> particlAround = game.get("bot");
        ArrayList<Particle> players = game.get("player");
        particlAround.addAll(players);
        double distToP;
        Particle nearestP = null;
        for(Particle p : particlAround){
            if(p.getSize() < particle.getSize() && Math.abs(p.getSize() - particle.getSize()) > 10){
                distToP = GameMath.distance(p.getPosition(),particle.getPosition());
                if(distToP < MIN_DITANCE){
                    nearestP = p;
                }
           }
        }
        return nearestP;
    }
    
    
    // !TODO Вынести получение ботов и игроков в общий файл, и передавать как параметр
    // что бы он по три раза не перебирал и не копировал массив
    public Particle findNearestBiggerParticle(){
        ArrayList<Particle> particlAround = game.get("bot");
        ArrayList<Particle> players = game.get("player");
        particlAround.addAll(players);
        double distToP;
        Particle nearestP = null;
        for(Particle p : particlAround){
           if(p.getSize()>particle.getSize() && Math.abs(p.getSize() - particle.getSize()) > 10){
               distToP = GameMath.distance(p.getPosition(),particle.getPosition());
               if(distToP < MIN_DITANCE){
                   nearestP = p;
               }
           }
        }
        return nearestP;
    }
    
    
    // !TODO Здесь можно оставить
    public Particle findNearestAgar(){
        ArrayList<Particle> agars = game.get("agar");
        double distToP;
        Particle nearestP = null;
        for(Particle p : agars){
            distToP = GameMath.distance(p.getPosition(), particle.getPosition());
           // && (nearestP == null || nearestP.getSize() < p.getSize())
            if(distToP < MIN_DITANCE ){
                nearestP = p;
            }
        }
        return nearestP;
    }
    
    public void setCollision(int angle) {
            particle.setAngle(angle);
            particle.setSpeed(0.1);
        
    }
    
    public int checkGoOutBorder() {
        double x = particle.getPosition().getX(),
                y = particle.getPosition().getY();
        if(x > game.getSize().getWidth() ){ 
            return (int)(100 + Math.random()*(160));
            //return 180;
        }
        else if(y > game.getSize().getHeight()){
            return (int)(190 + Math.random()*(160));
            //return 270;
        }
        else if(x < 0){
            ArrayList<Integer> n = new ArrayList<Integer>();
            n.add((int)(1 + Math.random()*(80)));
            n.add((int)(280 + Math.random()*(80)));
            int i = (int)(Math.random());
            return n.get(i);
            //return 0;
        }
        else if(y < 0){
            return (int)(10 + Math.random()*(160));
            //return 90;
        }
        return -1;
    }
}
