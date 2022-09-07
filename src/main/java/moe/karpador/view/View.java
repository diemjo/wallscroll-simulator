package moe.karpador.view;


import moe.karpador.WallscrollSimulator;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public abstract class View {
    private final String renderer;
    protected PGraphics g;
    private boolean modified = true;

    protected View(String renderer) {
        this.renderer = renderer;
    }
    protected View() {
        this(PConstants.JAVA2D);
    }

    protected abstract PGraphics build(ViewConstraint constraint);

    public final PGraphics draw(ViewConstraint constraint) {
        if (modified || g==null || !constraint.matchesSize(new PVector(g.width, g.height))) {
            g = build(constraint);
            modified = false;
        }
        if (!constraint.matchesSize(new PVector(g.width, g.height))) {
            throw new IllegalStateException("View "+this+"("+g.width+","+g.height+") did not follow constraints "+constraint);
        }
        return g;
    }

    protected void modified() {
        modified = true;
    }

    protected PGraphics clearG(PGraphics g, int w, int h) {
        if (g==null || g.width!=w || g.height!=h) {
            PGraphics pg = WallscrollSimulator.createPGraphics(w, h, renderer);
            pg.beginDraw();
            return pg;
        } else {
            g.beginDraw();
            g.clear();
            return g;
        }
    }

    public boolean update(long time) {
        return modified;
    }

    public void mousePressed(int mouseButton, PVector mouse) {

    }

    public void mouseReleased(int mouseButton, PVector mouse) {

    }

    public void mouseDragged(int mouseButton, PVector mouse, PVector pmouse) {

    }

    public void mouseWheel(int scrollCount, PVector mouse) {

    }

    public boolean keyPressed(int key, int keyCode, PVector mouse) {
        if (keyCode== PConstants.ESC) {
            WallscrollSimulator.preventEscape();
            WallscrollSimulator.popView();
            return true;
        }
        return false;
    }
}
