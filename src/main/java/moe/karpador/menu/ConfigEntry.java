package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.*;
import processing.awt.PImageAWT;
import processing.core.*;

import java.nio.file.Path;
import java.util.List;

public class ConfigEntry extends View {
    public final Path config;
    private final ViewInstance<TextView> name;
    private final ViewInstance<ImageView> image;

    public ConfigEntry(Path config, int textSize) {
        super();
        this.config = config;
        this.image = new ViewInstance<>(new ImageView(WallscrollSimulator.getConfigIcon()));
        this.name = new ViewInstance<>(new TextView(config.getFileName().toString(), textSize, true));
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        //int textBoxHeight = (int) (3f * name.v.textSize + 4);
        int width = (int) constraint.minSize.x;

        name.draw(ViewConstraint.with(
                new PVector(width, 0),
                new PVector(width, constraint.maxSize.y))
        );
        int textBoxHeight = name.g.height;

        int height = (int) PApplet.min(constraint.maxSize.y, PApplet.max(width + textBoxHeight, constraint.minSize.y));
        image.draw(ViewConstraint.exact(new PVector(width - 4, height - textBoxHeight - 4)));

        g = clearG(g, width, height);
        g.beginDraw();

        g.noFill();
        g.strokeWeight(2);
        g.rect(1, 1, width-2, width-1);
        //g.rect(1, height-textBoxHeight, width-2, textBoxHeight-1);

        image.position = new PVector(2, 2);
        g.image(image.g, image.position.x, image.position.y);

        name.position = new PVector(0, height - textBoxHeight);
        g.image(name.g, name.position.x, name.position.y);

        g.endDraw();
        return g;
    }
}
