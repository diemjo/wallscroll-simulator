package moe.karpador.menu;

import moe.karpador.room.Wallscroll;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.*;

public class WallscrollEntry extends View {
    public final Wallscroll wallscroll;
    private final ViewInstance<ImageView> image;
    private final ViewInstance<TextView> name;

    public WallscrollEntry(Wallscroll wallscroll, int textSize) {
        super();
        this.wallscroll = wallscroll;
        this.image = new ViewInstance<>(new ImageView(wallscroll.image));
        this.name = new ViewInstance<>(new TextView(wallscroll.path.getFileName().toString(), textSize));
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
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
