/**
 * @author Nico Hezel
 */
package de.htw.ba.ue01;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Ue01_FelixLausch extends Application {

	@Override
	public void start(Stage stage) throws Exception {
				
		Parent ui = new FXMLLoader(getClass().getResource("EdgeDetectionView.fxml")).load();
		Scene scene = new Scene(ui);
		stage.setScene(scene);
		stage.setTitle("Kantendetektion - Felix Lausch");
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}