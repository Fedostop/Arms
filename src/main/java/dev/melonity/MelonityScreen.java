package dev.melonity;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MelonityScreen extends Screen {
    private static final int GREEN = 0xFF9DFF45, BG = 0xF0101311;
    private static final int SURFACE = 0xFF171C18, HOVER = 0xFF222B23, MUTED = 0xFF89948B;
    private static final Identifier FONT = Identifier.of("melonity", "oxanium");
    private static final String[] CATEGORIES = {"Combat", "Movement", "Render", "Player", "Misc", "HUD"};

    private final List<ModuleEntry> modules = new ArrayList<>();
    private int category = 2, scroll, px, py, pw, ph;
    private int navX, navY, navW, contentX, contentRight, listRight, listTop, listBottom;
    private boolean settingsVisible, searchFocused;
    private String selected = "Interface", search = "";

    public MelonityScreen() { super(Text.literal("Melonity Client")); seed(); }

    private void seed() {
        modules.clear();
        switch (category) {
            case 0 -> { add("Totem Counter", "Counts totems in your inventory", "Display"); add("Hit Sounds", "Hit confirmation audio", "Audio"); add("Crosshair", "Crosshair appearance", "Visual"); add("Target Info", "Current target information", "HUD"); }
            case 1 -> { add("Sprint", "Automatically holds sprint", "Movement"); add("Motion Info", "Movement speed and direction", "HUD"); add("Elytra Info", "Elytra flight information", "HUD"); }
            case 2 -> { add("Interface", "Client theme and accent options", "Client"); add("Coordinates", "Current world coordinates", "HUD"); add("FPS Counter", "Current frame rate", "HUD"); add("Armor HUD", "Equipped armor display", "HUD"); add("Potion HUD", "Active status effects", "HUD"); add("Crosshair", "Custom crosshair appearance", "Visual"); add("Item Tags", "Names above dropped items", "Visual"); }
            case 3 -> { add("Auto Respawn", "Respawn after death", "Player"); add("Inventory View", "Compact inventory information", "HUD"); add("Totem Counter", "Counts inventory totems", "Display"); }
            case 4 -> { add("Notifications", "Client notification center", "Client"); add("Config Manager", "Configuration profiles", "Client"); add("ClickGUI", "Menu appearance and behavior", "Client"); }
            default -> { add("Watermark", "Client name and version", "HUD"); add("Array List", "Enabled module list", "HUD"); add("Target HUD", "Target health and equipment", "HUD"); add("Keybinds", "Active module keybinds", "HUD"); }
        }
        selected = modules.isEmpty() ? "" : modules.get(0).name;
        scroll = 0;
    }

    private void add(String name, String description, String group) { modules.add(new ModuleEntry(name, description, group)); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // A deliberately compact window; all dimensions are derived from the actual screen.
        pw = Math.min(640, Math.max(220, width - 24));
        ph = Math.min(400, Math.max(220, height - 24));
        px = (width - pw) / 2;
        py = (height - ph) / 2;

        context.fill(0, 0, width, height, 0x99000000);
        context.fill(px, py, px + pw, py + ph, BG);
        context.fill(px, py, px + 3, py + ph, GREEN);
        context.fill(px + 1, py + 1, px + pw - 1, py + 53, 0xFF151A16);
        context.fill(px + 1, py + 52, px + pw - 1, py + 53, 0xFF303A32);

        context.fill(px + 14, py + 10, px + 46, py + 42, GREEN);
        drawCentered(context, "M", px + 30, py + 21, 0xFF101510);
        draw(context, "MELONITY", px + 56, py + 13, 0xFFFFFFFF, true);
        draw(context, "CLIENT  /  1.21.11", px + 56, py + 29, MUTED, false);
        draw(context, "RIGHT SHIFT", px + pw - 82, py + 22, GREEN, false);

        navX = px + 12;
        navY = py + 64;
        navW = Math.min(112, Math.max(76, pw / 4));
        int bodyBottom = py + ph - 12;
        context.fill(navX, navY, navX + navW, bodyBottom, 0xFF141915);
        draw(context, "MENU", navX + 9, navY + 8, MUTED, false);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = navY + 27 + i * 31;
            boolean active = i == category;
            if (active) {
                context.fill(navX + 4, y - 5, navX + navW - 4, y + 20, 0xFF263525);
                context.fill(navX + 4, y - 5, navX + 7, y + 20, GREEN);
            }
            drawFit(context, CATEGORIES[i], navX + 12, y + 2, navW - 22, active ? GREEN : 0xFFB6BDB7, false);
        }

        contentX = navX + navW + 12;
        contentRight = px + pw - 12;
        int contentWidth = Math.max(0, contentRight - contentX);
        settingsVisible = contentWidth >= 390;
        int settingsWidth = settingsVisible ? 164 : 0;
        int panelGap = settingsVisible ? 10 : 0;
        int listWidth = Math.max(0, contentWidth - settingsWidth - panelGap);
        listRight = contentX + listWidth;

        int top = py + 65;
        draw(context, CATEGORIES[category].toUpperCase(Locale.ROOT), contentX, top, GREEN, false);
        draw(context, "MODULES  /  " + modules.size(), contentX, top + 14, MUTED, false);

        int searchY = top + 29;
        context.fill(contentX, searchY, listRight, searchY + 23, SURFACE);
        drawFit(context, search.isEmpty() ? "Search modules..." : search, contentX + 8, searchY + 7,
                Math.max(0, listWidth - 16), search.isEmpty() ? MUTED : 0xFFFFFFFF, false);
        if (searchFocused && listWidth > 4) context.fill(contentX + 2, searchY + 21, listRight - 2, searchY + 23, GREEN);

        listTop = searchY + 29;
        listBottom = bodyBottom - 2;
        List<ModuleEntry> visible = filtered();
        int rowHeight = 33, rowGap = 5, rowStep = rowHeight + rowGap;
        int visibleRows = Math.max(1, (listBottom - listTop + rowGap) / rowStep);
        int maxScroll = Math.max(0, visible.size() - visibleRows);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        if (listWidth > 0 && listBottom > listTop) {
            context.enableScissor(contentX, listTop, listRight, listBottom);
            for (int i = 0; i < visible.size(); i++) {
                int y = listTop + i * rowStep - scroll * rowStep;
                if (y + rowHeight < listTop || y > listBottom) continue;
                ModuleEntry module = visible.get(i);
                boolean hovered = mouseX >= contentX && mouseX <= listRight && mouseY >= y && mouseY <= y + rowHeight;
                context.fill(contentX, y, listRight, y + rowHeight,
                        module.name.equals(selected) || hovered ? HOVER : SURFACE);
                drawFit(context, module.name, contentX + 8, y + 5, Math.max(0, listWidth - 54), 0xFFFFFFFF, false);
                drawFit(context, module.description, contentX + 8, y + 18, Math.max(0, listWidth - 54), MUTED, false);
                int toggleX = listRight - 31;
                context.fill(toggleX, y + 9, toggleX + 23, y + 24, module.enabled ? GREEN : 0xFF465047);
                context.fill(toggleX + (module.enabled ? 13 : 2), y + 11,
                        toggleX + (module.enabled ? 21 : 10), y + 22, 0xFFFFFFFF);
            }
            context.disableScissor();
        }

        if (settingsVisible) renderSettings(context, top, contentRight, listRight + panelGap, bodyBottom, settingsWidth);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSettings(DrawContext context, int top, int right, int left, int bottom, int panelWidth) {
        context.fill(left, top, right, bottom, 0xFF141915);
        draw(context, "SETTINGS", left + 10, top + 10, GREEN, false);
        ModuleEntry module = findSelected();
        if (module == null) {
            draw(context, "Select a module", left + 10, top + 32, MUTED, false);
        } else {
            drawFit(context, module.name, left + 10, top + 31, panelWidth - 20, 0xFFFFFFFF, false);
            wrap(context, module.description, left + 10, top + 49, panelWidth - 20, MUTED, 3);
            draw(context, "CATEGORY", left + 10, top + 101, MUTED, false);
            drawFit(context, module.group, left + 10, top + 114, panelWidth - 20, 0xFFFFFFFF, false);
            draw(context, "STATE", left + 10, top + 139, MUTED, false);
            draw(context, module.enabled ? "ENABLED" : "DISABLED", left + 10, top + 152,
                    module.enabled ? GREEN : 0xFFB6BDB7, false);
        }
        draw(context, "0.1.1  /  FABRIC", left + 10, bottom - 15, MUTED, false);
    }

    private void wrap(DrawContext context, String value, int x, int y, int maxWidth, int color, int maxLines) {
        if (maxWidth <= 0) return;
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : value.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (widthOf(candidate) > maxWidth && !line.isEmpty()) {
                draw(context, line.toString(), x, y, color, false);
                y += 11;
                if (++lines >= maxLines) return;
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty() && lines < maxLines) drawFit(context, line.toString(), x, y, maxWidth, color, false);
    }

    private List<ModuleEntry> filtered() {
        if (search.isBlank()) return modules;
        String query = search.toLowerCase(Locale.ROOT);
        return modules.stream().filter(module ->
                module.name.toLowerCase(Locale.ROOT).contains(query)
                        || module.description.toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private ModuleEntry findSelected() {
        return modules.stream().filter(module -> module.name.equals(selected)).findFirst().orElse(null);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.buttonInfo().button() != 0) return super.mouseClicked(click, doubled);
        double mouseX = click.x(), mouseY = click.y();

        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = navY + 27 + i * 31;
            if (hit(mouseX, mouseY, navX + 4, y - 5, navX + navW - 4, y + 20)) {
                category = i;
                seed();
                search = "";
                searchFocused = false;
                return true;
            }
        }

        int searchY = py + 65 + 29;
        searchFocused = hit(mouseX, mouseY, contentX, searchY, listRight, searchY + 23);
        if (searchFocused) return true;

        if (mouseX >= contentX && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            List<ModuleEntry> visible = filtered();
            int rowStep = 38;
            int index = (int) ((mouseY - listTop + scroll * rowStep) / rowStep);
            if (index >= 0 && index < visible.size()) {
                ModuleEntry module = visible.get(index);
                int rowY = listTop + index * rowStep - scroll * rowStep;
                if (mouseY <= rowY + 33) {
                    selected = module.name;
                    if (mouseX >= listRight - 39) module.enabled = !module.enabled;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (searchFocused && input.key() == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused) { searchFocused = false; return true; }
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused && !Character.isISOControl(input.codepoint()) && search.length() < 32) {
            search += Character.toString(input.codepoint());
            scroll = 0;
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX >= contentX && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            int rowStep = 38;
            int visibleRows = Math.max(1, (listBottom - listTop + 5) / rowStep);
            scroll = Math.max(0, Math.min(Math.max(0, filtered().size() - visibleRows),
                    scroll - (int) Math.signum(vertical)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private Text styled(String value) { return Text.literal(value).setStyle(Style.EMPTY.withFont(FONT)); }
    private int widthOf(String value) { return textRenderer.getWidth(styled(value)); }
    private void draw(DrawContext context, String value, int x, int y, int color, boolean shadow) {
        context.drawText(textRenderer, styled(value), x, y, color, shadow);
    }
    private void drawCentered(DrawContext context, String value, int x, int y, int color) {
        context.drawCenteredTextWithShadow(textRenderer, styled(value), x, y, color);
    }
    private void drawFit(DrawContext context, String value, int x, int y, int maxWidth, int color, boolean shadow) {
        if (maxWidth <= 0) return;
        String result = value;
        while (!result.isEmpty() && widthOf(result) > maxWidth) result = result.substring(0, result.length() - 1);
        if (!result.equals(value) && result.length() > 2) result = result.substring(0, result.length() - 2) + "..";
        if (!result.isEmpty()) draw(context, result, x, y, color, shadow);
    }
    private boolean hit(double x, double y, int x1, int y1, int x2, int y2) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }
    @Override public boolean shouldPause() { return false; }

    private static final class ModuleEntry {
        final String name, description, group;
        boolean enabled;
        ModuleEntry(String name, String description, String group) {
            this.name = name; this.description = description; this.group = group;
        }
    }
}
