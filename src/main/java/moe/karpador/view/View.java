package moe.karpador.view;


import moe.karpador.WallscrollSimulator;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public abstract class View {
    private final String renderer;
    protected PGraphics g;
    private PGraphics builtG;
    private boolean modified = true;

    protected View(String renderer) {
        this.renderer = renderer;
    }
    protected View() {
        this(PConstants.JAVA2D);
    }

    protected abstract PGraphics build(ViewConstraint constraint);

    public final PGraphics draw(ViewConstraint constraint) {
        if (modified || builtG==null || !constraint.matchesSize(new PVector(builtG.width, builtG.height))) {
            builtG = build(constraint);
            modified = false;
        }
        if (!constraint.matchesSize(new PVector(builtG.width, builtG.height))) {
            throw new IllegalStateException("View "+this+"("+builtG.width+","+builtG.height+") did not follow constraints "+constraint);
        }
        return builtG;
    }

    protected void modified() {
        modified = true;
    }

    protected PGraphics clearG(PGraphics g, int w, int h) {
        return clearG(g, w, h, this.renderer);
    }

    protected PGraphics clearG(PGraphics g, int w, int h, String renderer) {
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
