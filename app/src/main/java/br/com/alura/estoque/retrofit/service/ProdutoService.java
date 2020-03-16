package br.com.alura.estoque.retrofit.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.GET;

/*
Aqui nós inseriremos o código referente à definição das requisições
por meio de assinaturas, da documentação do Retrofit
 */
public interface ProdutoService {

    /*
    GET = requisições get
    O valor dentro de get é nosso endpoint (será concatenado com a BaseURL)

    A assinatura obrigatoriamente devolverá uma Call, que é a entidade que
    representará nossa requisição e permitirá que ela seja executada.

    Importaremos a Call de Retrofit e toda vez que a devolvemos, é obrigatório
    indicar que tipo de retorno é esperado. No nosso caso, é uma lista de produtos.


     */
    @GET("produto")
    Call<List<Produto>> buscaTodos();
}
