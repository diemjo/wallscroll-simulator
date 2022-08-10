package moe.karpador.room;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Furniture {
    public final String name;
    public PVector position;
    public PVector size;
    int color;

    public Furniture(String name) {
        this.name = name;
        this.color = FurnitureColor.getColor(name);
        this.position = new PVector();
        this.size = new PVector();
    }

    static Furniture fromYaml(Map<String, Object> root) throws ClassCastException {
        Furniture furniture = new Furniture((String) root.get("name"));
        List<Integer> pos = (List<Integer>) root.get("position");
        if (pos.size()!=3)
            throw new IllegalArgumentException("position must contain three integer values [cm]");
        furniture.position.set(pos.get(0), pos.get(1), pos.get(2));
        ArrayList<Integer> size = (ArrayList<Integer>) root.get("size");
        if (pos.size()!=3)
            throw new IllegalArgumentException("furniture size must contain three integer values [cm]");
        furniture.size.set(size.get(0), size.get(1), size.get(2));
        return furniture;
    }

    public void draw(PGraphics g) {
        g.fill(color);
        g.pushMatrix();
        g.translate(position.x+size.x/2, position.y+size.y/2, position.z+size.z/2);
        g.box(size.x, size.y, size.z);
        g.popMatrix();
    }

    static class FurnitureColor {
        private static int id = 0;
        private static final HashMap<String, Integer> colors = new HashMap<>();

        static int getColor(String name) {
            Integer id = colors.get(name);
            if (id==null) {
                colors.put(name, FurnitureColor.id++);
                id = FurnitureColor.id;
            }
            return Color.HSBtoRGB(id * 0.38f, 0.5f, 0.8f);
        }
    }
}
