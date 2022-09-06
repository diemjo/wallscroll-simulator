package moe.karpador.menu;


import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class TextButton extends View {
    public final String text;
    public final int textSize;
    private final Runnable func;

    public TextButton(String text, Runnable func, int textSize) {
        super();
        this.text = text;
        this.textSize = textSize;
        this.func = func;
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------


    @Override
    protected PGraphics build(ViewConstraint constraint) {
        g = clearG(g,
                (int) PApplet.max(constraint.maxSize.x, WallscrollSimulator.getTextWidth(text, textSize)),
                (int) (PApplet.max(constraint.minSize.y, textSize * 1.5f)));
        g.beginDraw();

        g.fill(255);
        g.strokeWeight(2);
        g.rect(1, 1, g.width-2, g.height-2);
        g.fill(0);
        g.textSize(textSize);
        g.textAlign(PConstants.CENTER, PConstants.CENTER);
        g.text(text, g.width/2, g.height/2);

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == PConstants.LEFT) {
            func.run();
        }
    }
}
