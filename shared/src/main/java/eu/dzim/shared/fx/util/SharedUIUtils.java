package eu.dzim.shared.fx.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Injector;

import eu.dzim.shared.fx.text.TextFlowService;
import eu.dzim.shared.fx.ui.LighweightDialog;
import eu.dzim.shared.fx.ui.SharedUIComponentType;
import eu.dzim.shared.fx.ui.controller.SimpleDialogController;
import eu.dzim.shared.resource.BaseResource;
import eu.dzim.shared.resource.Resource;
import eu.dzim.shared.util.BaseEnumType;
import eu.dzim.shared.util.IconEnumType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SharedUIUtils {
	
	private static final Logger LOG = LogManager.getLogger(SharedUIUtils.class);
	
	protected SharedUIUtils() {
		// sonar
	}
	
	public static final void appendIcon(final Labeled node, final Path path) {
		SwingUtilities.invokeLater(() -> {
			final BufferedImage bImg = additionalFileSystemIcon(path);
			PlatformHelper.run(() -> {
				ImageView iv = new ImageView(SwingFXUtils.toFXImage(bImg, null));
				iv.setFitWidth(16);
				iv.setFitHeight(20);
				node.setGraphic(iv);
			});
		});
	}
	
	private static final BufferedImage additionalFileSystemIcon(Path path) {
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
		// alternative: icon = new JFileChooser().getIcon(path.toFile());
		if (!(icon instanceof ImageIcon))
			return null;
		java.awt.Image awtImage = ((ImageIcon) icon).getImage();
		BufferedImage bImg;
		if (awtImage instanceof BufferedImage) {
			bImg = (BufferedImage) awtImage;
		} else {
			bImg = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = bImg.createGraphics();
			graphics.drawImage(awtImage, 0, 0, null);
			graphics.dispose();
		}
		return bImg;
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle,
			String keyMessage) {
		return simplePromptRes(stage, baseResource, resource, keyTitle, keyMessage, Duration.millis(150));
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle, String keyMessage,
			Duration duration) {
		return simplePromptRes(stage, baseResource, resource, keyTitle, keyMessage, "generic.yes", "generic.no", "generic.cancel", duration);
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle, String keyMessage,
			boolean cancelable) {
		return simplePromptRes(stage, baseResource, resource, keyTitle, keyMessage, cancelable, Duration.millis(150));
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle, String keyMessage,
			boolean cancelable, Duration duration) {
		return simplePromptRes(stage, baseResource, resource, keyTitle, keyMessage, "generic.yes", "generic.no", cancelable ? "generic.cancel" : null,
				duration);
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle, String keyMessage,
			String baseKeyYes, String baseKeyNo, String baseKeyCancel) {
		return simplePromptRes(stage, baseResource, resource, keyTitle, keyMessage, baseKeyYes, baseKeyNo, baseKeyCancel, Duration.millis(150));
	}
	
	public static Optional<ButtonType> simplePromptRes(Stage stage, BaseResource baseResource, Resource resource, String keyTitle, String keyMessage,
			String baseKeyYes, String baseKeyNo, String baseKeyCancel, Duration duration) {
		return simplePrompt(stage, keyTitle == null ? null : resource.getGuaranteedString(keyTitle),
				keyMessage == null ? null : resource.getGuaranteedString(keyMessage),
				baseKeyYes == null ? null : baseResource.getGuaranteedString(baseKeyYes),
				baseKeyNo == null ? null : baseResource.getGuaranteedString(baseKeyNo),
				baseKeyCancel == null ? null : baseResource.getGuaranteedString(baseKeyCancel), duration);
	}
	
	public static Optional<ButtonType> simplePrompt(Stage stage, String title, String message, String yes, String no, String cancel) {
		return simplePrompt(stage, title, message, yes, no, cancel, Duration.millis(150));
	}
	
	public static Optional<ButtonType> simplePrompt(Stage stage, String title, String message, String yes, String no, String cancel,
			Duration duration) {
		
		FXMLLoader loader = new FXMLLoader(SharedUIUtils.class.getResource(SharedUIComponentType.SIMPLE_DIALOG.getAbsoluteLocation()));
		BorderPane dialogPane;
		SimpleDialogController dialogController;
		try {
			dialogPane = loader.load();
			dialogController = loader.getController();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return Optional.empty();
		}
		
		ButtonType yesType = new ButtonType(yes, ButtonData.YES);
		ButtonType noType = new ButtonType(no, ButtonData.NO);
		ButtonType cancelType = new ButtonType(cancel, ButtonData.CANCEL_CLOSE);
		
		List<ButtonType> buttons = cancel != null ? Arrays.asList(yesType, noType, cancelType) : Arrays.asList(yesType, noType);
		final LighweightDialog dialog = new LighweightDialog(title, stage, buttons.stream().toArray(size -> new ButtonType[size]));
		dialog.setResizable(false);
		dialog.setDraggable(false);
		dialog.setChromeVisible(false);
		dialog.setUseTransition(true);
		dialog.setTransitionDuration(duration);
		dialog.setClosable(false);
		dialog.setModal(true);
		
		dialogController.setDialogType(AlertType.CONFIRMATION);
		dialogController.setTitle(title);
		dialogController.setMessage(message);
		
		dialog.setContentPane(dialogPane);
		return dialog.show();
	}
	
	public static void simpleAlertRes(Stage stage, AlertType type, Resource resource, String keyTitle, String keyMessage, Object... messageParams) {
		simpleAlert(stage, type, resource.getGuaranteedString(keyTitle), String.format(resource.getGuaranteedString(keyMessage), messageParams));
	}
	
	public static void simpleAlert(Stage stage, AlertType type, String title, String message) {
		
		FXMLLoader loader = new FXMLLoader(SharedUIUtils.class.getResource(SharedUIComponentType.SIMPLE_DIALOG.getAbsoluteLocation()));
		BorderPane dialogPane;
		SimpleDialogController dialogController;
		try {
			dialogPane = loader.load();
			dialogController = loader.getController();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return;
		}
		
		final LighweightDialog dialog = new LighweightDialog(title, stage, ButtonType.OK);
		dialog.setResizable(false);
		dialog.setDraggable(false);
		dialog.setChromeVisible(false);
		dialog.setUseTransition(true);
		dialog.setTransitionDuration(Duration.millis(150));
		dialog.setClosable(false);
		dialog.setModal(true);
		
		dialogController.setDialogType(type);
		dialogController.setTitle(title);
		dialogController.setMessage(message);
		
		dialog.setContentPane(dialogPane);
		dialog.show();
	}
	
	public static FileChooser getSaveFileChooser(Resource resource, String keyTitle, Path defaultPath, String keyExtensionDescription,
			String... extensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(resource.getGuaranteedString(keyTitle));
		if (keyExtensionDescription != null && !keyExtensionDescription.isEmpty())
			fileChooser.getExtensionFilters().add(new ExtensionFilter(resource.getGuaranteedString(keyExtensionDescription), extensions));
		fileChooser.setInitialDirectory(defaultPath.toFile());
		return fileChooser;
	}
	
	public static FileChooser getOpenFileChooser(Resource resource, String keyTitle, Path defaultPath, String keyExtensionDescription,
			String... extensions) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(resource.getGuaranteedString(keyTitle));
		if (keyExtensionDescription != null && !keyExtensionDescription.isEmpty())
			fileChooser.getExtensionFilters().add(new ExtensionFilter(resource.getGuaranteedString(keyExtensionDescription), extensions));
		fileChooser.setInitialDirectory(defaultPath.toFile());
		return fileChooser;
	}
	
	public static DirectoryChooser getDirectoryChooser(Resource resource, String keyTitle, Path defaultPath) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(resource.getGuaranteedString(keyTitle));
		dirChooser.setInitialDirectory(defaultPath.toFile());
		return dirChooser;
	}
	
	public static void bindIntText(Dirtyable controller, Dirty applicationModel, TextField textField, IntegerProperty property) {
		textField.setText(property.get() + "");
		property.bind(bindInt(controller, applicationModel, textField));
	}
	
	public static IntegerBinding bindInt(Dirtyable controller, Dirty applicationModel, TextField textField) {
		return Bindings.createIntegerBinding(new IntegerCallable(controller, applicationModel, textField), textField.textProperty());
	}
	
	public static void bindDoubleText(Dirtyable controller, Dirty applicationModel, TextField textField, DoubleProperty property) {
		textField.setText(property.get() + "");
		property.bind(bindDouble(controller, applicationModel, textField));
	}
	
	public static DoubleBinding bindDouble(Dirtyable controller, Dirty applicationModel, TextField textField) {
		return Bindings.createDoubleBinding(new DoubleCallable(controller, applicationModel, textField), textField.textProperty());
	}
	
	public static void bindStringText(Dirtyable controller, Dirty applicationModel, TextField textField, StringProperty property) {
		textField.setText(property.get());
		property.bind(bindString(controller, applicationModel, textField));
	}
	
	public static StringBinding bindString(Dirtyable controller, Dirty applicationModel, TextField textField) {
		return Bindings.createStringBinding(new StringCallable(controller, applicationModel, textField), textField.textProperty());
	}
	
	public static <T extends BaseEnumType> void updateComboBox(ComboBox<T> comboBox, ObjectProperty<T> property, ChangeListener<T> changeHandler) {
		comboBox.valueProperty().removeListener(changeHandler);
		comboBox.getSelectionModel().clearSelection();
		comboBox.setValue(property.get());
		property.bind(comboBox.valueProperty());
		comboBox.valueProperty().addListener(changeHandler);
	}
	
	public static <T extends BaseEnumType> void updateToggleGroup(ToggleGroup toggleGroup, Enum<?> enumeration, ObjectProperty<T> property,
			ChangeListener<Toggle> changeHandler) {
		toggleGroup.selectedToggleProperty().removeListener(changeHandler);
		toggleGroup.getToggles().stream().filter(t -> property.get() != null && enumeration.name().equals(((ToggleButton) t).getId()))
				.forEach(t -> toggleGroup.selectToggle(t));
		toggleGroup.selectedToggleProperty().addListener(changeHandler);
	}
	
	public static void updateCheckBox(CheckBox checkBox, BooleanProperty property, ChangeListener<Boolean> changeHandler) {
		checkBox.selectedProperty().removeListener(changeHandler);
		checkBox.selectedProperty().unbind();
		checkBox.setSelected(property.get());
		property.bind(checkBox.selectedProperty());
		checkBox.selectedProperty().addListener(changeHandler);
	}
	
	public static void setTexts(Injector injector, Labeled labeled, TextFlow var, TextFlow unit, String resourceKeyPrefix) {
		String normalizedKey = resourceKeyPrefix + (!resourceKeyPrefix.endsWith(".") ? "." : "");
		Resource resource = injector.getInstance(Resource.class);
		if (labeled != null) {
			String labeledKey = normalizedKey;
			if (labeled instanceof Label)
				labeledKey += "label";
			else if (labeled instanceof CheckBox)
				labeledKey += "checkBox";
			else if (labeled instanceof ButtonBase)
				labeledKey += "button";
			labeled.setText(resource.getGuaranteedString(labeledKey));
		}
		TextFlowService textFlowService = injector.getInstance(TextFlowService.class);
		if (var != null)
			var.getChildren().setAll(textFlowService.listFromString(resource.getGuaranteedString(normalizedKey + "v")));
		if (unit != null)
			unit.getChildren().setAll(textFlowService.listFromString(resource.getGuaranteedString(normalizedKey + "u")));
	}
	
	public static RadioButton createRadioButton(Resource resource, ToggleGroup toggleGroup, String id, BaseEnumType type) {
		RadioButton rb = new RadioButton();
		rb.getStyleClass().add("toggle-button-transparent");
		rb.setToggleGroup(toggleGroup);
		rb.setId(id);
		rb.setText(resource.getGuaranteedString(type.getKey()));
		return rb;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends BaseEnumType> void enrichBaseEnumTypeCombo(Resource resource, ComboBox<T> combo, ObservableList<T> items) {
		combo.setItems(items);
		combo.setCellFactory(param -> new EnumListCell(resource));
		combo.setButtonCell(new EnumListCell(resource));
	}
	
	public static ToggleButton createToggleButton(Resource resource, ToggleGroup toggleGroup, String id, IconEnumType icon, double height) {
		ToggleButton tb = new ToggleButton();
		tb.getStyleClass().add("toggle-button-transparent");
		tb.setToggleGroup(toggleGroup);
		tb.setId(id);
		tb.setUserData(icon);
		setGraphicNodeOn(resource, tb, icon, height);
		return tb;
	}
	
	private static void setGraphicNodeOn(Resource resource, Labeled node, IconEnumType iconType, double height) {
		ImageView iv = new ImageView(new Image(IconEnumType.class.getResourceAsStream(iconType.getIconRecource())));
		iv.setPreserveRatio(true);
		iv.setFitHeight(height);
		node.setGraphicTextGap(0.0);
		node.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		node.setGraphic(iv);
		if (iconType instanceof BaseEnumType) {
			Tooltip tooltip = new Tooltip();
			tooltip.textProperty().bind(resource.getBinding(((BaseEnumType) iconType).getKey()));
			node.setTooltip(tooltip);
		}
	}
}
