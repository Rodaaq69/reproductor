import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReproductorDeMusica extends Application {

    private ListView<String> listaDeCanciones;
    private MediaPlayer mediaPlayer;
    private ProgressBar progressBar;
    private Slider volumenSlider;
    private Button botonReproducirAleatorio;
    private Button botonSiguiente;
    private Button botonAnterior;
    private Button botonPausar;
    private Label labelCancionActual;
    private List<File> canciones;
    private boolean isPlaying;
    private int currentSongIndex;

    @Override
    public void start(Stage primaryStage) {
        canciones = new ArrayList<>();
        isPlaying = false;
        currentSongIndex = -1;

        BorderPane root = new BorderPane();

        // Barra de herramientas
        HBox herramientas = new HBox(10);
        herramientas.setAlignment(Pos.CENTER);
        herramientas.setPadding(new Insets(10));

        Button botonAgregarCancion = new Button("Agregar canción");
        botonAgregarCancion.setOnAction(e -> agregarCancion());

        botonReproducirAleatorio = new Button("Reproducir aleatorio");
        botonReproducirAleatorio.setOnAction(e -> reproducirAleatorio());

        botonSiguiente = new Button("Siguiente");
        botonSiguiente.setOnAction(e -> siguienteCancion());

        botonAnterior = new Button("Anterior");
        botonAnterior.setOnAction(e -> anteriorCancion());

        botonPausar = new Button("Pausar");
        botonPausar.setOnAction(e -> pausarCancion());

        herramientas.getChildren().addAll(botonAgregarCancion, botonReproducirAleatorio, botonSiguiente, botonAnterior, botonPausar);

        root.setTop(herramientas);

        // Lista de canciones
        listaDeCanciones = new ListView<>();
        listaDeCanciones.setPrefWidth(200);
        listaDeCanciones.setPrefHeight(300);

        listaDeCanciones.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String seleccionada = listaDeCanciones.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    reproducirCancion(seleccionada);
                }
            }
        });

        root.setLeft(listaDeCanciones);

        // Barra de progreso y volumen
        HBox progresoYVolumen = new HBox(10);
        progresoYVolumen.setAlignment(Pos.CENTER);
        progresoYVolumen.setPadding(new Insets(10));

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);

        progressBar.setOnMouseClicked(e -> {
            double mouseX = e.getX();
            double progressBarWidth = progressBar.getWidth();
            double duration = mediaPlayer.getMedia().getDuration().toSeconds();
            double newTime = mouseX / progressBarWidth * duration;
            mediaPlayer.seek(Duration.seconds(newTime));
        });

        volumenSlider = new Slider(0, 1, 0.5);
        volumenSlider.setPrefWidth(100);

        labelCancionActual = new Label("No se está reproduciendo ninguna canción");
        labelCancionActual.setPrefWidth(200);

        progresoYVolumen.getChildren().addAll(progressBar, volumenSlider, labelCancionActual);

        root.setBottom(progresoYVolumen);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void agregarCancion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar canción");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos MP3", "*.mp3"));

        File archivoSeleccionado = fileChooser.showOpenDialog(null);
        if (archivoSeleccionado != null) {
            canciones.add(archivoSeleccionado);
            listaDeCanciones.getItems().add(archivoSeleccionado.getName());
        }
    }

    private void reproducirCancion(String nombreCancion) {
        if (isPlaying) {
            mediaPlayer.stop();
        }

        File archivo = null;
        for (File file : canciones) {
            if (file.getName().equals(nombreCancion)) {
                archivo = file;
                break;
            }
        }
    
        if (archivo != null) {
            Media media = new Media(archivo.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
    
            isPlaying = true;
    
            progressBar.setProgress(0);
            mediaPlayer.setOnReady(() -> {
                double duration = media.getDuration().toSeconds();
                progressBar.setMaxHeight(duration);
                mediaPlayer.currentTimeProperty().addListener((obs, oldValue, newValue) -> {
                    progressBar.setProgress(newValue.toSeconds() / duration);
                });
            });
    
            volumenSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                mediaPlayer.setVolume(newValue.doubleValue());
            });
    
            mediaPlayer.setOnEndOfMedia(() -> {
                isPlaying = false;
                siguienteCancion();
            });
    
            labelCancionActual.setText("Reproduciendo: " + nombreCancion);
        }
    }
    
    private void reproducirAleatorio() {
        if (!canciones.isEmpty()) {
            Collections.shuffle(canciones);
            reproducirCancion(canciones.get(0).getName());
        }
    }
    
    private void siguienteCancion() {
        if (!canciones.isEmpty()) {
            if (currentSongIndex == -1) {
                currentSongIndex = 0;
            } else {
                currentSongIndex = (currentSongIndex + 1) % canciones.size();
            }
            reproducirCancion(canciones.get(currentSongIndex).getName());
        }
    }
    
    private void anteriorCancion() {
        if (!canciones.isEmpty()) {
            if (currentSongIndex == -1) {
                currentSongIndex = canciones.size() - 1;
            } else {
                currentSongIndex = (currentSongIndex - 1 + canciones.size()) % canciones.size();
            }
            reproducirCancion(canciones.get(currentSongIndex).getName());
        }
    }
    
    private void pausarCancion() {
        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
    