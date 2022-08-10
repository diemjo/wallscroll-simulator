package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.*;
import processing.opengl.PGraphics2D;

import java.nio.file.Path;

public class ConfigEntry extends View {
    public final Path config;
    private final int textSize;
    private final Runnable func;

    public ConfigEntry(Path config, int textSize, Runnable func) {
        super();
        this.config = config;
        this.textSize = textSize;
        this.func = func;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        int textBoxHeight = (int) (3f * textSize + 4);
        int width = (int) constraint.minSize.x;
        int height = (int) PApplet.max(width + textBoxHeight, constraint.minSize.y);
        g = clearG(g, width, height);
        g.beginDraw();

        g.noFill();
        g.strokeWeight(2);
        g.rect(1, 1, width-2, width-1);
        g.rect(1, height-textBoxHeight, width-2, textBoxHeight-1);

        g.fill(0);
        g.textAlign(PConstants.CENTER, PConstants.CENTER);
        g.textSize(textSize*3);
        g.text("config", width / 2, (height - textBoxHeight) / 2);

        g.textAlign(PConstants.LEFT, PConstants.TOP);
        g.textSize(textSize);
        g.text(config.getFileName().toString(), 2, height-textBoxHeight + 2, width - 4, textBoxHeight - 4);

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == PConstants.LEFT && mouseX >= 0 && mouseX < g.width && mouseY >= 0 && mouseY < g.height) {
            func.run();
        }
    }
}
