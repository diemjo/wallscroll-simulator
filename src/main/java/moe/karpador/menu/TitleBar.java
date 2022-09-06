package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.Container;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PConstants.LEFT;

public class TitleBar extends View {
    public final String title;
    public final int textSize;
    private final ArrayList<ViewInstance<Container<CheckBox>>> checkboxes;
    private final ArrayList<CheckBox.Option> options;

    public TitleBar(String title, int textSize) {
        super();
        this.title = title;
        this.textSize = textSize;
        this.checkboxes = new ArrayList<>();
        this.options = new ArrayList<>();
    }

    public TitleBar withCheckbox(String name, boolean defaultState) {
        CheckBox.Option option = new CheckBox.Option(name, defaultState);
        options.add(option);
        CheckBox c = new CheckBox(name, WallscrollSimulator.buttonDescTextSize(), option);
        checkboxes.add(new ViewInstance<>(new Container<>(c, false)));
        return this;
    }

    public boolean checked(String name) {
        return options.stream().anyMatch(o -> o.id.equals(name) && o.get());
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        checkboxes.forEach(ci -> ci.draw(ViewConstraint.with(
                new PVector(0, constraint.maxSize.y),
                new PVector(constraint.maxSize.x, constraint.maxSize.y)
        )));
        float titleWidth = WallscrollSimulator.getTextWidth(title, textSize);

        g = clearG(g, (int) constraint.maxSize.x, (int) constraint.maxSize.y);
        g.beginDraw();

        g.fill(200, 255, 230);
        g.strokeWeight(2);
        g.rect(1, 1, g.width - 2, g.height - 2);
        g.fill(0);
        g.textAlign(PConstants.LEFT, PConstants.CENTER);
        g.textSize(textSize);
        g.text(title, 5, g.height/2 - textSize/8);

        if (checkboxes.size()>0) {
            float boxesOffsetX = titleWidth + 5;
            float totalWidth = boxesOffsetX + checkboxes.stream()
                    .mapToInt(ci -> ci.g.width)
                    .sum();
            float gap = (g.width - totalWidth) / (options.size() + 1);
            boxesOffsetX += gap;
            for (ViewInstance<Container<CheckBox>> ci : checkboxes) {
                ci.position = new PVector(boxesOffsetX, 0);
                g.image(ci.g, ci.position.x, ci.position.y);
                boxesOffsetX += ci.g.width;
                boxesOffsetX += gap;
            }
        }

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouse == null)
            return;
        if (mouseButton == LEFT) {
            for (ViewInstance<Container<CheckBox>> ci : checkboxes) {
                if (ci.hover(mouse)) {
                    PVector pos = ci.mousePos(mouse);
                    ci.v.mousePressed(mouseButton, pos);
                    break;
                }
            }
        }
    }

    @Override
    public boolean update(long time) {
        for (ViewInstance<Container<CheckBox>> ci : checkboxes) {
            if (ci.v.update(time)) {
                modified();
            }
        }
        return super.update(time);
    }
}
