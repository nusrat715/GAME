package chocolate.myfirstgame;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    MyGame game;
    SpriteBatch batch;
    BitmapFont font;

    Texture playerImg;
    Texture grassImg;
    Texture houseImg;
    Texture herbImg;

    int villageLevel = 1;
    int upgradeCost = 100;

    com.badlogic.gdx.Preferences prefs;

    com.badlogic.gdx.audio.Music bgMusic;
    String[] musicFiles = {"music1.mp3", "music2.mp3"};
    int currentMusicIndex = 0;

    public GameScreen(MyGame game) {
        this.game = game;
    }


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

    float enemyX = 150, enemyY = -50;
    Rectangle enemyBounds;

    int enemyMaxHealth = 30;
    int enemyHealth = enemyMaxHealth;
    boolean enemyAlive = true;
    float enemyRespawnTimer = 0;
    float enemyRespawnTime = 8f;

    float attackCooldown = 0;
    float attackCooldownMax = 0.5f;  // half second between attacks

    int playerMaxHealth = 100;
    int playerHealth = playerMaxHealth;

    float enemyAttackCooldown = 0;
    float enemyAttackCooldownMax = 1.5f;  // enemy attacks every 1.5 sec
    int enemyDamage = 5;

    boolean gameOver = false;

    @Override
    public void show() {
        prefs = Gdx.app.getPreferences("MyGameSave");

        String savedName = prefs.getString("playerName", "");

        if (savedName.equals(MyGame.playerName)) {
            // Same player returning — load their saved progress
            coins = prefs.getInteger("coins", 0);
            hasWeapon = prefs.getBoolean("hasWeapon", false);
            playerHealth = prefs.getInteger("playerHealth", playerMaxHealth);
            villageLevel = prefs.getInteger("villageLevel", 1);
        } else {
            // New player (different name) — start completely fresh
            coins = 0;
            hasWeapon = false;
            playerHealth = playerMaxHealth;
            villageLevel = 1;

            prefs.putString("playerName", MyGame.playerName);
            prefs.flush();
        }

        upgradeCost = 100 * villageLevel;

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

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal(musicFiles[currentMusicIndex]));
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.5f);
        bgMusic.play();
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            ScreenUtils.clear(0, 0, 0, 1);
            batch.begin();
            font.draw(batch, "GAME OVER - Press R to restart", 150, 250);
            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                playerHealth = playerMaxHealth;
                gameOver = false;
                coins = 0;
                x = 50; y = 50;
            }
            return;   // skip rest of render() while game over
        }

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
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                herbCollected[i] = true;
                coins += 5;
                saveGame();   // <-- add this
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
        if (coins >= 20 && !hasWeapon) {
            coins -= 20;
            hasWeapon = true;
            saveGame();   // <-- add this
        }

        // ---- VILLAGE UPGRADE ----
        boolean canUpgrade = coins >= upgradeCost;

        if (canUpgrade && Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            coins -= upgradeCost;
            villageLevel++;
            upgradeCost = 100 * villageLevel;
            saveGame();
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
            if (enemyHealth <= 0) {
                enemyAlive = false;
                coins += 15;
                saveGame();   // <-- add this
            }
        }

        // ---- ENEMY ATTACKS BACK ----
        if (nearEnemy && !gameOver) {
            enemyAttackCooldown -= dt;
            if (enemyAttackCooldown <= 0) {
                playerHealth -= enemyDamage;
                enemyAttackCooldown = enemyAttackCooldownMax;

                if (playerHealth <= 0) {
                    playerHealth = 0;
                    gameOver = true;
                }
            }
        }

        // ---- MUSIC SWITCH ----
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            bgMusic.stop();
            bgMusic.dispose();
            currentMusicIndex = (currentMusicIndex + 1) % musicFiles.length;
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal(musicFiles[currentMusicIndex]));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.5f);
            bgMusic.play();
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
        font.draw(batch, "HP: " + playerHealth + "/" + playerMaxHealth, 20, Gdx.graphics.getHeight() - 40);

        font.draw(batch, "Village Level: " + villageLevel, 20, Gdx.graphics.getHeight() - 80);

        if (canUpgrade) {
            font.draw(batch, "Press U to upgrade village (" + upgradeCost + " coins)", 20, Gdx.graphics.getHeight() - 100);
        }


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

        font.draw(batch, "Press M to change music", 20, Gdx.graphics.getHeight() - 120);

        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    public void saveGame() {
        prefs.putString("playerName", MyGame.playerName);   // <-- add this
        prefs.putInteger("coins", coins);
        prefs.putBoolean("hasWeapon", hasWeapon);
        prefs.putInteger("playerHealth", playerHealth);
        prefs.putInteger("villageLevel", villageLevel);
        prefs.flush();
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
        bgMusic.dispose();
    }
}
