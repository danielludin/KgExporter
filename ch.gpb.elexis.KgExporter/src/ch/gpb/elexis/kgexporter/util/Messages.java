package ch.gpb.elexis.kgexporter.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "ch.gpb.elexis.kgexporter.util.messages";

    public static String KgExport_Wizard_title_1;
    public static String KgExport_Wizard_title_step_1;
    public static String KgExport_Wizard_title_step_2;
    public static String KgExport_Wizard_desc;
    public static String KgExport_Wizard_desc2;
    public static String KgExport_Wizard_choose_omnivore;
    public static String KgExport_Wizard_choose_export_dir;
    public static String KgExport_Wizard_choose_dir;
    public static String KgExport_Wizard_select_all;
    public static String KgExport_Wizard_set_footer;
    public static String KgExport_Wizard_deselect_all;
    public static String KgExport_Wizard_doctitle_briefe;
    public static String KgExport_Wizard_doctitle_diagnosen;
    public static String KgExport_Wizard_doctitle_laborblatt;
    public static String KgExport_Wizard_doctitle_medikation;


    public Messages() {
    }

    static {
	// load message values from bundle file
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}

