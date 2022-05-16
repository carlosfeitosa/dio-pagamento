package com.dio.pagamento.servicebus;

import java.util.UUID;

import com.dio.pagamento.business.dto.PagamentoProcessadoResponseDTO;
import com.dio.pagamento.business.dto.PedidoCompletoRequestDTO;
import com.dio.pagamento.business.dto.PedidoRequestDTO;
import com.dio.pagamento.controller.PagamentoController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class QueueConsumer {

    private PagamentoController controller;
    private QueueSender queueSender;

    @RabbitListener(queues = { "${queue.name.pagamento}" })
    public void receive(@Payload String payload) {

        ObjectMapper objectMapper = new ObjectMapper();

        PedidoRequestDTO pedido = new PedidoRequestDTO();

        try {

            pedido = objectMapper.readValue(payload, PedidoRequestDTO.class);

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        ResponseEntity<PagamentoProcessadoResponseDTO> pagamento = controller.realizarPagamento(pedido);

        UUID idPagamento = UUID.fromString(pagamento.getBody().getMessage());

        PedidoCompletoRequestDTO pedidoCompleto = new PedidoCompletoRequestDTO(pedido, idPagamento);

        queueSender.send(pedidoToJson(pedidoCompleto));
    }

    private String pedidoToJson(PedidoCompletoRequestDTO pedido) {

        ObjectMapper mapper = new ObjectMapper();

        try {

            return mapper.writeValueAsString(pedido);

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return null;
    }
}
