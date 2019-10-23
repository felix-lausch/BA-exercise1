/**
 * @author Nico Hezel
 */
package de.htw.ba.ue01;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;

import de.htw.ba.ue01.EdgeDetectionController.Methods;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;


public abstract class EdgeDetectionBase {
	
	@FXML
	private ImageView leftImageView;
	
	@FXML
	private ImageView rightImageView;
	
	@FXML
	private ComboBox<Methods> methodSelection;
	
	@FXML
	private TextField parameter1Input;
	
	@FXML
	private Label runtimeLabel;
		
	@FXML
	public void initialize() {
		methodSelection.getItems().addAll(Methods.values());
		methodSelection.setValue(Methods.Kopie);
		methodSelection.setOnAction(this::applyFilter);
		loadImage(new File("360411_pixelio.png"));
	}

	public void onOpenFileClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(".")); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		loadImage(fileChooser.showOpenDialog(null));
	}
	
	public void loadImage(File file) {
		if(file != null) {
			leftImageView.setImage(new Image(file.toURI().toString()));
			applyFilter(null);
		}		
	}
	
	public int[] imageToPixel(Image image) {
		int width = (int)image.getWidth();
		int height = (int)image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return pixels;
	}
	
	public Image pixelToImage(int[] pixels, int width, int height) {
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();
		pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		return wr;
	}
	
	public abstract void runMethod(Methods currentMethod, int[] srcPixels, int[] dstPixels, int width, int height) throws Exception;
	
	@FXML
	public void applyFilter(ActionEvent event) {

		// no images loaded
		if(leftImageView.getImage() == null)
			return;
		
	  	// get image dimensions
    	int width = (int)leftImageView.getImage().getWidth();
    	int height = (int)leftImageView.getImage().getHeight();

    	// get pixels arrays
    	int[] srcPixels = imageToPixel(leftImageView.getImage());
    	int[] dstPixels = new int[width * height];
    	
		long startTime = System.currentTimeMillis();

		// get method choice 
		Methods currentMethod = methodSelection.getSelectionModel().getSelectedItem();
		try {
			runMethod(currentMethod, srcPixels, dstPixels, width, height);
		} catch (Exception e) { e.printStackTrace();
			e.printStackTrace();
		}
		
		rightImageView.setImage(pixelToImage(dstPixels, width, height));
    	runtimeLabel.setText("Methode " + currentMethod + " ausgeführt in " + (System.currentTimeMillis() - startTime) + " ms");
	}
    
	/**
	 * Gibt den in der GUI eingegebenen Parameter zurück
	 * 
	 * @return
	 * @throws ParseException
	 */
	protected double getParameter() throws ParseException {
		return NumberFormat.getNumberInstance(java.util.Locale.US).parse(parameter1Input.getText()).doubleValue();
	}

}
