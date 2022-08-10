package moe.karpador.view;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Container<T extends View> extends View {
    protected enum Alignment { CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_CENTER, BOTTOM_CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    public final ViewInstance<T> viewi;

    private final Alignment alignment;

    public Container(T view, Alignment alignment) {
        super();
        this.viewi = new ViewInstance<>(view);
        this.alignment = alignment;
    }

    public Container(T view) {
        this(view, Alignment.CENTER);
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
        return viewi.view;
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

    public boolean update(long time, int mouseX, int mouseY) {
        PVector pos = viewi.mousePos(mouseX, mouseY);
        if (viewi.view.update(time, (int) pos.x, (int) pos.y)) {
            modified();
        }
        return super.update(time, mouseX, mouseY);
    }

    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        PVector pos = viewi.mousePos(mouseX, mouseY);
        viewi.view.mousePressed(mouseButton, (int) pos.x, (int) pos.y);
    }

    public void mouseReleased(int mouseButton, int mouseX, int mouseY) {
        PVector pos = viewi.mousePos(mouseX, mouseY);
        viewi.view.mouseReleased(mouseButton, (int) pos.x, (int) pos.y);
    }

    public void mouseDragged(int mouseButton, int mouseX, int mouseY, int pmouseX, int pmouseY) {
        PVector pos = viewi.mousePos(mouseX, mouseY);
        PVector ppos = viewi.mousePos(pmouseX, pmouseY);
        viewi.view.mouseDragged(mouseButton, (int) pos.x, (int) pos.y, (int) ppos.x, (int) ppos.y);
    }

    public void mouseWheel(MouseEvent e) {
        viewi.view.mouseWheel(e);
    }

    public void keyPressed(int key, int keyCode) {
        viewi.view.keyPressed(key, keyCode);
    }
}
