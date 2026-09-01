package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.database.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoService produtoService;

    private Produto produtoDeTeste;

    @BeforeEach
    void setUp() {
        produtoService = new ProdutoService(produtoRepository);

        produtoDeTeste = new Produto();
        produtoDeTeste.setId(1L);
        produtoDeTeste.setNome("Coxinha");
        produtoDeTeste.setDescricao("Coxinha de frango tradicional");
        produtoDeTeste.setPreco(new BigDecimal("6.50"));
        produtoDeTeste.setTempoPreparoMinutos(5);
        produtoDeTeste.setCategoriaProduto(CategoriaProduto.SALGADO);
        produtoDeTeste.setAtivo(true);
        produtoDeTeste.setQuantidadePadraoDiaria(20);
        produtoDeTeste.setQuantidadeDisponivelHoje(10);
    }

    @Test
    void baixarEstoque_deveDiminuirQuantidadeDisponivel_quandoHaEstoqueSuficiente() {
        when(produtoRepository.save(produtoDeTeste)).thenReturn(produtoDeTeste);

        produtoService.baixarEstoque(produtoDeTeste, 3);

        assertThat(produtoDeTeste.getQuantidadeDisponivelHoje()).isEqualTo(7);
    }

    @Test
    void baixarEstoque_deveLancarExcecao_quandoQuantidadeMaiorQueEstoque() {
        assertThatThrownBy(() -> produtoService.baixarEstoque(produtoDeTeste, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade indisponível");

        assertThat(produtoDeTeste.getQuantidadeDisponivelHoje()).isEqualTo(10);
    }

    @Test
    void baixarEstoque_deveLancarExcecao_quandoProdutoEstaInativo() {
        produtoDeTeste.setAtivo(false);

        assertThatThrownBy(() -> produtoService.baixarEstoque(produtoDeTeste, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void devolverEstoque_deveAumentarQuantidadeDisponivel() {
        when(produtoRepository.save(produtoDeTeste)).thenReturn(produtoDeTeste);

        produtoService.devolverEstoque(produtoDeTeste, 5);

        assertThat(produtoDeTeste.getQuantidadeDisponivelHoje()).isEqualTo(15);
    }

    @Test
    void resetarEstoqueDiario_deveChamarResetNoRepositorio() {
        produtoService.resetarEstoqueDiario();

        verify(produtoRepository, times(1)).resetarEstoqueDeProdutosAtivos();
    }
}
