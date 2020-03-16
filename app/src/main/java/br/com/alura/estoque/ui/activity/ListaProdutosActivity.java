package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.R;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;
import retrofit2.Call;
import retrofit2.Response;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    private ProdutoDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();

        EstoqueDatabase db = EstoqueDatabase.getInstance(this);
        dao = db.getProdutoDAO();

        buscaProdutos();
    }

    private void buscaProdutos() {

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
        ProdutoService service = new EstoqueRetrofit().getProdutoService();
        Call<List<Produto>> call = service.buscaTodos();

        /*
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
                // primeiro listener que sera executado em doInBackground
                () -> {
                    try {
                        Response<List<Produto>> resposta = call.execute();
                        List<Produto> produtosNovos = resposta.body();
                        return produtosNovos;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                },

                // segundo listener que sera executado em onPostExecute
                produtosNovos -> {
                    if (produtosNovos != null) {
                        adapter.atualiza(produtosNovos);
                    } else {
                        Toast.makeText(this, "Não foi possível buscar os produtos da API",
                                Toast.LENGTH_SHORT).show();
                    }
                }).execute();

        /*new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> adapter.atualiza(resultado))
                .execute();*/
    }

    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(this::remove);
    }

    private void remove(int posicao,
                        Produto produtoRemovido) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produtoRemovido);
            return null;
        }, resultado -> adapter.remove(posicao))
                .execute();
    }

    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, this::salva).mostra();
    }

    private void salva(Produto produto) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, produtoSalvo ->
                adapter.adiciona(produtoSalvo))
                .execute();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoEditado -> edita(posicao, produtoEditado))
                .mostra();
    }

    private void edita(int posicao, Produto produto) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, produtoEditado ->
                adapter.edita(posicao, produtoEditado))
                .execute();
    }


}
