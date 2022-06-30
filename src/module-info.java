module TermProject {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires java.desktop;
	requires javafx.graphics;
	
	opens notePad to javafx.graphics, javafx.fxml;
}
