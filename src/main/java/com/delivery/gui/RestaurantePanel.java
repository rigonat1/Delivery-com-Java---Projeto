package com.delivery.gui;

import com.delivery.dao.RestauranteDAO;
import com.delivery.model.Restaurante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class RestaurantePanel extends JPanel {

    private final RestauranteDAO dao = new RestauranteDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Id", "Nome", "Categoria", "Endereço", "Telefone"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoCategoria = new JTextField(15);
    private final JTextField campoEndereco = new JTextField(25);
    private final JTextField campoTelefone = new JTextField(15);
    private Integer idSelecionado = null;

    public RestaurantePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Restaurante"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addCampo(form, gc, y++, "Nome:", campoNome);
        addCampo(form, gc, y++, "Categoria:", campoCategoria);
        addCampo(form, gc, y++, "Endereço:", campoEndereco);
        addCampo(form, gc, y++, "Telefone:", campoTelefone);

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
                campoCategoria.setText((String) tableModel.getValueAt(row, 2));
                campoEndereco.setText((String) tableModel.getValueAt(row, 3));
                campoTelefone.setText((String) tableModel.getValueAt(row, 4));
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
            for (Restaurante r : dao.listarTodos()) {
                tableModel.addRow(new Object[]{r.getId(), r.getNome(), r.getCategoria(), r.getEndereco(), r.getTelefone()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void salvar() {
        try {
            Restaurante r = new Restaurante();
            r.setNome(campoNome.getText());
            r.setCategoria(campoCategoria.getText());
            r.setEndereco(campoEndereco.getText());
            r.setTelefone(campoTelefone.getText());
            if (idSelecionado == null) {
                dao.inserir(r);
            } else {
                r.setId(idSelecionado);
                dao.atualizar(r);
            }
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um restaurante na tabela primeiro.");
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
        campoCategoria.setText("");
        campoEndereco.setText("");
        campoTelefone.setText("");
        table.clearSelection();
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
