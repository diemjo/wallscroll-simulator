package moe.karpador.view;

import processing.core.PGraphics;
import processing.core.PVector;

public class ViewInstance<T extends View> {
    public final T v;
    public PVector position;
    public PGraphics g;

    public ViewInstance(T v) {
        this.v = v;
    }

    public PGraphics draw(ViewConstraint c) {
        g = v.draw(c);
        return g;
    }

    public boolean hover(PVector mouse) {
        if (g == null || mouse == null) {
            return false;
        }
        return mouse.x >= position.x && mouse.y < position.x + g.width && mouse.y >= position.y && mouse.y < position.y + g.height;
    }

    public PVector mousePos(PVector mouse) {
        if (g == null || mouse == null || mouse.x < position.x || mouse.x >= position.x + g.width || mouse.y < position.y || mouse.y >= position.y + g.height) {
            return null;
        }
        return new PVector(mouse.x - position.x, mouse.y - position.y);
    }
}
