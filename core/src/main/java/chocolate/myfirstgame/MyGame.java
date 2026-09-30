package chocolate.myfirstgame;

import com.badlogic.gdx.Game;

public class MyGame extends Game {
    public static String playerName = "Player";
    public static boolean isMale = true;

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
