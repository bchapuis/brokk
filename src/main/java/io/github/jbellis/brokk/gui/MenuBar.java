package io.github.jbellis.brokk.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class MenuBar {
    /**
     * Builds the menu bar
     * @param chrome
     */
    static JMenuBar buildMenuBar(Chrome chrome) {
        var menuBar = new JMenuBar();

        // File menu
        var fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        var editKeysItem = new JMenuItem("Edit secret keys");
        editKeysItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK));
        editKeysItem.addActionListener(e -> {
            chrome.showSecretKeysDialog();
            if (chrome.contextManager != null) {
                // Reopen the current project to create new Models and Coder with updated keys
                var currentPath = chrome.contextManager.getProject().getRoot();
                if (currentPath != null) {
                    io.github.jbellis.brokk.Brokk.openProject(currentPath);
                }
            }
        });
        fileMenu.add(editKeysItem);

        fileMenu.addSeparator();

        var refreshItem = new JMenuItem("Refresh Code Intelligence");
        refreshItem.addActionListener(e -> {
            chrome.contextManager.requestRebuild();
            chrome.systemOutput("Code intelligence will refresh in the background");
        });
        fileMenu.add(refreshItem);

        fileMenu.addSeparator();

        var openProjectItem = new JMenuItem("Open Project...");
        openProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openProjectItem.addActionListener(e -> {
            // Use a directory chooser
            var chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select a Git project directory");
            int result = chooser.showOpenDialog(chrome.frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                var dir = chooser.getSelectedFile().toPath();
                io.github.jbellis.brokk.Brokk.openProject(dir);
            }
        });
        fileMenu.add(openProjectItem);

        var recentProjectsMenu = new JMenu("Recent Projects");
        fileMenu.add(recentProjectsMenu);
        rebuildRecentProjectsMenu(recentProjectsMenu);

        menuBar.add(fileMenu);

        // Edit menu
        var editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        var undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            chrome.disableUserActionButtons();
            chrome.disableContextActionButtons();
            chrome.currentUserTask = chrome.contextManager.undoContextAsync();
        });
        editMenu.add(undoItem);

        var redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                                       InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        redoItem.addActionListener(e -> {
            chrome.disableUserActionButtons();
            chrome.disableContextActionButtons();
            chrome.currentUserTask = chrome.contextManager.redoContextAsync();
        });
        editMenu.add(redoItem);

        editMenu.addSeparator();

        var copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyMenuItem.addActionListener(e -> {
            var selectedFragments = chrome.getSelectedFragments();
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(Chrome.ContextAction.COPY, selectedFragments);
        });
        editMenu.add(copyMenuItem);

        var pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteMenuItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(Chrome.ContextAction.PASTE, List.of());
        });
        editMenu.add(pasteMenuItem);

        menuBar.add(editMenu);

        // Context menu
        var contextMenu = new JMenu("Context");
        contextMenu.setMnemonic(KeyEvent.VK_C);

        var editFilesItem = new JMenuItem("Edit Files");
        editFilesItem.setMnemonic(KeyEvent.VK_D);
        editFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
        editFilesItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(
                    Chrome.ContextAction.EDIT, List.of());
        });
        contextMenu.add(editFilesItem);

        var readFilesItem = new JMenuItem("Read Files");
        readFilesItem.setMnemonic(KeyEvent.VK_R);
        readFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        readFilesItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(
                    Chrome.ContextAction.READ, List.of());
        });
        contextMenu.add(readFilesItem);

        var summarizeFilesItem = new JMenuItem("Summarize Files");
        summarizeFilesItem.setMnemonic(KeyEvent.VK_M);
        summarizeFilesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));
        summarizeFilesItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(
                    Chrome.ContextAction.SUMMARIZE, List.of());
        });
        contextMenu.add(summarizeFilesItem);

        var symbolUsageItem = new JMenuItem("Symbol Usage");
        symbolUsageItem.setMnemonic(KeyEvent.VK_Y);
        symbolUsageItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.ALT_DOWN_MASK));
        symbolUsageItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.findSymbolUsageAsync();
        });
        contextMenu.add(symbolUsageItem);

        var callersItem = new JMenuItem("Call graph to function");
        callersItem.setMnemonic(KeyEvent.VK_C);
        callersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        callersItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.findMethodCallersAsync();
        });
        contextMenu.add(callersItem);

        var calleesItem = new JMenuItem("Call graph from function");
        calleesItem.setMnemonic(KeyEvent.VK_L);
        calleesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK));
        calleesItem.addActionListener(e -> {
            chrome.currentUserTask = chrome.contextManager.findMethodCalleesAsync();
        });
        contextMenu.add(calleesItem);

        var dropAllItem = new JMenuItem("Drop All");
        dropAllItem.setMnemonic(KeyEvent.VK_P);
        dropAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
        dropAllItem.addActionListener(e -> {
            chrome.disableContextActionButtons();
            chrome.currentUserTask = chrome.contextManager.performContextActionAsync(
                    Chrome.ContextAction.DROP, List.of());
        });
        contextMenu.add(dropAllItem);

        menuBar.add(contextMenu);

        // Help menu
        var helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        // Theme submenu
        var themeMenu = new JMenu("Theme");
        themeMenu.setMnemonic(KeyEvent.VK_T);
        
        var lightThemeItem = new JMenuItem("Light");
        lightThemeItem.addActionListener(e -> chrome.switchTheme(false));
        lightThemeItem.setToolTipText("Switch to light theme");

        var darkThemeItem = new JMenuItem("Dark");
        darkThemeItem.addActionListener(e -> chrome.switchTheme(true));
        darkThemeItem.setToolTipText("Switch to dark theme");
        
        themeMenu.add(lightThemeItem);
        themeMenu.add(darkThemeItem);
        helpMenu.add(themeMenu);
        
        helpMenu.addSeparator();
        
        var aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(chrome.frame,
                                          "Brokk Swing UI\nVersion X\n...",
                                          "About Brokk",
                                          JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Rebuilds the Recent Projects submenu using up to 5 from Project.loadRecentProjects(),
     * sorted by lastOpened descending.
     */
    private static void rebuildRecentProjectsMenu(JMenu recentMenu) {
        recentMenu.removeAll();

        var map = io.github.jbellis.brokk.Project.loadRecentProjects();
        if (map.isEmpty()) {
            var emptyItem = new JMenuItem("(No Recent Projects)");
            emptyItem.setEnabled(false);
            recentMenu.add(emptyItem);
            return;
        }

        var sorted = map.entrySet().stream()
            .sorted((a,b)-> Long.compare(b.getValue(), a.getValue()))
            .limit(5)
            .toList();

        for (var entry : sorted) {
            var path = entry.getKey();
            var item = new JMenuItem(path);
            item.addActionListener(e -> {
                io.github.jbellis.brokk.Brokk.openProject(java.nio.file.Path.of(path));
            });
            recentMenu.add(item);
        }
    }
}
