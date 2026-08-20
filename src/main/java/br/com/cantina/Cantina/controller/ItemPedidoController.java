package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.service.ItemPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/item-pedido")
public class ItemPedidoController {
    private final ItemPedidoService itemPedidoService;

    @Autowired
    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

    @GetMapping("/listar")
    public String listarItemPedido(Model model) {
        model.addAttribute("itensPedido", itemPedidoService.listAll());
        return "item-pedido/listar";
    }

    @GetMapping("/criar")
    public String mostrarFormularioCriarItemPedido(Model model) {
        model.addAttribute("itemPedido", new ItemPedido());
        return "item-pedido/criar";
    }

    @PostMapping("/criar")
    public String criarItemPedido(@Valid @ModelAttribute ItemPedido itemPedido, BindingResult resultado, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            model.addAttribute("itemPedido",itemPedido);
            return "item-pedido/criar";
        }
        itemPedidoService.criarItemPedido(itemPedido);
        flash.addFlashAttribute("mensagem", "Item do pedido criado com sucesso!");
        return "redirect:/item-pedido/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarItemPedido(@PathVariable Long id, Model model) {
        ItemPedido itemPedido = itemPedidoService.buscarPorId(id);

        if(itemPedido == null) {
            return "redirect:/item-pedido/listar";
        }

        model.addAttribute("itemPedido", itemPedido);

        return "item-pedido/editar";
    }

    @PutMapping("/{id}")
    public String atualizarItemPedido(@PathVariable Long id, @Valid @ModelAttribute ItemPedido itemPedido, BindingResult resultado, Model model, RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            itemPedido.setId(id);
            model.addAttribute("itemPedido", itemPedido);
            return "item-pedido/editar" ;
        }
        itemPedidoService.atualizarItemPedido(id, itemPedido);
        flash.addFlashAttribute("mensagem", "Item do pedido atualizado com sucesso!");
        return "redirect:/item-pedido/listar";
    }

    @PostMapping("/deletar/{id}")
    public String deletarItemPedido(@PathVariable Long id, RedirectAttributes flash) {
        itemPedidoService.excluirItemPedido(id);
        flash.addFlashAttribute("mensagem", "Item do pedido removido com sucesso!");
        return "redirect:/item-pedido/listar";
    }
}