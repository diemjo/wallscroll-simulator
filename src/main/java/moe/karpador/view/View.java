package moe.karpador.view;


import moe.karpador.WallscrollSimulator;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

public abstract class View {
    private final String renderer;
    protected PGraphics g;
    protected int mouseX, mouseY;
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
            return WallscrollSimulator.createPGraphics(w, h, renderer);
        } else {
            g.beginDraw();
            g.clear();
            return g;
        }
    }



    public boolean update(long time, int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        return modified;
    }

    public void mousePressed(int mouseButton, int mouseX, int mouseY) {

    }

    public void mouseReleased(int mouseButton, int mouseX, int mouseY) {

    }

    public void mouseDragged(int mouseButton, int mouseX, int mouseY, int pmouseX, int pmouseY) {

    }

    public void mouseWheel(MouseEvent e) {

    }

    public void keyPressed(int key, int keyCode) {
        if (keyCode== PConstants.ESC) {
            WallscrollSimulator.preventEscape();
            WallscrollSimulator.popView();
        }
    }
}
