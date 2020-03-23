package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * Essa classe foi criada para tratar das requisiçoes que não possuem
 * retorno. Tanto que a implementação de Callback recebe um Void.
 *
 * Até o momento, somente o método remove() utiliza essa Callback
 */
public class CallbackSemRetorno implements Callback<Void> {

    private final RespostaCallback callback;

    public CallbackSemRetorno(RespostaCallback callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.isSuccessful()){
            callback.quandoSucesso();
        } else {
            callback.quandoFalha("Resposta não esperada do servidor");
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<Void> call, Throwable t) {
        callback.quandoFalha("Erro na comunicação. Mensagem: " + t.getMessage());
    }

    public interface RespostaCallback {
        void quandoSucesso();
        void quandoFalha(String erro);
    }
}
