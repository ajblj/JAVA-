package notePad;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class NotePadController {

	@FXML private BorderPane bp;
    @FXML private MenuItem Undoitem;
    @FXML private MenuItem Undoitem1;
    @FXML private Label ScaleLabel;
    @FXML private MenuItem OpenFileitem;
    @FXML private MenuItem Redoitem;
    @FXML private MenuItem SaveFileitem;
    @FXML private MenuItem NewFileitem;
    @FXML private Label PositionLabel;
    @FXML private MenuItem Finditem;
    @FXML private MenuItem Replaceitem;
    @FXML private TextArea TextArea;
    @FXML private HBox StateBar;
    @FXML private CheckMenuItem StateBarCheck;
    @FXML private CheckMenuItem WrapLineCheck;
    @FXML private MenuItem Cutitem;
    @FXML private MenuItem Cutitem1;
    @FXML private MenuItem Copyitem;
    @FXML private MenuItem Copyitem1;
    @FXML private MenuItem Pasteitem;
    @FXML private MenuItem Deleteitem;
    @FXML private MenuItem Deleteitem1;
    @FXML private MenuItem SelectAllitem;
    @FXML private MenuItem Fontitem;
    @FXML private MenuItem ZoomInitem;
    @FXML private MenuItem ZoomOutitem;
    @FXML private MenuItem DefaultZoomitem;
    @FXML private MenuItem AboutNoteitem;

	private int startIndex = 0;//��ʼ������λ��
	private int endIndex = 0;//����������λ��
	private int scale = 100;//��¼���Ŵ�С,��λΪ%
	private double scalingfactor = 1;//��¼���Ŵ�С,��λΪ1
	File file;//��¼��ǰ���ļ��ı����ַ
	private Map<String, Integer> sizeMap;//�ֺ�ӳ���б�
	private FontWeight weiStyle = FontWeight.NORMAL;// Ĭ������(�ǼӴ�)
	private FontPosture posStyle = FontPosture.REGULAR;// Ĭ������(��б��)
	private SelectedFont selectedFont = new SelectedFont();// ��ȡ�����������
	private double fontsize = selectedFont.getFontSize();//�������ֶ���������С
	private Clipboard clipboard;//������

	// �Ҷȿ���
	public void initialize() {
		
		if((clipboard = Clipboard.getSystemClipboard())==null)Pasteitem.setDisable(true);
		// ��ʼ״̬�²���ʹ�ò������滻����
		Finditem.setDisable(true);
		Replaceitem.setDisable(true);
		// ��ʼ״̬����ʹ�ó�������������
		Redoitem.setDisable(true);
		Undoitem.setDisable(true);
		Undoitem1.setDisable(true);

		//����굥�����м������޸Ĺ������λ�û�˵��Ŀ���
		TextArea.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				RawAndColCaculate(TextArea.getCaretPosition());
			}
			if(!TextArea.getSelectedText().isEmpty())
			{
				Copyitem.setDisable(false); Copyitem1.setDisable(false);
				Cutitem.setDisable(false); Cutitem1.setDisable(false);
				Deleteitem.setDisable(false); Deleteitem1.setDisable(false);
			}
			else
			{
				Copyitem.setDisable(true); Copyitem1.setDisable(true);
				Cutitem.setDisable(true); Cutitem1.setDisable(true);
				Deleteitem.setDisable(true); Deleteitem1.setDisable(true);
			}
		});

		// ��textarea�������Ƿ�ı���м���
		TextArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// ���testarea�����ݲ�Ϊ��,�����ʹ�ò������滻
				if (TextArea.getLength() > 0) {
					Finditem.setDisable(false);
					Replaceitem.setDisable(false);
				} // ������ò������滻
				else {
					Finditem.setDisable(true);
					Replaceitem.setDisable(true);
				}
				Redoitem.setDisable(false);
				Undoitem.setDisable(false);
				Undoitem1.setDisable(false);
				// ���λ�ã���Ҫע����ǣ��õ���position��textarea��û�иı�֮ǰ�Ĺ��λ�ã�����Ҫ+1��-1
				if(newValue.length() >= oldValue.length())
					RawAndColCaculate(TextArea.getCaretPosition() + 1);
				else 
					RawAndColCaculate(TextArea.getCaretPosition() - 1);
			}
		});
		
	}
	
	//�������ڼ��м��еĺ���
	void RawAndColCaculate(int position) {
		try {
			int col, row;
			String strA, strB;
			String[] strings;
			strA = TextArea.getText();
			strB = strA.substring(0, position);
			strB = strB + "0";
			strings = strB.split("\n");
			row = strings.length;
			col = strings[row - 1].length();
			PositionLabel.setText(String.format("��%d����%d", row, col));
		} catch (Exception e) {}
			
	}

	// �½����ǰ�ж��Ƿ񱣴�
	void SaveCheck() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("ȷ�϶Ի���");
    	alert.setHeaderText(null);
    	alert.setContentText("�Ƿ�Ҫ������ģ�");

    	Optional<ButtonType> result = alert.showAndWait();
    	if (result.get() == ButtonType.OK){
    		if (file != null && TextArea.getLength() > 0) {
    			FileTools.writeFile(file, TextArea.getText());
    		} 
    		else if (file == null && TextArea.getLength() > 0) {
    			FileChooser fileChooser = new FileChooser();
    			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
    			fileChooser.getExtensionFilters().add(extFilter);
    			fileChooser.setTitle("���浱ǰ����");
    			file = fileChooser.showSaveDialog(null);
    			if (file != null) {
    				FileTools.writeFile(file, TextArea.getText());
    			}
    		}
    	}
	}

	//�½��ļ�
	@FXML
	void NewFileClick(ActionEvent event) {
		SaveCheck();//�ж��Ƿ񱣴�
		TextArea.clear();
		file = null;
	}

	//���ļ�
	@FXML
	void OpenFileClick(ActionEvent event) {
		SaveCheck();//�ж��Ƿ񱣴�
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		file = fileChooser.showOpenDialog(null);
		if (file != null) {
			TextArea.setText(FileTools.readFile(file));
		}
	}

	//�����ļ�
	@FXML
	void SaveFileClick(ActionEvent event) {
		if (file != null)// ����Ѿ����ڱ���·��
		{
			FileTools.writeFile(file, TextArea.getText());
		} 
		else// ��������ڱ����·��
		{
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
			fileChooser.getExtensionFilters().add(extFilter);
			file = fileChooser.showSaveDialog(null);
			if (file != null) {
				FileTools.writeFile(file, TextArea.getText());
			}
		}
	}

	//����
	@FXML
	void UndoClick(ActionEvent event) {
		TextArea.undo();
		Undoitem.setDisable(true);
		Undoitem1.setDisable(true);
	}

	//����
	@FXML
	void RedoClick(ActionEvent event) {
		TextArea.redo();
		Redoitem.setDisable(true);
	}

	//����
	@FXML
	void FindClick(ActionEvent event) {
		HBox h1 = new HBox();
		h1.setPadding(new Insets(20, 5, 20, 5));
		h1.setSpacing(5);
		Label lable1 = new Label("��������(N):");
		TextField tf1 = new TextField();
		h1.getChildren().addAll(lable1, tf1);

		VBox v1 = new VBox();
		v1.setPadding(new Insets(20, 5, 20, 10));
		v1.setSpacing(5);
		Button Usearch = new Button("������һ��");
		Button Dsearch = new Button("������һ��");
		Button Cancel = new Button("ȡ��");
		v1.getChildren().addAll(Usearch, Dsearch, Cancel);

		VBox v2 = new VBox();
		v2.setPadding(new Insets(20, 5, 20, 5));
		v2.setSpacing(5);
		CheckBox circle = new CheckBox("ѭ��");
		v2.getChildren().add(circle);

		HBox findRootNode = new HBox();
		findRootNode.getChildren().addAll(h1, v1, v2);

		Stage findStage = new Stage();
		Scene scene1 = new Scene(findRootNode, 450, 120);
		findStage.setTitle("����");
		findStage.setScene(scene1);
		findStage.setResizable(false);
		findStage.show();
	
		// ���²�
		Dsearch.setOnAction((ActionEvent e) -> {
			
			String textString = TextArea.getText();
			String tfString = tf1.getText();

			if (!tf1.getText().isEmpty()) {
				if (textString.contains(tfString)) {
					startIndex = textString.indexOf(tfString, startIndex);
					if (startIndex == -1) // û�ҵ�
					{
						if (circle.isSelected()) {
							startIndex = 0;
							endIndex = textString.length() - 1;
						}
						else {
							Alert alert1 = new Alert(AlertType.WARNING);
							alert1.titleProperty().set("��ʾ");
							alert1.headerTextProperty().set("�Ѿ��Ҳ�����������ˣ�");
							alert1.show();
							Dsearch.setDisable(true);
						}
					}
					else if (startIndex >= 0 && startIndex < textString.length()) {
						Usearch.setDisable(false);
						TextArea.selectRange(startIndex, startIndex + tfString.length());
						startIndex += tfString.length();
						if((startIndex - tfString.length() - 1) > 0)
							endIndex = startIndex - tfString.length() - 1;
						else
							endIndex = textString.length() - 1;
					}
				}
				else {
					Alert alert1 = new Alert(AlertType.WARNING);
					alert1.titleProperty().set("��ʾ");
					alert1.headerTextProperty().set("δ�鵽������ݣ�");
					alert1.show();
					Dsearch.setDisable(true);
				}
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("������Ϣ");
				alert1.headerTextProperty().set("��������Ϊ��");
				alert1.show();
			}
		});

		// ���ϲ�
		Usearch.setOnAction((ActionEvent e) -> {
			String textString = TextArea.getText(0, endIndex + 1);
			String tfString = tf1.getText();
			if (!tf1.getText().isEmpty()) {
				if (textString.contains(tfString)) {
					endIndex = textString.lastIndexOf(tfString);
					if (endIndex == -1) // û�ҵ�
					{
						if (circle.isSelected()) {
							startIndex = 0;
							endIndex = textString.length() - 1;
						}
						else {
							Alert alert1 = new Alert(AlertType.WARNING);
							alert1.titleProperty().set("��ʾ");
							alert1.headerTextProperty().set("�Ѿ��Ҳ�����������ˣ�");
							alert1.show();
							Usearch.setDisable(true);
						}
					}
					else if (endIndex >= 0 && endIndex < TextArea.getText().length()) {
						Dsearch.setDisable(false);
						TextArea.selectRange(endIndex, endIndex + tfString.length());
						endIndex -= 1;
						if((endIndex + tfString.length() + 1) < TextArea.getText().length())
							startIndex = endIndex + tfString.length() + 1;
						else
							startIndex = 0;
					}
					
				}
				if (!textString.contains(tfString)) {
					Alert alert1 = new Alert(AlertType.WARNING);
					alert1.titleProperty().set("��ʾ");
					alert1.headerTextProperty().set("δ�ҵ�������ݣ�");
					alert1.show();
					Usearch.setDisable(true);
				}
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("������Ϣ");
				alert1.headerTextProperty().set("��������Ϊ��");
				alert1.show();
			}
		});

		// ȡ��
		Cancel.setOnAction((ActionEvent e) -> {
			findStage.close();
			startIndex = 0;
		});
	}

	//�滻
	@FXML
	void ReplaceClick(ActionEvent event) {
		HBox h1 = new HBox();
		h1.setPadding(new Insets(20, 5, 10, 8));
		h1.setSpacing(5);
		Label label1 = new Label("��������:");
		TextField tf1 = new TextField();
		h1.getChildren().addAll(label1, tf1);

		HBox h2 = new HBox();
		h2.setPadding(new Insets(5, 5, 20, 8));
		h2.setSpacing(5);
		Label label2 = new Label("�滻����:");
		TextField tf2 = new TextField();
		h2.getChildren().addAll(label2, tf2);

		VBox v1 = new VBox();
		v1.getChildren().addAll(h1, h2);

		VBox v2 = new VBox();
		v2.setPadding(new Insets(21, 5, 20, 10));
		v2.setSpacing(13);
		Button search = new Button("������һ��");
		Button replace = new Button("�滻");
		Button rpAll = new Button("ȫ���滻");
		Button Cancel = new Button("ȡ��");
		v2.getChildren().addAll(search, replace,rpAll,Cancel);

		HBox replaceRootNode = new HBox();
		replaceRootNode.getChildren().addAll(v1, v2);

		Stage replaceStage = new Stage();
		Scene scene = new Scene(replaceRootNode, 430, 150);
		replaceStage.setTitle("�滻");
		replaceStage.setScene(scene);
		replaceStage.setResizable(false); 
		replaceStage.show();
		
		rpAll.setOnAction((ActionEvent ae)->{
			
			String afterRp = TextArea.getText().replaceAll(tf1.getText(), tf2.getText());
			TextArea.setText(afterRp);
		});

		search.setOnAction((ActionEvent e) -> {
			String textString = TextArea.getText(); 
			String tfString = tf1.getText();
			if (!tf1.getText().isEmpty()) {
				
				rpAll.setOnAction((ActionEvent ae)->{
					
					String afterRp=TextArea.getText().replaceAll(tf1.getText(), tf2.getText());
					TextArea.setText(afterRp);
				});
				if (textString.contains(tfString)) {
					startIndex = TextArea.getText().indexOf(tf1.getText(),startIndex);
					if (startIndex == -1) {
						Alert alert1 = new Alert(AlertType.WARNING);
						alert1.titleProperty().set("��ʾ");
						alert1.headerTextProperty().set("�Ѿ��Ҳ�����������ˣ�");
						alert1.show();
					}
					if (startIndex >= 0 && startIndex < TextArea.getText().length()) {
						TextArea.selectRange(startIndex, startIndex+tf1.getText().length());
						startIndex += tf1.getText().length(); 
					}
					replace.setOnAction((ActionEvent e2) -> {
						if(tf2.getText().isEmpty()) 
						{
							Alert alert1 = new Alert(AlertType.WARNING);
							alert1.titleProperty().set("������");
							alert1.headerTextProperty().set("�滻����Ϊ��");
							alert1.show();
						}
						else
						{   //����ѡ�е��ı����滻
							if(TextArea.getSelectedText() != "")
								TextArea.replaceSelection(tf2.getText());
							
						}
						
					});
				}
				if (!textString.contains(tfString)) {
					Alert alert1 = new Alert(AlertType.WARNING);
					alert1.titleProperty().set("��ʾ");
					alert1.headerTextProperty().set("δ�ҵ�������ݣ�");
					alert1.show();
				}
				
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("������");
				alert1.headerTextProperty().set("��������Ϊ��");
				alert1.show();
			}
		});
		
		Cancel.setOnAction((ActionEvent e)->{
			replaceStage.close();
			startIndex = 0;
		});
	}
	
	//����
	@FXML
	void CutClick(ActionEvent event) {
		clipboard = Clipboard.getSystemClipboard();
		ClipboardContent cutcontent = new ClipboardContent();
		String temp = TextArea.getSelectedText();
		cutcontent.putString(temp);
		clipboard.setContent(cutcontent);
		TextArea.replaceSelection("");
		Pasteitem.setDisable(false);
	}
	
	//����
	@FXML
	void CopyClick(ActionEvent event) {
		clipboard = Clipboard.getSystemClipboard();
		ClipboardContent copycontent = new ClipboardContent();

		String temp = TextArea.getSelectedText();
		// ����ȡ�����ı��ŵ�ϵͳ��������
		copycontent.putString(temp);
		// �����ݷŵ��ı���������
		clipboard.setContent(copycontent);
		Pasteitem.setDisable(false);
	}
	
	//ճ��
	@FXML
	void PasteClick(ActionEvent event) {
		clipboard = Clipboard.getSystemClipboard();
		if (clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
			String s = clipboard.getContent(DataFormat.PLAIN_TEXT).toString();
			if (TextArea.getSelectedText() != null) { // ���Ҫճ��ʱ������Ѿ�ѡȡ��һ���ַ�
				TextArea.replaceSelection(s);
			} else { // ���Ҫճ��ʱ�����û��ѡȡ�ַ���ֱ�Ӵӹ���ʼճ��
				int mouse = TextArea.getCaretPosition(); // ��ȡ�ı����������λ��
				TextArea.insertText(mouse, s);
			}
		}
	}
	
	//ɾ��
	@FXML
	void DeleteClick(ActionEvent event) {
		TextArea.replaceSelection("");
	}
	
	//ȫѡ
	@FXML
	void SelectAllClick(ActionEvent event) {
		TextArea.selectAll();
	}
	
	//�Ŵ�
	@FXML
	void ZoomInClick(ActionEvent event) {
		scale = scale + 10;
		scalingfactor += 0.1;
		selectedFont.setFontSize(weiStyle, posStyle, fontsize + (scalingfactor - 1) * 10);
		TextArea.setFont(selectedFont.getFont());
		ScaleLabel.setText(scale + "%");
	}
	
	//��С
	@FXML
	void ZoomOutClick(ActionEvent event) {
		if(scale > 10) {
			scale = scale - 10;
			scalingfactor -= 0.1;
			selectedFont.setFontSize(weiStyle, posStyle, fontsize + (scalingfactor - 1) * 10);
			TextArea.setFont(selectedFont.getFont());
			ScaleLabel.setText(scale + "%");
		}
	}
	
	//�ָ�Ĭ������
	@FXML
	void DefaultZoomClick(ActionEvent event) {
		scale = 100;
		scalingfactor = 1;
		selectedFont.setFontSize(weiStyle, posStyle, fontsize);
		TextArea.setFont(selectedFont.getFont());
		ScaleLabel.setText(scale + "%");
	}
	
	//״̬����չʾ����
	@FXML
    void StateBarCheckItemClick(ActionEvent event) {
		if(StateBarCheck.isSelected())
			StateBar.setVisible(true);
		else StateBar.setVisible(false);
    }
	
	//�Զ������Ƿ�ѡ��
	@FXML
	void WrapLineCheckItemClick(ActionEvent event) {
		if(WrapLineCheck.isSelected())
			TextArea.setWrapText(true);
		else TextArea.setWrapText(false);
	}
	
	//����ѡ��
	@FXML
	void FontClick(ActionEvent event) {
		FontSetting();
	}
	
	//�����Ի���
	@FXML
	void AboutNoteClick(ActionEvent event) {
		BorderPane Aboutbp=new BorderPane();
		VBox v=new VBox();
		v.setSpacing(10);
		VBox v1=new VBox();
		VBox v2=new VBox();
		VBox v3=new VBox();
		HBox h1=new HBox();

		v1.setPadding(new Insets(20, 5, 20, 200));
		v2.setPadding(new Insets(20,5,20,10));
		v3.setPadding(new Insets(20, 5, 20, 100));
		h1.setPadding(new Insets(100,10,10,400));
		
		Label blg=new Label();
		Label Info=new Label();
		Info.setText(String.format("�汾��1.0%n����ƽ̨��Eclipse%n�������ԣ�Java%n��������Ϣ�����ņ�"));
		
		Button Ok = new Button("ȷ��");

		
		v1.getChildren().add(blg);
		v3.getChildren().add(Info);
		h1.getChildren().add(Ok);

		v.getChildren().addAll(v1,v2,v3,h1);

		Aboutbp.setCenter(v);

		
		Scene scene = new Scene(Aboutbp, 530, 530);
		Stage AboutStage = new Stage();
		AboutStage.setTitle("���ڼ��±�");
		AboutStage.setScene(scene);
		AboutStage.show();

		Ok.setOnAction((ActionEvent e)->{
			AboutStage.close();
		});
	}
	
	//�������ú���
	public void FontSetting() {
		// pane����
		Pane p = new Pane();
		p.setPrefSize(531, 416);
		VBox v1 = new VBox();
		v1.setPrefSize(143, 237);
		Label titleFont = new Label("����: ");
		TextField font = new TextField();
		ListView<Text> fontList = new ListView<Text>();
		v1.getChildren().addAll(titleFont, font, fontList);

		VBox v2 = new VBox();
		v2.setPrefSize(143, 237);
		Label titleStyle = new Label("����: ");
		TextField style = new TextField();
		ListView<Text> styleList = new ListView<Text>();
		v2.getChildren().addAll(titleStyle, style, styleList);

		VBox v3 = new VBox();
		v3.setPrefSize(143, 237);
		Label titleSize = new Label("��С: ");
		TextField size = new TextField();
		ListView<String> sizeList = new ListView<String>();
		v3.getChildren().addAll(titleSize, size, sizeList);

		Label titleShow = new Label("��ǰʾ��:");
		TextField showField = new TextField();
		showField.setPrefSize(241, 114);
		showField.setText("Sammean Shaw");
		showField.setAlignment(Pos.CENTER);
		HBox h = new HBox();
		h.setSpacing(30);
		h.getChildren().addAll(titleShow, showField);

		Button sure = new Button("ȷ��");
		Button cancel = new Button("ȡ��");
		sure.setPrefSize(71, 30);
		cancel.setPrefSize(74, 30);

		p.getChildren().addAll(v1, v2, v3, h, sure, cancel);
		v1.setLayoutX(14);
		v1.setLayoutY(14);
		v2.setLayoutX(189);
		v2.setLayoutY(14);
		v3.setLayoutX(366);
		v3.setLayoutY(14);
		h.setLayoutX(14);
		h.setLayoutY(272);
		sure.setLayoutX(350);
		sure.setLayoutY(356);
		cancel.setLayoutX(430);
		cancel.setLayoutY(356);

		Scene scene = new Scene(p, 550, 420);
		Stage FontStage = new Stage();
		FontStage.setTitle("����");
		FontStage.setScene(scene);

		// ������https://bbs.csdn.net/topics/390993580 Java����ѡ����

		// ���塢���Ρ���С�б�����
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		ObservableList<Text> fList = FXCollections.observableArrayList();
		for (int i = 0; i < fontNames.length; i++) {
			// ��fontNames.length��Text������ӵ�textList��
			fList.add(new Text(fontNames[i]));
			// ���������б�����
			fList.get(i).setFont(Font.font(fontNames[i]));
		}
		// ����õ������б��������ListView��
		fontList.setItems(fList);
		// ����Ĭ��ѡ�е�����
		fontList.getSelectionModel().selectFirst();
		// ��ʾѡ�е���������
		font.setText(fontList.getSelectionModel().getSelectedItem().getText());

		// ����ListView�����¼�
		fontList.getSelectionModel().selectedItemProperty().addListener(event -> {
			// ��ʾѡ�е���������
			font.setText(fontList.getSelectionModel().getSelectedItem().getText());
			// ����������Ԥ��Ч��
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});

		// ʵ���������б�����
		Text normal_regular = new Text("����");
		Text bold_regular = new Text("�Ӵ�");
		Text normal_italic = new Text("б��");
		Text bold_italic = new Text("��б��");
		// ���������б�����
		normal_regular.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, FontPosture.REGULAR,
				Font.getDefault().getSize()));
		bold_regular.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, FontPosture.REGULAR,
				Font.getDefault().getSize()));
		normal_italic.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, FontPosture.ITALIC,
				Font.getDefault().getSize()));
		bold_italic.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, FontPosture.ITALIC,
				Font.getDefault().getSize()));
		// �������������Ʒ�������ListView��
		styleList.setItems(FXCollections.observableArrayList(normal_regular, bold_regular, normal_italic, bold_italic));
		// ����Ĭ��ѡ�е��ֳ�
		styleList.getSelectionModel().selectFirst();
		// ��ʾѡ�е���������
		style.setText(styleList.getSelectionModel().getSelectedItem().getText());
		// ����ListView�����¼�
		styleList.getSelectionModel().selectedItemProperty().addListener(event -> {
			String value = styleList.getSelectionModel().getSelectedItem().getText();
			if (value.equals("����")) {
				weiStyle = FontWeight.NORMAL;
				posStyle = FontPosture.REGULAR;
			} else if (value.equals("�Ӵ�")) {
				weiStyle = FontWeight.BOLD;
				posStyle = FontPosture.REGULAR;
			} else if (value.equals("б��")) {
				weiStyle = FontWeight.NORMAL;
				posStyle = FontPosture.ITALIC;
			} else if (value.equals("��б��")) {
				weiStyle = FontWeight.BOLD;
				posStyle = FontPosture.ITALIC;
			}
			style.setText(value);
			// ����Ԥ����������Ч��
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});
		// �ֺ��ַ���
		String sizeStr[] = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48",
				"72", "����", "С��", "һ��", "Сһ", "����", "С��", "����", "С��", "�ĺ�", "С��", "���", "С��", "����", "С��", "�ߺ�", "�˺�" };
		// �ֺ�ֵ
		int sizeValue[] = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72, 42, 36, 26, 24, 22, 18, 16,
				15, 14, 12, 11, 9, 8, 7, 6, 5 };
		sizeList.setItems(FXCollections.observableArrayList(sizeStr));

		// ʵ�����ֺ�ӳ���
		sizeMap = new HashMap<String, Integer>();
		// ѭ���õ�ÿ���ֺ��ַ�����Ӧ��ֵ
		for (int i = 0; i < sizeList.getItems().size(); ++i) {
			sizeMap.put(sizeStr[i], sizeValue[i]);
		}
		// ����Ĭ��ѡ���ֺ�
		sizeList.getSelectionModel().select(4);
		// ��ʾѡ�е��ֺ�
		size.setText(sizeList.getSelectionModel().getSelectedItem());
		// �ֺ�ListView�����¼�
		sizeList.getSelectionModel().selectedItemProperty().addListener(event -> {
			size.setText(sizeList.getSelectionModel().getSelectedItem());
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});

		// ʾ�����ֳ�ʼ����ʽ
		showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
				sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		FontStage.show();

		//�����ı����е����壬��������ű�����ȷ����������Ĵ�С
		sure.setOnAction(event -> {
			selectedFont = new SelectedFont();
			selectedFont.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem()) + (scalingfactor - 1) * 10));
			fontsize = selectedFont.getFontSize();
			TextArea.setFont(selectedFont.getFont());
			FontStage.close();

		});
		cancel.setOnAction(event -> {
			FontStage.close();
		});
	}
}
