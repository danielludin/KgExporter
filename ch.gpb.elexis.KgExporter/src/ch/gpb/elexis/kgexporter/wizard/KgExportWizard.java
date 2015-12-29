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

import java.util.HashMap;

import org.eclipse.jface.wizard.Wizard;

import ch.gpb.elexis.kgexporter.util.Messages;

public class KgExportWizard extends Wizard {

    public static String EXPORTCATEGORY_DIAGNOSEPROBLEMLISTE = "PROBLEMLISTE";
    public static String EXPORTCATEGORY_MEDIKATIONSLISTE = "MEDIKATIONSLISTE";
    public static String EXPORTCATEGORY_LABORBLATT = "LABORBLATT";
    public static String EXPORTCATEGORY_BRIEFE = "BRIEFE";

    protected KgExportWizardPage1 one;
    protected KgExportWizardPage2 two;
    protected String exportPath;
    protected String footerText;
    protected HashMap<String, Boolean> selectedCats = new HashMap<>();

    public KgExportWizard() {
	super();
	setNeedsProgressMonitor(true);

    }

    @Override
    public String getWindowTitle() {
	return Messages.KgExport_Wizard_title_1;
    }

    @Override
    public void addPages() {
	one = new KgExportWizardPage1();
	two = new KgExportWizardPage2();

	addPage(one);
	addPage(two);
    }

    @Override
    public boolean performFinish() {
	this.exportPath = two.getExportPath();
	this.footerText = two.getFooterText();
	this.selectedCats = one.getSelectedCategories();
	return true;
    }

    public String getFooterText() {
	return this.footerText;

    }

    public String getExportPath() {
	return exportPath;

    }

    public HashMap<String, Boolean> getSelectedCategories() {
	return this.selectedCats;
    }

}
