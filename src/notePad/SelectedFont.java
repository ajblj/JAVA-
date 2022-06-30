package notePad;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class SelectedFont {

    private Font font;
    
    SelectedFont(){
    	font = Font.getDefault();
    }
    
    public Font getFont() { return font;}
    
    public double getFontSize() {return font.getSize();}
    
    public void setFontSize(FontWeight weiStyle, FontPosture posStyle, double size) {
    	this.font = Font.font(font.getName(), weiStyle, posStyle, size);
    }

    public void setFont(Font font) { this.font = font;}
}
