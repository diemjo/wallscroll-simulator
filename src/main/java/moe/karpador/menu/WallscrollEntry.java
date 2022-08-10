package moe.karpador.menu;

import com.jogamp.opengl.util.texture.spi.LEDataInputStream;
import moe.karpador.WallscrollSimulator;
import moe.karpador.room.Room;
import moe.karpador.room.Wallscroll;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.*;

import java.nio.file.Path;

public class WallscrollEntry extends View {
    public final Wallscroll wallscroll;
    private final int textSize;
    private final Runnable func;

    public WallscrollEntry(Wallscroll wallscroll, int textSize, Runnable func) {
        super();
        this.wallscroll = wallscroll;
        this.textSize = textSize;
        this.func = func;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        int textBoxHeight = (int) (1.5f * textSize + 4);
        int width = (int) constraint.minSize.x;
        int height = (int) PApplet.max(width + textBoxHeight, constraint.minSize.y);
        g = clearG(g, width, height);
        g.beginDraw();

        g.noFill();
        g.strokeWeight(2);
        g.rect(1, 1, width-2, width-1);
        g.rect(1, height-textBoxHeight, width-2, textBoxHeight-1);

        float imgratio = (float) wallscroll.image.height / (float) wallscroll.image.width;
        float slotratio = (height - textBoxHeight) / width;
        if (imgratio >= slotratio) {
            int newWidth = (int) (width * (slotratio / imgratio));
            g.image(wallscroll.image, (width - newWidth) / 2, 0, newWidth, height - textBoxHeight);
        } else {
            int newHeight = (int) ((height - textBoxHeight) * (imgratio / slotratio));
            g.image(wallscroll.image, 0, (height - textBoxHeight - newHeight) / 2, width, newHeight);
        }

        g.textAlign(PConstants.CENTER, PConstants.TOP);
        g.textSize(textSize);
        g.fill(0);
        g.text(wallscroll.path.getFileName().toString(), 2, height-textBoxHeight + 2, width - 4, textBoxHeight - 4);

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
