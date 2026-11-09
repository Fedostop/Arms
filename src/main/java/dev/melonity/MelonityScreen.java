package dev.melonity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class MelonityScreen extends Screen {
    private static final int GREEN = 0xFF9DFF45;
    private static final int PANEL = 0xF0111412;
    private static final int PANEL_LIGHT = 0xFF191F1B;
    private static final int MUTED = 0xFF89938B;
    private static final String[] CATEGORIES = {"Combat", "Movement", "Render", "Player", "Misc", "HUD"};

    private final List<ModuleEntry> modules = new ArrayList<>();
    private int selectedCategory = 2;
    private String selectedModule = "Interface";
    private int panelX, panelY, panelW = 650, panelH = 410;

    public MelonityScreen() {
        super(Component.literal("Melonity"));
        seedModules();
    }

    private void seedModules() {
        modules.clear();
        switch (selectedCategory) {
            case 0 -> {
                modules.add(new ModuleEntry("Aim Assist", "Client-side aim display helper"));
                modules.add(new ModuleEntry("Totem Counter", "Shows remaining totems"));
                modules.add(new ModuleEntry("Crosshair", "Custom crosshair overlay"));
            }
            case 1 -> {
                modules.add(new ModuleEntry("Sprint", "Automatically keeps sprint enabled"));
                modules.add(new ModuleEntry("Safe Walk", "Visual edge warning"));
                modules.add(new ModuleEntry("Motion HUD", "Displays movement statistics"));
            }
            case 2 -> {
                modules.add(new ModuleEntry("Interface", "Main client visual settings"));
                modules.add(new ModuleEntry("Fullbright", "Brightness convenience toggle"));
                modules.add(new ModuleEntry("Coordinates", "Shows current coordinates"));
                modules.add(new ModuleEntry("FPS Counter", "Displays frame rate"));
                modules.add(new ModuleEntry("Armor HUD", "Displays equipped armor"));
                modules.add(new ModuleEntry("Potion HUD", "Displays active effects"));
            }
            case 3 -> {
                modules.add(new ModuleEntry("Auto Respawn", "Respawns after death"));
                modules.add(new ModuleEntry("Inventory HUD", "Compact inventory overview"));
            }
            case 4 -> {
                modules.add(new ModuleEntry("Notifications", "Client notification center"));
                modules.add(new ModuleEntry("Config Manager", "Configuration workflow"));
            }
            default -> {
                modules.add(new ModuleEntry("Watermark", "Melonity client watermark"));
                modules.add(new ModuleEntry("Array List", "Enabled module list"));
                modules.add(new ModuleEntry("Target HUD", "Target information panel"));
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        int sw = this.width;
        int sh = this.height;
        panelX = (sw - panelW) / 2;
        panelY = (sh - panelH) / 2;

        g.fill(0, 0, sw, sh, 0x99000000);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        g.fill(panelX, panelY, panelX + 5, panelY + panelH, GREEN);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 58, 0xFF151A16);
        g.fill(panelX + 1, panelY + 57, panelX + panelW - 1, panelY + 58, 0xFF303A32);

        // Brand and header
        g.fill(panelX + 18, panelY + 13, panelX + 56, panelY + 51, GREEN);
        g.drawCenteredString(font, "M", panelX + 37, panelY + 25, 0xFF101510);
        g.drawString(font, "MELONITY", panelX + 68, panelY + 17, 0xFFFFFFFF);
        g.drawString(font, "CLIENT  /  1.21.11", panelX + 68, panelY + 33, MUTED);
        g.drawString(font, "RIGHT SHIFT", panelX + panelW - 91, panelY + 25, GREEN);

        // Navigation
        int navX = panelX + 16;
        int navY = panelY + 75;
        g.fill(navX, navY, navX + 145, panelY + panelH - 17, 0xFF141915);
        g.drawString(font, "CATEGORIES", navX + 12, navY + 12, MUTED);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = navY + 34 + i * 43;
            boolean active = i == selectedCategory;
            if (active) {
                g.fill(navX + 6, y - 7, navX + 139, y + 27, 0xFF263525);
                g.fill(navX + 6, y - 7, navX + 9, y + 27, GREEN);
            }
            g.drawString(font, CATEGORIES[i], navX + 18, y + 4, active ? GREEN : 0xFFB6BDB7);
        }

        // Module list
        int listX = panelX + 176;
        int listY = panelY + 86;
        g.drawString(font, CATEGORIES[selectedCategory].toUpperCase(), listX, panelY + 73, GREEN);
        g.drawString(font, "Select a module to view its settings", listX, panelY + 88, MUTED);

        for (int i = 0; i < modules.size(); i++) {
            ModuleEntry entry = modules.get(i);
            int y = listY + 28 + i * 48;
            boolean active = entry.enabled;
            g.fill(listX, y, listX + 270, y + 40, entry.name.equals(selectedModule) ? 0xFF222B23 : PANEL_LIGHT);
            g.drawString(font, entry.name, listX + 12, y + 7, 0xFFFFFFFF);
            g.drawString(font, entry.description, listX + 12, y + 22, MUTED);
            int toggleColor = active ? GREEN : 0xFF4A554C;
            g.fill(listX + 235, y + 12, listX + 257, y + 27, toggleColor);
            g.fill(listX + (active ? 247 : 237), y + 14, listX + (active ? 255 : 245), y + 25, 0xFFFFFFFF);
        }

        // Settings panel
        int settingsX = panelX + 464;
        int settingsY = panelY + 86;
        g.fill(settingsX, settingsY, panelX + panelW - 17, panelY + panelH - 18, 0xFF141915);
        g.drawString(font, "SETTINGS", settingsX + 13, settingsY + 13, GREEN);
        g.drawString(font, selectedModule, settingsX + 13, settingsY + 39, 0xFFFFFFFF);
        g.drawString(font, "Module options will appear here.", settingsX + 13, settingsY + 59, MUTED);
        g.drawString(font, "Build 0.1.0", settingsX + 13, panelY + panelH - 34, MUTED);

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int navX = panelX + 16;
        int navY = panelY + 75;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int y = navY + 34 + i * 43;
            if (mouseX >= navX + 6 && mouseX <= navX + 139 && mouseY >= y - 7 && mouseY <= y + 27) {
                selectedCategory = i;
                seedModules();
                selectedModule = modules.isEmpty() ? "" : modules.get(0).name;
                return true;
            }
        }

        int listX = panelX + 176;
        int listY = panelY + 86;
        for (int i = 0; i < modules.size(); i++) {
            int y = listY + 28 + i * 48;
            if (mouseX >= listX && mouseX <= listX + 270 && mouseY >= y && mouseY <= y + 40) {
                ModuleEntry entry = modules.get(i);
                selectedModule = entry.name;
                if (mouseX >= listX + 225) entry.enabled = !entry.enabled;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ModuleEntry {
        private final String name;
        private final String description;
        private boolean enabled;

        private ModuleEntry(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}
