package game31.app.flapee;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import game31.renderer.TrailMesh;
import sengine.animation.Animation;
import sengine.animation.CompoundAnim;
import sengine.animation.FadeAnim;
import sengine.animation.ScaleAnim;
import sengine.calc.CompoundGraph;
import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.calc.LinearGraph;
import sengine.calc.QuadraticGraph;
import sengine.calc.Range;
import sengine.calc.SetRandomizedSelector;
import sengine.calc.SetSelector;
import sengine.graphics2d.Material;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;

public class PissEffect {

    private static class SplashInstance {
        static final Pool<SplashInstance> pool = new Pool<SplashInstance>() {
            @Override
            protected SplashInstance newObject() {
                return new SplashInstance();
            }

            @Override
            protected void reset(SplashInstance object) {
                // nothing
            }
        };


        float x;
        float y;
        float size;
        Animation.Instance anim;
        Sprite mat;
    }

    private static class PositionEntry {
        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        float width;
        float time;
        boolean isBelowGround = false;
    }

    private static final Vector2 tl = new Vector2();
    private static final Vector2 tr = new Vector2();
    private static final Vector2 bl = new Vector2();
    private static final Vector2 br = new Vector2();
    private static final Vector2 va = new Vector2();

    private final PositionEntry[] entries;

    private final Array<SplashInstance> splashes = new Array<>(SplashInstance.class);

    // Material type
    public final TrailMesh mesh;
    private final int maxPositions;
    private final float materialLengthMultiplier;
    private final float materialFlowSpeed;
    private final Graph materialWidthGraph;
    private final float materialWidthGraphSpeed;

    private final float minDistance;
    private final float maxDistance;

    private final float gravityY;


    // Splash type
    public final SetSelector<Sprite> splashMats;
    private final Range splashSize;
    private final SetSelector<Animation> splashAnims;
    private final float splashMinDistance;
    private final float splashMinTime;
    private final float splashY;


    // Current
    private int positionsUsed = 0;
    private float materialFlow = 0;
    private float tSplashLastTime = -Float.MAX_VALUE;
    private final Vector2 splashLastPosition = new Vector2();

    public void clear() {
        positionsUsed = 0;
        // Clear mesh
        for(int c = 0; c < mesh.vertices.length; c++) {
            mesh.vertices[c] = 0;
        }

        SplashInstance.pool.freeAll(splashes);
        splashes.clear();

        tSplashLastTime = -Float.MAX_VALUE;
        splashLastPosition.set(0, 0);
    }

    public void render(float cameraX) {
        Matrix4 m = Matrices.model;
        for(int c = 0; c < splashes.size; c++) {
            SplashInstance i = splashes.items[c];

            Matrices.push();
            m.translate(i.x, i.y, 0);
            m.scale(i.size, i.size, i.size);

            i.anim.apply(i.mat);
            i.mat.render();

            Matrices.pop();
        }

        // TODO: particles
    }

    public boolean updatePosition(float renderTime, float deltaTime, float x, float y, float shootAngle, float shootVelocity, float shootVolume, float groundY, boolean isDead) {

        // Update splashes
        for(int c = 0; c < splashes.size; c++) {
            SplashInstance s = splashes.items[c];
            if(!s.anim.update(deltaTime)) {
                if(splashes.size == 1) {
                    SplashInstance.pool.freeAll(splashes);
                    splashes.clear();
                    break;
                }
                SplashInstance.pool.free(splashes.items[c]);
                splashes.items[c] = splashes.items[splashes.size - 1];
                splashes.size--;
                c--;
            }
        }

        // Calculate velocity
        va.set(1, 0).rotate(shootAngle).scl(shootVelocity);
        float vx = va.x;
        float vy = va.y;

        if(positionsUsed == 0) {
            for(int c = 0; c < entries.length; c++) {
                // Reset all
                PositionEntry e = entries[c];
                e.position.set(x, y);
                e.velocity.set(vx, vy);
                e.width = shootVolume;
                e.time = renderTime;
                e.isBelowGround = false;
            }
            positionsUsed = 2;
            return false;
        }
        // Check if last position is close enough
        entries[0].position.set(x, y);
        entries[0].velocity.set(vx, vy);
        entries[0].width = shootVolume;
        float distance = entries[0].position.dst(entries[1].position);
        if(distance > minDistance) {
            // Shift up
            PositionEntry last = entries[entries.length - 1];
            System.arraycopy(entries, 0, entries, 1, entries.length - 1);
            entries[0] = last;
            last.position.set(x, y);
            last.velocity.set(x, y);
            last.width = shootVolume;
            last.time = renderTime;
            last.isBelowGround = false;
            positionsUsed++;
            if(positionsUsed > entries.length)
                positionsUsed = entries.length;
        }

        // Enforce max distance, create intermediary positions if too far
        for(int c = 1; c < positionsUsed; c++) {
            PositionEntry current = entries[c];
            PositionEntry prev = entries[c - 1];
            distance = current.position.dst(prev.position);
            if(distance > maxDistance) {
                // Create intermediary position entry
                PositionEntry last = entries[entries.length - 1];
                System.arraycopy(entries, c, entries, c + 1, entries.length - c - 1);
                entries[c] = last;
                last.position.set(prev.position).lerp(current.position, 0.5f);
                last.velocity.set(prev.velocity).lerp(current.velocity, 0.5f);
                last.velocity.y = Math.max(prev.velocity.y, current.velocity.y);
                last.width = (prev.width + current.width) / 2f;
                last.time = (prev.time + current.time) / 2f;
                last.isBelowGround = prev.isBelowGround || current.isBelowGround;
                positionsUsed++;
                if(positionsUsed > entries.length)
                    positionsUsed = entries.length;
            }
        }

        // Update velocities
        for(int c = 1; c < positionsUsed; c++) {
            PositionEntry e = entries[c];
            e.velocity.y += -gravityY * deltaTime;
            e.position.x += e.velocity.x * deltaTime;
            e.position.y += e.velocity.y * deltaTime;
            e.time += deltaTime * materialWidthGraphSpeed;
        }


        float colorPacked = Color.toFloatBits(255, 255, 255, 255);

        // Update mesh
        materialFlow += deltaTime * materialFlowSpeed;
        float materialTopY = 0;
        float materialBottomY = materialFlow;

        for(int c = 0, fo = 0; c < positionsUsed - 1; c++, fo += 6 * 6) {   // face offset
            Vector2 vec1 = entries[c].position;
            Vector2 vec2 = entries[c + 1].position;
            float halfWidth = entries[c + 1].width / 2f;
            halfWidth *= materialWidthGraph.generate(entries[c + 1].time);

            float dst = vec1.dst(vec2);

            materialTopY = materialBottomY;
            materialBottomY += dst * materialLengthMultiplier;



            // Calculate angle
//            float dx = vec2.x - vec1.x;
//            float dy = vec2.y - vec1.y;
//            float rads = (float) Math.atan2(dx, dy);
//            if(rads < 0)
//                rads = -rads;
//            else
//                rads = (float) (2 * Math.PI * rads);
//            float angle = (float) (Math.toDegrees(rads) - 90f);
////            float angle = 360f - va.set(vec2).sub(vec1).nor().angle() + 90f;
//            if(c == 0) {
//                bl.set(-halfWidth, 0).rotate(angle);
//                br.set(+halfWidth, 0).rotate(angle);
//                tl.set(bl);
//                tr.set(br);
//            }
//            else {
//                tl.set(bl);
//                tr.set(br);
//                bl.set(-halfWidth, 0).rotate(angle);
//                br.set(+halfWidth, 0).rotate(angle);
//            }

            va.set(vec2).sub(vec1).nor();                 // TODO: this is the most correct way, but the above creates nice ripples
            if(c == 0) {
                bl.set(va).rotate(90).scl(halfWidth);
                br.set(va).rotate(-90).scl(halfWidth);
                tl.set(bl);
                tr.set(br);
            }
            else {
                tl.set(bl);
                tr.set(br);
                bl.set(va).rotate(90).scl(halfWidth);
                br.set(va).rotate(-90).scl(halfWidth);
            }

            // Create triangle
            mesh.vertices[fo + (0 * 6) + 0] = vec2.x + bl.x;      // bl-x
            mesh.vertices[fo + (0 * 6) + 1] = vec2.y + bl.y;      // bl-y
            mesh.vertices[fo + (0 * 6) + 3] = 0.0f;               // bl-u
            mesh.vertices[fo + (0 * 6) + 4] = materialBottomY;    // bl-v
            mesh.vertices[fo + (0 * 6) + 5] = colorPacked;        // bl-color

            mesh.vertices[fo + (4 * 6) + 0] = mesh.vertices[fo + (1 * 6) + 0] = vec2.x + br.x;      // br-x
            mesh.vertices[fo + (4 * 6) + 1] = mesh.vertices[fo + (1 * 6) + 1] = vec2.y + br.y;      // br-y
            mesh.vertices[fo + (4 * 6) + 3] = mesh.vertices[fo + (1 * 6) + 3] = 1.0f;               // br-u
            mesh.vertices[fo + (4 * 6) + 4] = mesh.vertices[fo + (1 * 6) + 4] = materialBottomY;    // br-v
            mesh.vertices[fo + (4 * 6) + 5] = mesh.vertices[fo + (1 * 6) + 5] = colorPacked;        // br-color

            mesh.vertices[fo + (3 * 6) + 0] = mesh.vertices[fo + (2 * 6) + 0] = vec1.x + tl.x;      // tl-x
            mesh.vertices[fo + (3 * 6) + 1] = mesh.vertices[fo + (2 * 6) + 1] = vec1.y + tl.y;      // tl-y
            mesh.vertices[fo + (3 * 6) + 3] = mesh.vertices[fo + (2 * 6) + 3] = 0.0f;               // tl-u
            mesh.vertices[fo + (3 * 6) + 4] = mesh.vertices[fo + (2 * 6) + 4] = materialTopY;       // tl-v
            mesh.vertices[fo + (3 * 6) + 5] = mesh.vertices[fo + (2 * 6) + 5] = colorPacked;        // tl-color

            mesh.vertices[fo + (5 * 6) + 0] = vec1.x + tr.x;      // tr-x
            mesh.vertices[fo + (5 * 6) + 1] = vec1.y + tr.y;      // tr-y
            mesh.vertices[fo + (5 * 6) + 3] = 1.0f;               // tr-u
            mesh.vertices[fo + (5 * 6) + 4] = materialTopY;       // tr-v
            mesh.vertices[fo + (5 * 6) + 5] = colorPacked;        // tr-color
        }

        // Check ground position
        boolean isBelowGround = false;
        boolean isSplashTimeAllowed = (renderTime - tSplashLastTime) > splashMinTime;
        for(int c = 0; c < positionsUsed; c++) {   // face offset
            PositionEntry e = entries[c];
            if(!e.isBelowGround && e.width > 0 && e.position.y < groundY) {
                e.isBelowGround = true;
                isBelowGround = true;

                float splashDistance = e.position.dst(splashLastPosition);
                if((splashDistance > splashMinDistance || isSplashTimeAllowed) && !isDead) {
                    // Create splash particle
                    SplashInstance splash = SplashInstance.pool.obtain();
                    splash.mat = splashMats.select();
                    splash.size = splashSize.generate();
                    splash.x = e.position.x;
                    splash.y = groundY + (splash.mat.length * splash.size / 2f) + splashY;
                    splash.anim = splashAnims.select().startAndReset();

                    splashes.add(splash);

                    // Remember
                    tSplashLastTime = renderTime;
                    splashLastPosition.set(e.position);
                }
            }
        }

        mesh.upload();

        return isBelowGround;
    }

    public PissEffect(Material material, SetRandomizedSelector<Sprite> splashMats, Range splashSize) {

//        Material material = Material.load("apps/flapee/game/piss.png");

        this.maxPositions = 50;
        entries = new PositionEntry[maxPositions];
        for(int c = 0; c < maxPositions; c++) {
            entries[c] = new PositionEntry();
        }

        minDistance = 0.005f;
        maxDistance = 0.115f;

        gravityY = 5.5f;

        int numVertices = (maxPositions - 1) * 6;

        mesh = new TrailMesh(numVertices, numVertices, material);

        this.materialLengthMultiplier = 15f;
        this.materialFlowSpeed = 5f;
        this.materialWidthGraph = new CompoundGraph(
                new ConstantGraph(1f, 0.4f),
                new LinearGraph(1f, -1f, 0.1f),
                new ConstantGraph(-1f, 0.4f),
                new LinearGraph(-1f, 1f, 0.1f)
        );

        this.materialWidthGraphSpeed = 0.4f;

        this.splashMats = splashMats;
//        splashMats = new SetRandomizedSelector<>(
//                Sprite.load("apps/flapee/game/splash1.png"),
//                Sprite.load("apps/flapee/game/splash2.png"),
//                Sprite.load("apps/flapee/game/splash3.png")
//        );
//        splashSize = new Range(0.04f, 0.03f);
        this.splashSize = splashSize;
        splashAnims = new SetRandomizedSelector<Animation>(
                new CompoundAnim(0.2f,
                        new ScaleAnim(1f, ScaleAnim.Location.BOTTOM, new QuadraticGraph(0.5f, 1f, true)),
                        new FadeAnim(1f, new CompoundGraph(
                                new ConstantGraph(1f, 0.6f),
                                new LinearGraph(1f, 0f, 0.4f)
                        ))
                )
        );

        splashMinDistance = 0.01f;
        splashMinTime = 0.05f;

        splashY = -0.015f;
    }



}
