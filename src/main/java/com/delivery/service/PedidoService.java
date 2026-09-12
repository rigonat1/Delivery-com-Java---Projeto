package com.delivery.service;

import com.delivery.dao.EntregadorDAO;
import com.delivery.dao.PedidoDAO;
import com.delivery.model.Entregador;
import com.delivery.model.ItemPedido;
import com.delivery.model.Pedido;
import com.delivery.model.StatusEntregador;
import com.delivery.model.StatusPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

/**
 * Concentra as regras de negócio do pedido. Nem o DAO (que só fala SQL)
 * nem o Main (que só fala com o usuário) deveriam saber calcular desconto
 * ou decidir se um entregador pode ser atribuído — essa responsabilidade
 * é toda desta classe.
 */
public class PedidoService {

    private static final BigDecimal TAXA_ENTREGA = new BigDecimal("8.00");

    private static final BigDecimal LIMITE_5_PORCENTO  = new BigDecimal("100");
    private static final BigDecimal LIMITE_10_PORCENTO = new BigDecimal("200");
    private static final BigDecimal LIMITE_15_PORCENTO = new BigDecimal("300");

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final EntregadorDAO entregadorDAO = new EntregadorDAO();

    /**
     * Passo 1 a 3 da regra de negócio: calcula subtotal, desconto progressivo
     * e taxa de entrega, preenchendo os campos financeiros do próprio Pedido.
     * O pedido já deve ter sua lista de itens preenchida antes de chamar isto.
     */
    public void calcularValores(Pedido pedido) {
        // 1. Subtotal = soma de (preço unitário x quantidade) de cada item
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedido item : pedido.getItens()) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        // 2. Desconto progressivo: usamos a MAIOR faixa em que o subtotal se enquadra
        //    (não é cumulativo - não soma 5%+10%+15%, aplica só o percentual da faixa atingida)
        BigDecimal percentualDesconto = obterPercentualDesconto(subtotal);
        BigDecimal valorDesconto = subtotal
                .multiply(percentualDesconto)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 3. Taxa de entrega fixa + valor final
        BigDecimal valorTotal = subtotal
                .subtract(valorDesconto)
                .add(TAXA_ENTREGA);

        pedido.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        pedido.setPercentualDesconto(percentualDesconto);
        pedido.setValorDesconto(valorDesconto);
        pedido.setTaxaEntrega(TAXA_ENTREGA);
        pedido.setValorTotal(valorTotal.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal obterPercentualDesconto(BigDecimal subtotal) {
        if (subtotal.compareTo(LIMITE_15_PORCENTO) > 0) {
            return new BigDecimal("15");
        }
        if (subtotal.compareTo(LIMITE_10_PORCENTO) > 0) {
            return new BigDecimal("10");
        }
        if (subtotal.compareTo(LIMITE_5_PORCENTO) > 0) {
            return new BigDecimal("5");
        }
        return BigDecimal.ZERO;
    }

    /** Calcula os valores e persiste o pedido (com seus itens) no banco. */
    public Pedido criarPedido(Pedido pedido) throws SQLException {
        if (pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter ao menos um item.");
        }
        calcularValores(pedido);
        pedido.setStatus(StatusPedido.PENDENTE);
        return pedidoDAO.inserir(pedido);
    }

    /**
     * Passo 4 da regra de negócio: atribuir um entregador a um pedido.
     * Só é permitido se o entregador estiver DISPONIVEL; nesse caso,
     * o entregador passa a EM_ENTREGA e o pedido é atualizado.
     */
    public void atribuirEntregador(int pedidoId, int entregadorId) throws SQLException {
        Entregador entregador = entregadorDAO.buscarPorId(entregadorId);
        if (entregador == null) {
            throw new IllegalArgumentException("Entregador #" + entregadorId + " não encontrado.");
        }
        if (entregador.getStatus() != StatusEntregador.DISPONIVEL) {
            throw new EntregadorIndisponivelException(
                    "Entregador " + entregador.getNome() +
                    " não está disponível (status atual: " + entregador.getStatus() + ").");
        }

        pedidoDAO.atribuirEntregador(pedidoId, entregadorId);
        entregadorDAO.atualizarStatus(entregadorId, StatusEntregador.EM_ENTREGA);
    }

    /** Ao concluir a entrega, o entregador volta a ficar disponível. */
    public void concluirEntrega(int pedidoId, int entregadorId) throws SQLException {
        pedidoDAO.atualizarStatus(pedidoId, StatusPedido.ENTREGUE);
        entregadorDAO.atualizarStatus(entregadorId, StatusEntregador.DISPONIVEL);
    }
}
