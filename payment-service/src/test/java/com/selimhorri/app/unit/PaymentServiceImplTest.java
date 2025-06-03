package com.selimhorri.app.unit;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private RestTemplate restTemplate;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        restTemplate = mock(RestTemplate.class);
        paymentService = new PaymentServiceImpl(paymentRepository, restTemplate);
    }

    @Test
    void testSave() {
        PaymentDto paymentDto = PaymentDto.builder()
                .paymentId(1)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderDto(OrderDto.builder().orderId(10).build())
                .build();

        Payment payment = Payment.builder()
                .paymentId(1)
                .orderId(10)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.save(paymentDto);

        assertNotNull(result);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdate() {
        PaymentDto paymentDto = PaymentDto.builder()
                .paymentId(2)
                .isPayed(false)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .orderDto(OrderDto.builder().orderId(20).build())
                .build();

        Payment payment = Payment.builder()
                .paymentId(2)
                .orderId(20)
                .isPayed(false)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.update(paymentDto);

        assertNotNull(result);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testFindById() {
        Integer paymentId = 4;
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .orderId(40)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(java.util.Optional.of(payment));

        PaymentDto result = paymentService.findById(paymentId);

        assertNotNull(result);
        assertEquals(paymentId, result.getPaymentId());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void testDeleteById() {
        Integer paymentId = 3;

        paymentService.deleteById(paymentId);

        verify(paymentRepository, times(1)).deleteById(paymentId);
    }

    @Test
    void testFindAll() {
        Payment payment1 = Payment.builder()
                .paymentId(1)
                .orderId(10)
                .isPayed(true)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();
        
        Payment payment2 = Payment.builder()
                .paymentId(2)
                .orderId(20)
                .isPayed(false)
                .paymentStatus(PaymentStatus.IN_PROGRESS)
                .build();
        
        List<Payment> payments = Arrays.asList(payment1, payment2);
        
        when(paymentRepository.findAll()).thenReturn(payments);
        
        List<PaymentDto> result = paymentService.findAll();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(payment1.getPaymentId(), result.get(0).getPaymentId());
        assertEquals(payment2.getPaymentId(), result.get(1).getPaymentId());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdNotFound() {
        Integer paymentId = 999;
        
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            paymentService.findById(paymentId);
        });
        
        verify(paymentRepository, times(1)).findById(paymentId);
    }
}