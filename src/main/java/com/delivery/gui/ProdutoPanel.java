package com.delivery.gui;

import com.delivery.dao.ProdutoDAO;
import com.delivery.dao.RestauranteDAO;
import com.delivery.model.Produto;
import com.delivery.model.Restaurante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProdutoPanel extends JPanel {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final RestauranteDAO restauranteDAO = new RestauranteDAO();

    private final JComboBox<Restaurante> comboRestaurante = new JComboBox<>();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Id", "Nome", "Descrição", "Preço"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);

    private final JTextField campoNome = new JTextField(20);
    private final JTextField campoDescricao = new JTextField(25);
    private final JTextField campoPreco = new JTextField(10);
    private Integer idSelecionado = null;

    public ProdutoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Restaurante:"));
        topo.add(comboRestaurante);
        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Produto"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addCampo(form, gc, y++, "Nome:", campoNome);
        addCampo(form, gc, y++, "Descrição:", campoDescricao);
        addCampo(form, gc, y++, "Preço:", campoPreco);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNovo = new JButton("Novo");
        JButton btnSalvar = new JButton("Salvar");
        JButton btnExcluir = new JButton("Excluir");
        botoes.add(btnNovo);
        botoes.add(btnSalvar);
        botoes.add(btnExcluir);

        JPanel sul = new JPanel(new BorderLayout());
        sul.add(form, BorderLayout.CENTER);
        sul.add(botoes, BorderLayout.SOUTH);
        add(sul, BorderLayout.SOUTH);

        comboRestaurante.addActionListener(e -> carregarTabela());
        btnNovo.addActionListener(e -> limparFormulario());
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e -> excluir());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                campoNome.setText((String) tableModel.getValueAt(row, 1));
                campoDescricao.setText((String) tableModel.getValueAt(row, 2));
                campoPreco.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        carregarRestaurantes();
    }

    private void addCampo(JPanel form, GridBagConstraints gc, int y, String label, JTextField campo) {
        gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(campo, gc);
    }

    /** Chamado pelo MainFrame sempre que esta aba é aberta, para trazer restaurantes novos. */
    public void carregarRestaurantes() {
        try {
            Restaurante selecionadoAntes = (Restaurante) comboRestaurante.getSelectedItem();
            comboRestaurante.removeAllItems();
            for (Restaurante r : restauranteDAO.listarTodos()) {
                comboRestaurante.addItem(r);
            }
            if (selecionadoAntes != null) {
                for (int i = 0; i < comboRestaurante.getItemCount(); i++) {
                    if (comboRestaurante.getItemAt(i).getId().equals(selecionadoAntes.getId())) {
                        comboRestaurante.setSelectedIndex(i);
                    }
                }
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void carregarTabela() {
        Restaurante r = (Restaurante) comboRestaurante.getSelectedItem();
        tableModel.setRowCount(0);
        if (r == null) return;
        try {
            for (Produto p : produtoDAO.listarPorRestaurante(r.getId())) {
                tableModel.addRow(new Object[]{p.getId(), p.getNome(), p.getDescricao(), p.getPreco()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void salvar() {
        Restaurante r = (Restaurante) comboRestaurante.getSelectedItem();
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Cadastre um restaurante primeiro.");
            return;
        }
        try {
            BigDecimal preco = new BigDecimal(campoPreco.getText().replace(",", "."));
            Produto p = new Produto();
            p.setRestauranteId(r.getId());
            p.setNome(campoNome.getText());
            p.setDescricao(campoDescricao.getText());
            p.setPreco(preco);
            if (idSelecionado == null) {
                produtoDAO.inserir(p);
            } else {
                p.setId(idSelecionado);
                produtoDAO.atualizar(p);
            }
            limparFormulario();
            carregarTabela();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preço inválido. Use algo como 29.90");
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela primeiro.");
            return;
        }
        try {
            produtoDAO.excluir(idSelecionado);
            limparFormulario();
            carregarTabela();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void limparFormulario() {
        idSelecionado = null;
        campoNome.setText("");
        campoDescricao.setText("");
        campoPreco.setText("");
        table.clearSelection();
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
