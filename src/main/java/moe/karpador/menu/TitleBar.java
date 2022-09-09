package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.*;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PConstants.LEFT;

public class TitleBar extends View {
    public final String title;
    public final int textSize;
    private final ViewInstance<Row> checkboxes;
    private final List<CheckBox.Option> options;

    public TitleBar(String title, int textSize, List<CheckBox.Option> options) {
        super();
        this.title = title;
        this.textSize = textSize;
        this.options = options;
        this.checkboxes = new ViewInstance<>(new Row(options.stream()
                .map(o -> (View) new Container<>(new CheckBox(o.id, WallscrollSimulator.buttonDescTextSize(), o), false))
                .toList()
                , SpacingType.EQUAL_SPACING));
    }

    public boolean checked(String name) {
        return options.stream().anyMatch(o -> o.id.equals(name) && o.get());
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float titleWidth = WallscrollSimulator.getTextWidth(title, textSize);
        checkboxes.draw(ViewConstraint.with(
                new PVector(1, constraint.maxSize.y),
                new PVector(constraint.maxSize.x - titleWidth - 5, constraint.maxSize.y)
        ));

        g = clearG(g, (int) constraint.maxSize.x, (int) constraint.maxSize.y);
        g.beginDraw();

        g.fill(200, 255, 230);
        g.strokeWeight(2);
        g.rect(1, 1, g.width - 2, g.height - 2);
        g.fill(0);
        g.textAlign(PConstants.LEFT, PConstants.CENTER);
        g.textSize(textSize);
        g.text(title, 5, g.height/2 - textSize/8);

        checkboxes.position = new PVector(titleWidth + 5, 0);
        if (checkboxes.v.children().size()>0) {
            g.image(checkboxes.g, checkboxes.position.x, checkboxes.position.y);
        }

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouse == null)
            return;
        if (mouseButton == LEFT) {
            if (checkboxes.hover(mouse)) {
                PVector pos = checkboxes.mousePos(mouse);
                checkboxes.v.mousePressed(mouseButton, pos);
            }
        }
    }

    @Override
    public boolean update(long time) {
        if (checkboxes.v.update(time)) {
            modified();
        }
        return super.update(time);
    }
}
