package com.delivery.gui;

import com.delivery.dao.ClienteDAO;
import com.delivery.model.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class ClientePanel extends JPanel {

    private final ClienteDAO dao = new ClienteDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Id", "Nome", "Endereço", "Telefone", "Email"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoEndereco = new JTextField(25);
    private final JTextField campoTelefone = new JTextField(15);
    private final JTextField campoEmail = new JTextField(20);
    private Integer idSelecionado = null;

    public ClientePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Cliente"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addCampo(form, gc, y++, "Nome:", campoNome);
        addCampo(form, gc, y++, "Endereço:", campoEndereco);
        addCampo(form, gc, y++, "Telefone:", campoTelefone);
        addCampo(form, gc, y++, "Email:", campoEmail);

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
                campoEndereco.setText((String) tableModel.getValueAt(row, 2));
                campoTelefone.setText((String) tableModel.getValueAt(row, 3));
                campoEmail.setText((String) tableModel.getValueAt(row, 4));
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
            for (Cliente c : dao.listarTodos()) {
                tableModel.addRow(new Object[]{c.getId(), c.getNome(), c.getEndereco(), c.getTelefone(), c.getEmail()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void salvar() {
        try {
            Cliente c = new Cliente();
            c.setNome(campoNome.getText());
            c.setEndereco(campoEndereco.getText());
            c.setTelefone(campoTelefone.getText());
            c.setEmail(campoEmail.getText());
            if (idSelecionado == null) {
                dao.inserir(c);
            } else {
                c.setId(idSelecionado);
                dao.atualizar(c);
            }
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela primeiro.");
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
        campoEndereco.setText("");
        campoTelefone.setText("");
        campoEmail.setText("");
        table.clearSelection();
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
