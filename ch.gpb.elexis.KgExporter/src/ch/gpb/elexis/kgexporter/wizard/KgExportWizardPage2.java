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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.activator.CoreHub;
import ch.gpb.elexis.kgexporter.util.KgExportPreference;
import ch.gpb.elexis.kgexporter.util.Messages;

public class KgExportWizardPage2 extends WizardPage implements IWizardPage {
    private Text exportPath;
    private Composite container;
    String selectedDir;
    String sFooterText;
    Label label1;
    private Label lblTextFusszeile;
    private Text text;

    /**
     * @wbp.parser.constructor
     */
    public KgExportWizardPage2() {
	super(Messages.KgExport_Wizard_title_1);
	setTitle(Messages.KgExport_Wizard_title_1 + " " + Messages.KgExport_Wizard_title_step_2);
	setDescription(Messages.KgExport_Wizard_desc2);
    }

    @Override
    public void createControl(Composite parent) {
	container = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	container.setLayout(layout);
	layout.numColumns = 2;
	label1 = new Label(container, SWT.NONE);
	label1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
	label1.setText(Messages.KgExport_Wizard_choose_export_dir);

	exportPath = new Text(container, SWT.BORDER | SWT.SINGLE);
	exportPath.setText("");
	exportPath.addKeyListener(new KeyListener() {

	    @Override
	    public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
		if (!exportPath.getText().isEmpty()) {
		    setPageComplete(true);
		}
	    }

	});
	exportPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	Button btnSelectFolder = new Button(container, SWT.NONE);
	btnSelectFolder.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetSelected(SelectionEvent e) {
		DirectoryDialog directoryDialog = new DirectoryDialog(container.getShell());
		directoryDialog.setFilterPath(selectedDir);

		String sLatestPath = CoreHub.userCfg.get(KgExportPreference.PREF_KGEXPORT_LATESTPATH, null);
		directoryDialog.setFilterPath(sLatestPath);

		String dir = directoryDialog.open();
		if (dir != null) {
		    exportPath.setText(dir);
		    selectedDir = dir;
		    CoreHub.userCfg.set(KgExportPreference.PREF_KGEXPORT_LATESTPATH, dir);
		    setPageComplete(true);

		}

	    }
	});
	btnSelectFolder.setText(Messages.KgExport_Wizard_choose_dir);
	btnSelectFolder.setFocus();
	// required to avoid an error in the system
	setControl(container);

	lblTextFusszeile = new Label(container, SWT.NONE);
	GridData gd_lblTextFusszeile = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
	gd_lblTextFusszeile.verticalIndent = 20;
	lblTextFusszeile.setLayoutData(gd_lblTextFusszeile);
	lblTextFusszeile.setText(Messages.KgExport_Wizard_set_footer);
	new Label(container, SWT.NONE);

	text = new Text(container, SWT.BORDER);
	text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

	sFooterText = CoreHub.userCfg.get(KgExportPreference.PREF_KGEXPORT_FOOTERTEXT, "Default footer text");
	text.setText(sFooterText);

	new Label(container, SWT.NONE);
	new Label(container, SWT.NONE);

	setPageComplete(false);

    }


    public String getExportPath() {
	return exportPath.getText();
    }

    public String getFooterText() {
	return text.getText();
    }
}
