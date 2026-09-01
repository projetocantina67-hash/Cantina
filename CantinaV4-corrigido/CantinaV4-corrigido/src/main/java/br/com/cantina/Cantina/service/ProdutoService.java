package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.DTO.CadastroProdutoDTO;
import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.database.repository.ProdutoRepository;
import br.com.cantina.Cantina.exception.RegistroNaoEncontradoException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Page<Produto> listar(CategoriaProduto categoria, Pageable pageable) {
        if (categoria != null) {
            return produtoRepository.findByAtivoAndCategoriaProduto(true, categoria, pageable);
        }
        return produtoRepository.findByAtivo(true, pageable);
    }

    public Page<Produto> listarParaAdmin(CategoriaProduto categoria, Pageable pageable) {
        if (categoria != null) {
            return produtoRepository.findByCategoriaProduto(categoria, pageable);
        }
        return produtoRepository.findAll(pageable);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Produto não encontrado"));
    }

    @Transactional
    public Produto cadastrar(CadastroProdutoDTO dto) {
        if (produtoRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Já existe um produto com esse nome");
        }

        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setTempoPreparoMinutos(dto.getTempoPreparoMinutos());
        produto.setQuantidadePadraoDiaria(dto.getQuantidadePadraoDiaria());
        produto.setQuantidadeDisponivelHoje(dto.getQuantidadePadraoDiaria());
        produto.setCategoriaProduto(dto.getCategoriaProduto());

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(Long id, CadastroProdutoDTO dto) {
        Produto produto = buscarPorId(id);
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setTempoPreparoMinutos(dto.getTempoPreparoMinutos());
        produto.setQuantidadePadraoDiaria(dto.getQuantidadePadraoDiaria());
        produto.setCategoriaProduto(dto.getCategoriaProduto());

        return produtoRepository.save(produto);
    }

    @Transactional
    public void baixarEstoque(Produto produto, int quantidade) {
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                    "Produto indisponível no momento: " + produto.getNome());
        }
        if (produto.getQuantidadeDisponivelHoje() < quantidade) {
            throw new IllegalArgumentException(
                    "Quantidade indisponível para o produto: " + produto.getNome());
        }
        produto.setQuantidadeDisponivelHoje(produto.getQuantidadeDisponivelHoje() - quantidade);
        produtoRepository.save(produto);
    }

    @Transactional
    public void devolverEstoque(Produto produto, int quantidade) {
        produto.setQuantidadeDisponivelHoje(produto.getQuantidadeDisponivelHoje() + quantidade);
        produtoRepository.save(produto);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }


    @Transactional
    public Produto ativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(true);
        return produtoRepository.save(produto);
    }

    @Transactional
    public void resetarEstoqueDiario() {
        produtoRepository.resetarEstoqueDeProdutosAtivos();
    }



}
