package gui;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class crop extends Application {
    public void createNewUserScene(Stage primaryStage) {
        FileChooser imageChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");
        FileChooser.ExtensionFilter extFilterpng = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        imageChooser.getExtensionFilters().addAll(extFilterJPG, extFilterjpg, extFilterPNG, extFilterpng);

        File file = imageChooser.showOpenDialog(primaryStage);
        try {
            ImageView temp = new ImageView(SwingFXUtils.toFXImage(ImageIO.read(file), null));
            temp.setFitHeight(400);
            temp.setPreserveRatio(true);
            BufferedImage pic = SwingFXUtils.fromFXImage(temp.snapshot(null, null), null);
            Group group = new Group(temp);
            Scene scene = new Scene(group, pic.getWidth(), pic.getHeight());
            scene.setRoot(group);
            primaryStage.setScene(scene);
            // https://stackoverflow.com/questions/30993681/how-to-make-a-javafx-image-crop-app
        } catch (IOException e) {}

        

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Chat-Clientv0.1");
        createNewUserScene(primaryStage);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
