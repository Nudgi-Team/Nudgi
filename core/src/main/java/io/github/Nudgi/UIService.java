package io.github.Nudgi;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;


/**
 * An assortment of methods allowing for easy ways of manipulating and creating TextButton
 * and Label objects. The creation methods within this class all use a default style
 * for creation, which has the following properties:
 * <ul>
 * <li>The style uses the default {@link BitmapFont#BitmapFont() BitmapFont}.</li>
 * <li>The style defaults to a color of {@code Color.WHITE}.</li>
 * </ul>
 */
public final class UIService {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE = new TextButtonStyle();
    private static final LabelStyle DEFAULT_LABEL_STYLE = new LabelStyle();

    static {
        DEFAULT_BUTTON_STYLE.font = new BitmapFont();
        DEFAULT_BUTTON_STYLE.fontColor = Color.WHITE;
        DEFAULT_LABEL_STYLE.font = new BitmapFont();
        DEFAULT_LABEL_STYLE.fontColor = Color.WHITE;
    }

    private UIService() {}

    public static Drawable drawableFromTexture(Texture texture) {
        if (texture == null) { return null; }
        Skin skin = new Skin();
        skin.add("1", texture);
        return skin.getDrawable("1");
    }

    public static TextButton createTextButton(@Null String message) {
        return new TextButton(message, new TextButtonStyle(DEFAULT_BUTTON_STYLE));
    }

    /**
     * Any of the parameters may be null.
     * Creates a new TextButton with the specified message and textures.
     * Buttons act similarly to a checkbox, in that clicking on them causes them
     * to flip between the checked and unchecked states.
     * @param message
     * @param up the texture to display when the button is unchecked. 
     * Also displayed by default if either down or checked are not specified
     * and would be displayed.
     * @param down the texture to display only when the button is currently being held down.
     * @param checked the texture to display only when the button is checked.
     * @return the new TextButton.
     * @see com.badlogic.gdx.scenes.scene2d.ui.Button
     */
    public static TextButton createTextButton(@Null String message, @Null Texture up, @Null Texture down, @Null Texture checked) {
        TextButton textButton = createTextButton(message);
        
        modifyTextButtonStyle(textButton, style -> {            
            style.up = drawableFromTexture(up);
            style.down = drawableFromTexture(down);
            style.checked = drawableFromTexture(checked);
        });
        return textButton;
    }

    /**
     * Creates a new Label with the given message.
     * @param message
     * @return the new Label.
     */
    public static Label createLabel(@Null String message) {
        return new Label(message, new LabelStyle(DEFAULT_LABEL_STYLE));
    }

    /**
     * Same as {@link #createLabel(String)},
     * except it also adds the given background texture to it.
     * @param message
     * @param background
     * @return the new Label.
     */
    public static Label createLabel(@Null String message, Texture background) {
        Label label = createLabel(message);
        modifyLabelStyle(label, style -> style.background = drawableFromTexture(background));
        return label;
    }

    public static void modifyLabelStyle(Label onLabel, Consumer<LabelStyle> action) {
        LabelStyle style = onLabel.getStyle();
        action.accept(style);
        onLabel.setStyle(style);
    }

    public static void modifyTextButtonStyle(TextButton button, Consumer<TextButtonStyle> action) {
        TextButtonStyle style = button.getStyle();
        action.accept(style);
        button.setStyle(style);
    }
}
