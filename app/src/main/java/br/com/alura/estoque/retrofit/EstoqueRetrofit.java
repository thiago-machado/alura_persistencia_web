package br.com.alura.estoque.retrofit;

import org.jetbrains.annotations.NotNull;

import br.com.alura.estoque.retrofit.service.ProdutoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
Segundo a documentação, é necessária primeiramente uma instância do Retrofit
com Builder() e a configuração de um Service.
Ele será a instância principal para toda a configuração de qualquer requisição HTTP que fizermos.

O Service será a entidade que manterá as possíveis requisições, e é desta forma que
identificamos os dois principais componentes do Retrofit. Precisaremos centralizar a
sua instância, assim como fazemos ao utilizarmos o "database", em que temos a classe
EstoqueDatabase com a configuração geral da instância.

O Retrofit é uma camada acima de okhttp, que é o User-Agent exibido na aba Request,
que realmente faz a execução, a requisição web.

 */
public class EstoqueRetrofit {

    private static final String URL_BASE = "http://192.168.0.105:8080/";
    private final ProdutoService produtoService;

    public EstoqueRetrofit() {

        OkHttpClient client = configuraClient();

        /*
        Criando uma instância do Retrofit utilizando um endereço URL raiz (Base URL)
         */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .client(client) // Logging Interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        /*
        Para usarmos a instância do nosso serviço ProdutoService, precisamos
        chamar o método create() do Retrofit, passando nosso serviço como parâmetro.
        Feito isso, basta deixarmos a instância do nosso serviço disponível para uso.
         */
        produtoService = retrofit.create(ProdutoService.class);
    }

    @NotNull
    private OkHttpClient configuraClient() {

        /*
        Existe uma biblioteca bastante utilizada em verificações para cada chamada que
        fazemos com o Retrofit.

        Esta biblioteca é denominada Logging interceptor, cuja proposta é interceptar
        todas as requisições feitas com o Retrofit com maior precisão, em específico o
        próprio OkHttp, agente principal responsável pelas requisições.
        Sendo assim, ele conseguirá fazer o log de tudo que estiver acontecendo.

        Para adicioná-lo, basta utilizar a dependência via Gradle.

        Criamos o client do OkHttpClient, o qual precisaremos adicionar e atrelar ao
        Retrofit, pois ele criará um client por padrão e precisaremos adicionar um
        com as modificações que queremos (neste caso, o Logging interceptor).
        Então, no processo de build incluiremos client(), que assim ficará
        acessível para ser adicionado.

        Com o Logging Interceptor, qualquer conexão realizada com Retrofit será exibida
        em logcat.
         */
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder().addInterceptor(logging).build();
    }

    public ProdutoService getProdutoService() {
        return produtoService;
    }
}
