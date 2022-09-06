package moe.karpador.view;

import moe.karpador.WallscrollSimulator;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Container<T extends View> extends View {
    protected enum Alignment { CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_CENTER, BOTTOM_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    public final ViewInstance<T> viewi;

    private final Alignment alignment;
    private final boolean closeOnOutsidePress;

    public Container(T view, boolean closeOnOutsidePress, Alignment alignment) {
        super();
        this.viewi = new ViewInstance<>(view);
        this.alignment = alignment;
        this.closeOnOutsidePress = closeOnOutsidePress;
    }

    public Container(T view, boolean closeOnOutsidePress) {
        this(view, closeOnOutsidePress, Alignment.CENTER);
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    private PVector calculateViewPosition(PVector viewSize) {
        float diffX = g.width - viewSize.x;
        float diffY = g.height - viewSize.y;
        return switch (alignment) {
            case TOP_LEFT -> new PVector(0, 0);
            case TOP_RIGHT -> new PVector(diffX, 0);
            case TOP_CENTER -> new PVector(diffX/2, 0);
            case CENTER_LEFT -> new PVector(0, diffY/2);
            case CENTER -> new PVector(diffX/2, diffY/2);
            case CENTER_RIGHT -> new PVector(diffX, diffY/2);
            case BOTTOM_LEFT -> new PVector(0, diffY);
            case BOTTOM_CENTER -> new PVector(diffX/2, diffY);
            case BOTTOM_RIGHT -> new PVector(diffX, diffY);
        };
    }

    public T view() {
        return viewi.v;
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public PGraphics build(ViewConstraint constraint) {
        viewi.draw(ViewConstraint.max(constraint.maxSize));
        if (constraint.matchesSize(new PVector(viewi.g.width, viewi.g.height))) {
            viewi.position = new PVector(0, 0);
            g = viewi.g;
        } else {
            int cwidth = PApplet.max(viewi.g.width, (int) constraint.minSize.x);
            int cheight = PApplet.max(viewi.g.height, (int) constraint.minSize.y);
            g = clearG(g, cwidth, cheight);
            g.beginDraw();
            viewi.position = calculateViewPosition(new PVector(viewi.g.width, viewi.g.height));
            g.image(viewi.g, viewi.position.x, viewi.position.y);
            g.endDraw();
        }
        return g;
    }

    public boolean update(long time) {
        if (viewi.v.update(time)) {
            modified();
        }
        return super.update(time);
    }

    public void mousePressed(int mouseButton, PVector mouse) {
        PVector pos = viewi.mousePos(mouse);
        if (pos != null) {
            viewi.v.mousePressed(mouseButton, pos);
        } else if (closeOnOutsidePress) {
            WallscrollSimulator.popView();
        }
    }

    public void mouseReleased(int mouseButton, PVector mouse) {
        PVector pos = viewi.mousePos(mouse);
        if (pos != null) {
            viewi.v.mouseReleased(mouseButton, pos);
        }
    }

    public void mouseDragged(int mouseButton, PVector mouse, PVector pmouse) {
        PVector pos = viewi.mousePos(mouse);
        PVector ppos = viewi.mousePos(pmouse);
        if (pos != null && ppos != null) {
            viewi.v.mouseDragged(mouseButton, pos, ppos);
        }
    }

    public void mouseWheel(int scrollCount, PVector mouse) {
        PVector pos = viewi.mousePos(mouse);
        viewi.v.mouseWheel(scrollCount, pos);
    }

    public void keyPressed(int key, int keyCode, PVector mouse) {
        PVector pos = viewi.mousePos(mouse);
        viewi.v.keyPressed(key, keyCode, pos);
    }

}
