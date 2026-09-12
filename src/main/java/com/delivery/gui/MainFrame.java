package com.delivery.gui;

import javax.swing.*;
import java.awt.Component;

public class MainFrame extends JFrame {

    private final RestaurantePanel restaurantePanel = new RestaurantePanel();
    private final ProdutoPanel produtoPanel = new ProdutoPanel();
    private final ClientePanel clientePanel = new ClientePanel();
    private final EntregadorPanel entregadorPanel = new EntregadorPanel();
    private final PedidoPanel pedidoPanel = new PedidoPanel();
    private final RelatorioPanel relatorioPanel = new RelatorioPanel();

    public MainFrame() {
        super("Sistema de Delivery de Comida");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Restaurantes", restaurantePanel);
        abas.addTab("Produtos", produtoPanel);
        abas.addTab("Clientes", clientePanel);
        abas.addTab("Entregadores", entregadorPanel);
        abas.addTab("Pedidos", pedidoPanel);
        abas.addTab("Relatórios", relatorioPanel);

        // Sempre que o usuário troca de aba, atualizamos os dados dela
        // (assim, um restaurante cadastrado na aba 1 já aparece no combo da aba 2).
        abas.addChangeListener(e -> {
            Component selecionado = abas.getSelectedComponent();
            if (selecionado == restaurantePanel) restaurantePanel.carregarTabela();
            else if (selecionado == produtoPanel) produtoPanel.carregarRestaurantes();
            else if (selecionado == clientePanel) clientePanel.carregarTabela();
            else if (selecionado == entregadorPanel) entregadorPanel.carregarTabela();
            else if (selecionado == pedidoPanel) pedidoPanel.carregarPedidos();
            else if (selecionado == relatorioPanel) relatorioPanel.carregarTabela();
        });

        add(abas);
    }
}
