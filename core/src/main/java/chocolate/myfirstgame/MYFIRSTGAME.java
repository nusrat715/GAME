package chocolate.myfirstgame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class MYFIRSTGAME extends ApplicationAdapter {
    SpriteBatch batch;
    BitmapFont font;

    Texture playerImg;
    Texture grassImg;
    Texture houseImg;
    Texture herbImg;

    float x = 50, y = 50;
    float speed = 200;

    float houseX = 150, houseY = 150;

    Rectangle playerBounds;
    Rectangle houseBounds;

    // ---- Herb system ----
    float[] herbX = {100, 500, 250};
    float[] herbY = {400, 300, 100};
    boolean[] herbCollected = {false, false, false};
    Rectangle[] herbBounds;

    int coins = 0;
    boolean nearHerb = false;   // true if player close enough to collect

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);   // make text bigger

        playerImg = new Texture("character.png");
        grassImg = new Texture("grass.png");
        houseImg = new Texture("house.png");
        herbImg = new Texture("herb.png");

        playerBounds = new Rectangle(x, y, playerImg.getWidth(), playerImg.getHeight());
        houseBounds = new Rectangle(houseX, houseY, houseImg.getWidth(), houseImg.getHeight());

        herbBounds = new Rectangle[herbX.length];
        for (int i = 0; i < herbX.length; i++) {
            herbBounds[i] = new Rectangle(herbX[i], herbY[i], herbImg.getWidth(), herbImg.getHeight());
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // ---- MOVEMENT ----
        float newX = x, newY = y;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  newX -= speed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) newX += speed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    newY += speed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  newY -= speed * dt;

        newX = Math.max(0, Math.min(newX, Gdx.graphics.getWidth() - playerImg.getWidth()));
        newY = Math.max(0, Math.min(newY, Gdx.graphics.getHeight() - playerImg.getHeight()));

        playerBounds.setPosition(newX, newY);

        if (!playerBounds.overlaps(houseBounds)) {
            x = newX;
            y = newY;
        } else {
            playerBounds.setPosition(x, y);
        }

        // ---- HERB COLLECTION ----
        nearHerb = false;
        for (int i = 0; i < herbBounds.length; i++) {
            if (!herbCollected[i] && playerBounds.overlaps(herbBounds[i])) {
                nearHerb = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    herbCollected[i] = true;
                    coins += 5;   // each herb gives 5 coins
                }
            }
        }

        // ---- DRAW ----
        ScreenUtils.clear(0.2f, 0.6f, 0.3f, 1);

        batch.begin();
        batch.draw(houseImg, houseX, houseY);

        for (int i = 0; i < herbX.length; i++) {
            if (!herbCollected[i]) {
                batch.draw(herbImg, herbX[i], herbY[i]);
            }
        }

        batch.draw(playerImg, x, y);

        font.draw(batch, "Coins: " + coins, 20, Gdx.graphics.getHeight() - 20);

        if (nearHerb) {
            font.draw(batch, "Press E to collect", x, y + playerImg.getHeight() + 30);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        playerImg.dispose();
        grassImg.dispose();
        houseImg.dispose();
        herbImg.dispose();
    }
}
