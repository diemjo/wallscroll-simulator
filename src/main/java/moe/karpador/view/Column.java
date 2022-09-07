package moe.karpador.view;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

public class Column<T extends View> extends View {
    public enum SpacingType {
        EQUAL_INNER_SPACING,
        EQUAL_SPACING,
        NO_SPACING,
    }

    private final List<ViewInstance<T>> children;
    private final SpacingType spacingType;

    public Column(List<T> children, SpacingType spacingType) {
        this.children = children.stream()
                .map(ViewInstance::new)
                .toList();
        this.spacingType = spacingType;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        if (children.isEmpty()) {
            g = clearG(g, (int) constraint.minSize.x, (int) constraint.minSize.y);
            return g;
        }
        int totalHeight = 0;
        int maxWidth = 0;
        for (ViewInstance<T> child : children) {
            child.draw(constraint);
            totalHeight += child.g.height;
            maxWidth = PApplet.max(maxWidth, child.g.width);
        }
        float spacing = 0;
        float offset = 0;
        switch (spacingType) {
            case NO_SPACING -> {
                g = clearG(g, PApplet.max(maxWidth, (int) constraint.minSize.x), PApplet.max(totalHeight, (int) constraint.minSize.y));
                spacing = 0;
                offset = (g.height - totalHeight) / 2;
            }
            case EQUAL_SPACING -> {
                g = clearG(g, PApplet.max(maxWidth, (int) constraint.minSize.x), (int) constraint.maxSize.y);
                spacing = (constraint.maxSize.y - totalHeight) / (children.size() + 1);
                offset = spacing;
            }
            case EQUAL_INNER_SPACING -> {
                g = clearG(g, PApplet.max(maxWidth, (int) constraint.minSize.x), (int) constraint.maxSize.y);
                spacing = children.size() > 1 ? (constraint.maxSize.y - totalHeight) / (children.size() - 1) : 0;
                offset = children.size() > 1 ? 0 : (constraint.maxSize.y - totalHeight) / 2;
            }
        }
        for (ViewInstance<T> child : children) {
            child.position = new PVector((g.width - child.g.width) / 2, offset);
            offset += child.g.height;
            offset += spacing;
            g.image(child.g, child.position.x, child.position.y);
        }
        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        for (ViewInstance<T> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mousePressed(mouseButton, pos);
                break;
            }
        }
    }

    public void mouseReleased(int mouseButton, PVector mouse) {
        for (ViewInstance<T> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mouseReleased(mouseButton, pos);
                break;
            }
        }
    }

    public void mouseDragged(int mouseButton, PVector mouse, PVector pmouse) {
        for (ViewInstance<T> child : children) {
            PVector pos = child.mousePos(mouse);
            PVector ppos = child.mousePos(pmouse);
            if (pos != null && ppos != null) {
                child.v.mouseDragged(mouseButton, pos, ppos);
                break;
            }
        }
    }

    public void mouseWheel(int scrollCount, PVector mouse) {
        for (ViewInstance<T> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mouseWheel(scrollCount, pos);
                break;
            }
        }
    }

    public boolean keyPressed(int key, int keyCode, PVector mouse) {
        for (ViewInstance<T> child : children) {
            PVector pos = child.mousePos(mouse);
            if (child.v.keyPressed(key, keyCode, pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(long time) {
        if (children.stream().anyMatch(vi -> vi.v.update(time))) {
            modified();
        }
        return super.update(time);
    }
}
