package moe.karpador.menu;


import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class TextView extends View {
    public final String text;
    public final int textSize;
    private final boolean multiline;
    public TextView(String text, int textSize, boolean multiline) {
        super();
        this.text = text;
        this.textSize = textSize;
        this.multiline = multiline;
    }

    public TextView(String text, int textSize) {
        this(text, textSize, false);
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------


    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float textWidth = WallscrollSimulator.getTextWidth(text, textSize) * 1.05f;
        int lines = multiline ? PApplet.ceil(textWidth / constraint.maxSize.x) : 1;
        int height = (int) (lines * textSize * 1.7);

        g = clearG(g,
                (int) PApplet.max(constraint.minSize.x, PApplet.min(textWidth, constraint.maxSize.x)),
                (int) PApplet.max(constraint.minSize.y, height)
        );
        g.beginDraw();

        g.fill(255);
        g.strokeWeight(2);
        g.rect(1, 1, g.width-2, g.height-2);
        g.fill(0);
        g.textSize(textSize);
        if (multiline) {
            g.textAlign(PConstants.LEFT, PConstants.TOP);
            g.text(text, textWidth/lines*0.025f, 1, g.width - textWidth/lines*0.05f, g.height - 2);
        } else {
            g.textAlign(PConstants.CENTER, PConstants.CENTER);
            g.text(text, g.width / 2f, g.height / 2f);
        }

        g.endDraw();
        return g;
    }
}
