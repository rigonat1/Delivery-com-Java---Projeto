package com.delivery;

import com.delivery.dao.*;
import com.delivery.model.*;
import com.delivery.service.EntregadorIndisponivelException;
import com.delivery.service.PedidoService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final RestauranteDAO restauranteDAO = new RestauranteDAO();
    private static final ProdutoDAO produtoDAO = new ProdutoDAO();
    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final EntregadorDAO entregadorDAO = new EntregadorDAO();
    private static final PedidoDAO pedidoDAO = new PedidoDAO();
    private static final PedidoService pedidoService = new PedidoService();

    public static void main(String[] args) {
        int opcao;
        do {
            System.out.println("\n===== SISTEMA DE DELIVERY DE COMIDA =====");
            System.out.println("1 - Restaurantes");
            System.out.println("2 - Produtos");
            System.out.println("3 - Clientes");
            System.out.println("4 - Entregadores");
            System.out.println("5 - Pedidos");
            System.out.println("6 - Relatórios");
            System.out.println("0 - Sair");
            opcao = lerInt("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1 -> menuRestaurantes();
                    case 2 -> menuProdutos();
                    case 3 -> menuClientes();
                    case 4 -> menuEntregadores();
                    case 5 -> menuPedidos();
                    case 6 -> menuRelatorios();
                    case 0 -> System.out.println("Até logo!");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            } catch (IllegalArgumentException | EntregadorIndisponivelException e) {
                System.out.println("Aviso: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    // ================= RESTAURANTES =================

    private static void menuRestaurantes() throws SQLException {
        System.out.println("\n--- Restaurantes ---");
        System.out.println("1-Inserir 2-Atualizar 3-Excluir 4-Listar 0-Voltar");
        int op = lerInt("Opção: ");
        switch (op) {
            case 1 -> {
                Restaurante r = new Restaurante();
                r.setNome(lerTexto("Nome: "));
                r.setCategoria(lerTexto("Categoria: "));
                r.setEndereco(lerTexto("Endereço: "));
                r.setTelefone(lerTexto("Telefone: "));
                restauranteDAO.inserir(r);
                System.out.println("Restaurante cadastrado com id " + r.getId());
            }
            case 2 -> {
                int id = lerInt("Id do restaurante: ");
                Restaurante r = restauranteDAO.buscarPorId(id);
                if (r == null) { System.out.println("Não encontrado."); return; }
                r.setNome(lerTexto("Novo nome (" + r.getNome() + "): "));
                r.setCategoria(lerTexto("Nova categoria (" + r.getCategoria() + "): "));
                r.setEndereco(lerTexto("Novo endereço (" + r.getEndereco() + "): "));
                r.setTelefone(lerTexto("Novo telefone (" + r.getTelefone() + "): "));
                restauranteDAO.atualizar(r);
                System.out.println("Atualizado.");
            }
            case 3 -> {
                int id = lerInt("Id do restaurante: ");
                restauranteDAO.excluir(id);
                System.out.println("Excluído.");
            }
            case 4 -> restauranteDAO.listarTodos().forEach(System.out::println);
            default -> { }
        }
    }

    // ================= PRODUTOS =================

    private static void menuProdutos() throws SQLException {
        System.out.println("\n--- Produtos ---");
        System.out.println("1-Inserir 2-Atualizar 3-Excluir 4-Listar por restaurante 0-Voltar");
        int op = lerInt("Opção: ");
        switch (op) {
            case 1 -> {
                Produto p = new Produto();
                p.setRestauranteId(lerInt("Id do restaurante: "));
                p.setNome(lerTexto("Nome do produto: "));
                p.setDescricao(lerTexto("Descrição: "));
                p.setPreco(lerBigDecimal("Preço: "));
                produtoDAO.inserir(p);
                System.out.println("Produto cadastrado com id " + p.getId());
            }
            case 2 -> {
                int id = lerInt("Id do produto: ");
                Produto p = produtoDAO.buscarPorId(id);
                if (p == null) { System.out.println("Não encontrado."); return; }
                p.setNome(lerTexto("Novo nome (" + p.getNome() + "): "));
                p.setDescricao(lerTexto("Nova descrição (" + p.getDescricao() + "): "));
                p.setPreco(lerBigDecimal("Novo preço (" + p.getPreco() + "): "));
                produtoDAO.atualizar(p);
                System.out.println("Atualizado.");
            }
            case 3 -> {
                int id = lerInt("Id do produto: ");
                produtoDAO.excluir(id);
                System.out.println("Excluído.");
            }
            case 4 -> {
                int restauranteId = lerInt("Id do restaurante: ");
                produtoDAO.listarPorRestaurante(restauranteId).forEach(System.out::println);
            }
            default -> { }
        }
    }

    // ================= CLIENTES =================

    private static void menuClientes() throws SQLException {
        System.out.println("\n--- Clientes ---");
        System.out.println("1-Inserir 2-Atualizar 3-Excluir 4-Listar 0-Voltar");
        int op = lerInt("Opção: ");
        switch (op) {
            case 1 -> {
                Cliente c = new Cliente();
                c.setNome(lerTexto("Nome: "));
                c.setEndereco(lerTexto("Endereço: "));
                c.setTelefone(lerTexto("Telefone: "));
                c.setEmail(lerTexto("Email: "));
                clienteDAO.inserir(c);
                System.out.println("Cliente cadastrado com id " + c.getId());
            }
            case 2 -> {
                int id = lerInt("Id do cliente: ");
                Cliente c = clienteDAO.buscarPorId(id);
                if (c == null) { System.out.println("Não encontrado."); return; }
                c.setNome(lerTexto("Novo nome (" + c.getNome() + "): "));
                c.setEndereco(lerTexto("Novo endereço (" + c.getEndereco() + "): "));
                c.setTelefone(lerTexto("Novo telefone (" + c.getTelefone() + "): "));
                c.setEmail(lerTexto("Novo email (" + c.getEmail() + "): "));
                clienteDAO.atualizar(c);
                System.out.println("Atualizado.");
            }
            case 3 -> {
                int id = lerInt("Id do cliente: ");
                clienteDAO.excluir(id);
                System.out.println("Excluído.");
            }
            case 4 -> clienteDAO.listarTodos().forEach(System.out::println);
            default -> { }
        }
    }

    // ================= ENTREGADORES =================

    private static void menuEntregadores() throws SQLException {
        System.out.println("\n--- Entregadores ---");
        System.out.println("1-Inserir 2-Atualizar 3-Excluir 4-Listar todos 5-Listar disponíveis 0-Voltar");
        int op = lerInt("Opção: ");
        switch (op) {
            case 1 -> {
                Entregador e = new Entregador();
                e.setNome(lerTexto("Nome: "));
                e.setTelefone(lerTexto("Telefone: "));
                e.setVeiculo(lerTexto("Veículo: "));
                e.setStatus(StatusEntregador.DISPONIVEL);
                entregadorDAO.inserir(e);
                System.out.println("Entregador cadastrado com id " + e.getId());
            }
            case 2 -> {
                int id = lerInt("Id do entregador: ");
                Entregador e = entregadorDAO.buscarPorId(id);
                if (e == null) { System.out.println("Não encontrado."); return; }
                e.setNome(lerTexto("Novo nome (" + e.getNome() + "): "));
                e.setTelefone(lerTexto("Novo telefone (" + e.getTelefone() + "): "));
                e.setVeiculo(lerTexto("Novo veículo (" + e.getVeiculo() + "): "));
                entregadorDAO.atualizar(e);
                System.out.println("Atualizado.");
            }
            case 3 -> {
                int id = lerInt("Id do entregador: ");
                entregadorDAO.excluir(id);
                System.out.println("Excluído.");
            }
            case 4 -> entregadorDAO.listarTodos().forEach(System.out::println);
            case 5 -> entregadorDAO.listarDisponiveis().forEach(System.out::println);
            default -> { }
        }
    }

    // ================= PEDIDOS =================

    private static void menuPedidos() throws SQLException {
        System.out.println("\n--- Pedidos ---");
        System.out.println("1-Novo pedido 2-Ver detalhes 3-Listar todos 4-Atribuir entregador " +
                "5-Concluir entrega 6-Atualizar status 7-Excluir 0-Voltar");
        int op = lerInt("Opção: ");
        switch (op) {
            case 1 -> novoPedido();
            case 2 -> {
                int id = lerInt("Id do pedido: ");
                Pedido p = pedidoDAO.buscarPorId(id);
                if (p == null) System.out.println("Não encontrado.");
                else p.imprimirResumo();
            }
            case 3 -> pedidoDAO.listarTodos().forEach(p ->
                    System.out.printf("#%d | %s | status: %s | total: R$ %.2f%n",
                            p.getId(), p.getDataHora(), p.getStatus(), p.getValorTotal()));
            case 4 -> {
                int pedidoId = lerInt("Id do pedido: ");
                int entregadorId = lerInt("Id do entregador: ");
                pedidoService.atribuirEntregador(pedidoId, entregadorId);
                System.out.println("Entregador atribuído com sucesso!");
            }
            case 5 -> {
                int pedidoId = lerInt("Id do pedido: ");
                int entregadorId = lerInt("Id do entregador: ");
                pedidoService.concluirEntrega(pedidoId, entregadorId);
                System.out.println("Entrega concluída. Entregador está disponível novamente.");
            }
            case 6 -> {
                int pedidoId = lerInt("Id do pedido: ");
                System.out.println("Status: PENDENTE, CONFIRMADO, EM_PREPARO, SAIU_PARA_ENTREGA, ENTREGUE, CANCELADO");
                StatusPedido status = StatusPedido.valueOf(lerTexto("Novo status: ").toUpperCase());
                pedidoDAO.atualizarStatus(pedidoId, status);
                System.out.println("Status atualizado.");
            }
            case 7 -> {
                int id = lerInt("Id do pedido: ");
                pedidoDAO.excluir(id);
                System.out.println("Excluído.");
            }
            default -> { }
        }
    }

    private static void novoPedido() throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setClienteId(lerInt("Id do cliente: "));
        int restauranteId = lerInt("Id do restaurante: ");
        pedido.setRestauranteId(restauranteId);

        List<Produto> cardapio = produtoDAO.listarPorRestaurante(restauranteId);
        if (cardapio.isEmpty()) {
            System.out.println("Este restaurante não tem produtos cadastrados.");
            return;
        }
        System.out.println("Cardápio:");
        cardapio.forEach(System.out::println);

        boolean continuar = true;
        while (continuar) {
            int produtoId = lerInt("Id do produto a adicionar: ");
            Produto produto = cardapio.stream()
                    .filter(p -> p.getId() == produtoId)
                    .findFirst()
                    .orElse(null);
            if (produto == null) {
                System.out.println("Produto não pertence a este restaurante.");
            } else {
                int quantidade = lerInt("Quantidade: ");
                pedido.adicionarItem(new ItemPedido(
                        produto.getId(), produto.getNome(), quantidade, produto.getPreco()));
            }
            System.out.print("Adicionar outro produto? (s/n): ");
            continuar = sc.nextLine().trim().equalsIgnoreCase("s");
        }

        pedidoService.criarPedido(pedido);
        System.out.println("\nPedido criado com sucesso!");
        pedido.imprimirResumo();
    }

    // ================= RELATÓRIOS =================

    private static void menuRelatorios() throws SQLException {
        System.out.println("\n--- Relatório: total de pedidos e valor vendido por restaurante ---");
        List<PedidoDAO.RelatorioRestaurante> relatorio = pedidoDAO.relatorioPorRestaurante();
        for (PedidoDAO.RelatorioRestaurante r : relatorio) {
            System.out.printf("%-30s | pedidos: %-5d | total vendido: R$ %.2f%n",
                    r.nomeRestaurante, r.totalPedidos, r.valorTotalVendido);
        }
    }

    // ================= AUXILIARES DE LEITURA =================

    private static int lerInt(String mensagem) {
        System.out.print(mensagem);
        while (!sc.hasNextInt()) {
            System.out.print("Digite um número válido: ");
            sc.next();
        }
        int valor = sc.nextInt();
        sc.nextLine();
        return valor;
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return sc.nextLine();
    }

    private static BigDecimal lerBigDecimal(String mensagem) {
        System.out.print(mensagem);
        while (!sc.hasNextBigDecimal()) {
            System.out.print("Digite um valor numérico válido: ");
            sc.next();
        }
        BigDecimal valor = sc.nextBigDecimal();
        sc.nextLine();
        return valor;
    }
}
