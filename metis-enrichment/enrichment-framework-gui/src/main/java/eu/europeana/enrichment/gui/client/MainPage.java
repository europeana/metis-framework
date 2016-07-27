package eu.europeana.enrichment.gui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HRElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

/**
 * Main GWT web page for enrichment
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class MainPage implements EntryPoint {

    private RootPanel rootPanel;
    private VerticalPanel sp;
    final EnrichmentServiceAsync enrichmentService = GWT
            .create(EnrichmentService.class);
    List<InputValueDTO> inputValueDTOs;
    final TextArea enrichment = new TextArea();
    AsyncDataProvider<InputValueDTO> inputValueProvider;
    DataGrid<InputValueDTO> inputGrid;
    final CheckBox asEdm = new CheckBox("Retrieve as XML");

    @Override
    public void onModuleLoad() {
        initialize();
    }

    private void initialize() {
        inputValueDTOs = new ArrayList<InputValueDTO>();
        inputValueProvider = new AsyncDataProvider<InputValueDTO>() {

            @Override
            protected void onRangeChanged(HasData<InputValueDTO> arg0) {
                // Not implemented

            }
        };

        inputValueProvider.updateRowCount(inputValueDTOs.size(), true);
        inputValueProvider.updateRowData(0, inputValueDTOs);
        rootPanel = RootPanel.get();
        sp = new VerticalPanel();
        sp.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        sp.add(createFirstHeader());
        sp.add(createInputArea());
        sp.add(createEnrichmentTable());
        sp.add(createEnrichmentArea());
        sp.add(createLastFooter());
        rootPanel.setWidth("768px");
        rootPanel.setHeight("1024px");

        rootPanel.add(sp);

        createEnrichmentTable();
    }

    private Widget createFirstHeader() {
        VerticalPanel vertP = new VerticalPanel();
        HorizontalPanel vp = new HorizontalPanel();
        final HTML label = new HTML("<p><font size='4'><b>Europeana Enrichment Framework GUI</b></font></p>");
        Image img = new Image("europeana-logo-en.png");
        label.setHeight("" + img.getHeight());
        vp.add(img);
        vp.add(label);
        vp.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
        vertP.add(vp);
        HRElement hr = Document.get().createHRElement();
        hr.setAttribute("width", "1024px");
        vertP.add(InlineHTML.wrap(hr));
        vertP.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        return vertP;
    }

    private Widget createLastFooter() {
        VerticalPanel vertP = new VerticalPanel();
        HRElement hr = Document.get().createHRElement();
        hr.setAttribute("width", "1024px");
        vertP.add(InlineHTML.wrap(hr));
        HTML lbl = new HTML();
        lbl.setHTML("<p align='center'><font size='2'>Europeana 2014 (C)</font></p>");
        vertP.add(lbl);
        vertP.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
        return vertP;
    }

    private Widget createEnrichmentArea() {
        final DecoratorPanel headerPanel = new DecoratorPanel();
        final FlexTable headerTable = new FlexTable();
        enrichment.setSize("1024px", "400px");
        headerTable.setWidget(0, 0, enrichment);
        headerPanel.add(headerTable);
        return headerPanel;
    }

    private Widget createEnrichmentTable() {
        Button enrichButton = new Button();
        enrichButton.setText("Enrich values");

        enrichButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                enrichmentService.enrich(inputValueDTOs, asEdm.getValue(),
                        new AsyncCallback<List<EntityWrapperDTO>>() {

                            @Override
                            public void onSuccess(List<EntityWrapperDTO> arg0) {
                                inputValueDTOs.clear();
                                inputValueProvider.updateRowCount(inputValueDTOs.size(), true);
                                inputValueProvider.updateRowData(0, inputValueDTOs);
                                createEnrichmentTable();
                                enrichment.setText("");

                                for (EntityWrapperDTO entityWrapper : arg0) {
                                    enrichment.setText(enrichment.getText()
                                            + "Original Field: "
                                            + entityWrapper.getOriginalField());
                                    enrichment.setText(enrichment.getText()
                                            + "\nClassName: "
                                            + entityWrapper.getClassName());
                                    enrichment.setText(enrichment.getText()
                                            + "\nContextual Entity:"
                                            + entityWrapper
                                            .getContextualEntity()
                                            + "\n==========================================\n");

                                }
                            }

                            @Override
                            public void onFailure(Throwable arg0) {
                                Window.alert(arg0.getMessage());
                            }
                        });
            }
        });
        DecoratorPanel dp = new DecoratorPanel();
        final VerticalPanel headerPanel = new VerticalPanel();

        TextColumn<InputValueDTO> originalColumn = new TextColumn<InputValueDTO>() {

            @Override
            public String getValue(InputValueDTO arg0) {
                return arg0.getOriginalField();
            }
        };
        TextColumn<InputValueDTO> valueColumn = new TextColumn<InputValueDTO>() {

            @Override
            public String getValue(InputValueDTO arg0) {
                return arg0.getValue();
            }
        };
        TextColumn<InputValueDTO> vocColumn = new TextColumn<InputValueDTO>() {

            @Override
            public String getValue(InputValueDTO arg0) {
                return arg0.getVocabulary();
            }
        };
        TextColumn<InputValueDTO> languageColumn = new TextColumn<InputValueDTO>() {

            @Override
            public String getValue(InputValueDTO arg0) {
                return arg0.getLanguage();
            }
        };
        ButtonCell clear = new ButtonCell();
        Column<InputValueDTO, String> clearColumn = new Column<InputValueDTO, String>(
                clear) {

            @Override
            public String getValue(InputValueDTO arg0) {
                return "Clear";
            }
        };
        ProvidesKey<InputValueDTO> key = new ProvidesKey<InputValueDTO>() {

            @Override
            public Object getKey(InputValueDTO arg0) {
                String keyStr = arg0.getValue();
                keyStr += arg0.getVocabulary();
                if (arg0.getOriginalField() != null) {
                    keyStr += arg0.getOriginalField();
                }
                return keyStr;
            }
        };
        clearColumn.setFieldUpdater(new FieldUpdater<InputValueDTO, String>() {

            @Override
            public void update(int arg0, InputValueDTO arg1, String arg2) {
                inputValueDTOs.remove(arg0);
                inputValueProvider.updateRowCount(inputValueDTOs.size(), true);
                inputValueProvider.updateRowData(0, inputValueDTOs);
                createEnrichmentTable();
            }
        });
        inputGrid = new DataGrid<InputValueDTO>();

        inputValueProvider.addDataDisplay(inputGrid);

        SingleSelectionModel<InputValueDTO> selectionModel = new SingleSelectionModel<InputValueDTO>(
                key);
        selectionModel
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                    public void onSelectionChange(SelectionChangeEvent arg0) {

                    }
                });
        inputGrid.setSelectionModel(selectionModel);
        inputGrid.addColumn(valueColumn, "Value to Enrich");
        inputGrid.addColumn(originalColumn, "Original Field");
        inputGrid.addColumn(vocColumn, "Vocabulary");
        inputGrid.addColumn(languageColumn, "Language");
        inputGrid.addColumn(clearColumn, "Clear");
        inputGrid.setSize("1024px", "300px");

        headerPanel.add(inputGrid);
        headerPanel.add(asEdm);
        headerPanel.add(enrichButton);
        dp.add(headerPanel);
        return dp;
    }

    private Widget createInputArea() {
        final DecoratorPanel headerPanel = new DecoratorPanel();
        final FlexTable headerTable = new FlexTable();
        headerTable.setWidth("1024px");
        final Label lblOriginal = new Label("Original Field");
        final Label lblValue = new Label("Value to enrich");
        final Label lblVocabulary = new Label("Vocabulary to use");
        final Label lblLanguage = new Label("Language");
        final TextBox txtOriginal = new TextBox();
        final TextBox txtValue = new TextBox();
        final TextBox txtLanguage = new TextBox();
        final TextBox txtHidden = new TextBox();
        txtHidden.setText("CONCEPT");
        final ListBox lstSelection = new ListBox(false);
        lstSelection.addItem("CONCEPT");
        lstSelection.addItem("AGENT");
        lstSelection.addItem("TIMESPAN");
        lstSelection.addItem("PLACE");
        lstSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent arg0) {
                txtHidden.setText(lstSelection.getValue(lstSelection
                        .getSelectedIndex()));
            }
        });

        Button addButton = new Button("Add for enrichment");
        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (txtValue.getText() != null
                        || txtValue.getText().trim().length() > 0) {
                    InputValueDTO inputDTO = new InputValueDTO();
                    inputDTO.setOriginalField(txtOriginal.getText());
                    inputDTO.setValue(txtValue.getText());
                    inputDTO.setVocabulary(txtHidden.getText());
                    inputDTO.setLanguage(txtLanguage.getText());
                    txtOriginal.setText("");
                    txtValue.setText("");
                    txtLanguage.setText("");
                    inputValueDTOs.add(inputDTO);
                    inputValueProvider.updateRowCount(inputValueDTOs.size(), true);
                    inputValueProvider.updateRowData(0, inputValueDTOs);
                    createEnrichmentTable();
                } else {
                    Window.alert("No text provided");
                }

            }
        });

        headerTable.setWidget(0, 0, lblOriginal);
        headerTable.setWidget(0, 1, txtOriginal);
        headerTable.setWidget(1, 0, lblValue);
        headerTable.setWidget(1, 1, txtValue);
        headerTable.setWidget(2, 0, lblLanguage);
        headerTable.setWidget(2, 1, txtLanguage);
        headerTable.setWidget(3, 0, lblVocabulary);
        headerTable.setWidget(3, 1, lstSelection);
        headerTable.setWidget(4, 1, addButton);
        headerPanel.add(headerTable);
        return headerPanel;
    }

}
