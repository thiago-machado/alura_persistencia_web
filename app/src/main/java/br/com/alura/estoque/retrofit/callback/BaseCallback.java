package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/*
Como vimos, temos implementações de Callbacks que são bem similares entre si.
De que maneira podemos trabalhar com uma única implementação, a ser reutilizada?

Assim como fizemos em BaseAsyncTask(), criaremos uma referência genérica o suficiente
para que seja reutilizada em qualquer situação, evitando repetições desnecessárias de código.

Para isto, em "app > java > br.com.alura.estoque > retrofit" criaremos o pacote "callback",
em que serão implementados outros, caso necessário.

Nele, criamos essa interface BaseCallback, que precisará receber um tipo genérico
e implementará a Callback do Retrofit, além dos métodos onResponse() e onFailure().

Incluiremos as mesmas chamadas de ProdutoRepository.java, comuns à qualquer uma das
implementações, no caso, verificar a resposta bem sucedida, se o conteúdo é diferente de
nulo, para então passar à ação desejada.

BaseCallback<T> =  Recebe um tipo genérico
Callback<T> = retorna um tipo genérico
 */
public class BaseCallback<T> implements Callback<T> {

    private final RespostaCallback<T> callback;

    public BaseCallback(RespostaCallback<T> callback) {
        this.callback = callback;
    }


    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()) {
            T resultado = response.body();
            if(resultado != null) {
                callback.quandoSucesso(resultado);
            }
        } else {
            callback.quandoFalha("Resposta não esperada do servidor");
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        callback.quandoFalha("Erro na comunicação. Mensagem: " + t.getMessage());
    }

    /*
    Feito isso, precisaremos incluir um Callback específico para notificar e
    fazer uso desta implementação genérica.

    E então poderemos utilizar a mesma técnica de BaseAsyncTask.java para que
    isto seja um atributo de classe que exija tal implementação. Ou seja,
    vamos forçar a implementação dessa interface no Construtor da nossa BaseCallback
     */
    public interface RespostaCallback<T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
