package br.com.cantina.Cantina.repository.entity;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;


@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nome;

    @Column
    private String cpf;

    @Column
    private boolean ativo;

    @Column
    private String senha;

    @Column
    private String telefone;

    @Column
    private String email;

    @Column
    private Perfil perfil;
}
