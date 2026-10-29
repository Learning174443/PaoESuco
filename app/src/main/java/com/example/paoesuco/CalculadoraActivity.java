package com.example.paoesuco;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paoesuco.databinding.ActivityCalculadoraBinding;

// Tela Calculadora. Baseada no exemplo calculadora_ref2026.
public class CalculadoraActivity extends AppCompatActivity {

    //1 criando as variáveis Java associadas aos elementos XML do layout
    private EditText cafe;
    private EditText almoco;
    private Button calcular;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityCalculadoraBinding binding = ActivityCalculadoraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        //2 associando as variáveis aos correspondentes elementos XML
        cafe = findViewById(R.id.cafeXML);
        almoco = findViewById(R.id.almocoXML);
        calcular = findViewById(R.id.calcularXML);
        result = findViewById(R.id.resultXML);

        //3 barra inferior: marca a aba desta tela e abre as outras telas com Intent
        binding.bottomNav.setSelectedItemId(R.id.nav_calculadora);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent it = null;

            if (id == R.id.nav_cardapio)
                it = new Intent(CalculadoraActivity.this, MainActivity.class);
            else if (id == R.id.nav_saldo)
                it = new Intent(CalculadoraActivity.this, SaldoActivity.class);
            else if (id == R.id.nav_recarga)
                it = new Intent(CalculadoraActivity.this, RecargaActivity.class);

            if (it != null) {
                startActivity(it);
                overridePendingTransition(0, 0);   // troca sem animação
                finish();                          // não empilha telas
            }
            return true;
        });

        //4 listener do botão Calcular
        calcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //5 verifica se os campos não estão vazios (igual ao exemplo)
                if (cafe.getText().length() == 0 || almoco.getText().length() == 0) {
                    //6 se algum não estiver preenchido mostra a mensagem pedindo o preenchimento
                    Toast.makeText(CalculadoraActivity.this, R.string.msg_campo_vazio,
                            Toast.LENGTH_LONG).show();
                } else {
                    try {
                        //7 se ok! captura o texto e converte para int
                        int nCafe = Integer.parseInt(cafe.getText().toString());
                        int nAlmoco = Integer.parseInt(almoco.getText().toString());

                        //8 calcula (café x R$2, almoço/jantar x R$3) e mostra no TextView
                        result.setText(RuUtils.calcular(nCafe, nAlmoco));
                    }
                    //9 se a entrada for inválida o catch captura a exceção e mostra uma mensagem
                    catch (NumberFormatException e) {
                        Toast.makeText(CalculadoraActivity.this, R.string.msg_numero_invalido,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
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
            // abre a tela Cardápio, que já baixa os dados ao abrir
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
