package com.dio.pagamento.controller;

import java.util.UUID;

import com.dio.pagamento.business.dto.PagamentoProcessadoResponseDTO;
import com.dio.pagamento.business.dto.PedidoCompletoRequestDTO;
import com.dio.pagamento.business.dto.PedidoRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@AllArgsConstructor
public final class PagamentoController {

    private static final int TEMPO_PROCESSAMENTO = 2000;

    private static final String ESTOQUE_BASE_URL = "http://localhost:8183";
    private static final String PROCESSAR_ESTOQUE_API = "/api/dio/v1/processarEstoqueCompleto";

    @PostMapping("v1/realizarPagamento")
    public ResponseEntity<PagamentoProcessadoResponseDTO> realizarPagamento(
            final @RequestBody PedidoRequestDTO pedido) {

        UUID pagamentoId = processar(pedido);

        PagamentoProcessadoResponseDTO response = new PagamentoProcessadoResponseDTO(pagamentoId.toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("v1/realizarPagamentoCompleto")
    public ResponseEntity<PagamentoProcessadoResponseDTO> realizarPedidoCompleto(
            final @RequestBody PedidoRequestDTO pedido) {

        ResponseEntity<PagamentoProcessadoResponseDTO> response = realizarPagamento(pedido);

        UUID idPagamento = UUID.fromString(response.getBody().getMessage());

        PedidoCompletoRequestDTO pedidoCompleto = new PedidoCompletoRequestDTO(pedido, idPagamento);

        ResponseEntity<PagamentoProcessadoResponseDTO> estoqueResponse = processarEstoque(pedido);

        if (estoqueResponse.getStatusCode() == HttpStatus.OK) {

            String mensagemPagamento = response.getBody().getMessage();
            String mensagemEstoque = estoqueResponse.getBody().getMessage();

            response.getBody().setMessage(mensagemPagamento.concat("|").concat(mensagemEstoque));
        }

        return response;
    }

    private ResponseEntity<PagamentoProcessadoResponseDTO> processarEstoque(PedidoRequestDTO pedido) {

        WebClient client = WebClient.create(ESTOQUE_BASE_URL);

        Mono<PagamentoProcessadoResponseDTO> responsePagamento = client.post().uri(PROCESSAR_ESTOQUE_API)
                .body(Mono.just(pedido), PedidoRequestDTO.class).retrieve()
                .bodyToMono(PagamentoProcessadoResponseDTO.class);

        PagamentoProcessadoResponseDTO response = responsePagamento.block();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private UUID processar(PedidoRequestDTO pedido) {

        UUID idPagamento = UUID.randomUUID();

        try {

            log.info("Processando pagamento...");
            log.debug("Pagamento número: {}", idPagamento);
            log.debug("Pedido completo: {}", pedido.toString());

            log.info("Estado 2: Aguardando confirmação do pagamento");

            Thread.sleep(TEMPO_PROCESSAMENTO);

            log.info("Estado 3: Pagamento confirmado");

        } catch (InterruptedException e) {

            log.warn("Interrupted!", e);

            Thread.currentThread().interrupt();
        }

        return idPagamento;
    }
}
