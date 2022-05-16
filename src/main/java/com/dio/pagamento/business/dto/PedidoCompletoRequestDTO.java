package com.dio.pagamento.business.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PedidoCompletoRequestDTO extends PedidoRequestDTO {

    private UUID idPagamento;

    public PedidoCompletoRequestDTO(PedidoRequestDTO pedido, UUID idPagamento) {

        this.setDataPedido(pedido.getDataPedido());
        this.setId(pedido.getId());
        this.setIdPagamento(idPagamento);
        this.setItens(pedido.getItens());
        this.setNomeCliente(pedido.getNomeCliente());
        this.setNumeroCartao(pedido.getNumeroCartao());
    }
}
