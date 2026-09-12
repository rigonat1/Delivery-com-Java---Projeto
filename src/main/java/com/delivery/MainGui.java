package com.delivery;

import com.delivery.gui.MainFrame;

import javax.swing.*;

/**
 * Ponto de entrada da versão com interface gráfica (Swing).
 * Reaproveita exatamente as mesmas classes de model/dao/service
 * do Main.java (CLI) — só a "camada de apresentação" é diferente.
 */
public class MainGui {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // se falhar, segue com o look and feel padrão do Swing
            }
            new MainFrame().setVisible(true);
        });
    }
}
