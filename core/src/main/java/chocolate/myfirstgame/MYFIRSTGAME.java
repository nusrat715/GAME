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
    float[] herbX = {100, 550, 400};
    float[] herbY = {400, 350, 50};
    boolean[] herbCollected = {false, false, false};
    float[] herbRespawnTimer = {0, 0, 0};
    float respawnTime = 5f;
    Rectangle[] herbBounds;

    int coins = 0;
    boolean nearHerb = false;// true if player close enough to collect
    float shopX = 500, shopY = 400;
    Rectangle shopBounds;

    boolean hasWeapon = false;
    boolean shopOpen = false;

    Texture enemyImg;

    float enemyX = 300, enemyY = 50;
    Rectangle enemyBounds;

    int enemyMaxHealth = 30;
    int enemyHealth = enemyMaxHealth;
    boolean enemyAlive = true;
    float enemyRespawnTimer = 0;
    float enemyRespawnTime = 8f;

    float attackCooldown = 0;
    float attackCooldownMax = 0.5f;  // half second between attacks

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
            shopBounds = new Rectangle(shopX, shopY, 60, 60);  // simple 60x60 shop zone
             }

        enemyImg = new Texture("enemy.png");
        enemyBounds = new Rectangle(enemyX, enemyY, enemyImg.getWidth(), enemyImg.getHeight());
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
        for (int i = 0; i < herbBounds.length; i++) {
            if (herbCollected[i]) {
                herbRespawnTimer[i] += dt;
                if (herbRespawnTimer[i] >= respawnTime) {
                    herbCollected[i] = false;
                    herbRespawnTimer[i] = 0;
                }
            } else if (playerBounds.overlaps(herbBounds[i])) {
                nearHerb = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    herbCollected[i] = true;
                    coins += 5;
                }
            }
        }

        // ---- SHOP ----
        boolean nearShop = playerBounds.overlaps(shopBounds);

        if (nearShop && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            shopOpen = !shopOpen;   // toggle shop menu on/off
        }

        if (shopOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                if (coins >= 20 && !hasWeapon) {
                    coins -= 20;
                    hasWeapon = true;
                }
            }
        }

        // ---- ENEMY / FIGHT SYSTEM ----
        attackCooldown -= dt;

        if (!enemyAlive) {
            enemyRespawnTimer += dt;
            if (enemyRespawnTimer >= enemyRespawnTime) {
                enemyAlive = true;
                enemyHealth = enemyMaxHealth;
                enemyRespawnTimer = 0;
            }
        }

        boolean nearEnemy = enemyAlive && playerBounds.overlaps(enemyBounds);

        if (nearEnemy && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && attackCooldown <= 0) {
            enemyHealth -= 10;   // each hit does 10 damage
            attackCooldown = attackCooldownMax;

            if (enemyHealth <= 0) {
                enemyAlive = false;
                coins += 15;   // reward for defeating enemy
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
        if (enemyAlive) {
            batch.draw(enemyImg, enemyX, enemyY);
        }

        batch.draw(playerImg, x, y);
        batch.draw(houseImg, shopX, shopY, 60, 60);  // shop marker (small, temp)

        font.draw(batch, "Coins: " + coins, 20, Gdx.graphics.getHeight() - 20);

        if (enemyAlive) {
            font.draw(batch, "Enemy HP: " + enemyHealth + "/" + enemyMaxHealth, enemyX, enemyY + enemyImg.getHeight() + 20);
        }

        if (nearEnemy) {
            font.draw(batch, "Press SPACE to attack", x, y + playerImg.getHeight() + 50);
        }

        if (nearShop && !shopOpen) {
            font.draw(batch, "Press S to open shop", x, y + playerImg.getHeight() + 30);
        }

        if (shopOpen) {
            font.draw(batch, "SHOP - Press 1 to buy Sword (20 coins)", 50, 400);
            if (hasWeapon) {
                font.draw(batch, "You own a Sword!", 50, 360);
            }
        }

        if (hasWeapon) {
            font.draw(batch, "Weapon: Sword", 20, Gdx.graphics.getHeight() - 60);
        }

        if (nearHerb) {
            font.draw(batch, "Press E to collect", x, y + playerImg.getHeight() + 30);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        enemyImg.dispose();
        batch.dispose();
        font.dispose();
        playerImg.dispose();
        grassImg.dispose();
        houseImg.dispose();
        herbImg.dispose();
    }
}
