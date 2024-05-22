import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class CrazyTracker extends Bot {
    private double turnRate = 10;

    private boolean movingForward;

    float boundaryDistance = 150;

    // The main method starts our bot
    public static void main(String[] args) {
        new CrazyTracker().start();
    }

    // Constructor, which loads the bot config file
    CrazyTracker() {
        super(BotInfo.fromFile("CrazyTracker.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        turnRate = 10;

        // Repeat while the bot is running
        while (isRunning()) {
            try {
                setGunTurnRate(turnRate);
                
                // Tell the game we will want to move ahead 40000 -- some large number
                setForward(40000);
                movingForward = true;
                // Tell the game we will want to turn right 90
                setTurnRight(90);
                // At this point, we have indicated to the game that *when we do something*,
                // we will want to move ahead and turn right. That's what "set" means.
                // It is important to realize we have not done anything yet!
                // In order to actually move, we'll want to call a method that takes real time, such as
                // waitFor.
                // waitFor actually starts the action -- we start moving and turning.
                // It will not return until we have finished turning.
                waitFor(new TurnCompleteCondition(this));
                // Note: We are still moving ahead now, but the turn is complete.
                // Now we'll turn the other way...
                setTurnLeft(180);
                // ... and wait for the turn to finish ...
                waitFor(new TurnCompleteCondition(this));
                // ... then the other way ...
                setTurnRight(180);
                // ... and wait for that turn to finish.
                waitFor(new TurnCompleteCondition(this));
                // then back to the top to do it all again.

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
        } catch (Exception ex) {
            System.err.println("Error handling ScannedBot event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Condition that is triggered when the turning is complete
    public static class TurnCompleteCondition extends Condition {

        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            // turn is complete when the remainder of the turn is zero
            return bot.getTurnRemaining() == 0;
        }
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setForward(40000);
            movingForward = true;
        }
    }

    // We hit another bot -> back up!
    @Override
    public void onHitBot(HitBotEvent e) {
        // If we're moving into the other bot, reverse!
        if (e.isRammed()) {
            reverseDirection();
        }
    }

    @Override
    public void onTick(TickEvent tickEvent) {
        checkWalls();
    }

    // We collided with a wall -> reverse the direction
    @Override
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        reverseDirection();
    }

    public void checkWalls() {
        float leftBound = boundaryDistance;
        float rightBound = getArenaWidth() - boundaryDistance;

        float lowerBound = boundaryDistance;
        float upperBound = getArenaHeight() - boundaryDistance;

        if (getX() < leftBound) {
            System.out.println("Too close to left wall! Moving away.");
            turnLeft(-getDirection()); // Rotates to face East
            movingForward = true;
            setForward(100);
        } else if (getX() > rightBound) {
            System.out.println("Too close to right wall! Moving away.");
            turnLeft(180 - getDirection()); // Rotates to face West
            movingForward = true;
            setForward(100);
        } else if (getY() < lowerBound) {
            System.out.println("Too close to lower wall! Moving away.");
            turnLeft(90 - getDirection()); // Rotates to face North
            movingForward = true;
            setForward(100);
        } else if (getY() > upperBound) { // Rotates to face South
            System.out.println("Too close to upper wall! Moving away.");
            turnLeft(270 - getDirection());
            movingForward = true;
            setForward(100);
        }
    }
}