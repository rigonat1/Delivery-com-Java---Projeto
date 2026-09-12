package com.delivery.dao;

import com.delivery.model.Restaurante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestauranteDAO {

    public Restaurante inserir(Restaurante r) throws SQLException {
        String sql = "INSERT INTO restaurantes (nome, categoria, endereco, telefone) VALUES (?,?,?,?) RETURNING id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getNome());
            stmt.setString(2, r.getCategoria());
            stmt.setString(3, r.getEndereco());
            stmt.setString(4, r.getTelefone());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    r.setId(rs.getInt("id"));
                }
            }
        }
        return r;
    }

    public void atualizar(Restaurante r) throws SQLException {
        String sql = "UPDATE restaurantes SET nome=?, categoria=?, endereco=?, telefone=? WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getNome());
            stmt.setString(2, r.getCategoria());
            stmt.setString(3, r.getEndereco());
            stmt.setString(4, r.getTelefone());
            stmt.setInt(5, r.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM restaurantes WHERE id=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Restaurante buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM restaurantes WHERE id=?";
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

    public List<Restaurante> listarTodos() throws SQLException {
        List<Restaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM restaurantes ORDER BY nome";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Restaurante mapear(ResultSet rs) throws SQLException {
        Restaurante r = new Restaurante();
        r.setId(rs.getInt("id"));
        r.setNome(rs.getString("nome"));
        r.setCategoria(rs.getString("categoria"));
        r.setEndereco(rs.getString("endereco"));
        r.setTelefone(rs.getString("telefone"));
        return r;
    }
}
