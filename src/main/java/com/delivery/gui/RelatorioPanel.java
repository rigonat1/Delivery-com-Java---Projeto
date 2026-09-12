package com.delivery.gui;

import com.delivery.dao.PedidoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class RelatorioPanel extends JPanel {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Restaurante", "Total de Pedidos", "Valor Total Vendido (R$)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    public RelatorioPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Total de pedidos e valor total vendido por restaurante (pedidos cancelados não entram na soma):"),
                BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnAtualizar = new JButton("Atualizar Relatório");
        btnAtualizar.addActionListener(e -> carregarTabela());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(btnAtualizar);
        add(botoes, BorderLayout.SOUTH);

        carregarTabela();
    }

    public void carregarTabela() {
        try {
            tableModel.setRowCount(0);
            for (PedidoDAO.RelatorioRestaurante r : pedidoDAO.relatorioPorRestaurante()) {
                tableModel.addRow(new Object[]{r.nomeRestaurante, r.totalPedidos,
                        String.format("%.2f", r.valorTotalVendido)});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
