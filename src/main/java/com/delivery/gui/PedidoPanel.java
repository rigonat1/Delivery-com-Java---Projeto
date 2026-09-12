package com.delivery.gui;

import com.delivery.dao.ClienteDAO;
import com.delivery.dao.EntregadorDAO;
import com.delivery.dao.PedidoDAO;
import com.delivery.dao.ProdutoDAO;
import com.delivery.dao.RestauranteDAO;
import com.delivery.model.*;
import com.delivery.service.EntregadorIndisponivelException;
import com.delivery.service.PedidoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PedidoPanel extends JPanel {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RestauranteDAO restauranteDAO = new RestauranteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final EntregadorDAO entregadorDAO = new EntregadorDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PedidoService pedidoService = new PedidoService();

    private final JComboBox<Cliente> comboCliente = new JComboBox<>();
    private final JComboBox<Restaurante> comboRestaurante = new JComboBox<>();

    private final DefaultTableModel modeloCardapio =
            new DefaultTableModel(new Object[]{"Id", "Produto", "Preço"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable tabelaCardapio = new JTable(modeloCardapio);
    private final JSpinner spinnerQtd = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private final DefaultTableModel modeloCarrinho =
            new DefaultTableModel(new Object[]{"ProdutoId", "Produto", "Qtd", "Preço Unit.", "Subtotal"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable tabelaCarrinho = new JTable(modeloCarrinho);

    private final DefaultTableModel modeloPedidos =
            new DefaultTableModel(new Object[]{"Id", "Data/Hora", "Status", "Entregador Id", "Valor Total (R$)"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable tabelaPedidos = new JTable(modeloPedidos);

    private Pedido pedidoEmMontagem = new Pedido();

    public PedidoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(montarPainelNovoPedido(), BorderLayout.NORTH);
        add(montarPainelListaPedidos(), BorderLayout.CENTER);

        comboRestaurante.addActionListener(e -> carregarCardapio());
        carregarCombos();
        carregarPedidos();
    }

    // -------------------- Área de criação de pedido --------------------

    private JPanel montarPainelNovoPedido() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Novo Pedido"));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Cliente:"));
        topo.add(comboCliente);
        topo.add(new JLabel("Restaurante:"));
        topo.add(comboRestaurante);
        painel.add(topo, BorderLayout.NORTH);

        JPanel meio = new JPanel(new GridLayout(1, 2, 8, 0));

        JPanel esquerda = new JPanel(new BorderLayout(4, 4));
        esquerda.add(new JLabel("Cardápio do restaurante:"), BorderLayout.NORTH);
        tabelaCardapio.setPreferredScrollableViewportSize(new Dimension(300, 120));
        esquerda.add(new JScrollPane(tabelaCardapio), BorderLayout.CENTER);
        JPanel addLinha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addLinha.add(new JLabel("Quantidade:"));
        addLinha.add(spinnerQtd);
        JButton btnAdicionar = new JButton("Adicionar ao pedido");
        btnAdicionar.addActionListener(e -> adicionarItemAoCarrinho());
        addLinha.add(btnAdicionar);
        esquerda.add(addLinha, BorderLayout.SOUTH);

        JPanel direita = new JPanel(new BorderLayout(4, 4));
        direita.add(new JLabel("Itens do pedido:"), BorderLayout.NORTH);
        tabelaCarrinho.setPreferredScrollableViewportSize(new Dimension(320, 120));
        direita.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);
        JPanel botoesCarrinho = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRemover = new JButton("Remover item selecionado");
        btnRemover.addActionListener(e -> removerItemDoCarrinho());
        JButton btnFinalizar = new JButton("Finalizar Pedido");
        btnFinalizar.addActionListener(e -> finalizarPedido());
        botoesCarrinho.add(btnRemover);
        botoesCarrinho.add(btnFinalizar);
        direita.add(botoesCarrinho, BorderLayout.SOUTH);

        meio.add(esquerda);
        meio.add(direita);
        painel.add(meio, BorderLayout.CENTER);

        return painel;
    }

    private void carregarCombos() {
        try {
            comboCliente.removeAllItems();
            for (Cliente c : clienteDAO.listarTodos()) comboCliente.addItem(c);
            comboRestaurante.removeAllItems();
            for (Restaurante r : restauranteDAO.listarTodos()) comboRestaurante.addItem(r);
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void carregarCardapio() {
        modeloCardapio.setRowCount(0);
        Restaurante r = (Restaurante) comboRestaurante.getSelectedItem();
        if (r == null) return;
        try {
            for (Produto p : produtoDAO.listarPorRestaurante(r.getId())) {
                modeloCardapio.addRow(new Object[]{p.getId(), p.getNome(), p.getPreco()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void adicionarItemAoCarrinho() {
        int linha = tabelaCardapio.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto do cardápio.");
            return;
        }
        int produtoId = (Integer) modeloCardapio.getValueAt(linha, 0);
        String nome = (String) modeloCardapio.getValueAt(linha, 1);
        BigDecimal preco = (BigDecimal) modeloCardapio.getValueAt(linha, 2);
        int quantidade = (Integer) spinnerQtd.getValue();

        ItemPedido item = new ItemPedido(produtoId, nome, quantidade, preco);
        pedidoEmMontagem.adicionarItem(item);
        modeloCarrinho.addRow(new Object[]{produtoId, nome, quantidade, preco, item.getSubtotal()});
    }

    private void removerItemDoCarrinho() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) return;
        pedidoEmMontagem.getItens().remove(linha);
        modeloCarrinho.removeRow(linha);
    }

    private void finalizarPedido() {
        Cliente cliente = (Cliente) comboCliente.getSelectedItem();
        Restaurante restaurante = (Restaurante) comboRestaurante.getSelectedItem();
        if (cliente == null || restaurante == null) {
            JOptionPane.showMessageDialog(this, "Selecione cliente e restaurante.");
            return;
        }
        if (pedidoEmMontagem.getItens().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione ao menos um item ao pedido.");
            return;
        }
        try {
            pedidoEmMontagem.setClienteId(cliente.getId());
            pedidoEmMontagem.setRestauranteId(restaurante.getId());
            pedidoService.criarPedido(pedidoEmMontagem);

            StringBuilder resumo = new StringBuilder();
            resumo.append("Pedido #").append(pedidoEmMontagem.getId()).append(" criado!\n\n");
            resumo.append(String.format("Subtotal:        R$ %.2f%n", pedidoEmMontagem.getSubtotal()));
            resumo.append(String.format("Desconto (%s%%): R$ %.2f%n",
                    pedidoEmMontagem.getPercentualDesconto(), pedidoEmMontagem.getValorDesconto()));
            resumo.append(String.format("Taxa de entrega: R$ %.2f%n", pedidoEmMontagem.getTaxaEntrega()));
            resumo.append(String.format("VALOR FINAL:     R$ %.2f", pedidoEmMontagem.getValorTotal()));

            JOptionPane.showMessageDialog(this, resumo.toString(), "Resumo do Pedido", JOptionPane.INFORMATION_MESSAGE);

            pedidoEmMontagem = new Pedido();
            modeloCarrinho.setRowCount(0);
            carregarPedidos();
        } catch (SQLException | IllegalArgumentException ex) {
            mostrarErro(ex);
        }
    }

    // -------------------- Área de listagem/gestão de pedidos --------------------

    private JPanel montarPainelListaPedidos() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Pedidos"));
        painel.add(new JScrollPane(tabelaPedidos), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnDetalhes = new JButton("Ver Detalhes");
        JButton btnAtribuir = new JButton("Atribuir Entregador");
        JButton btnConcluir = new JButton("Concluir Entrega");
        JButton btnStatus = new JButton("Atualizar Status");
        JButton btnExcluir = new JButton("Excluir");
        botoes.add(btnAtualizar);
        botoes.add(btnDetalhes);
        botoes.add(btnAtribuir);
        botoes.add(btnConcluir);
        botoes.add(btnStatus);
        botoes.add(btnExcluir);
        painel.add(botoes, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarPedidos());
        btnDetalhes.addActionListener(e -> verDetalhes());
        btnAtribuir.addActionListener(e -> atribuirEntregador());
        btnConcluir.addActionListener(e -> concluirEntrega());
        btnStatus.addActionListener(e -> atualizarStatus());
        btnExcluir.addActionListener(e -> excluirPedido());

        return painel;
    }

    public void carregarPedidos() {
        try {
            modeloPedidos.setRowCount(0);
            for (Pedido p : pedidoDAO.listarTodos()) {
                modeloPedidos.addRow(new Object[]{
                        p.getId(), p.getDataHora(), p.getStatus(), p.getEntregadorId(), p.getValorTotal()});
            }
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private Integer pedidoSelecionadoId() {
        int linha = tabelaPedidos.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido na tabela.");
            return null;
        }
        return (Integer) modeloPedidos.getValueAt(linha, 0);
    }

    private void verDetalhes() {
        Integer id = pedidoSelecionadoId();
        if (id == null) return;
        try {
            Pedido p = pedidoDAO.buscarPorId(id);
            if (p == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("Pedido #").append(p.getId()).append(" - status: ").append(p.getStatus()).append("\n\n");
            for (ItemPedido item : p.getItens()) {
                sb.append("  ").append(item).append("\n");
            }
            sb.append(String.format("%nSubtotal:  R$ %.2f%n", p.getSubtotal()));
            sb.append(String.format("Desconto (%s%%): R$ %.2f%n", p.getPercentualDesconto(), p.getValorDesconto()));
            sb.append(String.format("Taxa:      R$ %.2f%n", p.getTaxaEntrega()));
            sb.append(String.format("TOTAL:     R$ %.2f", p.getValorTotal()));
            JOptionPane.showMessageDialog(this, sb.toString(), "Detalhes do Pedido", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void atribuirEntregador() {
        Integer pedidoId = pedidoSelecionadoId();
        if (pedidoId == null) return;
        try {
            List<Entregador> disponiveis = entregadorDAO.listarDisponiveis();
            if (disponiveis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há entregadores disponíveis no momento.");
                return;
            }
            Entregador escolhido = (Entregador) JOptionPane.showInputDialog(this,
                    "Escolha o entregador:", "Atribuir Entregador",
                    JOptionPane.PLAIN_MESSAGE, null, disponiveis.toArray(), disponiveis.get(0));
            if (escolhido == null) return;

            pedidoService.atribuirEntregador(pedidoId, escolhido.getId());
            JOptionPane.showMessageDialog(this, "Entregador atribuído com sucesso!");
            carregarPedidos();
        } catch (EntregadorIndisponivelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException | IllegalArgumentException ex) {
            mostrarErro(ex);
        }
    }

    private void concluirEntrega() {
        Integer pedidoId = pedidoSelecionadoId();
        if (pedidoId == null) return;
        try {
            Pedido p = pedidoDAO.buscarPorId(pedidoId);
            if (p == null || p.getEntregadorId() == null) {
                JOptionPane.showMessageDialog(this, "Este pedido ainda não tem entregador atribuído.");
                return;
            }
            pedidoService.concluirEntrega(pedidoId, p.getEntregadorId());
            JOptionPane.showMessageDialog(this, "Entrega concluída. Entregador está disponível novamente.");
            carregarPedidos();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void atualizarStatus() {
        Integer pedidoId = pedidoSelecionadoId();
        if (pedidoId == null) return;
        StatusPedido status = (StatusPedido) JOptionPane.showInputDialog(this,
                "Novo status:", "Atualizar Status",
                JOptionPane.PLAIN_MESSAGE, null, StatusPedido.values(), StatusPedido.PENDENTE);
        if (status == null) return;
        try {
            pedidoDAO.atualizarStatus(pedidoId, status);
            carregarPedidos();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void excluirPedido() {
        Integer pedidoId = pedidoSelecionadoId();
        if (pedidoId == null) return;
        int confirmacao = JOptionPane.showConfirmDialog(this, "Excluir o pedido #" + pedidoId + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacao != JOptionPane.YES_OPTION) return;
        try {
            pedidoDAO.excluir(pedidoId);
            carregarPedidos();
        } catch (SQLException ex) {
            mostrarErro(ex);
        }
    }

    private void mostrarErro(Exception ex) {
        JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
