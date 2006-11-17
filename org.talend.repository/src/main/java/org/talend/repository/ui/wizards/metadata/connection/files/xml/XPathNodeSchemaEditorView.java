// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.connection.files.xml;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator.CELL_EDITOR_STATE;
import org.talend.commons.ui.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.tableviewer.celleditor.DialogErrorForCellEditorListener;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.commons.utils.data.list.ListenableListEvent.TYPE;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.targetschema.editor.XPathNodeSchemaModel;
import org.talend.core.ui.extended.AbstractExtendedTableToolbarView;

/**
 * DOC amaumont class global comment. Detailled comment <br/> TGU same purpose as TargetSchemaTableEditorView but uses
 * EMF model directly
 * 
 * $Id$
 * 
 */
public class XPathNodeSchemaEditorView extends AbstractExtendedTableViewer<SchemaTarget> {

    private Label nameLabel;

    private TableViewerCreator<SchemaTarget> tableViewerCreator;

    private Composite composite;

    private XPathNodeSchemaModel targetSchemaTableEditor;

    private boolean executeSelectionEvent = true;

    private AbstractExtendedTableToolbarView targetSchemaToolbarEditorView2;

    public static final String ID_COLUMN_NAME = "ID_COLUMN_NAME";

    private boolean showDbTypeColumn = false;

    private TextCellEditorWithProposal xPathCellEditor;

    private TableViewerCreatorColumn xPathColumn;

    private XmlToSchemaLinker linker;

    public XPathNodeSchemaEditorView(XPathNodeSchemaModel model, Composite parent, int styleChild) {
        this(model, parent, styleChild, false);
    }

    /**
     * TargetSchemaTableEditorView2 constructor comment.
     * 
     * @param parent
     * @param styleChild
     * @param showDbTypeColumn
     */
    public XPathNodeSchemaEditorView(XPathNodeSchemaModel model, Composite parent, int styleChild, boolean showDbTypeColumn) {
        super(model, parent, styleChild);
        this.showDbTypeColumn = showDbTypeColumn;
    }

    public void setReadOnly(boolean b) {
        targetSchemaToolbarEditorView2.setReadOnly(b);
        this.tableViewerCreator.getTable().setEnabled(!b);
    }

    /**
     * Getter for xPathCellEditor.
     * 
     * @return the xPathCellEditor
     */
    public TextCellEditorWithProposal getXPathCellEditor() {
        return this.xPathCellEditor;
    }

    
    
/* (non-Javadoc)
     * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#handleListenableListEvent(org.talend.commons.utils.data.list.ListenableListEvent)
     */
    @Override
    protected void handleAfterListenableListOperationEvent(ListenableListEvent event) {
        super.handleAfterListenableListOperationEvent(event);
        if (event.type == TYPE.REMOVED) {
            linker.updateBackground();
        }
//        if(linker != null) {
//            linker.handleListenableListAfterTableViewerRefreshedEvent(event);
//        }
    }

    //    /**
//     * DOC amaumont Comment method "createEntry".
//     * 
//     * @param selectedIndices selected indices in Table
//     */
//    public SchemaTarget createEntry(int[] selectedIndices) {
//        XPathNodeSchemaModel model = getXpathNodeSchemaModel();
//        SchemaTarget schemaTarget = null;
//        if (model != null) {
//            getTableViewerCreator().getTable().setFocus();
//            TargetSchemaEditorEvent targetSchemaEditorEvent = new TargetSchemaEditorEvent();
//            targetSchemaEditorEvent.type = TargetSchemaEditorEvent.TYPE.ADD;
//            schemaTarget = ConnectionFactory.eINSTANCE.createSchemaTarget();
//            targetSchemaEditorEvent.entries.add(schemaTarget);
//            targetSchemaEditorEvent.entriesIndices = selectedIndices;
//            IAction action = TargetSchemaEditorActionFactory2.getInstance().getAction(this, targetSchemaEditorEvent);
//            action.run(targetSchemaEditorEvent);
//        }
//        return schemaTarget;
//    }
//
//    /**
//     * DOC amaumont Comment method "createEntry".
//     */
//    public SchemaTarget createEntry(int indexWhereInsert) {
//        return createEntry(new int[] { indexWhereInsert });
//    }
//
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.macrotable.AbstractExtendedTableViewer#createColumns(org.talend.commons.ui.swt.tableviewer.TableViewerCreator,
     * org.eclipse.swt.widgets.Table)
     */
    @Override
    protected void createColumns(TableViewerCreator<SchemaTarget> tableViewerCreator, Table table) {
        CellEditorValueAdapter intValueAdapter = new CellEditorValueAdapter() {

            public Object getOriginalTypedValue(final CellEditor cellEditor, Object value) {
                try {
                    return new Integer(value.toString());
                } catch (Exception ex) {
                    return null;
                }
            }

            public Object getCellEditorTypedValue(final CellEditor cellEditor, Object value) {
                if (value != null) {
                    return String.valueOf(value);
                }
                return "";
            }
        };

        // boolean ValueAdapter
        CellEditorValueAdapter booleanValueAdapter = new CellEditorValueAdapter() {

            public Object getOriginalTypedValue(final CellEditor cellEditor, Object value) {
                return (value == new Integer(1));
            }

            public Object getCellEditorTypedValue(final CellEditor cellEditor, Object value) {
                if (value == new Boolean(true)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        // comboValueAdapter
        CellEditorValueAdapter comboValueAdapter = new CellEditorValueAdapter() {

            public Object getOriginalTypedValue(final CellEditor cellEditor, Object value) {
                String[] items = ((ComboBoxCellEditor) cellEditor).getItems();
                int i = new Integer(value.toString());
                if (i >= 0) {
                    return items[i];
                } else {
                    return "";
                }
            }

            public Object getCellEditorTypedValue(final CellEditor cellEditor, Object value) {
                String[] items = ((ComboBoxCellEditor) cellEditor).getItems();
                for (int i = 0; i < items.length; i++) {
                    if (items[i] == value) {
                        return i;
                    }
                }
                return -1;
            }
        };

        // //////////////////////////////////////////////////////////////////////////////////////

        TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
        column.setTitle("");
        column.setDefaultInternalValue("");
        column.setWidth(15);

        // //////////////////////////////////////////////////////////////////////////////////////
        // X Path Query

        column = new TableViewerCreatorColumn(tableViewerCreator);
        xPathColumn = column;
        column.setTitle("XPath expression");
        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<SchemaTarget, String>() {

            public String get(SchemaTarget bean) {
                return bean.getXPathQuery();
            }

            public void set(SchemaTarget bean, String value) {
                bean.setXPathQuery(value);
            }
        });
        xPathCellEditor = new TextCellEditorWithProposal(tableViewerCreator.getTable(), SWT.NONE, column);
        column.setCellEditor(xPathCellEditor);
        xPathCellEditor.addListener(new DialogErrorForCellEditorListener(xPathCellEditor, column) {

            @Override
            public void newValidValueApplied(String previousValue, String newValue, CELL_EDITOR_STATE state) {
//                Object currentModifiedObject = tableViewerCreator.getModifiedObjectInfo().getCurrentModifiedBean();
//                ArrayList modifiedObjectList = new ArrayList(1);
//                modifiedObjectList.add(currentModifiedObject);
//                TargetSchemaEditorEvent event = new TargetSchemaEditorEvent(TargetSchemaEditorEvent.TYPE.XPATH_VALUE_CHANGED);
//                event.entries = modifiedObjectList;
//                event.entriesIndices = new int[] { tableViewerCreator.getModifiedObjectInfo().getCurrentModifiedIndex() };
//                event.previousValue = previousValue;
//                event.newValue = newValue;
//                event.state = state;
//                // targetSchemaTableEditor.fireEvent(event);
            }

            @Override
            public String validateValue(String newValue, int beanPosition) {
                XPathFactory xpf = XPathFactory.newInstance();
                XPath xpath = xpf.newXPath();
                try {
                    xpath.compile(newValue);
                } catch (Exception e) {
                    return e.getMessage();
                }
                return null;
            }

        });
        column.setModifiable(true);
        column.setWeight(10);
        column.setMinimumWidth(50);
        column.setDefaultInternalValue("");
        // //////////////////////////////////////////////////////////////////////////////////////

//        // //////////////////////////////////////////////////////////////////////////////////////
//        // Tag Name
//        column = new TableViewerCreatorColumn(tableViewerCreator);
//        column.setTitle("Tag Name");
//        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<SchemaTarget, String>() {
//
//            public String get(SchemaTarget bean) {
//                return bean.getTagName();
//            }
//
//            public void set(SchemaTarget bean, String value) {
//                bean.setTagName(value);
//            }
//
//        });
//        column.setModifiable(true);
//        column.setWeight(10);
//        column.setMinimumWidth(50);
//        column.setCellEditor(new TextCellEditor(table));
//
//        // //////////////////////////////////////////////////////////////////////////////////////
//        // Loop
//        column = new TableViewerCreatorColumn(tableViewerCreator);
//        column.setTitle("Loop");
//        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<SchemaTarget, String>() {
//
//            public String get(SchemaTarget bean) {
//                return "" + bean.isBoucle();
//            }
//
//            public void set(SchemaTarget bean, String value) {
//                bean.setBoucle(false);
//            }
//
//        });
//        column.setModifiable(true);
//        column.setWidth(50);
//        column.setDisplayedValue("");
//        // column.setTableEditorContent(new CheckboxTableEditorContent());
//        // column.setCellEditor(new TextCellEditor(table), booleanValueAdapter);
//        String[] bool = { "false", "true" };
//        ComboBoxCellEditor comboTypeCellEditor = new ComboBoxCellEditor(table, bool);
//        ((CCombo) comboTypeCellEditor.getControl()).setEditable(false);
//        column.setCellEditor(comboTypeCellEditor, comboValueAdapter);
//
//        // //////////////////////////////////////////////////////////////////////////////////////
//        // Loop limit
//        column = new TableViewerCreatorColumn(tableViewerCreator);
//        column.setTitle("Loop limit");
//        column.setBeanPropertyAccessors(new IBeanPropertyAccessors<SchemaTarget, Integer>() {
//
//            public Integer get(SchemaTarget bean) {
//                return bean.getLimitBoucle();
//            }
//
//            public void set(SchemaTarget bean, Integer value) {
//                bean.setLimitBoucle(value.intValue());
//            }
//
//        });
//        column.setModifiable(true);
//        column.setWidth(30);
//        column.setCellEditor(new TextCellEditor(table), intValueAdapter);

    }

    public XPathNodeSchemaModel getXpathNodeSchemaModel() {
        return (XPathNodeSchemaModel) extendedControl;
    }

    
    /**
     * Getter for xPathColumn.
     * @return the xPathColumn
     */
    public TableViewerCreatorColumn getXPathColumn() {
        return this.xPathColumn;
    }

    /**
     * DOC amaumont Comment method "setLinker".
     * @param linker
     */
    public void setLinker(XmlToSchemaLinker linker) {
        this.linker = linker;
    }

    
}
