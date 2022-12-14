package moe.karpador.view;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Row extends View implements Iterable {
    private final List<ViewInstance<View>> children;
    private final SpacingType spacingType;

    public Row(List<View> children, SpacingType spacingType) {
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
        int totalWidth = 0;
        int maxHeight = 0;
        for (ViewInstance<View> child : children) {
            child.draw(constraint);
            totalWidth += child.g.width;
            maxHeight = PApplet.max(maxHeight, child.g.height);
        }
        float spacing = 0;
        float offset = 0;
        switch (spacingType) {
            case NO_SPACING -> {
                g = clearG(g, PApplet.max(totalWidth, (int) constraint.minSize.x), PApplet.max(maxHeight, (int) constraint.minSize.y));
                spacing = 0;
                offset = (g.width - totalWidth) / 2;
            }
            case EQUAL_SPACING -> {
                g = clearG(g, (int) constraint.maxSize.x, PApplet.max(maxHeight, (int) constraint.minSize.y));
                spacing = (constraint.maxSize.x - totalWidth) / (children.size() + 1);
                offset = spacing;
            }
            case EQUAL_INNER_SPACING -> {
                g = clearG(g, (int) constraint.maxSize.x, PApplet.max(maxHeight, (int) constraint.minSize.y));
                spacing = children.size() > 1 ? (constraint.maxSize.x - totalWidth) / (children.size() - 1) : 0;
                offset = children.size() > 1 ? 0 : (constraint.maxSize.x - totalWidth) / 2;
            }
        }
        for (ViewInstance<View> child : children) {
            child.position = new PVector(offset, (g.height - child.g.height) / 2);
            offset += child.g.width;
            offset += spacing;
            g.image(child.g, child.position.x, child.position.y);
        }
        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        for (ViewInstance<View> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mousePressed(mouseButton, pos);
                break;
            }
        }
    }

    public void mouseReleased(int mouseButton, PVector mouse) {
        for (ViewInstance<View> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mouseReleased(mouseButton, pos);
                break;
            }
        }
    }

    public void mouseDragged(int mouseButton, PVector mouse, PVector pmouse) {
        for (ViewInstance<View> child : children) {
            PVector pos = child.mousePos(mouse);
            PVector ppos = child.mousePos(pmouse);
            if (pos != null && ppos != null) {
                child.v.mouseDragged(mouseButton, pos, ppos);
                break;
            }
        }
    }

    public void mouseWheel(int scrollCount, PVector mouse) {
        for (ViewInstance<View> child : children) {
            PVector pos = child.mousePos(mouse);
            if (pos != null) {
                child.v.mouseWheel(scrollCount, pos);
                break;
            }
        }
    }

    public boolean keyPressed(int key, int keyCode, PVector mouse) {
        for (ViewInstance<View> child : children) {
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


    public List<View> children() {
        return children.stream().map(c -> c.v).toList();
    }

    @Override
    public Iterator<View> iterator() {
        return children.stream().map(c -> c.v).iterator();
    }

    @Override
    public void forEach(Consumer action) {
        children.stream().map(c -> c.v).forEach(action);
    }

    @Override
    public Spliterator<View> spliterator() {
        return children.stream().map(c -> c.v).spliterator();
    }
}
