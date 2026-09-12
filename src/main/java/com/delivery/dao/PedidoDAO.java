package com.delivery.dao;

import com.delivery.model.ItemPedido;
import com.delivery.model.Pedido;
import com.delivery.model.StatusPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    /**
     * Insere o pedido e todos os seus itens em uma ÚNICA transação:
     * ou os dois passos são gravados juntos, ou nenhum é (rollback).
     * Isso evita "pedido órfão sem itens" caso algo falhe no meio.
     */
    public Pedido inserir(Pedido pedido) throws SQLException {
        String sqlPedido = "INSERT INTO pedidos " +
                "(cliente_id, restaurante_id, entregador_id, status, subtotal, " +
                " percentual_desconto, valor_desconto, taxa_entrega, valor_total) " +
                "VALUES (?,?,?,?,?,?,?,?,?) RETURNING id, data_hora";

        String sqlItem = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario) " +
                "VALUES (?,?,?,?)";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // início da transação

            try (PreparedStatement stmt = conn.prepareStatement(sqlPedido)) {
                stmt.setInt(1, pedido.getClienteId());
                stmt.setInt(2, pedido.getRestauranteId());
                if (pedido.getEntregadorId() != null) {
                    stmt.setInt(3, pedido.getEntregadorId());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }
                stmt.setString(4, pedido.getStatus().name());
                stmt.setBigDecimal(5, pedido.getSubtotal());
                stmt.setBigDecimal(6, pedido.getPercentualDesconto());
                stmt.setBigDecimal(7, pedido.getValorDesconto());
                stmt.setBigDecimal(8, pedido.getTaxaEntrega());
                stmt.setBigDecimal(9, pedido.getValorTotal());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        pedido.setId(rs.getInt("id"));
                        pedido.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlItem)) {
                for (ItemPedido item : pedido.getItens()) {
                    stmt.setInt(1, pedido.getId());
                    stmt.setInt(2, item.getProdutoId());
                    stmt.setInt(3, item.getQuantidade());
                    stmt.setBigDecimal(4, item.getPrecoUnitario());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit(); // tudo certo: confirma as duas gravações
            return pedido;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // algo falhou: desfaz tudo
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void atualizarStatus(int pedidoId, StatusPedido status) throws SQLException {
        String sql = "UPDATE pedidos SET status=? WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, pedidoId);
            stmt.executeUpdate();
        }
    }

    /** Grava o entregador escolhido no pedido (a checagem de disponibilidade fica no Service). */
    public void atribuirEntregador(int pedidoId, int entregadorId) throws SQLException {
        String sql = "UPDATE pedidos SET entregador_id=?, status='SAIU_PARA_ENTREGA' WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entregadorId);
            stmt.setInt(2, pedidoId);
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id=?"; // itens_pedido caem em cascata (ON DELETE CASCADE)
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sqlPedido = "SELECT * FROM pedidos WHERE id=?";
        String sqlItens = "SELECT ip.*, p.nome AS nome_produto FROM itens_pedido ip " +
                "JOIN produtos p ON p.id = ip.produto_id WHERE ip.pedido_id=?";

        Pedido pedido = null;
        try (Connection conn = ConnectionFactory.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlPedido)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        pedido = mapearPedido(rs);
                    }
                }
            }
            if (pedido == null) {
                return null;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ItemPedido item = new ItemPedido();
                        item.setId(rs.getInt("id"));
                        item.setPedidoId(rs.getInt("pedido_id"));
                        item.setProdutoId(rs.getInt("produto_id"));
                        item.setNomeProduto(rs.getString("nome_produto"));
                        item.setQuantidade(rs.getInt("quantidade"));
                        item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                        pedido.adicionarItem(item);
                    }
                }
            }
        }
        return pedido;
    }

    public List<Pedido> listarTodos() throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedidos ORDER BY data_hora DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearPedido(rs));
            }
        }
        return lista;
    }

    /** Relatório: total de pedidos e valor total vendido, agrupado por restaurante. */
    public List<RelatorioRestaurante> relatorioPorRestaurante() throws SQLException {
        List<RelatorioRestaurante> lista = new ArrayList<>();
        String sql = "SELECT r.id, r.nome, COUNT(p.id) AS total_pedidos, " +
                "COALESCE(SUM(p.valor_total), 0) AS valor_total_vendido " +
                "FROM restaurantes r " +
                "LEFT JOIN pedidos p ON p.restaurante_id = r.id AND p.status <> 'CANCELADO' " +
                "GROUP BY r.id, r.nome " +
                "ORDER BY valor_total_vendido DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RelatorioRestaurante r = new RelatorioRestaurante();
                r.restauranteId = rs.getInt("id");
                r.nomeRestaurante = rs.getString("nome");
                r.totalPedidos = rs.getInt("total_pedidos");
                r.valorTotalVendido = rs.getBigDecimal("valor_total_vendido");
                lista.add(r);
            }
        }
        return lista;
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setRestauranteId(rs.getInt("restaurante_id"));
        int entregadorId = rs.getInt("entregador_id");
        p.setEntregadorId(rs.wasNull() ? null : entregadorId);
        p.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        p.setStatus(StatusPedido.valueOf(rs.getString("status")));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setPercentualDesconto(rs.getBigDecimal("percentual_desconto"));
        p.setValorDesconto(rs.getBigDecimal("valor_desconto"));
        p.setTaxaEntrega(rs.getBigDecimal("taxa_entrega"));
        p.setValorTotal(rs.getBigDecimal("valor_total"));
        return p;
    }

    /** DTO simples só para carregar a linha do relatório. */
    public static class RelatorioRestaurante {
        public int restauranteId;
        public String nomeRestaurante;
        public int totalPedidos;
        public java.math.BigDecimal valorTotalVendido;
    }
}
