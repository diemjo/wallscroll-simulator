package moe.karpador.menu;

import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.List;

import static processing.core.PConstants.LEFT;

public class EntryGrid<T extends View> extends View {

    final int cols;
    private List<ViewInstance<T>> entries;
    private PGraphics gridG;
    private boolean entriesChanged;
    private float scrollOffset = 0f;
    private static final float scrollBarWidth = 10f;

    public EntryGrid(int cols) {
        this.cols = cols;
        this.entriesChanged = true;
    }

    public void setEntries(List<T> entries) {
        this.entries = entries.stream().map(ViewInstance::new).toList();
        this.scrollOffset = 0f;
        this.entriesChanged = true;
        modified();
    }

    private PGraphics buildEntryGrid(ViewConstraint constraint) {
        if (entries.size() == 0) {
            gridG = clearG(gridG, (int) constraint.minSize.x, (int) constraint.minSize.y);
            return gridG;
        }
        float width = constraint.maxSize.x / cols;
        float totalHeight = 0;
        for (int i = 0; i < entries.size(); i+=cols) {
            float maxHeight = 0;
            for (int col = 0; col < cols && i + col < entries.size(); col++) {
                entries.get(i + col).draw(ViewConstraint.with(new PVector(width, 0), new PVector(width, constraint.maxSize.y)));
                maxHeight = PApplet.max(maxHeight, entries.get(i + col).g.height);
            }
            for (int col = 0; col < cols && i + col < entries.size(); col++) {
                entries.get(i + col).position = new PVector(col * width, totalHeight);
            }
            totalHeight += maxHeight;
        }

        gridG = clearG(gridG, (int) constraint.maxSize.x, (int) totalHeight);
        gridG.beginDraw();
        for(ViewInstance<T> entry : entries) {
            gridG.image(entry.g, entry.position.x, entry.position.y);
        }
        gridG.endDraw();
        return gridG;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        if (entriesChanged) {
            gridG = buildEntryGrid(ViewConstraint.with(
                    new PVector(PApplet.max(0, constraint.minSize.x - scrollBarWidth), constraint.minSize.y),
                    new PVector(PApplet.max(0, constraint.maxSize.x - scrollBarWidth), constraint.maxSize.y)));
            entriesChanged = false;
        }
        g = clearG(g, (int) (gridG.width + scrollBarWidth), (int) constraint.maxSize.y);

        g.image(gridG, 0, -scrollOffset);

        if (g.height < gridG.height) {
            float scrollDrawHeight = g.height * ((float)g.height/gridG.height);
            float scrollDrawOffset = (g.height - scrollDrawHeight) * (scrollOffset / (gridG.height - g.height));
            g.strokeWeight(2);
            g.stroke(0);
            g.fill(255);
            g.rect(gridG.width, scrollDrawOffset, scrollBarWidth, scrollDrawHeight);
        }

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == LEFT) {
            float mouseY = (mouse.y + scrollOffset);
            for(ViewInstance<T> ei : entries) {
                PVector offsetPos = new PVector(mouse.x, mouseY);
                if (ei.hover(offsetPos)) {
                    PVector pos = ei.mousePos(offsetPos);
                    ei.v.mousePressed(mouseButton, pos);
                    break;
                }
            }
        }
    }

    @Override
    public boolean update(long time) {
        for (ViewInstance<T> ei : entries) {
            if (ei.v.update(time)) {
                modified();
            }
        }
        return super.update(time);
    }

    @Override
    public void mouseWheel(int scrollCount, PVector mouse) {
        if (gridG==null || g==null)
            return;
        if (gridG.height > g.height) {
            float newScrollOffset = PApplet.constrain(scrollOffset + scrollCount * gridG.height / 10, 0, gridG.height - g.height);
            if (newScrollOffset != scrollOffset) {
                scrollOffset = newScrollOffset;
                modified();
            }
        }
    }
}
