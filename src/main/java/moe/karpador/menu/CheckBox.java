package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class CheckBox extends View {

    public final String hint;
    public final int textSize;
    private boolean state;
    private float boxSide;

    public CheckBox(String hint, int textSize, boolean defaultState) {
        super();
        this.hint = hint;
        this.textSize = textSize;
        this.state = defaultState;
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean isChecked() {
        return state;
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float textWidth = WallscrollSimulator.getTextWidth(hint, textSize);
        boxSide = PApplet.max(textSize * 1.5f, constraint.minSize.y);
        float minWidth = PApplet.max(constraint.minSize.x, boxSide*1.1f + textWidth+1);

        g = clearG(g, (int) minWidth, (int) boxSide);
        g.beginDraw();

        g.noFill();
        g.strokeWeight(2);
        g.rect(1, 1, boxSide - 2, boxSide - 2);
        if (state) {
            g.strokeWeight(4);
            g.line(3, 3, boxSide - 3, boxSide - 3);
            g.line(boxSide - 3, 3, 3, boxSide - 3);
        }
        g.fill(0);
        g.textSize(textSize);
        g.textAlign(PConstants.LEFT, PConstants.BOTTOM);
        g.text(hint, boxSide*1.1f, boxSide - 3);

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == PConstants.LEFT) {
            if (mouseX >= 0 && mouseX < boxSide && mouseY >= 0 && mouseY < boxSide) {
                state = !state;
                modified();
            }
        }
    }
}
