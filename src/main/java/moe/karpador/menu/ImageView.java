package moe.karpador.menu;

import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class ImageView extends View {
    private final PImage image;

    public ImageView(PImage image) {
        this.image = image;
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float imgratio = (float) image.height / (float) image.width;
        float slotratio = constraint.maxSize.y / constraint.maxSize.x;

        int width = (int) (imgratio >= slotratio ? (image.width * constraint.maxSize.y / image.height) : constraint.maxSize.x);
        int height = (int) (imgratio >= slotratio ? constraint.maxSize.y : (image.height * constraint.maxSize.x / image.width));
        int gwidth = PApplet.max(width, (int) constraint.minSize.x);
        int gheight = PApplet.max(height, (int) constraint.minSize.y);

        g = clearG(g, gwidth, gheight);
        g.image(image, (gwidth - width) / 2f, (gheight - height) / 2f, width, height);

        g.endDraw();
        return g;
    }
}
