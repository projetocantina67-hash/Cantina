package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.exception.ResourceNotFoundException;
import br.com.cantina.Cantina.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Transactional
    public Produto criarProduto(Produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProduto(Long id, Produto produto) {
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricao(produto.getDescricao());
        produtoExistente.setCategoriaProduto(produto.getCategoriaProduto());
        produtoExistente.setTempoPreparoMinutos(produto.getTempoPreparoMinutos());
        produtoExistente.setQuantidadeDisponivelHoje(produto.getQuantidadeDisponivelHoje());
        if (produto.getAtivo() != null) {
            produtoExistente.setAtivo(produto.getAtivo());
        }
        produtoExistente.setPreco(produto.getPreco());
        
        return produtoRepository.save(produtoExistente);
    }

    @Transactional
    public void excluirProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }
        produtoRepository.deleteById(id);
    }
}
