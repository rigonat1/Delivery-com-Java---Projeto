package com.delivery.dao;

import com.delivery.model.Entregador;
import com.delivery.model.StatusEntregador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntregadorDAO {

    public Entregador inserir(Entregador e) throws SQLException {
        String sql = "INSERT INTO entregadores (nome, telefone, veiculo, status) VALUES (?,?,?,?) RETURNING id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNome());
            stmt.setString(2, e.getTelefone());
            stmt.setString(3, e.getVeiculo());
            stmt.setString(4, e.getStatus().name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    e.setId(rs.getInt("id"));
                }
            }
        }
        return e;
    }

    public void atualizar(Entregador e) throws SQLException {
        String sql = "UPDATE entregadores SET nome=?, telefone=?, veiculo=?, status=? WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNome());
            stmt.setString(2, e.getTelefone());
            stmt.setString(3, e.getVeiculo());
            stmt.setString(4, e.getStatus().name());
            stmt.setInt(5, e.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM entregadores WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Entregador buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM entregadores WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Entregador> listarTodos() throws SQLException {
        List<Entregador> lista = new ArrayList<>();
        String sql = "SELECT * FROM entregadores ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Entregador> listarDisponiveis() throws SQLException {
        List<Entregador> lista = new ArrayList<>();
        String sql = "SELECT * FROM entregadores WHERE status = 'DISPONIVEL' ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Usado pela regra de negócio ao atribuir/concluir uma entrega. */
    public void atualizarStatus(int entregadorId, StatusEntregador status) throws SQLException {
        String sql = "UPDATE entregadores SET status=? WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, entregadorId);
            stmt.executeUpdate();
        }
    }

    private Entregador mapear(ResultSet rs) throws SQLException {
        Entregador e = new Entregador();
        e.setId(rs.getInt("id"));
        e.setNome(rs.getString("nome"));
        e.setTelefone(rs.getString("telefone"));
        e.setVeiculo(rs.getString("veiculo"));
        e.setStatus(StatusEntregador.valueOf(rs.getString("status")));
        return e;
    }
}
