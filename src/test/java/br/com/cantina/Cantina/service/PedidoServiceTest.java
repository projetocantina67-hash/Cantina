package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.database.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ProdutoService produtoService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepository, usuarioService, produtoService);
    }

    private Pedido criarPedidoComUmItem(StatusPedido statusInicial, int quantidade) {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Suco de Laranja");
        produto.setDescricao("Suco natural");
        produto.setPreco(new BigDecimal("5.00"));
        produto.setTempoPreparoMinutos(2);
        produto.setCategoriaProduto(CategoriaProduto.BEBIDA);
        produto.setQuantidadePadraoDiaria(30);
        produto.setQuantidadeDisponivelHoje(10);

        ItemPedido item = new ItemPedido();
        item.setId(1L);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setStatus(statusInicial);
        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setHorarioEstimadoRetirada(LocalDateTime.now().plusHours(1));
        pedido.setValorTotal(item.getSubtotal());
        pedido.adicionarItem(item);

        return pedido;
    }

    @Test
    void atualizarStatus_paraCancelado_deveDevolverEstoqueDeCadaItem() {
        Pedido pedido = criarPedidoComUmItem(StatusPedido.PENDENTE, 3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.atualizarStatus(1L, StatusPedido.CANCELADO);


        verify(produtoService, times(1)).devolverEstoque(any(Produto.class), eq(3));
    }

    @Test
    void atualizarStatus_paraCanceladoDeNovo_deveLancarExcecaoENaoDevolverEstoque() {
        Pedido pedido = criarPedidoComUmItem(StatusPedido.CANCELADO, 3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, StatusPedido.CANCELADO))
                .isInstanceOf(IllegalStateException.class);

        verify(produtoService, never()).devolverEstoque(any(Produto.class), anyInt());
    }

    @Test
    void atualizarStatus_paraProntoOuEntregue_naoDeveMexerNoEstoque() {
        Pedido pedido = criarPedidoComUmItem(StatusPedido.EM_PREPARO, 3);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.atualizarStatus(1L, StatusPedido.PRONTO);

        verifyNoInteractions(produtoService);
    }
}
