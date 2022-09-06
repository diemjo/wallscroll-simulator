package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class CheckBox extends View {

    public final String hint;
    public final int textSize;
    private Option option;
    private float boxSide;

    public CheckBox(String hint, int textSize, Option option) {
        super();
        this.hint = hint;
        this.textSize = textSize;
        this.option = option;
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public boolean isChecked() {
        return option.get();
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
        if (option.get()) {
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
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == PConstants.LEFT) {
            if (mouse.x >= 0 && mouse.x < boxSide && mouse.y >= 0 && mouse.y < boxSide) {
                option.toggle();
                modified();
            }
        }
    }

    static class Option {
        public final String id;
        private boolean state;
        public Option(String id, boolean defaultState) {
            this.id = id;
            this.state = defaultState;
        }

        public void toggle() {
            this.state = !this.state;
        }

        public boolean get() {
            return state;
        }
    }
}
