import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class MyFirstBot extends Bot {
    private double turnRate = 10;


    // The main method starts our bot
    public static void main(String[] args) {
        new MyFirstBot().start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() {
        super(BotInfo.fromFile("MyFirstBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        turnRate = 10;

        // Repeat while the bot is running
        while (isRunning()) {
            try {
                setGunTurnRate(turnRate);

                System.out.println("Moving forward");
                forward(100);
                
                System.out.println("Moving backward");
                back(100);
            } catch (Exception ex) {
                System.err.println("Error during movement: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        try {
            double bearingFromGun = gunBearingTo(e.getX(),e.getY());
            if (bearingFromGun>=0) {
                turnRate = 10;
            } else {
                turnRate = -10;
            }
            turnGunLeft(bearingFromGun);

            if(Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
                fire(1);
            }

            double distance = Math.sqrt(Math.pow((getX()-e.getX()), 2) + Math.pow((getY()-e.getY()), 2));

            if (distance <= 150) {
                System.out.println("Too close! Moving away.");
                
                System.out.println("Turning left by "+bearingTo(e.getX(), e.getY()));

                turnRight(bearingTo(e.getX(), e.getY()));
                back(-500);
            }
        } catch (Exception ex) {
            System.err.println("Error handling ScannedBot event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        try {
            System.out.println("Hit! Turning to strafe relative to the bullet.");
            double bearing = calcBearing(e.getBullet().getDirection());
            
            turnLeft(90 - bearing);
        } catch (Exception ex) {
            System.err.println("Error handling HitByBullet event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}