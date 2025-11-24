package com.example.actividadmqtt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MqttHelper mqttHelper;
    private TextView textViewReceived;
    private EditText editTextMessage;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewReceived = findViewById(R.id.textViewReceived);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        mqttHelper = new MqttHelper(getApplicationContext(), message ->
                runOnUiThread(() -> {
                    if (textViewReceived != null) {
                        String formatted = getString(R.string.received_message, message);
                        textViewReceived.append("\n" + formatted);
                    }
                })
        );

        buttonSend.setOnClickListener(v -> {
            if (mqttHelper == null) {
                Toast.makeText(this, "Cliente MQTT no inicializado", Toast.LENGTH_SHORT).show();
                return;
            }

            String msg = editTextMessage.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje antes de enviar", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!mqttHelper.isConnected()) {
                Toast.makeText(this, "MQTT no conectado aún", Toast.LENGTH_SHORT).show();
                return;
            }

            mqttHelper.publishMessage(msg);
            Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show();
        });
    }
}