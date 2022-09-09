package moe.karpador.view;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class Button<T extends View> extends View {

    private final ViewInstance<T> view;
    private final Runnable func;

    public Button(T view, Runnable func) {
        this.view = new ViewInstance<>(view);
        this.func = func;
    }

    public T view() {
        return view.v;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        return view.draw(constraint);
    }

    @Override
    public boolean update(long time) {
        if (view.v.update(time)) {
            modified();
        }
        return super.update(time);
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == PConstants.LEFT) {
            func.run();
        }
    }

    @Override
    public void mouseReleased(int mouseButton, PVector mouse) {
        view.v.mouseReleased(mouseButton, mouse);
    }

    @Override
    public void mouseDragged(int mouseButton, PVector mouse, PVector pmouse) {
        view.v.mouseDragged(mouseButton, mouse, pmouse);
    }

    @Override
    public void mouseWheel(int scrollCount, PVector mouse) {
        view.v.mouseWheel(scrollCount, mouse);
    }

    @Override
    public boolean keyPressed(int key, int keyCode, PVector mouse) {
        return view.v.keyPressed(key, keyCode, mouse);
    }
}
