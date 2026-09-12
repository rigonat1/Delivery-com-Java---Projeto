package com.delivery.gui;

import com.delivery.dao.EntregadorDAO;
import com.delivery.model.Entregador;
import com.delivery.model.StatusEntregador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class EntregadorPanel extends JPanel {

    private final EntregadorDAO dao = new EntregadorDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Id", "Nome", "Telefone", "Veículo", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoTelefone = new JTextField(15);
    private final JTextField campoVeiculo = new JTextField(15);
    private Integer idSelecionado = null;

    public EntregadorPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                "Entregador  (o status muda automaticamente ao atribuir/concluir entregas na aba Pedidos)"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addCampo(form, gc, y++, "Nome:", campoNome);
        addCampo(form, gc, y++, "Telefone:", campoTelefone);
        addCampo(form, gc, y++, "Veículo:", campoVeiculo);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNovo = new JButton("Novo");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnAtualizar = new JButton("Atualizar Lista");
        botoes.add(btnNovo);
        botoes.add(btnSalvar);
        botoes.add(btnExcluir);
        botoes.add(btnAtualizar);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(form, BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);
        add(sul, BorderLayout.SOUTH);

        btnNovo.addActionListener(e -> limparFormulario());
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnAtualizar.addActionListener(e -> carregarTabela());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                campoNome.setText((String) tableModel.getValueAt(row, 1));
                campoTelefone.setText((String) tableModel.getValueAt(row, 2));
                campoVeiculo.setText((String) tableModel.getValueAt(row, 3));
            }
        });

        carregarTabela();
    }

    private void addCampo(JPanel form, GridBagConstraints gc, int y, String label, JTextField campo) {
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(campo, gc);
    }

    public void carregarTabela() {
        try {
            tableModel.setRowCount(0);
            for (Entregador ent : dao.listarTodos()) {
                tableModel.addRow(new Object[]{ent.getId(), ent.getNome(), ent.getTelefone(), ent.getVeiculo(), ent.getStatus()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void salvar() {
        try {
            if (idSelecionado == null) {
                Entregador e = new Entregador();
                e.setNome(campoNome.getText());
                e.setTelefone(campoTelefone.getText());
                e.setVeiculo(campoVeiculo.getText());
                e.setStatus(StatusEntregador.DISPONIVEL);
                dao.inserir(e);
            } else {
                Entregador e = dao.buscarPorId(idSelecionado);
                e.setNome(campoNome.getText());
                e.setTelefone(campoTelefone.getText());
                e.setVeiculo(campoVeiculo.getText());
                dao.atualizar(e); // status não é alterado manualmente aqui
            }
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um entregador na tabela primeiro.");
            return;
        }
        try {
            dao.excluir(idSelecionado);
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void limparFormulario() {
        idSelecionado = null;
        campoNome.setText("");
        campoTelefone.setText("");
        campoVeiculo.setText("");
        table.clearSelection();
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
