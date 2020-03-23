package br.com.alura.estoque.retrofit.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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

    /*
    Precisamos fazer uma requisição que irá atender ao que nossa API espera,
    POST, a receber um produto via corpo da requisição, devolvendo um produto
    com ID esperado, isto é, gerado na API.

    Definiremos o tipo, @POST, que terá o endereço produto. A novidade aqui é
    entendermos como enviaremos o nosso objeto produto via corpo da requisição.

    Isto será feito via parâmetro, e indicaremos o que ele significa dentro da
    requisição. Já que ele fará parte do corpo, teremos uma notação @Body e então
    definiremos o retorno de um único produto, portanto usaremos um Generics.
     */
    @POST("produto")
    Call<Produto> salva(@Body Produto produto);

    /*
    O método PUT realizará uma alteração no servidor.
    {id} = define que estaremos enviando uma informação como parâmetro
    @Path("id") = estamos definindo que "long id" será inserido na URL da requisição, substituindo o {id}
    pelo ID do desejado.
    @Body = deine o corpo da requisição
     */
    @PUT("produto/{id}")
    Call<Produto> edita(@Path("id") long id, @Body Produto produto);

    /*
    Em situações em que fizermos requisições sem retorno no body(), podemos usar como referência o
    Void, como fazemos na Async Task.
     */
    @DELETE("produto/{id}")
    Call<Void> remove(@Path("id") long id);
}
