package org.talend.designer.business.model.business.diagram.edit.policies;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.notation.View;

/**
 * @generated
 */
public class ActionBusinessItemCanonicalEditPolicy extends CanonicalEditPolicy {

    /**
     * @generated
     */
    protected List getSemanticChildrenList() {
        List result = new LinkedList();
        return result;
    }

    /**
     * @generated
     */
    protected boolean shouldDeleteView(View view) {
        return view.isSetElement() && view.getElement() != null && view.getElement().eIsProxy();
    }

    /**
     * @generated
     */
    protected String getDefaultFactoryHint() {
        return null;
    }

}
