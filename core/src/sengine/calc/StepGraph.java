package sengine.calc;

/**
 * Created by Azmi on 9/29/2016.
 */

public class StepGraph extends Graph {

    public final Graph source;
    public final float step;

    public StepGraph(Graph source, float step) {
        this.source = source;

        if(step == 0f)
            throw new IllegalArgumentException("step cannot be 0");
        this.step = step;
    }

    public float step(float value) {
        int steps = Math.round(value / step);
        return steps * step;
    }

    @Override
    public float getStart() {
        return step(source.getStart());
    }

    @Override
    public float getEnd() {
        return step(source.getEnd());
    }

    @Override
    public float getLength() {
        return source.getLength();
    }

    @Override
    float calculate(float progress) {
        return step(source.calculate(progress));
    }
}
