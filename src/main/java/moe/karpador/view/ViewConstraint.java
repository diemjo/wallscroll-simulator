package moe.karpador.view;

import processing.core.PVector;

public class ViewConstraint {

    public final PVector maxSize;
    public final PVector minSize;

    private ViewConstraint(PVector minSize, PVector maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public static ViewConstraint max(PVector max) {
        if (max.x < 0 || max.y < 0) {
            throw new IllegalArgumentException("max constraint must not be negative: "+max);
        }
        return new ViewConstraint(new PVector(0, 0), max);
    }

    public static ViewConstraint with(PVector min, PVector max) {
        if (min.x < 0 || min.y < 0) {
            throw new IllegalArgumentException("min constraint must not be negative: max="+max);
        }
        if (max.x < min.x || max.y < min.y) {
            throw new IllegalArgumentException("max constraint must must be bigger than min: min="+min+" max="+max);
        }
        return new ViewConstraint(min, max);
    }

    public static ViewConstraint exact(PVector size) {
        if (size.x < 0 || size.y < 0) {
            throw new IllegalArgumentException("size constraint must not be negative: "+size);
        }
        return new ViewConstraint(size, size);
    }

    public boolean matchesSize(PVector size) {
        return  (int) size.x >= (int) minSize.x && (int) size.x <= (int) maxSize.x &&
                (int) size.y >= (int) minSize.y && (int) size.y <= (int) maxSize.y;
    }

    @Override
    public String toString() {
        return "Constraint[min="+minSize+", max="+maxSize+"]";
    }
}
