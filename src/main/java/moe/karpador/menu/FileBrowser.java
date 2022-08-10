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
                        .map(w -> new WallscrollEntry(w, textSize, () -> roomView.placeWallscroll(w.copy())))
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
                        .map(pa -> new ConfigEntry(pa, textSize, () -> roomView.loadWallscrolls(pa)))
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
                            case B2P -> titleBar.view.checked("B2") && titleBar.view.checked("portrait");
                            case B2L -> titleBar.view.checked("B2") && titleBar.view.checked("landscape");
                            case B1P -> titleBar.view.checked("B1") && titleBar.view.checked("portrait");
                            case B1L -> titleBar.view.checked("B1") && titleBar.view.checked("landscape");
                            case B0P -> titleBar.view.checked("B0") && titleBar.view.checked("portrait");
                            case LONG -> titleBar.view.checked("Long") && titleBar.view.checked("portrait");
                        })
                        .filter(w -> switch (w.wallscroll.rating) {
                            case SAFE -> true;
                            case EXPLICIT -> !titleBar.view.checked("Only safe");
                        })
                        .map(w -> (View) w)
                        .toList();
                grid.view.setEntries(currentWallscrolls);
            }
            case ConfigBrowser -> {
                List<View> currentConfigs = configs.stream()
                        .map(c -> (View) c)
                        .toList();
                grid.view.setEntries(currentConfigs);
            }
        }
    }

    @Override
    protected PGraphics build(ViewConstraint constraint) {
        float width = max(constraint.minSize.x, constraint.maxSize.x * 4 / 5);
        float height = max(constraint.minSize.y, constraint.maxSize.y * 4 / 5);
        float titleHeight = titleBar.view.textSize*3;
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
    public boolean update(long time, int mouseX, int mouseY) {
        PVector tPos = titleBar.mousePos(mouseX, mouseY);
        if (titleBar.view.update(time, (int) tPos.x, (int) tPos.y)) {
            loadEntries();
            modified();
        }
        PVector gPos = grid.mousePos(mouseX, mouseY);
        if (grid.view.update(time, (int) gPos.x, (int) gPos.y)) {
            modified();
        }
        return super.update(time, mouseX, mouseY);
    }

    @Override
    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == LEFT) {
            if (mouseX >= 0 && mouseX < g.width && mouseY >= 0 && mouseY < g.height) {
                if (titleBar.hover(mouseX, mouseY)) {
                    PVector pos = titleBar.mousePos(mouseX, mouseY);
                    titleBar.view.mousePressed(mouseButton, (int) pos.x, (int) pos.y);
                }
                if (grid.hover(mouseX, mouseY)) {
                    PVector pos = grid.mousePos(mouseX, mouseY);
                    grid.view.mousePressed(mouseButton, (int) pos.x, (int) pos.y);
                }
            } else {
                WallscrollSimulator.popView();
            }
        }
    }

    @Override
    public void mouseWheel(MouseEvent e) {
        if (grid.hover(mouseX, mouseY)) {
            grid.view.mouseWheel(e);
        }
    }

    public enum BrowserType {
        WallscrollBrowser,
        ConfigBrowser,
    }
}
