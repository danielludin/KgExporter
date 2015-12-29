/*******************************************************************************
 * Copyright (c) 2015, Daniel Ludin
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Ludin (ludin@hispeed.ch) - initial implementation
 *******************************************************************************/
/**
 * @author daniel ludin ludin@hispeed.ch
 * 27.06.2015
 * 
 */
package ch.gpb.elexis.kgexporter.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Patient;
import ch.elexis.omnivore.data.DocHandle;
import ch.gpb.elexis.kgexporter.handlers.KgExportHandler;
import ch.gpb.elexis.kgexporter.util.Messages;

public class KgExportWizardPage1 extends WizardPage implements IWizardPage {
    private Composite container;
    private Button cb_Problemliste;
    private Button cb_Medikationsliste;
    private Button cb_Laborblatt;
    private Button cb_Briefe;
    Button bSelectAll;
    HashMap<String, String> categories;
    List<Button> dynButtonList = new ArrayList<Button>();
    Color COLOR_RED;

    /**
     * @wbp.parser.constructor
     */
    public KgExportWizardPage1() {
	super(Messages.KgExport_Wizard_title_1);
	setTitle(Messages.KgExport_Wizard_title_1 + " " + Messages.KgExport_Wizard_title_step_1);
	setDescription(Messages.KgExport_Wizard_desc);
	COLOR_RED = UiDesk.getColorFromRGB("D90A0A");

    }

    @Override
    public void createControl(Composite parent) {
	container = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	container.setLayout(layout);
	layout.numColumns = 1;

	// required to avoid an error in the system
	setControl(container);

	cb_Problemliste = new Button(container, SWT.CHECK);
	cb_Problemliste.setText(Messages.KgExport_Wizard_doctitle_diagnosen);
	cb_Problemliste.setData(KgExportWizard.EXPORTCATEGORY_DIAGNOSEPROBLEMLISTE);

	cb_Medikationsliste = new Button(container, SWT.CHECK);
	cb_Medikationsliste.setText(Messages.KgExport_Wizard_doctitle_medikation);
	cb_Medikationsliste.setData(KgExportWizard.EXPORTCATEGORY_MEDIKATIONSLISTE);

	cb_Laborblatt = new Button(container, SWT.CHECK);
	cb_Laborblatt.setText(Messages.KgExport_Wizard_doctitle_laborblatt);
	cb_Laborblatt.setData(KgExportWizard.EXPORTCATEGORY_LABORBLATT);

	cb_Briefe = new Button(container, SWT.CHECK);
	cb_Briefe.setText(Messages.KgExport_Wizard_doctitle_briefe);
	cb_Briefe.setData(KgExportWizard.EXPORTCATEGORY_BRIEFE);

	CheckboxListener2 cl = new CheckboxListener2();

	dynButtonList.add(cb_Problemliste);
	dynButtonList.add(cb_Laborblatt);
	dynButtonList.add(cb_Medikationsliste);
	dynButtonList.add(cb_Briefe);

	cb_Medikationsliste.addSelectionListener(cl);
	cb_Laborblatt.addSelectionListener(cl);
	cb_Problemliste.addSelectionListener(cl);
	cb_Briefe.addSelectionListener(cl);

	Label lOmnivore = new Label(container, SWT.NONE);
	GridData gd_lOmnivore = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
	gd_lOmnivore.heightHint = 20;
	gd_lOmnivore.verticalIndent = 16;
	lOmnivore.setLayoutData(gd_lOmnivore);
	lOmnivore.setText(Messages.KgExport_Wizard_choose_omnivore);

	Patient selectedPatient = ElexisEventDispatcher.getSelectedPatient();
	List<DocHandle> docs = KgExportHandler.loadOmnivoreDocs(selectedPatient);
	categories = new HashMap<String, String>();
	for (DocHandle docHandle : docs) {
	    categories.put(docHandle.getCategory(), docHandle.getCategory());
	}
	Set<String> uniqueCats = categories.keySet();

	List<String> sortedCats = asSortedList(uniqueCats);

	for (Object key : sortedCats) {
	    String sCatName = key.toString();
	    System.out.println("category: " + sCatName);

	    Button bField = new Button(container, SWT.CHECK);
	    bField.addSelectionListener(cl);

	    bField.setText(sCatName);
	    bField.setData(sCatName);
	    dynButtonList.add(bField);

	}

	bSelectAll = new Button(container, SWT.CHECK);
	bSelectAll.setForeground(COLOR_RED);
	//bSelectAll.setBackground(COLOR_RED);

	GridData gd_bSelectAll = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
	gd_bSelectAll.verticalIndent = 20;
	gd_bSelectAll.widthHint = 100;

	bSelectAll.setLayoutData(gd_bSelectAll);
	bSelectAll.setText(Messages.KgExport_Wizard_select_all);
	bSelectAll.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseDown(MouseEvent e) {

		Control[] controls = container.getChildren();
		for (int i = 0; i < controls.length; i++) {
		    Control c = controls[i];
		    if (c instanceof Button) {
			Button b = (Button) c;
			b.setSelection(!bSelectAll.getSelection());
		    }
		}
		if (bSelectAll.getSelection()) {
		    setPageComplete(true);
		    bSelectAll.setText(Messages.KgExport_Wizard_deselect_all);
		} else {
		    bSelectAll.setText(Messages.KgExport_Wizard_select_all);
		    setPageComplete(false);

		}
		bSelectAll.setSelection(!bSelectAll.getSelection());

	    }
	});

	parent.layout();

	setPageComplete(false);

    }

    private boolean isDynButtonSelected() {
	for (Button b : dynButtonList) {
	    if (b.getSelection()) {
		return true;
	    }
	}
	return false;
    }

    class CheckboxListener2 extends SelectionAdapter {

	@Override
	public void widgetSelected(SelectionEvent e) {
	    if (isDynButtonSelected()) {

		setPageComplete(true);
	    }
	    else {
		setPageComplete(false);

	    }
	}
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	List<T> list = new ArrayList<T>(c);
	java.util.Collections.sort(list);
	return list;
    }

    public HashMap<String, Boolean> getSelectedCategories() {
	HashMap<String, Boolean> selectedCats = new HashMap<>();
	for (Button b : dynButtonList) {
	    //selectedCats.put((String) b.getData(), String.valueOf(b.getSelection()));
	    selectedCats.put((String) b.getData(), b.getSelection());
	}

	return selectedCats;
    }

    public boolean getAustrittsberichte() {
	return cb_Medikationsliste.getSelection();
    }

}
