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

	private int startIndex = 0;//开始搜索的位置
	private int endIndex = 0;//结束搜索的位置
	private int scale = 100;//记录缩放大小,单位为%
	private double scalingfactor = 1;//记录缩放大小,单位为1
	File file;//记录当前的文件的保存地址
	private Map<String, Integer> sizeMap;//字号映射列表
	private FontWeight weiStyle = FontWeight.NORMAL;// 默认字形(非加粗)
	private FontPosture posStyle = FontPosture.REGULAR;// 默认字形(非斜体)
	private SelectedFont selectedFont = new SelectedFont();// 获取到的字体对象
	private double fontsize = selectedFont.getFontSize();//保存文字对象的字体大小
	private Clipboard clipboard;//剪贴板

	// 灰度控制
	public void initialize() {
		
		if((clipboard = Clipboard.getSystemClipboard())==null)Pasteitem.setDisable(true);
		// 初始状态下不可使用查找与替换功能
		Finditem.setDisable(true);
		Replaceitem.setDisable(true);
		// 初始状态不可使用撤销和重做功能
		Redoitem.setDisable(true);
		Undoitem.setDisable(true);
		Undoitem1.setDisable(true);

		//对鼠标单击进行监听，修改光标所在位置或菜单的可用
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

		// 对textarea的内容是否改变进行监听
		TextArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// 如果testarea中内容不为空,则可以使用查找与替换
				if (TextArea.getLength() > 0) {
					Finditem.setDisable(false);
					Replaceitem.setDisable(false);
				} // 否则禁用查找与替换
				else {
					Finditem.setDisable(true);
					Replaceitem.setDisable(true);
				}
				Redoitem.setDisable(false);
				Undoitem.setDisable(false);
				Undoitem1.setDisable(false);
				// 光标位置，需要注意的是，得到的position是textarea还没有改变之前的光标位置，所以要+1或-1
				if(newValue.length() >= oldValue.length())
					RawAndColCaculate(TextArea.getCaretPosition() + 1);
				else 
					RawAndColCaculate(TextArea.getCaretPosition() - 1);
			}
		});
		
	}
	
	//计算光标在几行几列的函数
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
			PositionLabel.setText(String.format("行%d，列%d", row, col));
		} catch (Exception e) {}
			
	}

	// 新建或打开前判断是否保存
	void SaveCheck() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("确认对话框");
    	alert.setHeaderText(null);
    	alert.setContentText("是否要保存更改？");

    	Optional<ButtonType> result = alert.showAndWait();
    	if (result.get() == ButtonType.OK){
    		if (file != null && TextArea.getLength() > 0) {
    			FileTools.writeFile(file, TextArea.getText());
    		} 
    		else if (file == null && TextArea.getLength() > 0) {
    			FileChooser fileChooser = new FileChooser();
    			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
    			fileChooser.getExtensionFilters().add(extFilter);
    			fileChooser.setTitle("保存当前内容");
    			file = fileChooser.showSaveDialog(null);
    			if (file != null) {
    				FileTools.writeFile(file, TextArea.getText());
    			}
    		}
    	}
	}

	//新建文件
	@FXML
	void NewFileClick(ActionEvent event) {
		SaveCheck();//判断是否保存
		TextArea.clear();
		file = null;
	}

	//打开文件
	@FXML
	void OpenFileClick(ActionEvent event) {
		SaveCheck();//判断是否保存
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		fileChooser.getExtensionFilters().add(extFilter);
		file = fileChooser.showOpenDialog(null);
		if (file != null) {
			TextArea.setText(FileTools.readFile(file));
		}
	}

	//保存文件
	@FXML
	void SaveFileClick(ActionEvent event) {
		if (file != null)// 如果已经存在保存路径
		{
			FileTools.writeFile(file, TextArea.getText());
		} 
		else// 如果不存在保存的路径
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

	//撤销
	@FXML
	void UndoClick(ActionEvent event) {
		TextArea.undo();
		Undoitem.setDisable(true);
		Undoitem1.setDisable(true);
	}

	//重做
	@FXML
	void RedoClick(ActionEvent event) {
		TextArea.redo();
		Redoitem.setDisable(true);
	}

	//查找
	@FXML
	void FindClick(ActionEvent event) {
		HBox h1 = new HBox();
		h1.setPadding(new Insets(20, 5, 20, 5));
		h1.setSpacing(5);
		Label lable1 = new Label("查找内容(N):");
		TextField tf1 = new TextField();
		h1.getChildren().addAll(lable1, tf1);

		VBox v1 = new VBox();
		v1.setPadding(new Insets(20, 5, 20, 10));
		v1.setSpacing(5);
		Button Usearch = new Button("查找上一个");
		Button Dsearch = new Button("查找下一个");
		Button Cancel = new Button("取消");
		v1.getChildren().addAll(Usearch, Dsearch, Cancel);

		VBox v2 = new VBox();
		v2.setPadding(new Insets(20, 5, 20, 5));
		v2.setSpacing(5);
		CheckBox circle = new CheckBox("循环");
		v2.getChildren().add(circle);

		HBox findRootNode = new HBox();
		findRootNode.getChildren().addAll(h1, v1, v2);

		Stage findStage = new Stage();
		Scene scene1 = new Scene(findRootNode, 450, 120);
		findStage.setTitle("查找");
		findStage.setScene(scene1);
		findStage.setResizable(false);
		findStage.show();
	
		// 向下查
		Dsearch.setOnAction((ActionEvent e) -> {
			
			String textString = TextArea.getText();
			String tfString = tf1.getText();

			if (!tf1.getText().isEmpty()) {
				if (textString.contains(tfString)) {
					startIndex = textString.indexOf(tfString, startIndex);
					if (startIndex == -1) // 没找到
					{
						if (circle.isSelected()) {
							startIndex = 0;
							endIndex = textString.length() - 1;
						}
						else {
							Alert alert1 = new Alert(AlertType.WARNING);
							alert1.titleProperty().set("提示");
							alert1.headerTextProperty().set("已经找不到相关内容了！");
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
					alert1.titleProperty().set("提示");
					alert1.headerTextProperty().set("未查到相关内容！");
					alert1.show();
					Dsearch.setDisable(true);
				}
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("错误信息");
				alert1.headerTextProperty().set("输入内容为空");
				alert1.show();
			}
		});

		// 向上查
		Usearch.setOnAction((ActionEvent e) -> {
			String textString = TextArea.getText(0, endIndex + 1);
			String tfString = tf1.getText();
			if (!tf1.getText().isEmpty()) {
				if (textString.contains(tfString)) {
					endIndex = textString.lastIndexOf(tfString);
					if (endIndex == -1) // 没找到
					{
						if (circle.isSelected()) {
							startIndex = 0;
							endIndex = textString.length() - 1;
						}
						else {
							Alert alert1 = new Alert(AlertType.WARNING);
							alert1.titleProperty().set("提示");
							alert1.headerTextProperty().set("已经找不到相关内容了！");
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
					alert1.titleProperty().set("提示");
					alert1.headerTextProperty().set("未找到相关内容！");
					alert1.show();
					Usearch.setDisable(true);
				}
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("错误信息");
				alert1.headerTextProperty().set("输入内容为空");
				alert1.show();
			}
		});

		// 取消
		Cancel.setOnAction((ActionEvent e) -> {
			findStage.close();
			startIndex = 0;
		});
	}

	//替换
	@FXML
	void ReplaceClick(ActionEvent event) {
		HBox h1 = new HBox();
		h1.setPadding(new Insets(20, 5, 10, 8));
		h1.setSpacing(5);
		Label label1 = new Label("查找内容:");
		TextField tf1 = new TextField();
		h1.getChildren().addAll(label1, tf1);

		HBox h2 = new HBox();
		h2.setPadding(new Insets(5, 5, 20, 8));
		h2.setSpacing(5);
		Label label2 = new Label("替换内容:");
		TextField tf2 = new TextField();
		h2.getChildren().addAll(label2, tf2);

		VBox v1 = new VBox();
		v1.getChildren().addAll(h1, h2);

		VBox v2 = new VBox();
		v2.setPadding(new Insets(21, 5, 20, 10));
		v2.setSpacing(13);
		Button search = new Button("查找下一个");
		Button replace = new Button("替换");
		Button rpAll = new Button("全部替换");
		Button Cancel = new Button("取消");
		v2.getChildren().addAll(search, replace,rpAll,Cancel);

		HBox replaceRootNode = new HBox();
		replaceRootNode.getChildren().addAll(v1, v2);

		Stage replaceStage = new Stage();
		Scene scene = new Scene(replaceRootNode, 430, 150);
		replaceStage.setTitle("替换");
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
						alert1.titleProperty().set("提示");
						alert1.headerTextProperty().set("已经找不到相关内容了！");
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
							alert1.titleProperty().set("出错了");
							alert1.headerTextProperty().set("替换内容为空");
							alert1.show();
						}
						else
						{   //若有选中的文本则替换
							if(TextArea.getSelectedText() != "")
								TextArea.replaceSelection(tf2.getText());
							
						}
						
					});
				}
				if (!textString.contains(tfString)) {
					Alert alert1 = new Alert(AlertType.WARNING);
					alert1.titleProperty().set("提示");
					alert1.headerTextProperty().set("未找到相关内容！");
					alert1.show();
				}
				
			} else if (tf1.getText().isEmpty()) {
				Alert alert1 = new Alert(AlertType.WARNING);
				alert1.titleProperty().set("出错了");
				alert1.headerTextProperty().set("输入内容为空");
				alert1.show();
			}
		});
		
		Cancel.setOnAction((ActionEvent e)->{
			replaceStage.close();
			startIndex = 0;
		});
	}
	
	//剪切
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
	
	//复制
	@FXML
	void CopyClick(ActionEvent event) {
		clipboard = Clipboard.getSystemClipboard();
		ClipboardContent copycontent = new ClipboardContent();

		String temp = TextArea.getSelectedText();
		// 将获取到的文本放到系统剪贴板中
		copycontent.putString(temp);
		// 把内容放到文本剪贴板中
		clipboard.setContent(copycontent);
		Pasteitem.setDisable(false);
	}
	
	//粘贴
	@FXML
	void PasteClick(ActionEvent event) {
		clipboard = Clipboard.getSystemClipboard();
		if (clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
			String s = clipboard.getContent(DataFormat.PLAIN_TEXT).toString();
			if (TextArea.getSelectedText() != null) { // 如果要粘贴时，鼠标已经选取了一段字符
				TextArea.replaceSelection(s);
			} else { // 如果要粘贴时，鼠标没有选取字符，直接从光标后开始粘贴
				int mouse = TextArea.getCaretPosition(); // 获取文本域鼠标所在位置
				TextArea.insertText(mouse, s);
			}
		}
	}
	
	//删除
	@FXML
	void DeleteClick(ActionEvent event) {
		TextArea.replaceSelection("");
	}
	
	//全选
	@FXML
	void SelectAllClick(ActionEvent event) {
		TextArea.selectAll();
	}
	
	//放大
	@FXML
	void ZoomInClick(ActionEvent event) {
		scale = scale + 10;
		scalingfactor += 0.1;
		selectedFont.setFontSize(weiStyle, posStyle, fontsize + (scalingfactor - 1) * 10);
		TextArea.setFont(selectedFont.getFont());
		ScaleLabel.setText(scale + "%");
	}
	
	//缩小
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
	
	//恢复默认缩放
	@FXML
	void DefaultZoomClick(ActionEvent event) {
		scale = 100;
		scalingfactor = 1;
		selectedFont.setFontSize(weiStyle, posStyle, fontsize);
		TextArea.setFont(selectedFont.getFont());
		ScaleLabel.setText(scale + "%");
	}
	
	//状态栏的展示控制
	@FXML
    void StateBarCheckItemClick(ActionEvent event) {
		if(StateBarCheck.isSelected())
			StateBar.setVisible(true);
		else StateBar.setVisible(false);
    }
	
	//自动换行是否选中
	@FXML
	void WrapLineCheckItemClick(ActionEvent event) {
		if(WrapLineCheck.isSelected())
			TextArea.setWrapText(true);
		else TextArea.setWrapText(false);
	}
	
	//字体选择
	@FXML
	void FontClick(ActionEvent event) {
		FontSetting();
	}
	
	//帮助对话框
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
		Info.setText(String.format("版本：1.0%n开发平台：Eclipse%n开发语言：Java%n开发者信息：陈雅喆"));
		
		Button Ok = new Button("确定");

		
		v1.getChildren().add(blg);
		v3.getChildren().add(Info);
		h1.getChildren().add(Ok);

		v.getChildren().addAll(v1,v2,v3,h1);

		Aboutbp.setCenter(v);

		
		Scene scene = new Scene(Aboutbp, 530, 530);
		Stage AboutStage = new Stage();
		AboutStage.setTitle("关于记事本");
		AboutStage.setScene(scene);
		AboutStage.show();

		Ok.setOnAction((ActionEvent e)->{
			AboutStage.close();
		});
	}
	
	//字体设置函数
	public void FontSetting() {
		// pane布局
		Pane p = new Pane();
		p.setPrefSize(531, 416);
		VBox v1 = new VBox();
		v1.setPrefSize(143, 237);
		Label titleFont = new Label("字体: ");
		TextField font = new TextField();
		ListView<Text> fontList = new ListView<Text>();
		v1.getChildren().addAll(titleFont, font, fontList);

		VBox v2 = new VBox();
		v2.setPrefSize(143, 237);
		Label titleStyle = new Label("字体: ");
		TextField style = new TextField();
		ListView<Text> styleList = new ListView<Text>();
		v2.getChildren().addAll(titleStyle, style, styleList);

		VBox v3 = new VBox();
		v3.setPrefSize(143, 237);
		Label titleSize = new Label("大小: ");
		TextField size = new TextField();
		ListView<String> sizeList = new ListView<String>();
		v3.getChildren().addAll(titleSize, size, sizeList);

		Label titleShow = new Label("当前示例:");
		TextField showField = new TextField();
		showField.setPrefSize(241, 114);
		showField.setText("Sammean Shaw");
		showField.setAlignment(Pos.CENTER);
		HBox h = new HBox();
		h.setSpacing(30);
		h.getChildren().addAll(titleShow, showField);

		Button sure = new Button("确定");
		Button cancel = new Button("取消");
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
		FontStage.setTitle("字体");
		FontStage.setScene(scene);

		// 引用自https://bbs.csdn.net/topics/390993580 Java字体选择器

		// 字体、字形、大小列表设置
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();
		ObservableList<Text> fList = FXCollections.observableArrayList();
		for (int i = 0; i < fontNames.length; i++) {
			// 将fontNames.length个Text对象添加到textList中
			fList.add(new Text(fontNames[i]));
			// 设置字体列表字体
			fList.get(i).setFont(Font.font(fontNames[i]));
		}
		// 将获得的字体列表放入字体ListView中
		fontList.setItems(fList);
		// 设置默认选中的字体
		fontList.getSelectionModel().selectFirst();
		// 显示选中的字体名称
		font.setText(fontList.getSelectionModel().getSelectedItem().getText());

		// 字体ListView监听事件
		fontList.getSelectionModel().selectedItemProperty().addListener(event -> {
			// 显示选中的字体名称
			font.setText(fontList.getSelectionModel().getSelectedItem().getText());
			// 更新新字体预览效果
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});

		// 实例化字形列表内容
		Text normal_regular = new Text("常规");
		Text bold_regular = new Text("加粗");
		Text normal_italic = new Text("斜体");
		Text bold_italic = new Text("粗斜体");
		// 设置字形列表字体
		normal_regular.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, FontPosture.REGULAR,
				Font.getDefault().getSize()));
		bold_regular.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, FontPosture.REGULAR,
				Font.getDefault().getSize()));
		normal_italic.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, FontPosture.ITALIC,
				Font.getDefault().getSize()));
		bold_italic.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, FontPosture.ITALIC,
				Font.getDefault().getSize()));
		// 将字体类型名称放入字形ListView中
		styleList.setItems(FXCollections.observableArrayList(normal_regular, bold_regular, normal_italic, bold_italic));
		// 设置默认选中的字称
		styleList.getSelectionModel().selectFirst();
		// 显示选中的字形名称
		style.setText(styleList.getSelectionModel().getSelectedItem().getText());
		// 字形ListView监听事件
		styleList.getSelectionModel().selectedItemProperty().addListener(event -> {
			String value = styleList.getSelectionModel().getSelectedItem().getText();
			if (value.equals("常规")) {
				weiStyle = FontWeight.NORMAL;
				posStyle = FontPosture.REGULAR;
			} else if (value.equals("加粗")) {
				weiStyle = FontWeight.BOLD;
				posStyle = FontPosture.REGULAR;
			} else if (value.equals("斜体")) {
				weiStyle = FontWeight.NORMAL;
				posStyle = FontPosture.ITALIC;
			} else if (value.equals("粗斜体")) {
				weiStyle = FontWeight.BOLD;
				posStyle = FontPosture.ITALIC;
			}
			style.setText(value);
			// 更新预览文字字形效果
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});
		// 字号字符串
		String sizeStr[] = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48",
				"72", "初号", "小初", "一号", "小一", "二号", "小二", "三号", "小三", "四号", "小四", "五号", "小五", "六号", "小六", "七号", "八号" };
		// 字号值
		int sizeValue[] = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72, 42, 36, 26, 24, 22, 18, 16,
				15, 14, 12, 11, 9, 8, 7, 6, 5 };
		sizeList.setItems(FXCollections.observableArrayList(sizeStr));

		// 实例化字号映射表
		sizeMap = new HashMap<String, Integer>();
		// 循环得到每个字号字符串对应的值
		for (int i = 0; i < sizeList.getItems().size(); ++i) {
			sizeMap.put(sizeStr[i], sizeValue[i]);
		}
		// 设置默认选中字号
		sizeList.getSelectionModel().select(4);
		// 显示选中的字号
		size.setText(sizeList.getSelectionModel().getSelectedItem());
		// 字号ListView监听事件
		sizeList.getSelectionModel().selectedItemProperty().addListener(event -> {
			size.setText(sizeList.getSelectionModel().getSelectedItem());
			showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
					sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		});

		// 示例文字初始化样式
		showField.setFont(Font.font(fontList.getSelectionModel().getSelectedItem().getText(), weiStyle, posStyle,
				sizeMap.get(sizeList.getSelectionModel().getSelectedItem())));
		FontStage.show();

		//设置文本框中的字体，并结合缩放比例来确定最终字体的大小
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
