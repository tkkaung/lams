package ntu.celt.eUreka2.pages.admin;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;

import ntu.celt.eUreka2.annotation.PublicPage;
import ntu.celt.eUreka2.data.AppState;
import ntu.celt.eUreka2.internal.DatabaseInitializer;
import ntu.celt.eUreka2.modules.profiling.LDimension;
import ntu.celt.eUreka2.modules.profiling.ProfilingDAO;

@PublicPage
public class InitDatabase {

	@SessionState
	private AppState appState ;
	@Inject
	private ProfilingDAO profDAO;
	
	void setupRender(){
		
	}
	
	void onActionFromStartInitDb(){
		DatabaseInitializer dbInit = new DatabaseInitializer();
		dbInit.initAllTable();
		
		appState.recordInfoMsg("Done, You can now login as super admin");
	}
	
	@CommitAfter
	void onActionFromStartInitLDimension(){
//		LDimension d1 = new LDimension(LDimension._1_MBE_ACTIVE, "MBE (Active)", "", "fce4d6", 1);
//		LDimension d2 = new LDimension(LDimension._2_MBE_PASSIVE, "MBE (Passive)", "", "c6e0b4", 1);
//		LDimension d3 = new LDimension(LDimension._3_CONTINGENT_REWARD, "Contingent Reward", "", "b4c6e7", 2);
//		LDimension d4 = new LDimension(LDimension._4_TRANSF_INDIVIDUAL_CONSIDERATION, "Transf (Individual Consideration)", "", "ffe699", 3);
//		LDimension d5 = new LDimension(LDimension._5_TRANSF_IDEALIZED_INFLUENCE, "Transf (Idealized Influence)", "", "e2fad8", 4);
//		LDimension d6 = new LDimension(LDimension._6_LDR_FLEX, "Ldr Flex", "", "ff0000", 5);
//		LDimension d7 = new LDimension(LDimension._7_TRANSF_INSPIRATION_MOTIVATION, "Transf (Inspiration Motivation)", "", "bf8f00", 6);
//		LDimension d8 = new LDimension(LDimension._8_TRANSF_INTELLECTUAL_SIMULATION, "Transf (Intellectual Stimulation)", "", "548235", 7);
//		LDimension d9 = new LDimension(LDimension._9_LASSEIZ_FAIRE, "Lasseiz- Faire", "", "bfbfbf", 8);
//		LDimension d0 = new LDimension(LDimension._0_CALIBRATION, "Calibration", "", "00b0f0", 99);

		LDimension d1 = new LDimension(LDimension._1_INDIVIDUAL_CONSIDERATION, "Individual Consideration", "", "fce4d6", 1, true, LDimension._S_1_TRANSFORMATIONAL);
		LDimension d2 = new LDimension(LDimension._2_INTELLECTUAL_SIMULATION, "Intellectual Stimulation", "", "c6e0b4", 2, true, LDimension._S_1_TRANSFORMATIONAL);
		LDimension d3 = new LDimension(LDimension._3_INSPIRATION_MOTIVATION, "Inspirational Motivation", "", "b4c6e7", 3, true, LDimension._S_1_TRANSFORMATIONAL);
		LDimension d4 = new LDimension(LDimension._4_IDEALIZED_INFLUENCE, "Idealized Influence", "", "ffe699", 4, true, LDimension._S_1_TRANSFORMATIONAL);
		LDimension d5 = new LDimension(LDimension._5_CONTINGENT_REWARD, "Contingent Reward", "", "e2fad8", 5, true, LDimension._S_2_TRANSACTIONAL);
		LDimension d6 = new LDimension(LDimension._6_MNG_BY_EXCEPT_POLICEMAN, "Active MBE (Policeman)", "Management By Exception (Policeman)", "ff0000", 6, true, LDimension._S_2_TRANSACTIONAL);
		LDimension d7 = new LDimension(LDimension._7_MNG_BY_EXCEPT_FIREMAN, "Passive MBE (Fireman)", "Management by Exception (Fireman)", "bf8f00", 7, true, LDimension._S_2_TRANSACTIONAL);
		//LDimension d8 = new LDimension(LDimension._8_LDR_FLEX, "Leadership Flexibility", "", "548235", 8, false, LDimension._S_3_PARTICIPANT_DEMO);
		//LDimension d9 = new LDimension(LDimension._9_LASSEIZ_FAIRE, "Lasseiz- Faire", "", "bfbfbf", 9, false, LDimension._S_2_TRANSACTIONAL);
		LDimension d8 = new LDimension(LDimension._8_LASSEIZ_FAIRE, "Lasseiz- Faire", "", "548235", 8, true, LDimension._S_5_LASSEIZ_FAIRE);
		LDimension d9 = new LDimension(LDimension._9_LDR_FLEX, "Leadership Flexibility", "", "bfbfbf", 9, true, LDimension._S_3_LEADERSHIP_FLEXIBILITY);
		LDimension d0 = new LDimension(LDimension._10_PERCEIVED_LEADERSHIP_EFFECTIVENESS, "Perceived Leadership Effectivenesss", "", "00b0f0", 100, true, LDimension._S_4_PERCEIVED_LEADERSHIP_EFFECTIVENESS);

		
		profDAO.addLDimension(d1);
		profDAO.addLDimension(d2);
		profDAO.addLDimension(d3);
		profDAO.addLDimension(d4);
		profDAO.addLDimension(d5);
		profDAO.addLDimension(d6);
		profDAO.addLDimension(d7);
		profDAO.addLDimension(d8);
		profDAO.addLDimension(d9);
		profDAO.addLDimension(d0);
		
		
		
		appState.recordInfoMsg("Done");
	}
	
	
}
