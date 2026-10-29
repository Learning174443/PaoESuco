package com.example.paoesuco;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paoesuco.databinding.ActivityRecargaBinding;

// Tela Recarga: instruções + botão que abre o app "Carteirinha".
public class RecargaActivity extends AppCompatActivity {

    //1 pacote do app "Carteirinha" na Play Store
    private static final String PACOTE = "com.inovatecnologia.carteirinha";

    private Button btnRecarga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityRecargaBinding binding = ActivityRecargaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        //2 associando a variável ao botão no XML
        btnRecarga = findViewById(R.id.recargaXML);

        //3 barra inferior: marca a aba desta tela e abre as outras telas com Intent
        binding.bottomNav.setSelectedItemId(R.id.nav_recarga);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent it = null;

            if (id == R.id.nav_cardapio)
                it = new Intent(RecargaActivity.this, MainActivity.class);
            else if (id == R.id.nav_calculadora)
                it = new Intent(RecargaActivity.this, CalculadoraActivity.class);
            else if (id == R.id.nav_saldo)
                it = new Intent(RecargaActivity.this, SaldoActivity.class);

            if (it != null) {
                startActivity(it);
                overridePendingTransition(0, 0);   // troca sem animação
                finish();                          // não empilha telas
            }
            return true;
        });

        //4 botão "Recarga PIX": abre o app; se não tiver, abre a Play Store; se não tiver Play Store, abre o site
        btnRecarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appIntent = getPackageManager().getLaunchIntentForPackage(PACOTE);

                if (appIntent != null) {
                    //4.1 app instalado: abre direto
                    startActivity(appIntent);
                } else {
                    try {
                        //4.2 app não instalado: abre a página dele na Play Store
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + PACOTE)));
                    } catch (ActivityNotFoundException e) {
                        //4.3 sem Play Store (ex.: emulador): abre o link no navegador
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + PACOTE + "&hl=pt_BR")));
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
