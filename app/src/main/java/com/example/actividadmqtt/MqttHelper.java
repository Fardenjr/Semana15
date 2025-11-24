package com.example.actividadmqtt;

import android.util.Log;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.nio.charset.StandardCharsets;

public class MqttHelper {
    private static final String TAG = "MQTT";
    private static final String SERVER_URI = "da53951e1d5143b891928f79d75faff0.s1.eu.hivemq.cloud";
    private static final int PORT = 8883;
    private static final String TOPIC = "prueba1";

    private final Mqtt3AsyncClient client;
    private final MessageListener listener;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public MqttHelper(android.content.Context context, MessageListener listener) {
        this.listener = listener;

        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("AndroidHiveMQClient")
                .serverHost(SERVER_URI)
                .serverPort(PORT)
                .sslWithDefaultConfig()
                .simpleAuth()
                .username("prueba1")
                .password("Familyperalta2.".getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .buildAsync();

        connectAndSubscribe();
    }

    private void connectAndSubscribe() {
        client.connect()
                .thenAccept(connAck -> {
                    Log.i(TAG, "Conectado a HiveMQ Cloud");
                    subscribe();
                    publishMessage("Hola desde Android con HiveMQ!");
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error de conexión", throwable);
                    return null;
                });

        client.toAsync().publishes(MqttGlobalPublishFilter.ALL, publish -> {
            String payload = publish.getPayload()
                    .map(buffer -> {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        return new String(bytes, StandardCharsets.UTF_8);
                    })
                    .orElse("");

            Log.i(TAG, "Mensaje recibido: " + payload);

            if (listener != null) {
                try {
                    listener.onMessageReceived(payload);
                } catch (Exception e) {
                    Log.e(TAG, "Error al procesar mensaje recibido", e);
                }
            }
        });
    }

    public void publishMessage(String message) {
        if (!isConnected()) {
            Log.w(TAG, "Cliente no conectado. No se puede publicar.");
            return;
        }

        client.publishWith()
                .topic(TOPIC)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .send()
                .thenAccept(publishResult -> Log.i(TAG, "Mensaje publicado: " + message))
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error al publicar", throwable);
                    return null;
                });
    }

    public boolean isConnected() {
        return client.getState().isConnected();
    }

    private void subscribe() {
        client.subscribeWith()
                .topicFilter(TOPIC)
                .send()
                .thenAccept(subAck -> Log.i(TAG, "Suscripción exitosa al tópico: " + TOPIC))
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error al suscribirse", throwable);
                    return null;
                });
    }
}