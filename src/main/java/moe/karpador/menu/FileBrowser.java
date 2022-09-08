package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.View;
import moe.karpador.room.RoomView;
import moe.karpador.room.Wallscroll;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static processing.core.PApplet.*;


public class FileBrowser extends View {
    private static final int SCROLL_BAR_WIDTH = 15;
    private static final int COLS = 8;

    private final BrowserType type;

    private final List<ConfigEntry> configs;
    private final List<WallscrollEntry> wallscrolls;
    private final ViewInstance<EntryGrid> grid;

    private final ViewInstance<TitleBar> titleBar;


    public FileBrowser(BrowserType type, RoomView roomView) {
        super();
        this.type = type;
        int textSize = WallscrollSimulator.pathTextSize();
        this.grid = new ViewInstance<>(new EntryGrid(COLS));
        switch (type) {
            case WallscrollBrowser -> {
                this.wallscrolls = WallscrollSimulator.getWallscrolls().entrySet().stream()
                        .map(e -> new Wallscroll(Path.of(e.getKey()), e.getValue()))
                        .map(w -> new WallscrollEntry(w, textSize, () -> {
                            roomView.placeWallscroll(w.copy());
                            WallscrollSimulator.popView();
                        }))
                                .collect(Collectors.toList());
                this.configs = null;
                this.titleBar = new ViewInstance<>(new TitleBar("Select Wallscroll", WallscrollSimulator.viewTitleTextSize())
                        .withCheckbox("portrait", true)
                        .withCheckbox("landscape", true)
                        .withCheckbox("B0", true)
                        .withCheckbox("B1", true)
                        .withCheckbox("B2", true)
                        .withCheckbox("Long", true)
                        .withCheckbox("Only safe", true));
            }
            case ConfigBrowser -> {
                this.wallscrolls = null;
                WallscrollSimulator.loadConfigs();
                this.configs = WallscrollSimulator.getConfigs().stream()
                        .map(pa -> new ConfigEntry(pa, textSize, () -> {
                            roomView.loadWallscrolls(pa);
                            WallscrollSimulator.popView();
                        }))
                        .collect(Collectors.toList());
                this.titleBar = new ViewInstance<>(new TitleBar("Load Wallscroll Config", WallscrollSimulator.viewTitleTextSize()));
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    void loadEntries() {
        switch (type) {
            case WallscrollBrowser -> {
                List<View> currentWallscrolls = wallscrolls.stream()
                        .filter(w -> switch (w.wallscroll.format) {
                            case B2P -> titleBar.v.checked("B2") && titleBar.v.checked("portrait");
                            case B2L -> titleBar.v.checked("B2") && titleBar.v.checked("landscape");
                            case B1P -> titleBar.v.checked("B1") && titleBar.v.checked("portrait");
                            case B1L -> titleBar.v.checked("B1") && titleBar.v.checked("landscape");
                            case B0P -> titleBar.v.checked("B0") && titleBar.v.checked("portrait");
                            case LONG -> titleBar.v.checked("Long") && titleBar.v.checked("portrait");
                        })
                        .filter(w -> switch (w.wallscroll.rating) {
                            case SAFE -> true;
                            case EXPLICIT -> !titleBar.v.checked("Only safe");
                        })
                        .map(w -> (View) w)
                        .toList();
                grid.v.setEntries(currentWallscrolls);
            }
            case ConfigBrowser -> {
                List<View> currentConfigs = configs.stream()
                        .map(c -> (View) c)
                        .toList();
                grid.v.setEntries(currentConfigs);
            }
        }
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float width = max(constraint.minSize.x, constraint.maxSize.x * 4 / 5);
        float height = max(constraint.minSize.y, constraint.maxSize.y * 4 / 5);
        float titleHeight = titleBar.v.textSize*3;
        titleBar.draw(ViewConstraint.max(new PVector(width, titleHeight)));
        titleBar.position = new PVector(0, 0);
        grid.draw(ViewConstraint.max(new PVector(width, height - titleHeight)));
        grid.position = new PVector(0, titleHeight);

        g = clearG(g, (int) width, (int) height);
        g.beginDraw();
        g.background(255, 220, 255);
        g.image(titleBar.g, titleBar.position.x, titleBar.position.y);
        g.image(grid.g, grid.position.x, grid.position.y);

        g.endDraw();
        return g;
    }

    @Override
    public boolean update(long time) {
        if (titleBar.v.update(time)) {
            loadEntries();
            modified();
        }
        if (grid.v.update(time)) {
            modified();
        }
        return super.update(time);
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == LEFT) {
            if (titleBar.hover(mouse)) {
                PVector pos = titleBar.mousePos(mouse);
                titleBar.v.mousePressed(mouseButton, pos);
            } else if (grid.hover(mouse)) {
                PVector pos = grid.mousePos(mouse);
                grid.v.mousePressed(mouseButton, pos);
            }
        }
    }

    @Override
    public void mouseWheel(int scrollCount, PVector mouse) {
        if (grid.hover(mouse)) {
            PVector pos = grid.mousePos(mouse);
            grid.v.mouseWheel(scrollCount, pos);
        }
    }

    public enum BrowserType {
        WallscrollBrowser,
        ConfigBrowser,
    }
}
