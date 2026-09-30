package chocolate.myfirstgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen implements Screen  {
    MyGame game;
    SpriteBatch batch;
    BitmapFont font;

    StringBuilder nameInput = new StringBuilder();
    boolean genderChosen = false;

    public MenuScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (genderChosen) {
                    if (Character.isLetter(character) && nameInput.length() < 12) {
                        nameInput.append(character);
                    } else if (character == '\b' && nameInput.length() > 0) {
                        nameInput.deleteCharAt(nameInput.length() - 1);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);

        batch.begin();

        if (!genderChosen) {
            font.draw(batch, "Choose your character:", 200, 350);
            font.draw(batch, "Press 1 for Male", 200, 300);
            font.draw(batch, "Press 2 for Female", 200, 260);

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                MyGame.isMale = true;
                genderChosen = true;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                MyGame.isMale = false;
                genderChosen = true;
            }
        } else {
            font.draw(batch, "Enter your name:", 200, 350);
            font.draw(batch, nameInput.toString() + "_", 200, 300);
            font.draw(batch, "Press ENTER to start", 200, 250);
        }

        batch.end();   // <-- batch.end() AGE kora hocche

        // Screen switch ekhon batch.end()-er POR, tai kono crash hobe na
        if (genderChosen && Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && nameInput.length() > 0) {
            MyGame.playerName = nameInput.toString();
            game.setScreen(new GameScreen(game));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
