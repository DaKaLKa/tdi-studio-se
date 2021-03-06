// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.visualmap.table;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.commands.ExtendedTableRemoveCommand;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.swt.tableviewer.behavior.DefaultTableLabelProvider;
import org.talend.commons.ui.swt.tableviewer.behavior.ITableCellValueModifiedListener;
import org.talend.commons.ui.swt.tableviewer.behavior.TableCellValueModifiedEvent;
import org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonPushImageTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.components.IComponent;
import org.talend.designer.abstractmap.model.table.IDataMapTable;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.managers.UIManager;
import org.talend.designer.dbmap.model.table.AbstractDataMapTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.model.tableentry.OutputColumnTableEntry;
import org.talend.designer.dbmap.ui.dnd.DragNDrop;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: OutputDataMapTableView.java 1782 2007-02-03 07:57:38Z bqian $
 * 
 */
public class OutputDataMapTableView extends DataMapTableView {

    public OutputDataMapTableView(Composite parent, int style, IDataMapTable abstractDataMapTable, MapperManager mapperManager) {
        super(parent, style, abstractDataMapTable, mapperManager);
    }

    @Override
    public void initColumnsOfTableColumns(final TableViewerCreator tableViewerCreatorForColumns) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.expression")); //$NON-NLS-1$
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getExpression();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
                tableViewerCreatorForColumns.getTableViewer().refresh(bean);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        createExpressionCellEditor(tableViewerCreatorForColumns, column, new Zone[] { Zone.INPUTS, Zone.OUTPUTS }, false);
        column.setWeight(COLUMN_EXPRESSION_SIZE_WEIGHT);

        column = new TableViewerCreatorColumn(tableViewerCreatorForColumns);
        column.setTitle(DataMapTableView.COLUMN_NAME);
        column.setId(DataMapTableView.ID_NAME_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<OutputColumnTableEntry, String>() {

            public String get(OutputColumnTableEntry bean) {
                return bean.getMetadataColumn().getLabel();
            }

            public void set(OutputColumnTableEntry bean, String value) {
                bean.getMetadataColumn().setLabel(value);
            }

        });
        column.setWeight(COLUMN_NAME_SIZE_WEIGHT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#initTableConstraints()
     */
    @Override
    protected void initTableFilters(Composite parent) {
        createFiltersTable(parent);

        List<FilterTableEntry> entries = ((OutputTable) getDataMapTable()).getFilterEntries();

        // correct partially layout problem with GTK when cell editor value is applied
        tableViewerCreatorForFilters.setAdjustWidthValue(WindowSystem.isGTK() ? -20 : ADJUST_WIDTH_VALUE);

        tableViewerCreatorForFilters.init(entries);
        updateGridDataHeightForTableConstraints();

        if (WindowSystem.isGTK()) {
            tableViewerCreatorForFilters.layout();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#addEntriesActionsComponents()
     */
    @Override
    protected boolean addToolItems() {
        createFiltersToolItems();
        // addToolItemSeparator();
        // createToolItems();
        return true;
    }

    protected void createFiltersTable(Composite parent) {

        this.extendedTableViewerForFilters = new AbstractExtendedTableViewer<FilterTableEntry>(
                ((OutputTable) abstractDataMapTable).getTableFiltersEntriesModel(), parent) {

            @Override
            protected void createColumns(TableViewerCreator<FilterTableEntry> tableViewerCreator, Table table) {
                createFiltersColumns(tableViewerCreator);
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions
             * (org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<FilterTableEntry> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                newTableViewerCreator.setHeaderVisible(true);
                newTableViewerCreator.setColumnsResizableByDefault(false);
                newTableViewerCreator.setBorderVisible(false);
                newTableViewerCreator.setLayoutMode(LAYOUT_MODE.FILL_HORIZONTAL);
                newTableViewerCreator.setKeyboardManagementForCellEdition(false);
                // tableViewerCreatorForColumns.setUseCustomItemColoring(this.getDataMapTable() instanceof
                // AbstractInOutTable);
                newTableViewerCreator.setFirstColumnMasked(true);

            }

        };
        tableViewerCreatorForFilters = this.extendedTableViewerForFilters.getTableViewerCreator();
        this.extendedTableViewerForFilters.setCommandStack(mapperManager.getCommandStack());

        tableForConstraints = tableViewerCreatorForFilters.getTable();
        tableForConstraintsGridData = new GridData(GridData.FILL_HORIZONTAL);
        tableForConstraints.setLayoutData(tableForConstraintsGridData);

        boolean tableConstraintsVisible = false;
        if (abstractDataMapTable instanceof OutputTable) {
            tableConstraintsVisible = ((OutputTable) abstractDataMapTable).getFilterEntries().size() > 0;
        }

        tableForConstraintsGridData.exclude = !tableConstraintsVisible;
        tableForConstraints.setVisible(tableConstraintsVisible);

        new DragNDrop(mapperManager, tableForConstraints, false, true);

        tableViewerCreatorForFilters.addCellValueModifiedListener(new ITableCellValueModifiedListener() {

            public void cellValueModified(TableCellValueModifiedEvent e) {
                unselectAllEntriesIfErrorDetected(e);
            }
        });

        final TableViewer tableViewer = tableViewerCreatorForFilters.getTableViewer();

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                selectThisDataMapTableView();
                UIManager uiManager = mapperManager.getUiManager();
                uiManager.selectLinks(OutputDataMapTableView.this, uiManager.extractSelectedTableEntries(selection), true, false);
            }

        });

        tableForConstraints.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                processEnterKeyDown(tableViewerCreatorForFilters, event);
            }

        });

        tableViewerCreatorForFilters.setLabelProvider(new DefaultTableLabelProvider(tableViewerCreatorForFilters) {

            @Override
            public Color getBackground(Object element, int columnIndex) {
                return getBackgroundCellColor(tableViewerCreator, element, columnIndex);
            }

            @Override
            public Color getForeground(Object element, int columnIndex) {
                return getForegroundCellColor(tableViewerCreator, element, columnIndex);
            }

        });

        initShowMessageErrorListener(tableForConstraints);
    }

    public void createFiltersColumns(final TableViewerCreator<FilterTableEntry> tableViewerCreatorForFilters) {
        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
        IComponent component = getMapperManager().getComponent().getComponent();
        if (component != null && "tELTOracleMap".equals(component.getName())) { //$NON-NLS-1$
            column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.AdditionalClauses.oracle")); //$NON-NLS-1$
        } else {
            column.setTitle(Messages.getString("OutputDataMapTableView.columnTitle.AdditionalClauses")); //$NON-NLS-1$
        }
        column.setId(DataMapTableView.ID_EXPRESSION_COLUMN);
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<FilterTableEntry, String>() {

            public String get(FilterTableEntry bean) {
                return bean.getExpression();
            }

            public void set(FilterTableEntry bean, String value) {
                bean.setExpression(value);
                mapperManager.getProblemsManager().checkProblemsForTableEntry(bean, true);
            }

        });
        column.setModifiable(true);
        column.setDefaultInternalValue(""); //$NON-NLS-1$
        createExpressionCellEditor(tableViewerCreatorForFilters, column, new Zone[] { Zone.INPUTS, Zone.OUTPUTS }, true);
        column.setWeight(99);
        column.setMoveable(false);
        column.setResizable(false);

        // Column with remove button
        column = new TableViewerCreatorColumn(tableViewerCreatorForFilters);
        column.setTitle(""); //$NON-NLS-1$
        column.setDefaultDisplayedValue(""); //$NON-NLS-1$
        column.setWidth(16);
        column.setMoveable(false);
        column.setResizable(false);
        ButtonPushImageTableEditorContent buttonImage = new ButtonPushImageTableEditorContent() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * org.talend.commons.ui.swt.tableviewer.tableeditor.ButtonImageTableEditorContent#selectionEvent(org.talend
             * .commons.ui.swt.tableviewer.TableViewerCreatorColumn, java.lang.Object)
             */
            @Override
            protected void selectionEvent(TableViewerCreatorColumnNotModifiable column, Object bean) {
                ExtendedTableRemoveCommand removeCommand = new ExtendedTableRemoveCommand(bean,
                        extendedTableViewerForFilters.getExtendedTableModel());
                mapperManager.removeTableEntry((ITableEntry) bean);
                mapperManager.executeCommand(removeCommand);
                tableViewerCreatorForFilters.getTableViewer().refresh();
                List list = tableViewerCreatorForFilters.getInputList();
                updateGridDataHeightForTableConstraints();
                if (list != null && list.size() == 0) {
                    showTableConstraints(false);
                } else {
                    showTableConstraints(true);
                }

            }

        };
        buttonImage.setImage(ImageProvider.getImage(EImage.MINUS_ICON));
        column.setTableEditorContent(buttonImage);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getZone()
     */
    @Override
    public Zone getZone() {
        return Zone.OUTPUTS;
    }

    @Override
    public void unselectAllConstraintEntries() {
        tableViewerCreatorForFilters.getSelectionHelper().deselectAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#toolbarNeededToBeRightStyle()
     */
    @Override
    public boolean toolbarNeedToHaveRightStyle() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#hasDropDownToolBarItem()
     */
    @Override
    public boolean hasDropDownToolBarItem() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView#getTitle()
     */
    @Override
    protected String getTitle() {
        return ((AbstractDataMapTable) abstractDataMapTable).getTitle();
    }

}
