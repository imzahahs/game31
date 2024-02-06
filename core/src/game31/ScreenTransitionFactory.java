package game31;

import com.badlogic.gdx.graphics.GL20;

import game31.renderer.SaraRenderer;
import game31.renderer.ScreenMaterial;
import sengine.Entity;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.MoveAnim;
import sengine.animation.NullAnim;
import sengine.animation.ScaleAnim;
import sengine.animation.ScissorAnim;
import sengine.animation.SequenceAnim;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.SineGraph;
import sengine.calc.VibrationGraph;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 2/7/2016.
 */
public class ScreenTransitionFactory {
    static final String TAG = "HomesreenTransition";


    public static float tShortAnimTime = 0.35f;
    public static float tMediumAnimTime = 0.7f;
    public static float tVeryShortAnimTime = 0.35f;
    public static float tPowerAnimTime = 0.6f;

    public static ScreenTransition createCallTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        float scaleAmp = 1.2f;
        float colorAmp = 10.0f;
        float moveAmp = 0.15f;
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new ScaleAnim(tMediumAnimTime),               // background will be hidden anyway
                new CompoundAnim(tMediumAnimTime, new Animation[] {
                        new ScaleAnim(1f, new QuadraticGraph(scaleAmp, 1f, true)),
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(colorAmp, 0f, true), new ConstantGraph(1f)), null),
                        new MoveAnim(1f,
                                new SineGraph(1f, 10f, 0f, new QuadraticGraph(moveAmp, 0f, true), null, null),
                                null
                        )
                })
        );
    }

    public static ScreenTransition createAbruptTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        float bounceAmp = 0.3f;
        float vibrateAmp = 0.2f;
        float colorAmp = 10.0f;
        float moveAmp = 0.2f;
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new ScaleAnim(tMediumAnimTime),               // background will be hidden anyway
                new CompoundAnim(tMediumAnimTime, new Animation[] {
                        new ScaleAnim(1f, new SineGraph(1f, 3f, 0f, new LinearGraph(bounceAmp, 0.0f), new LinearGraph(1f + (bounceAmp / 2f), 1f), null)),
                        new ScaleAnim(1f, new VibrationGraph(1f, new LinearGraph(vibrateAmp, 0f), new ConstantGraph(1f))),
                        new ColorAnim(1f, new VibrationGraph(1f, new QuadraticGraph(colorAmp, 0f, true), new ConstantGraph(1f)), null),
                        new MoveAnim(1f,
                                new VibrationGraph(1f, new LinearGraph(moveAmp, 0f), new LinearGraph(-moveAmp / 2f, 0f)),
                                new VibrationGraph(1f, new LinearGraph(moveAmp, 0f), new LinearGraph(-moveAmp / 2f, 0f))
                        )
                })
        );
    }

    public static ScreenTransition createPowerOut(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new ColorAnim(tPowerAnimTime, new LinearGraph(0f, 1f), null),
                new CompoundAnim(tPowerAnimTime,
                        new ScaleAnim(1f,
                                new CompoundGraph(
                                        new LinearGraph(1f, 0.98f, 0.2f),
                                        new QuadraticGraph(0.98f, 0f, 0.8f, 0f, true)
                                ),
                                new CompoundGraph(
                                        new QuadraticGraph(1f, 0.01f, 0.2f, 0, false),
                                        new LinearGraph(0.01f, 0f, 0.8f)
                                )
                        ),
                        new ColorAnim(1f, new QuadraticGraph(1f, 10f, 1f, 0, true), null)
                )
        );
    }

    public static ScreenTransition createPowerIn(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new ColorAnim(tPowerAnimTime, new LinearGraph(1f, 0f), null),
                new CompoundAnim(tPowerAnimTime,
                        new ScaleAnim(1f,
                                new CompoundGraph(
                                        new QuadraticGraph(0f, 0.98f, 0.6f, 0f, true),
                                        new LinearGraph(0.98f, 1f, 0.4f)
                                ),
                                new CompoundGraph(
                                        new LinearGraph(0.0f, 0.01f, 0.2f),
                                        new QuadraticGraph(0.01f, 1f, 0.8f, 0, true)
                                )
                        ),
                        new ColorAnim(1f, new QuadraticGraph(10f, 1f, 1f, 0, false), null)
                )
        );
    }

    public static ScreenTransition createSwipeLeft(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new CompoundAnim(tShortAnimTime,
                        new MoveAnim(1f, new QuadraticGraph(0f, -0.5f, true), null),
                        new ColorAnim(1f, new QuadraticGraph(1f, 0.5f, 1f, 0f, true), ConstantGraph.one)
                ),
                new MoveAnim(0.35f, new QuadraticGraph(1f, 0f, true), null)
        );
    }

    public static ScreenTransition createSwipeRight(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new CompoundAnim(tShortAnimTime,
                        new MoveAnim(1f, new QuadraticGraph(-0.5f, 0f, true), null),
                        new ColorAnim(1f, new QuadraticGraph(0.5f, 1f, 1f, 0f, true), ConstantGraph.one)
                ),
                new MoveAnim(0.35f, new QuadraticGraph(0f, +1f, true), null)
        );
    }

    public static ScreenTransition createScissorRight(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                NullAnim.zero,
                new ScissorAnim(tShortAnimTime,
                        new MoveAnim(1f, new QuadraticGraph(+1f, 0f, true), null)
                )
        );
    }

    public static ScreenTransition createScissorLeft(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                NullAnim.zero,
                new ScissorAnim(tShortAnimTime,
                        new MoveAnim(1f, new QuadraticGraph(-1f, 0f, true), null)
                )
        );
    }

    public static ScreenTransition createFadeTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                NullAnim.zero,
                new FadeAnim(0.25f, new LinearGraph(0f, 1f))
        );
    }

    public static ScreenTransition createBrightTransition(Entity<?> from, Entity<?> to, Entity<?> target) {

        return new ScreenTransition(
                from, to, target,
                new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial),      // not needed
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                NullAnim.zero,
                new CompoundAnim(tShortAnimTime,
                        new ColorAnim(1f, new QuadraticGraph(30f, 1f, true), null),
                        new ScaleAnim(1f, new VibrationGraph(1f, new QuadraticGraph(0.5f, 0f, true), ConstantGraph.one))
                )
        );
    }

    public static ScreenTransition createStartTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new ColorAnim(tShortAnimTime, 0x000000ff, false),
                new SequenceAnim(new Animation[]{
                        new FadeAnim(0.09f, new ConstantGraph(0.3f)),
                        new FadeAnim(0.05f, new ConstantGraph(0)),
                        new FadeAnim(0.05f, new ConstantGraph(0.6f)),
                        new FadeAnim(0.1f, new ConstantGraph(1f)),
                        new FadeAnim(0.05f, new ConstantGraph(0.8f)),
                })
        );
    }

    public static ScreenTransition createUnlockTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new NullAnim(tShortAnimTime),
                new CompoundAnim(tShortAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(1f, 0f, 0f, false)),
                        new ScaleAnim(1f, new QuadraticGraph(1f, 1.5f, 0f, false))
                )
        );
    }


    public static ScreenTransition createLockTransition(Entity<?> from, Entity<?> to, Entity<?> target) {
        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new NullAnim(tShortAnimTime),
                new CompoundAnim(tShortAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(0f, 1f, 0f, true)),
                        new ScaleAnim(1f, new QuadraticGraph(1.5f, 1f, 0f, true))
                )
        );
    }

    public static ScreenTransition createHomescreenOutTransition(Entity<?> from, Entity<?> to, Entity<?> target, float x, float y, float size) {
        return createHomescreenOutTransition(from, to, target, x, y, size, 1f);

    }

    public static ScreenTransition createHomescreenOutTransition(Entity<?> from, Entity<?> to, Entity<?> target, float x, float y, float size, float length) {

        Graph secondScaleGraph = new CompoundGraph(
                new QuadraticGraph(size, size + ((1f - size) * 0.8f), 0.5f, 0, false),
                new QuadraticGraph(size + ((1f - size) * 0.8f), 1f, 0.5f, 0, true)
        );

        x = x - 0.5f;
        y = -y + (Globals.LENGTH / 2f);

        float tAnimTime = 0.4f;

        Animation firstScreenAnimType = new NullAnim(tAnimTime);

        Animation secondScreenAnimType = new CompoundAnim(tAnimTime,
                new FadeAnim(1f,
                        new CompoundGraph(
                                //new ConstantGraph(0f, 0.1f),
                                new QuadraticGraph(0f, 1f, 0.2f, 0f, false),
                                new ConstantGraph(1f, 0.8f)
                        )
                ),
                new MoveAnim(
                        1f,
                        new CompoundGraph(
                                new QuadraticGraph(x, x * 0.6f, 0.4f, 0, false),
                                new QuadraticGraph(x * 0.6f, 0, 0.6f, 0, true)
                        ),
                        new CompoundGraph(
                                new QuadraticGraph(y, y * 0.6f, 0.4f, 0, false),
                                new QuadraticGraph(y * 0.6f, 0, 0.6f, 0, true)
                        )
                ),
                new ScaleAnim(
                        1f,
                        ScaleAnim.Location.CENTER,
                        secondScaleGraph,
                        secondScaleGraph
                ),
                new ScissorAnim(1f,
                        new ScaleAnim(
                                1f,
                                new ConstantGraph(1f),
                                new QuadraticGraph(length / Globals.LENGTH, 1f, true)
                        )
                )
        );



        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                firstScreenAnimType, secondScreenAnimType
        );
    }

    public static ScreenTransition createHomescreenInTransition(Entity<?> from, Entity<?> to, Entity<?> target, float x, float y, float size) {
        return createHomescreenInTransition(from, to, target, x, y, size, 1f);

    }

    public static ScreenTransition createHomescreenInTransition(Entity<?> from, Entity<?> to, Entity<?> target, float x, float y, float size, float length) {

        Graph secondScaleGraph = new QuadraticGraph(1f, size, true);

        x = x - 0.5f;
        y = -y + (Globals.LENGTH / 2f);

        float tAnimTime = 0.3f;

        Animation firstScreenAnimType = new NullAnim(tAnimTime);
        Animation secondScreenAnimType = new CompoundAnim(tAnimTime,
                new FadeAnim(1f,
                        new CompoundGraph(
                                new ConstantGraph(1f, 0.2f),
                                new QuadraticGraph(1f, 0f, 0.7f, 0f, false),
                                new ConstantGraph(0f, 0.1f)
                        )
                ),
                new MoveAnim(
                        1f,
                        new QuadraticGraph(0, x, true),
                        new QuadraticGraph(0, y, true)
                ),
                new ScaleAnim(
                        1f,
                        ScaleAnim.Location.CENTER,
                        secondScaleGraph,
                        secondScaleGraph
                ),
                new ScissorAnim(1f,
                        new ScaleAnim(
                                1f,
                                new ConstantGraph(1f),
                                new QuadraticGraph(1f, length / Globals.LENGTH, true)
                        )
                )
        );

        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                firstScreenAnimType, secondScreenAnimType
        );
    }

    public static ScreenTransition createAltHomescreenOutTransition(Entity<?> from, Entity<?> to, Entity<?> target) {

        float tAnimTime = 0.3f;

        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new CompoundAnim(tAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(1f, 0.25f, 0f, false)),
                        new ScaleAnim(1f, new QuadraticGraph(1f, 0.5f, 0f, true))
                ),
                new CompoundAnim(tAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(0f, 1f, 0f, true)),
                        new ScaleAnim(1f, new QuadraticGraph(2f, 1.0f, 0f, true))
                )
        );
    }

    public static ScreenTransition createAltHomescreenInTransition(Entity<?> from, Entity<?> to, Entity<?> target) {

        float tAnimTime = 0.3f;

        return new ScreenTransition(
                from, to, target,
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_SECOND, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new Sprite(new ScreenMaterial(SaraRenderer.RENDER_FIRST, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)),
                new CompoundAnim(tAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(0.25f, 1f, 0f, true)),
                        new ScaleAnim(1f, new QuadraticGraph(0.5f, 1f, 0f, true))
                ),
                new CompoundAnim(tAnimTime,
                        new FadeAnim(1f, new QuadraticGraph(1f, 0f, 0f, true)),
                        new ScaleAnim(1f, new QuadraticGraph(1.0f, 2f, 0f, true))
                )
        );
    }

}
