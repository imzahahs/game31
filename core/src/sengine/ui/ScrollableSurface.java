package sengine.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;

import sengine.Sys;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.calc.Graph;
import sengine.calc.QuadraticGraph;
import sengine.graphics2d.Matrices;
import sengine.mass.Mass;

public class ScrollableSurface extends UIElement<Universe> {

    public static float desktopZoomPower = -0.15f;
    public static float desktopScrollPower = 1.0f;

    // Identity
    boolean horizontalScrollable = true;
    boolean verticalScrollable = true;

    boolean scissorViewableRegion = false;
    boolean selectiveRendering = false;

    float paddingLeft = 0f;
    float paddingRight = 0f;
    float paddingTop = 0f;
    float paddingBottom = 0f;

    float minLeft = 0;
    float minRight = 0;
    float minTop = 0;
    float minBottom = 0;

    float gravityThresholdX = 0;
    float gravityThresholdY = 0;
    float gravityIntervalX = 0;
    float gravityIntervalY = 0;
    Graph gravitySeekGraph = new QuadraticGraph(0, 1, 0.5f, 0f, true);
    float gravityMaxSpeed = 3f;
    float gravitySpeedVelocityMultiplier = 0.25f;

    float tMaxSpeedAssessmentTime = 0.08f;
    float tMinSpeedAssessmentTime = 0.04f;
    float minVelocity = 0.05f;
    float deceleration = 5.0f;

    float tTouchSmoothTime = 0.100f;        // 100ms, its a little choppy when its 60ms

    // Input
    float minTouchMoveDistance = 0;
    float minPinchSize = 0.05f;

    // Zoom
    float minZoom = 1f;
    float maxZoom = -1;

    // Current state
    int touchedPointer = -1;
    int touchedZoomPointer = -1;
    float touchedX;
    float touchedY;
    float touchedZoomX;
    float touchedZoomY;
    float touchStartedX;
    float touchStartedY;
    boolean isMoving = false;
    boolean isZooming = false;
    float touchZoomStartedDistance;
    float touchZoomPrev;
    float speedX;
    float speedY;
    float velocity;
    final FloatArray touchDeltas = new FloatArray();            // x, y, tRenderTime
    float reductionX;
    float reductionY;
    float movedX = 0f;
    float movedY = 0f;
    float moveXqueued = Float.MAX_VALUE;
    float moveYqueued = Float.MAX_VALUE;
    float tTouchStarted = -1;
    float prevZoom = 1f;
    float prevZoomX = 0;
    float prevZoomY = 0;
    float zoom = 1f;
    float zoomX = 0;
    float zoomY = 0;

    private final FloatArray smoothDeltas = new FloatArray();
    private final FloatArray smoothZoomDeltas = new FloatArray();

    float gravityTargetStartX;
    float gravityTargetEndX;
    float gravityTargetStartY;
    float gravityTargetEndY;
    float tGravityTargetStarted = -1;
    float gravityTargetSpeed;
    float seekGravityDirectionX = 0;
    float seekGravityDirectionY = 0;

    float windowLeft = 0f;
    float windowRight = 0f;
    float windowTop = 0f;
    float windowBottom = 0f;
    private float spaceLeft = 0;
    private float spaceRight = 0;
    private float spaceTop = 0;
    private float spaceBottom = 0;

    boolean passThroughInput = false;


    boolean scissorPushed = false;
    boolean refreshQueued = false;

    public void touchSmoothTime(float smoothTime) {
        this.tTouchSmoothTime = smoothTime;
    }

    public ScrollableSurface passThroughInput(boolean passThroughInput) {
        this.passThroughInput = passThroughInput;
        return this;
    }

    public ScrollableSurface minTouchMoveDistance(float minTouchMoveDistance) {
        this.minTouchMoveDistance = minTouchMoveDistance;
        return this;
    }

    public ScrollableSurface minPinchSize(float minPinchSize) {
        this.minPinchSize = minPinchSize;
        return this;
    }

    public ScrollableSurface maxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
        if(maxZoom <= 1f) {
            this.maxZoom = -1;
            zoom = prevZoom = 1f;
            zoomX = prevZoomX = 0f;
            zoomY = prevZoomY = 0f;
        }
        return this;
    }

    public ScrollableSurface minZoom(float minZoom) {
        this.minZoom = minZoom;
        if(minZoom <= 1f) {
            this.minZoom = 1;
            zoom = prevZoom = 1f;
            zoomX = prevZoomX = 0f;
            zoomY = prevZoomY = 0f;
        }
        return this;
    }

    public float maxZoom() {
        return maxZoom;
    }
    public float minZoom() {
        return minZoom;
    }

    public ScrollableSurface scrollAssessment(float minTime, float maxTime, float minVelocity) {
        this.tMinSpeedAssessmentTime = minTime;
        this.tMaxSpeedAssessmentTime = maxTime;
        this.minVelocity = minVelocity;
        return this;
    }

    public ScrollableSurface scrollDeceleration(float deceleration) {
        this.deceleration = deceleration;
        return this;
    }

    public ScrollableSurface scrollGravity(float thresholdX, float thresholdY, float intervalX, float intervalY) {
        this.gravityThresholdX = thresholdX;
        this.gravityThresholdY = thresholdY;
        this.gravityIntervalX = intervalX;
        this.gravityIntervalY = intervalY;
        return this;
    }

    public ScrollableSurface scrollGravitySpeed(float maxSpeed, float velocityMultiplier) {
        this.gravityMaxSpeed = maxSpeed;
        this.gravitySpeedVelocityMultiplier = velocityMultiplier;
        return this;
    }

    public ScrollableSurface scrollGravityGraph(Graph graph) {
        this.gravitySeekGraph = graph;
        return this;
    }

    public ScrollableSurface scrollable(boolean horizontal, boolean vertical) {
        horizontalScrollable = horizontal;
        verticalScrollable = vertical;
        refreshQueued = true;
        return this;
    }

    public ScrollableSurface selectiveRendering(boolean selectiveRendering, boolean scissorViewableRegion) {
        this.selectiveRendering = selectiveRendering;
        this.scissorViewableRegion = scissorViewableRegion;
        refreshQueued = true;
        return this;
    }

    public float paddingLeft() {
        return paddingLeft;
    }

    public float paddingTop() {
        return paddingTop;
    }

    public float paddingRight() {
        return paddingRight;
    }

    public float paddingBottom() {
        return paddingBottom;
    }

    public ScrollableSurface padding(float left, float top, float right, float bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        refreshQueued = true;
        return this;
    }

    public ScrollableSurface minimumPadding(float left, float top, float right, float bottom) {
        this.minLeft = left;
        this.minTop = top;
        this.minRight = right;
        this.minBottom = bottom;
        refreshQueued = true;
        return this;
    }

    public ScrollableSurface length(float length) {
        this.length = length;
        return this;
    }



    public ScrollableSurface() {
        // default
    }

    @MassConstructor
    public ScrollableSurface(Metrics metrics, String name, float length, UIElement<?>[] childs,
                             float tMinSpeedAssessmentTime, float tMaxSpeedAssessmentTime, float minVelocity,
                             float deceleration,
                             float gravityThresholdX, float gravityThresholdY, float gravityIntervalX, float gravityIntervalY,
                             float gravityMaxSpeed, float gravitySpeedVelocityMultiplier,
                             Graph gravitySeekGraph,
                             boolean horizontalScrollable, boolean verticalScrollable,
                             boolean selectiveRendering, boolean scissorViewableRegion,
                             float paddingLeft, float paddingTop, float paddingRight, float paddingBottom,
                             float minLeft, float minTop, float minRight, float minBottom,
                             float maxZoom, float minZoom,
                             float minTouchMoveDistance, float minPinchSize, float touchSmoothTime,
                             boolean passThroughInput

    ) {
        super(metrics, name, length, childs);

        scrollAssessment(tMinSpeedAssessmentTime, tMaxSpeedAssessmentTime, minVelocity);
        scrollDeceleration(deceleration);
        scrollGravity(gravityThresholdX, gravityThresholdY, gravityIntervalX, gravityIntervalY);
        scrollGravitySpeed(gravityMaxSpeed, gravitySpeedVelocityMultiplier);
        scrollGravityGraph(gravitySeekGraph);
        scrollable(horizontalScrollable, verticalScrollable);
        selectiveRendering(selectiveRendering, scissorViewableRegion);
        padding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        minimumPadding(minLeft, minTop, minRight, minBottom);
        maxZoom(maxZoom);
        minZoom(minZoom);
        minTouchMoveDistance(minTouchMoveDistance);
        minPinchSize(minPinchSize);
        touchSmoothTime(touchSmoothTime);
        passThroughInput(passThroughInput);
    }

    @Override
    public Object[] mass() {
        return Mass.concat(super.mass(),
                // Scroll
                tMinSpeedAssessmentTime, tMaxSpeedAssessmentTime, minVelocity,
                deceleration,
                gravityThresholdX, gravityThresholdY, gravityIntervalX, gravityIntervalY,
                gravityMaxSpeed, gravitySpeedVelocityMultiplier,
                gravitySeekGraph,
                horizontalScrollable, verticalScrollable,
                // Rendering
                selectiveRendering, scissorViewableRegion,
                // Metrics
                paddingLeft, paddingTop, paddingRight, paddingBottom,
                minLeft, minTop, minRight, minBottom,
                // Zoom
                maxZoom, minZoom,
                minTouchMoveDistance, minPinchSize, tTouchSmoothTime,
                passThroughInput
        );
    }

    @Override
    public ScrollableSurface instantiate() {
        ScrollableSurface surface = new ScrollableSurface();
        surface.name(name);
        surface.viewport(viewport);
        surface.metrics(metrics.instantiate());
        surface.scrollAssessment(tMinSpeedAssessmentTime, tMaxSpeedAssessmentTime, minVelocity);
        surface.scrollDeceleration(deceleration);
        surface.scrollGravity(gravityThresholdX, gravityThresholdY, gravityIntervalX, gravityIntervalY);
        surface.scrollGravitySpeed(gravityMaxSpeed, gravitySpeedVelocityMultiplier);
        surface.scrollGravityGraph(gravitySeekGraph);
        surface.scrollable(horizontalScrollable, verticalScrollable);
        surface.selectiveRendering(selectiveRendering, scissorViewableRegion);
        surface.padding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        surface.minimumPadding(minLeft, minTop, minRight, minBottom);
        surface.maxZoom(maxZoom);
        surface.minZoom(minZoom);
        surface.minTouchMoveDistance(minTouchMoveDistance);
        surface.minPinchSize(minPinchSize);
        surface.touchSmoothTime(tTouchSmoothTime);
        surface.passThroughInput(passThroughInput);
        surface.length(length);
        surface.instantiateChilds(this);
        return surface;
    }

    @Override
    public ScrollableSurface viewport(UIElement<?> viewport) {
        super.viewport(viewport);
        return this;
    }

    @Override
    public ScrollableSurface name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public ScrollableSurface windowAnimation(Animation.Handler windowAnim, boolean animateWindowAnim, boolean animateKeepAnim) {
        super.windowAnimation(windowAnim, animateWindowAnim, animateKeepAnim);
        return this;
    }

    @Override
    public ScrollableSurface metrics(Metrics metrics) {
        super.metrics(metrics);
        return this;
    }

    @Override
    public ScrollableSurface attach() {
        super.attach();
        return this;
    }

    @Override
    public ScrollableSurface attach(int index) {
        super.attach(index);
        return this;
    }

    public void cancelTouch(Universe v) {
        // cx = (inputX + camera.position.x + childInputX) * childInputScaleX;
        // inputX = (cx / childInputScaleX) - childInputX - camera.position.x;
        if(touchedZoomPointer != -1) {
            float x = (touchedZoomX / childInputScaleX) - childInputX - camera.position.x;
            float y = (touchedZoomY / childInputScaleY) - childInputY - camera.position.y;
            input(v, INPUT_TOUCH_UP, 0, (char)0, 0, touchedZoomPointer, x, y, 0);
        }
        if(touchedPointer != -1) {
            float x = (touchedX / childInputScaleX) - childInputX - camera.position.x;
            float y = (touchedY / childInputScaleY) - childInputY - camera.position.y;
            input(v, INPUT_TOUCH_UP, 0, (char)0, 0, touchedPointer, x, y, 0);
        }
    }

    public boolean isTouching() {
        return touchedPointer != -1;
    }

    public boolean isSmoothMoving() {
        return tGravityTargetStarted != -1;
    }

    public float zoom() {
        return zoom * prevZoom;
    }

    public void centerZoom() {
        zoom(1f, 0, 0);
        movedX += (prevZoomX / prevZoom);
        movedY += (prevZoomY / prevZoom);
        prevZoomX = prevZoomY = 0;
    }

    public void zoom(float deltaZoom, float x, float y) {
        // Zooming
        // Time to commit current zoom
        prevZoom *= zoom;
        Camera camera = this.camera;            // Keep camera set during render pass
        super.calculateWindow();
        this.camera = camera;                   // Restore camera set during render pass
        prevZoomX += ((1f - zoom) * (zoomX - (childX + prevZoomX)));
        prevZoomY += ((1f - zoom) * (zoomY - (childY + prevZoomY)));

        // Reset current zoom
        zoomX = x;
        zoomY = y;
        zoom = deltaZoom;


        float totalZoom = prevZoom * zoom;
        if (totalZoom < minZoom)
            zoom = minZoom / prevZoom;
        if(totalZoom > maxZoom)
            zoom = maxZoom / prevZoom;
        totalZoom = prevZoom * zoom;
        if(totalZoom < 1f) {
            // Cannot minify
            prevZoom = zoom = 1f;
            prevZoomX = zoomX = 0;
            prevZoomY = zoomY = 0;
        }
        refreshQueued = true;
    }

    public void zoom(float zoom) {
        if(maxZoom == -1)
            return;
        if(zoom < 1f)
            zoom = 1f;
        if(zoom < minZoom)
            zoom = minZoom;
        if(zoom > maxZoom)
            zoom = maxZoom;
        // Set zoom
        prevZoom = zoom;
        this.zoom = 1f;
        refreshQueued = true;

    }

    // TODO: this only works with direct childs of the surface
    public void moveTo(UIElement<?> view) {
        stop();

        float surfaceY = movedY;
        float viewY = (view.metrics.anchorWindowY * getLength()) + view.metrics.anchorY;

        float surfaceX = movedX;
        float viewX = view.metrics.anchorWindowX;

        move(-(surfaceX + viewX), -(surfaceY + viewY));
    }

    public void move(float deltaX, float deltaY) {
        if(moveXqueued == Float.MAX_VALUE || moveYqueued == Float.MAX_VALUE) {
            moveXqueued = deltaX;
            moveYqueued = deltaY;
        }
        else {
            moveXqueued += deltaX;
            moveYqueued += deltaY;
        }
    }

    public void smoothMove(float deltaX, float deltaY, float time) {
        if(time <= 0f) {
            move(deltaX, deltaY);
            return;
        }
        smoothDeltas.add(deltaX);
        smoothDeltas.add(deltaY);
        smoothDeltas.add(time);
        smoothDeltas.add(time);
        stopGravityTarget();
    }

    public float movedX() {
        return movedX + (prevZoomX / prevZoom);
    }

    public float movedY() {
        return movedY + (prevZoomY / prevZoom);
    }

    public void seekNearestGravityTarget() {
        float offsetX = movedX;
        float offsetY = movedY;
        if(gravityIntervalX > 0)
            offsetX = (Math.round(movedX / gravityIntervalX) * gravityIntervalX) - offsetX;
        else
            offsetX = 0;
        if(gravityIntervalY > 0)
            offsetY = (Math.round(movedY / gravityIntervalY) * gravityIntervalY) - offsetY;
        else
            offsetY = 0;
        seekNearestGravityTarget(offsetX, offsetY);
    }

    public void seekNearestGravityTarget(float directionX, float directionY) {
        seekGravityDirectionX = Math.signum(directionX);
        seekGravityDirectionY = Math.signum(directionY);
    }

    public void seekGravityTarget(float directionX, float directionY) {
        velocity = 0f;
        seekGravityDirectionX = Math.signum(directionX) * (gravityIntervalX / gravityThresholdX);
        seekGravityDirectionY = Math.signum(directionY) * (gravityIntervalY / gravityThresholdY);
    }

    private void stopGravityTarget() {
        gravityTargetStartX = gravityTargetEndX = gravityTargetStartY = gravityTargetEndY = tGravityTargetStarted = -1;
        seekGravityDirectionX = seekGravityDirectionY = 0;
    }

    public void stopCurrent() {
        speedX = speedY = reductionX = reductionY = moveXqueued = moveYqueued = 0f;
        touchDeltas.clear();
    }

    public void stop() {
        stopCurrent();
        smoothDeltas.clear();
        smoothZoomDeltas.clear();
    }

    public void queueRefresh() {
        refreshQueued = true;
    }

    public float spaceRight() {
        return spaceRight;
    }

    public float spaceLeft() {
        return spaceLeft;
    }

    public float spaceTop() {
        return spaceTop;
    }

    public float spaceBottom() {
        return spaceBottom;
    }

    public void refresh() {

        super.calculateWindow();

        float viewX = childX;
        float viewY = childY;
        float viewLeft = viewX - Math.abs(childScaleX / 2f);
        float viewRight = viewX + Math.abs(childScaleX / 2f);
        float viewTop = viewY + Math.abs(childLength / 2f);
        float viewBottom = viewY - Math.abs(childLength / 2f);


        Rectangle bounds = bounds(true, true, false, true, null);
        windowLeft = bounds.x - (paddingLeft * childScaleX);
        windowRight = bounds.x + bounds.width + (paddingRight * childScaleX);
        windowBottom = bounds.y - (paddingBottom * childScaleY);
        windowTop = bounds.y + bounds.height + (paddingTop * childScaleY);

        if(bounds.area() == 0)
            return;


        // Compensate for minimum bounds
        float allowedLeft = childX - Math.abs(childScaleX / 2f) - (minLeft * childScaleX);
        float allowedRight = childX + Math.abs(childScaleX / 2f) + (minRight * childScaleX);
        float allowedTop = childY + Math.abs(childLength / 2f) + minTop;
        float allowedBottom = childY - Math.abs(childLength / 2f) - minBottom;

        if(windowLeft > allowedLeft)
            windowLeft = allowedLeft;
        if(windowRight < allowedRight)
            windowRight = allowedRight;
        if(windowTop < allowedTop)
            windowTop = allowedTop;
        if(windowBottom > allowedBottom)
            windowBottom = allowedBottom;


        // Enforce view within window bounds
        boolean positionUpdated = false;
        float absSpeedX = Math.abs(speedX);
        float absSpeedY = Math.abs(speedY);
        if(viewLeft < windowLeft) {
            movedX -= (windowLeft - viewLeft) / childScaleX;
            if(horizontalScrollable && absSpeedX > absSpeedY)
                stopCurrent();
            positionUpdated = true;
            spaceLeft = 0;
        }
        else
            spaceLeft = viewLeft - windowLeft;
        if(viewRight > windowRight) {
            movedX += (viewRight - windowRight) / childScaleX;
            if(horizontalScrollable && absSpeedX > absSpeedY)
                stopCurrent();
            positionUpdated = true;
            spaceRight = 0;
        }
        else
            spaceRight = windowRight - viewRight;
        if(viewTop > windowTop) {
            movedY += (viewTop - windowTop) / childScaleY;
            if(verticalScrollable && absSpeedY > absSpeedX)
                stopCurrent();
            positionUpdated = true;
            spaceTop = 0;
        }
        else
            spaceTop = windowTop - viewTop;
        if(viewBottom < windowBottom) {
            movedY -= (windowBottom - viewBottom) / childScaleY;
            if(verticalScrollable && absSpeedY > absSpeedX)
                stopCurrent();
            positionUpdated = true;
            spaceBottom = 0;
        }
        else
            spaceBottom = viewBottom - windowBottom;

        if(selectiveRendering) {
            // Recalculate view bounds
            if(positionUpdated)
                bounds(true, true, false, true, null);

            UIElement<?> window;
            int idx = 0;
            while((window = getChild(UIElement.class, idx)) != null) {
                // Calculate window bounds
                float deltaX = Math.abs(window.childX - viewX);
                float deltaY = Math.abs(window.childY - viewY);
                float w = Math.abs(window.childScaleX / 2f) + Math.abs((viewRight - viewLeft) / 2f);
                float h = Math.abs(window.childLength / 2f) + Math.abs((viewTop - viewBottom) / 2f);
                if(deltaX < w && deltaY < h)
                    window.renderingEnabled = true;
                else
                    window.renderingEnabled = false;
                idx++;
            }
        }

        refreshQueued = false;
    }

    @Override
    protected void recreate(Universe v) {
        refresh();
    }

    @Override
    protected void release(Universe v) {
        super.release(v);

        // Finish gravity target
        if(tGravityTargetStarted != -1) {
            move((gravityTargetEndX - movedX) * childScaleX, (gravityTargetEndY - movedY) * childScaleY);
            stop();
            stopGravityTarget();
        }
    }

    @Override
    public void calculateWindow() {
        super.calculateWindow();

        if(maxZoom > 1f) {
            float left = childX - Math.abs(childScaleX / 2f);
            float right = childX + Math.abs(childScaleX / 2f);
            float top = childY + Math.abs(childLength / 2f);
            float bottom = childY - Math.abs(childLength / 2f);

            // Prev zoom
            childScaleX *= prevZoom;
            childScaleY *= prevZoom;
            childLength *= prevZoom;

            childX += prevZoomX;
            childY += prevZoomY;

            // Current zoom
            childScaleX *= zoom;
            childScaleY *= zoom;
            childLength *= zoom;

            float newZoomX = (1f - zoom) * (zoomX - childX);
            float newZoomY = (1f - zoom) * (zoomY - childY);
            childX += newZoomX;
            childY += newZoomY;

            float zoomedLeft = childX - Math.abs(childScaleX / 2f);
            float zoomedRight = childX + Math.abs(childScaleX / 2f);
            float zoomedTop = childY + Math.abs(childLength / 2f);
            float zoomedBottom = childY - Math.abs(childLength / 2f);

            if (zoomedLeft > left) {
                prevZoomX -= zoomedLeft - left;
                childX -= zoomedLeft - left;
            }
            if (zoomedRight < right) {
                prevZoomX += right - zoomedRight;
                childX += right - zoomedRight;
            }
            if (zoomedTop < top) {
                prevZoomY += top - zoomedTop;
                childY += top - zoomedTop;
            }
            if (zoomedBottom > bottom) {
                prevZoomY -= zoomedBottom - bottom;
                childY -= zoomedBottom - bottom;
            }
        }

        childX += movedX * childScaleX;
        childY += movedY * childScaleY;
    }

    public void smoothZoomAtPointer(int pointer, float deltaZoom, float time) {
        smoothZoom(
                deltaZoom,
                Sys.system.getInputX(pointer) + camera.position.x,
                Sys.system.getInputY(pointer) + camera.position.y,
                time
        );
    }

    public void smoothZoom(float deltaZoom, float x, float y, float time) {
        smoothZoomDeltas.add(deltaZoom);
        smoothZoomDeltas.add(x);
        smoothZoomDeltas.add(y);
        smoothZoomDeltas.add(getRenderTime());
        smoothZoomDeltas.add(time);
        smoothZoomDeltas.add(1f);
    }

    public void stopSmoothZoom() {
        smoothZoomDeltas.clear();
    }

    public void slideMove(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;

        velocity = (float) Math.sqrt((speedX * speedX) + (speedY * speedY));

        if(velocity <= 0f) {
            this.speedX = this.speedY = 0f;
            return;
        }
        // Calculate reduction
        reductionX = Math.abs(speedX / velocity * deceleration);
        reductionY = Math.abs(speedY / velocity * deceleration);
    }

    @Override
    protected void render(Universe v, float r, float renderTime) {
        // Touch zoom
        /*
        if(maxZoom > 1f && touchedZoomPointer != -1) {
            float deltaX = (Math.abs(touchedX - touchedZoomX) + minPinchSize) / (Math.abs(touchStartedX - touchZoomStartedX) + minPinchSize);
            float deltaY = (Math.abs(touchedY - touchedZoomY) + minPinchSize) / (Math.abs(touchStartedY - touchZoomStartedY) + minPinchSize);
            float delta = Math.max(deltaX, deltaY);
            float zoom = delta / touchZoomPrev;
            touchZoomPrev = delta;

            smoothZoom(zoom, zoomX, zoomY, 0.1f);
        }
        */

        // Smooth zoom
        if(smoothZoomDeltas.size > 0) {
            float totalZoomDelta = 1f;
            float totalX = 0;
            float totalY = 0;
            int samples = 0;

            for (int c = 0; c < smoothZoomDeltas.size; c += 6, samples++) {
                float zoom = smoothZoomDeltas.items[c];
                totalX += smoothZoomDeltas.items[c + 1];
                totalY += smoothZoomDeltas.items[c + 2];
                float tStarted = smoothZoomDeltas.items[c + 3];
                float tSmoothTime = smoothZoomDeltas.items[c + 4];
                float prevZoom = smoothZoomDeltas.items[c + 5];

                float elapsed = renderTime - tStarted;
                float currentZoom;
                if (elapsed < tSmoothTime) {
                    currentZoom = 1f + ((zoom - 1f) * (elapsed / tSmoothTime));
                    smoothZoomDeltas.items[c + 5] = currentZoom;
                }
                else {
                    // Finished
                    currentZoom = zoom;

                    // Finished, shift another set
                    if(smoothZoomDeltas.size > 6) {
                        int i = smoothZoomDeltas.size - 6;
                        smoothZoomDeltas.items[c] = smoothZoomDeltas.items[i];
                        smoothZoomDeltas.items[c + 1] = smoothZoomDeltas.items[i + 1];
                        smoothZoomDeltas.items[c + 2] = smoothZoomDeltas.items[i + 2];
                        smoothZoomDeltas.items[c + 3] = smoothZoomDeltas.items[i + 3];
                        smoothZoomDeltas.items[c + 4] = smoothZoomDeltas.items[i + 4];
                        smoothZoomDeltas.items[c + 5] = smoothZoomDeltas.items[i + 5];
                    }

                    // Reduce size
                    smoothZoomDeltas.size -= 6;
                    c -= 6;
                }

                // Aggregate
                totalZoomDelta *= currentZoom / prevZoom;
            }

            // Average it
            //totalZoomDelta /= samples;
            totalX /= samples;
            totalY /= samples;

            zoom(totalZoomDelta, totalX, totalY);

            if(smoothZoomDeltas.size == 0 && !isTouching())
                seekNearestGravityTarget();
        }

        // Smooth move
        if(smoothDeltas.size > 0) {
            float tDeltaTime = getRenderDeltaTime();

            for(int c = 0; c < smoothDeltas.size; c += 4) {
                float x = smoothDeltas.items[c];
                float y = smoothDeltas.items[c + 1];
                float tRemaining = smoothDeltas.items[c + 2];
                float tSmoothTime = smoothDeltas.items[c + 3];

                float tElapsed;

                if(tDeltaTime > tRemaining) {
                    tElapsed = tRemaining;      // finished

                    // Finished, shift another set
                    if(smoothDeltas.size > 4) {
                        int i = smoothDeltas.size - 4;
                        smoothDeltas.items[c] = smoothDeltas.items[i];
                        smoothDeltas.items[c + 1] = smoothDeltas.items[i + 1];
                        smoothDeltas.items[c + 2] = smoothDeltas.items[i + 2];
                        smoothDeltas.items[c + 3] = smoothDeltas.items[i + 3];
                    }

                    // Reduce size
                    smoothDeltas.size -= 4;
                    c -= 4;
                }
                else {
                    tElapsed = tDeltaTime;
                    smoothDeltas.items[c + 2] -= tDeltaTime;
                }

                float ratio = tElapsed / tSmoothTime;

                move(x * ratio, y * ratio);
            }

            if(smoothDeltas.size == 0 && !isTouching())
                seekNearestGravityTarget();
        }

        // Check speed
        if(tGravityTargetStarted != -1) {
            // Falling into this place, get
            float elapsed = (renderTime - tGravityTargetStarted) * gravityTargetSpeed;
            if(elapsed >= gravitySeekGraph.getLength()) {
                movedX = gravityTargetEndX;
                movedY = gravityTargetEndY;
                refreshQueued = true;
                stop();
                stopGravityTarget();
            }
            else {
                float progress = gravitySeekGraph.generate(elapsed);
                float targetX = gravityTargetStartX + ((gravityTargetEndX - gravityTargetStartX) * progress);
                float targetY = gravityTargetStartY + ((gravityTargetEndY - gravityTargetStartY) * progress);
                move((targetX - movedX) * childScaleX, (targetY - movedY) * childScaleY);
            }
        }
        else if(tTouchStarted == -1 && (speedX != 0f || speedY != 0f)) {
            float reductionX = this.reductionX * getRenderDeltaTime();
            float reductionY = this.reductionY * getRenderDeltaTime();
            if(speedX > 0f) {
                speedX -= reductionX;
                if(speedX < 0f)
                    speedX = 0f;
            } else if(speedX < 0) {
                speedX += reductionX;
                if(speedX > 0f)
                    speedX = 0f;
            }
            if(speedY > 0f) {
                speedY -= reductionY;
                if(speedY < 0f)
                    speedY = 0f;
            } else if(speedY < 0) {
                speedY += reductionY;
                if(speedY > 0f)
                    speedY = 0f;
            }
            move(speedX * getRenderDeltaTime(), speedY * getRenderDeltaTime());
        }

        // Check queued move
        if(moveXqueued != Float.MAX_VALUE && moveYqueued != Float.MAX_VALUE && childScaleX != 0 && childScaleY != 0) {
            movedX += (moveXqueued / childScaleX);
            movedY += (moveYqueued / childScaleY);
            moveXqueued = moveYqueued = Float.MAX_VALUE;
            refresh();
        }
        else if(refreshQueued) {
            refresh();
        }

        // Seek gravity target if queued
        if(seekGravityDirectionX != 0 || seekGravityDirectionY != 0) {
            float targetX = movedX;
            float targetY = movedY;
            if (gravityIntervalX > 0) {
                targetX = Math.round((movedX + (seekGravityDirectionX * gravityThresholdX)) / gravityIntervalX) * gravityIntervalX;
            }
            if (gravityIntervalY > 0) {
                targetY = Math.round((movedY + (seekGravityDirectionY * gravityThresholdY)) / gravityIntervalY) * gravityIntervalY;
            }
            if (targetX != movedX || targetY != movedY) {
                // start gravity target seeking
                gravityTargetStartX = movedX;
                gravityTargetStartY = movedY;
                gravityTargetEndX = targetX;
                gravityTargetEndY = targetY;
                tGravityTargetStarted = getRenderTime();
                // Speedup
                gravityTargetSpeed = 1f + (velocity * gravitySpeedVelocityMultiplier);
                if (gravityTargetSpeed > gravityMaxSpeed)
                    gravityTargetSpeed = gravityMaxSpeed;
                stop();
            }
            // Clear
            seekGravityDirectionX = seekGravityDirectionY = 0;
        }

        if(scissorViewableRegion) {
            super.calculateWindow();
            float scissorX = childX;
            float scissorY = childY;
            float scissorWidth = childScaleX;
            float scissorLength = childLength;

            // Update position
            calculateWindow();

            scissorPushed = true;
            Matrices.push();
            Matrices.scissor.x = scissorX;
            Matrices.scissor.y = scissorY;
            Matrices.scissor.width = scissorWidth;
            Matrices.scissor.height = scissorLength;
        }
        else
            calculateWindow();                      // Update position
    }

    @Override
    protected void renderFinish(Universe v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        // Scissor
        if(scissorPushed) {
            Matrices.pop();
            scissorPushed = false;
        }
    }

    private void addSpeedAssessment(float deltaX, float deltaY) {
        // Add to touch deltas
        float tRenderTime = getRenderTime();
        float tMaxRenderTime = tRenderTime - tMaxSpeedAssessmentTime;
        boolean saved = false;
        for(int c = 0; c < touchDeltas.size; c += 3) {
            float tPrevRenderTime = touchDeltas.items[c + 2];
            if(tPrevRenderTime < tMaxRenderTime) {
                touchDeltas.items[c] = deltaX;
                touchDeltas.items[c + 1] = deltaY;
                touchDeltas.items[c + 2] = tRenderTime;
                saved = true;
                break;
            }
        }
        if(!saved) {
            touchDeltas.add(deltaX);
            touchDeltas.add(deltaY);
            touchDeltas.add(tRenderTime);
        }
    }


    private void calculateSpeed(boolean ignoreThresholds) {
        speedX = speedY = reductionX = reductionY = 0f;

        float tCurrentTime = getRenderTime();
        float tOldestRenderTime = tCurrentTime;
        float tMaxRenderTime = tCurrentTime - tMaxSpeedAssessmentTime;

        for(int c = 0; c < touchDeltas.size; c += 3) {
            float deltaX = touchDeltas.items[c];
            float deltaY = touchDeltas.items[c + 1];
            float tRenderTime = touchDeltas.items[c + 2];
            if(tRenderTime < tMaxRenderTime)
                continue;       // too old
            // Else accept for assessment
            speedX += deltaX;
            speedY += deltaY;
            if(tRenderTime < tOldestRenderTime)
                tOldestRenderTime = tRenderTime;
        }

        // Check elapsed time
        if(!ignoreThresholds) {
            float tElapsed = tCurrentTime - tOldestRenderTime;
            if (tElapsed < tMinSpeedAssessmentTime) {
                speedX = speedY = 0;
                return;         // not enough input, accidental touch
            }
            // Calculate speed and velocity
            speedX /= tElapsed;
            speedY /= tElapsed;
        }

        velocity = (float) Math.sqrt((speedX * speedX) + (speedY * speedY));

        if(velocity <= 0f || (!ignoreThresholds && Math.abs(velocity) < minVelocity)) {
            speedX = speedY = 0f;
            return;
        }
        // Calculate reduction
        reductionX = Math.abs(speedX / velocity * deceleration);
        reductionY = Math.abs(speedY / velocity * deceleration);
    }

    @Override
    protected boolean input(Universe v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if((inputType & (INPUT_TOUCH | INPUT_SCROLLED)) == 0 || camera == null)
            return false;		// respond to only touch

        x += camera.position.x;
        y += camera.position.y;

        float cx = (x + childInputX) * childInputScaleX;
        float cy = (y + childInputY) * childInputScaleY;

        switch(inputType) {
            case INPUT_SCROLLED: {
                // Desktop zoom
                if(!checkTouched(x + (movedX * childScaleX / childInputScaleX), y + (movedY * childScaleY / childInputScaleY)))
                    return false;
                if (maxZoom <= 1f) {
                    if(!verticalScrollable)
                        return false;       // cannot scroll vertically
                    // No zoom allowed, default to scroll up / down
                    float dy  = scrolledAmount * desktopScrollPower;
                    isMoving = true;
                    isZooming = false;
                    addSpeedAssessment(0, dy);
                    calculateSpeed(true);
                    return !passThroughInput;
                }

                // Zooming
                isMoving = false;
                isZooming = true;
                stopCurrent();
                smoothZoom(1f + scrolledAmount * desktopZoomPower,
                        ((Sys.system.getInputX(0) + camera.position.x) + childInputX) * childInputScaleX,
                        ((Sys.system.getInputY(0) + camera.position.y) + childInputY) * childInputScaleY,
                        tTouchSmoothTime
                );

                return !passThroughInput;
            }


            case INPUT_TOUCH_DOWN:
                if(!checkTouched(x + (movedX * childScaleX / childInputScaleX), y + (movedY * childScaleY / childInputScaleY)))
                    return false;
                if(maxZoom > 1f && touchedPointer != -1) {
                    isMoving = false;
                    isZooming = true;
                    touchedZoomPointer = pointer;
                    touchedZoomX = cx;
                    touchedZoomY = cy;
                    touchStartedX = touchedX;
                    touchStartedY = touchedY;
                    touchZoomPrev = 1f;
                    touchZoomStartedDistance = Vector2.dst(touchStartedX, touchStartedY, touchedZoomX, touchedZoomY);

                    zoom(1f, (touchStartedX + touchedZoomX) / 2f, (touchStartedY + touchedZoomY) / 2f);
                }
                if(maxZoom <= 1f || touchedPointer == -1) {
                    touchPressed(v, cx, cy, button);
                    touchedPointer = pointer;
                    touchedX = cx;
                    touchedY = cy;
                    touchStartedX = cx;
                    touchStartedY = cy;
                    isMoving = false;
                    isZooming = false;
                    speedX = 0f;
                    speedY = 0f;
                    tTouchStarted = getRenderTime();
                    touchDeltas.clear();
                    stopGravityTarget();
                }
                return !passThroughInput;

            case INPUT_TOUCH_DRAGGED: {
                if(pointer != touchedPointer && pointer != touchedZoomPointer)
                    return false;
                if(maxZoom > 1f && touchedZoomPointer != -1) {
                    if(pointer == touchedPointer) {
                        touchedX = cx;
                        touchedY = cy;
                    }
                    else {
                        touchedZoomX = cx;
                        touchedZoomY = cy;
                    }

                    float deltaNow = Vector2.dst(touchedX, touchedY, touchedZoomX, touchedZoomY) + minPinchSize;
                    float deltaStarted = touchZoomStartedDistance + minPinchSize;
                    float delta = deltaNow / deltaStarted;
                    float zoom = delta / touchZoomPrev;
                    touchZoomPrev = delta;

                    smoothZoom(zoom, zoomX, zoomY, tTouchSmoothTime);

                    return !passThroughInput;
                }
                touchDragged(v, cx, cy, button);
                float deltaX = cx - touchedX;
                float deltaY = cy - touchedY;
                if(!horizontalScrollable)
                    deltaX = 0f;
                if(!verticalScrollable)
                    deltaY = 0f;
                if(!isMoving) {
                    float minDistance2 = minTouchMoveDistance * minTouchMoveDistance;
                    float startDeltaX = touchStartedX - cx;
                    float startDeltaY = touchStartedY - cy;
                    if(((startDeltaX * startDeltaX) + (startDeltaY * startDeltaY)) > minDistance2) {
                        isMoving = true;
                        smoothMove(deltaX, deltaY, tTouchSmoothTime);
                    }
                }
                else
                    smoothMove(deltaX, deltaY, tTouchSmoothTime);
                touchedX = cx;
                touchedY = cy;
                addSpeedAssessment(deltaX, deltaY);
                return !passThroughInput;
            }


            case INPUT_TOUCH_UP:
                if(touchedPointer == pointer) {
                    touchReleased(v, cx, cy, button);
                    if(!isMoving && !isZooming)
                        activated(v, button);
                    // Clear smooth move
                    smoothDeltas.clear();
                    // Calculate speed
                    float deltaX = touchedX - touchStartedX;
                    float deltaY = touchedY - touchStartedY;
                    if(Math.abs(deltaX) > 0 || Math.abs(deltaY) > 0) {
                        calculateSpeed(false);
                        seekNearestGravityTarget(deltaX, deltaY);
                    }
                    else
                        seekNearestGravityTarget();
                    tTouchStarted = -1;
                    touchedPointer = -1;
                    touchedZoomPointer = -1;
                    isMoving = false;
                    isZooming = false;
                    touchDeltas.clear();
                }
                else if(touchedZoomPointer == pointer) {
                    touchedZoomPointer = -1;
                }
                else
                    return false;
                return !passThroughInput;

            default:
                return false;
        }
    }

    public void activated(Universe v, int button) {
        OnClick callback = findParent(OnClick.class);
        if(callback != null)
            callback.onClick(v, this, button);
    }

    public void touchPressed(Universe v, float x, float y, int button) {
        OnPressed callback = findParent(OnPressed.class);
        if(callback != null)
            callback.onPressed(v, this, x, y, button);
    }
    public void touchReleased(Universe v, float x, float y, int button) {
        OnReleased callback = findParent(OnReleased.class);
        if(callback != null)
            callback.onReleased(v, this, x, y, button);

    }
    public void touchDragged(Universe v, float x, float y, int button) {
        OnDragged callback = findParent(OnDragged.class);
        if(callback != null)
            callback.onDragged(v, this, x, y, button);
    }
}
