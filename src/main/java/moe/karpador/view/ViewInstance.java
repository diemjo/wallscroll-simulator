package moe.karpador.view;

import processing.core.PGraphics;
import processing.core.PVector;

public class ViewInstance<T extends View> {
    public final T view;
    public PVector position;
    public PGraphics g;

    public ViewInstance(T v) {
        this.view = v;
    }

    public PGraphics draw(ViewConstraint c) {
        g = view.draw(c);
        return g;
    }

    public boolean hover(int mouseX, int mouseY) {
        if (g == null) {
            return false;
        }
        return mouseX >= position.x && mouseX < position.x + g.width && mouseY >= position.y && mouseY < position.y + g.height;
    }

    public PVector mousePos(int mouseX, int mouseY) {
        if (g == null) {
            return new PVector(-1, -1);
        }
        return new PVector(mouseX - position.x, mouseY - position.y);
    }
}
