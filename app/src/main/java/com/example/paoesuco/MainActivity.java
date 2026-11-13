package com.example.paoesuco;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paoesuco.databinding.ActivityMainBinding;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Tela Cardápio (tela inicial).
public class MainActivity extends AppCompatActivity {

    //1 variáveis das Views da tela
    private Spinner spinnerDatas;
    private ProgressBar progress;
    private TextView cardapioTxt;

    //1.1 variável String que recebe o conteúdo do site
    private String mFileContents;

    //1.2 lista com as datas disponíveis no site, no formato AAAA-MM-DD
    private ArrayList<String> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        //2 associando as variáveis às views no XML
        spinnerDatas = findViewById(R.id.spinnerDatasXML);
        progress = findViewById(R.id.progressXML);
        cardapioTxt = findViewById(R.id.cardapioXML);

        //3 barra inferior: marca a aba desta tela e abre as outras telas com Intent
        binding.bottomNav.setSelectedItemId(R.id.nav_cardapio);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent it = null;

            if (id == R.id.nav_calculadora)
                it = new Intent(MainActivity.this, CalculadoraActivity.class);
            else if (id == R.id.nav_saldo)
                it = new Intent(MainActivity.this, SaldoActivity.class);
            else if (id == R.id.nav_recarga)
                it = new Intent(MainActivity.this, RecargaActivity.class);

            if (it != null) {
                startActivity(it);
                overridePendingTransition(0, 0);   // troca sem animação
                finish();                          // não empilha telas
            }
            return true;
        });

        //4 quando o usuário escolhe uma data no Spinner, baixa o cardápio daquele dia
        spinnerDatas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                executeTask(getString(R.string.url_cardapio), datas.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //5 primeira carga: baixa a página do site para descobrir as datas disponíveis
        carregarDatas();
    }

    //6 limpa a lista e baixa a página inicial do site (GET, sem data)
    private void carregarDatas() {
        datas.clear();
        executeTask(getString(R.string.url_cardapio), null);
    }

    //7 método de download: baixa a página do site (se tiver data, envia por POST)
    private String downloadXMLFile(String theUrl, String data) {
        // precisamos usar um try pois as operações de abertura de conexão e leitura do stream de dados podem lançar exeções de IO.
        try {
            //cria um instância da classe URL com a url que será utilizada na conexão.
            URL myUrl = new URL(theUrl);

            //abre a conexão
            HttpURLConnection myconnection = (HttpURLConnection) myUrl.openConnection();

            //se tiver data, envia "data=AAAA-MM-DD" por POST (igual ao formulário do site)
            if (data != null) {
                myconnection.setRequestMethod("POST");
                myconnection.setDoOutput(true);
                OutputStream out = myconnection.getOutputStream();
                out.write(("data=" + data).getBytes());
                out.close();
            }

            //verifica se foi bem sucedida a conexão; se sim código 200
            int response = myconnection.getResponseCode();
            Log.d("DownloadData", "The response code is " + response);

            // cria a variável "dados" que irá receber o stream de bytes
            InputStream dados = myconnection.getInputStream();

            //usa a classe InputStreamReader para converter bytes em chars
            InputStreamReader caracteres = new InputStreamReader(dados);

            //variável para contagem de número de caracteres lidos
            int charRead;

            //criação de um array de char para leitura de 500 em 500 caracteres
            char[] inputBuffer = new char[500];

            //Criação de uma instância StringBuilder para formar a String final de interesse
            StringBuilder tempBuffer = new StringBuilder();

            //laço de leitura e formação da string
            while (true) {
                //lê os caracteres com tamanho máximo de inputBuffer(500) e informa o número lido
                charRead = caracteres.read(inputBuffer);

                //se -1 não há caracteres lidos e sai do laço.
                if (charRead <= 0) {
                    break;
                }

                //Enquanto charRead>0, vai "appendando" as leituras de 500 em 500 caracteres do inputBuffer
                tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
            //Retorna o conteúdo final do tempBuffer, convertido para String
            return tempBuffer.toString();
        } catch (IOException e) {
            Log.d("DownloadData", "IO Expection reading data: " + e.getMessage());
        } catch (SecurityException e) {
            Log.e("DownloadData", "Security exception. Needs permission?" + e.getMessage());
        }

        return null;
    }

    //8 executa o download numa thread paralela
    private void executeTask(String url, String data) {
        progress.setVisibility(View.VISIBLE);

        //Cria um executor com uma única thread para gerenciar tarefas.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //o executor submete a tarefa de download via metodo downloadXMLFile()
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return downloadXMLFile(url, data);
            }
        });

        //o executor submete a tarefa de processar o resultado, esperando o download terminar com future.get().
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mFileContents = future.get();
                    //as Views só podem ser alteradas na thread da tela, por isso runOnUiThread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mostrarResultado(mFileContents);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DownloadData", "Error executing task", e);
                }
            }
        });

        executor.shutdown();
    }

    //9 trata o HTML baixado: na primeira carga preenche o Spinner, depois mostra o cardápio
    private void mostrarResultado(String html) {
        progress.setVisibility(View.GONE);

        //9.1 erro no download (sem internet, site fora do ar...)
        if (html == null) {
            Toast.makeText(MainActivity.this, R.string.erro_cardapio, Toast.LENGTH_LONG).show();
            cardapioTxt.setText(R.string.erro_cardapio);
            return;
        }

        if (datas.isEmpty()) {
            //9.2 primeira carga: guarda as datas do site no ArrayList
            datas.addAll(RuUtils.extrairDatas(html));
            if (datas.isEmpty()) {
                cardapioTxt.setText(R.string.sem_cardapio);
                return;
            }

            //9.3 coloca as datas (formato dd/MM/aaaa) no Spinner.
            // Ao receber o adapter, o Spinner seleciona a 1ª data e chama o onItemSelected (passo 4).
            ArrayList<String> datasTela = new ArrayList<>();
            for (String d : datas) {
                datasTela.add(RuUtils.formatarData(d));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, datasTela);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDatas.setAdapter(adapter);
        } else {
            //9.4 cardápio de um dia: recorta o HTML e mostra no TextView
            String cardapio = RuUtils.extrairCardapio(html);
            if (cardapio == null) {
                cardapioTxt.setText(R.string.sem_cardapio);
            } else {
                cardapioTxt.setText(Html.fromHtml(cardapio, Html.FROM_HTML_MODE_LEGACY));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sobre) {
            Toast.makeText(this, R.string.msg_sobre, Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_atualizar) {
            //10 baixa tudo de novo
            cardapioTxt.setText(R.string.cardapio_carregando);
            carregarDatas();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
