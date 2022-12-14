package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.Button;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

import static processing.core.PApplet.max;
import static processing.core.PApplet.min;
import static processing.core.PConstants.*;

public class ConfirmMenu extends View {
    private final String text;
    private final int textSize;
    private final ViewInstance<Button<TextView>> confirm;
    private final ViewInstance<Button<TextView>> cancel;

    public ConfirmMenu(String text, int textSize, Runnable func) {
        super();
        this.text = text;
        this.textSize = textSize;
        confirm = new ViewInstance<>(new Button<>(new TextView("Confirm", textSize), () -> {
            WallscrollSimulator.popView();
            func.run();
        }));
        cancel = new ViewInstance<>(new Button<>(new TextView("Cancel", textSize), () -> {
            WallscrollSimulator.popView();
        }));
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float confirmWidth = WallscrollSimulator.getTextWidth(confirm.v.view().text, confirm.v.view().textSize) + 8;
        float cancelWidth = WallscrollSimulator.getTextWidth(cancel.v.view().text, cancel.v.view().textSize) + 8;
        float textWidth = WallscrollSimulator.getTextWidth(text, textSize);
        int padding = (int) min(50, textSize*1.5f);
        float maxWidth = max(confirmWidth + cancelWidth, textWidth);
        float maxHeight = 4*textSize;

        g = clearG(g, (int) maxWidth + 2 * padding, (int) maxHeight);
        g.beginDraw();

        confirm.draw(ViewConstraint.exact(new PVector(max(confirmWidth, confirmWidth + (maxWidth - (confirmWidth + cancelWidth))/2), 2*textSize-4)));
        cancel.draw(ViewConstraint.exact(new PVector(max(cancelWidth, cancelWidth + (maxWidth - (confirmWidth + cancelWidth))/2), 2*textSize-4)));

        g.fill(255, 200, 230);
        g.strokeWeight(4);
        g.rect(2, 2, g.width - 4, g.height - 4);
        g.fill(0);
        g.textSize(textSize);
        g.textAlign(CENTER, CENTER);
        g.text(text, g.width/2, g.height/4);

        cancel.position = new PVector(padding/2, g.height * 3 / 4 - cancel.g.height / 2);
        g.image(cancel.g, cancel.position.x, cancel.position.y);
        confirm.position = new PVector(padding/2 + cancel.g.width + padding, g.height * 3 / 4 - confirm.g.height / 2);
        g.image(confirm.g, confirm.position.x, confirm.position.y);

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == LEFT) {
            for (ViewInstance<Button<TextView>> bi : List.of(cancel, confirm)) {
                if (bi.hover(mouse)) {
                    PVector pos = bi.mousePos(mouse);
                    bi.v.mousePressed(mouseButton, pos);
                    break;
                }
            }
        }
    }

    @Override
    public boolean update(long time) {
        for (ViewInstance<Button<TextView>> bi : List.of(cancel, confirm)) {
            if (bi.v.update(time)) {
                modified();
            }
        }
        return super.update(time);
    }
}
