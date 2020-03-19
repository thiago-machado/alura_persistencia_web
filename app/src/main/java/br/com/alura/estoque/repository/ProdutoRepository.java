package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/*
Repositório a ser mantido pela Activity, que pedirá os dados para ele
e a lógica é mantida seja na API, no banco de dados, ou em qualquer tipo
de outra solução que armazene as informações dos nossos produtos.

Ou seja, incluiremos esta camada a mais em nosso aplicativo.

O Repository é um componente que lidará apenas com os dados, portanto chamadas
como atualiza() do Adapter, por exemplo, precisa ser feita para quem estiver
chamando o Repository, o qual não terá este tipo de responsabilidade.
 */
public class ProdutoRepository {

    /*
    No caso do DAO, será uma dependência, portanto criaremos um atributo de tipo ProdutoDAO.
    Podemos recebê-lo via construtor sem nenhum problema, ou então pedir um contexto e
    criar um banco de dados.
     */
    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        this.service = new EstoqueRetrofit().getProdutoService();
    }

    /*
    Neste momento, pediremos ao nosso cliente, que no caso é a nossa Activity,
    para que ela faça a implementação do Listener.

    Assim, mantemos uma única instância e, para cada comportamento, colocaremos
    o listener desejado, que teremos que delegar para os comportamentos mais
    internos, como é o caso da busca interna.

    Nosso Listener agora recebe e retorna um tipo Generics, e no caso, estamos
     definindo que iremos enviar e receber o tipo List<Produto>.
     */
    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {
        buscaProdutosInternos(listener);
    }

    private void buscaProdutosInternos(DadosCarregadosListener<List<Produto>> listener) {

        /*
        Criando AsyncTask para pegar os produtos salvos internamente.

        Por mais que tenhamos várias Async Tasks, elas não estão executando em paralelo.
        Isso quer dizer que, quando executamos várias Async Tasks, elas formam uma fila de
        execução em uma Thread separada, em background, e cada uma delas entrará em uma fila.

        Aqui, na nossa situação, nós criamos uma AsyncTask que primeiro faz uma busca interna e
        atualiza a visualização para o usuário.
        Em seguida, realizamos uma busca externa para pegar as informações e atualizá-las.
         */
        new BaseAsyncTask<>(dao::buscaTodos, // Fazendo a busca internamente e retornando todos os produtos
                produtos -> {
                    // Atualizando a lista de produtos (que foram pegos internamente logo acima) para visuzalização
                    listener.quandoCarregados(produtos);
                    buscaProdutosNaAPI(listener);
                }).execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosListener<List<Produto>> listener) {

        /*
        A implementação será feita com a instância de EstoqueRetrofit(), como
        havíamos comentado. Pegamos o getProdutoService() e o service, a partir do
        qual buscaremos a Call com buscaTodos().

        Para uma Call, existem duas alternativas — a execução síncrona e a assíncrona.
        Teremos os mesmos problemas vistos no curso de Room, que quando executávamos de
        maneira assíncrona na main thread, a tela poderia travar, resultando na interferência
        do próprio Room, a não ser que incluíssemos uma permissão.

        Neste caso, já que faremos uma requisição web, o próprio sistema operacional Android não
        permite este tipo de execução síncrona. Ou seja, faremos a execução síncrona também
        em uma Async Task.

        Precisaremos de dois Listeners, um para ser executado em background, cujo retorno
        devolveremos para que seja acessível para o outro.
        Este, por sua vez, fará uma execução deste retorno na UI thread. Faremos a execução
        síncrona via Call a partir do método execute(), que não pode ser usado de forma
        assíncrona, ou teremos uma exceção.

         */
        Call<List<Produto>> call = service.buscaTodos();

        /*
        Aqui foi inserida a AsyncTask que faz a busca externamente.

       O programa acusa um erro de compilação pois exige que se trate a Exception
       lançada, então usaremos um bloco Try/Catch. Cada vez que o execute() for rodado,
       teremos o retorno de uma entidade denominada response.

       O response mantém um Generics na Call, pois ele irá compor o conteúdo da resposta,
       seja Body, Header, entre outros. No caso, acessaremos o corpo, que contém nossos
       produtos, a serem retornados pelo Listener. Dessa forma, eles são enviados ao
       onPostExecute(), o qual faz o envio para o próximo Listener que estará sendo atualizado
       na UI thread.

       Estamos usando um Try/Catch, e pode ser que este retorno não chegue, por alguma falha
       de comunicação ou exceção. Para estes casos, é necessário declarar outro retorno, nulo.
        */
        new BaseAsyncTask<>(
                /*
                 Primeiro listener que sera executado em doInBackground
                 */
                () -> {
                    try {

                        Thread.sleep(3000);
                        Response<List<Produto>> resposta = call.execute();
                        List<Produto> produtosNovos = resposta.body();

                        /*
                        Após buscar os produtos na WEB, salva-os na base de dados.
                        Caso os produtos já existam, atualiza-os.
                         */
                        dao.salva(produtosNovos); // Mesmo que não exista produtos internos, a lista de produtos será VAZIA e não NULA
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*
                    Sempre retorna todos os produtos cadastrados na base de dados.
                    Mesmo que a conexão com a Internet retorne sucesso, ou não,
                    sempre buscará os produtos da base de dados.
                     */
                    return dao.buscaTodos();
                },

                /*
                Segundo listener que sera executado em onPostExecute
                 */
                produtosNovos -> {

                    listener.quandoCarregados(produtosNovos);


                    /*
                    O método executeOnExecutor com o valor AsyncTask.THREAD_POOL_EXECUTOR
                    significa que essa AsyncTask é uma execução fora do padrão e que não entrará na fila.
                     */
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        sakvaAPI(produto, callback);
    }

    private void sakvaAPI(Produto produto, DadosCarregadosCallback<Produto> callback) {

        /*
        Então, vamos entender como fazer tal implementação para que também não seja necessário nos
        atentarmos à parte do executeOnExecutor() da Async Task.

        Chamaremos a call e o método enqueue(), que fará a execução de maneira assíncrona,
        sem precisarmos de uma Async Task. Ele exige uma interface chamada Callback,
        que sempre receberá o Generics que temos em nossa Call, englobando um produto.

        Então, vamos entender como fazer tal implementação para que também não seja necessário
        nos atentarmos à parte do executeOnExecutor() da Async Task. Chamaremos a call e o método
        enqueue(), que fará a execução de maneira assíncrona, sem precisarmos de uma Async Task.

        Ele exige uma interface chamada Callback, que sempre receberá o Generics que temos em
        nossa Call, englobando um produto.

        Ambos são executados na UI Thread, sendo assim temos o mesmo resultado obtido no
        onPostExecute(), na interface listener::quandoCarregados.
        Ou seja, são comportamentos que podemos delegar diretamente à nossa Activity.
         */
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {

            @Override
            @EverythingIsNonNull // informando que nenhum parâmetro é NULL
            public void onResponse(Call<Produto> call, Response<Produto> response) {

                /*
                Já que recebemos o produto, podemos optar por salvar internamente e só depois notificarmos.

                Ele pegará o produtoSalvo proveniente da nossa API, que salvaremos internamente — então,
                substituiremos produto de dao.salva() do trecho acima por produtoSalvo.
                Outro detalhe, quando utilizamos o enqueue(), não precisamos chamar o execute() feito
                na Async Task. Além disso, sua Thread não é própria da Async Task, e sim uma nova Thread,
                desvinculada, executada em paralelo.
                 */
                if(response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if(produtoSalvo != null) {
                        salvaInternamente(produtoSalvo, callback);
                    }
                } else {
                    callback.quandoFalha("Resposta não esperada do servidor");
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call, Throwable t) {
                callback.quandoFalha("Erro na comunicação. Mensagem: " + t.getMessage());
            }
        });
    }

    private void salvaInternamente(Produto produtoSalvo, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() ->
        {
            long id = dao.salva(produtoSalvo);
            return dao.buscaProduto(id);
        }, produtoPersistido -> callback.quandoSucesso (produtoPersistido)).execute();
    }

    /*
    Utilizaremos a mesma técnica aplicada em Listeners, sendo assim teremos um Listener próprio
    para o repositório.

    Teremos que reutilizar este Listener nos pontos em que esta notificação é necessária e a
    Activity o implementará e fará a atualização.

    Receberemos esse listener no momento em que chamamos os nossos produtos, isto é, em buscaProdutos(...).


    Dado que queremos justamente flexibilizar o uso do nosso Listener para "n" situações,
    uma das abordagens que podemos considerar é criar um Listener genérico.
    Da mesma forma como criamos uma Async Task genérica, implementaremos uma interface
    genérica que pode receber qualquer tipo (T) e retornar qualquer tipo (T resultado).

    O T vem do Type, que no caso é tipo genérico.
     */
    public interface DadosCarregadosListener<T> {
        void quandoCarregados(T resultado);
    }

    public interface DadosCarregadosCallback <T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
