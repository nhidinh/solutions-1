package com.hansencx.portal.tests;

import com.hansencx.solutions.database.DatabaseHelper;
import com.hansencx.solutions.portal.PortalBaseTest;
import com.hansencx.solutions.portal.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.sql.ResultSet;
import java.util.List;

public class CancelRebillTest extends PortalBaseTest {

    private DatabaseHelper db;
    private String tranID = "61761470";
    private String cancelTransId = null;
    private String originalTransId = null;
    String userGeneric = "QAGENERIC";
    String pswGeneric = "QA!generic1";
    @BeforeTest
    public void startDB(){
        db = new DatabaseHelper();
        db.createConnection("PSOLQ");
    }

    @Test
    public void test3StepA() {
        //0. Check database before doing test

        //Verify the result
        //1. Check that there are a pair of new trans are returned
        String countLineQuery = "select count(ky_ba) from custpro.cpm_pnd_tran_hdr " +
                "where ky_enroll in(select ky_enroll " +
                "from custpro.cpm_pnd_tran_hdr where ky_pnd_seq_trans = " + tranID + ") " +
                "and cd_tran_status = 28";
        List<Integer> rsCount = db.executeQueryReturnInteger(countLineQuery);

        Assert.assertEquals(rsCount.get(0).intValue(), 2);

        /**  2. verify that two rounds are returned with value following pair of value (00, 01) or (17,18)
         3. verify that KY_BA, ID_TRANS_REF_NUM_867, ID_TRANS_REF_NUM_810 are the same for process, origin and cancel
         **/
        String inputQueryNew = "select CD_PURPOSE from custpro.cpm_pnd_tran_hdr " +
                "where ky_enroll in(select ky_enroll from custpro.cpm_pnd_tran_hdr" +
                " where ky_pnd_seq_trans =" + tranID + ") " +
                "and cd_tran_status = 28";

        List<String> rsNewCDPurpose = db.executeQueryReturnString(inputQueryNew);

        // process
        String ky_ba_ProcessedTran = db.querySpecificInfoInProcessedTrans("KY_BA", tranID);
        String id_trans_ref_867_ProcessedTran = db.querySpecificInfoInProcessedTrans("ID_TRANS_REF_NUM_867", tranID);
        String id_trans_ref_810_ProcessedTran = db.querySpecificInfoInProcessedTrans("ID_TRANS_REF_NUM_810", tranID);
        // cd_purpose = 00: origin
        String ky_ba_New00 = db.querySpecificInfoFollowingKyEnroll("KY_BA", tranID, "00");
        String id_trans_ref_867_New00 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_867", tranID, "00");
        String id_trans_ref_810_New00 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_810", tranID, "00");
        // cd_purpose = 01: cancel
        String ky_ba_New01 = db.querySpecificInfoFollowingKyEnroll("KY_BA", tranID, "01");
        String id_trans_ref_867_New01 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_867", tranID, "01");
        String id_trans_ref_810_New01 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_810", tranID, "01");

        // cd_purpose = 17: origin
        String ky_ba_New17 = db.querySpecificInfoFollowingKyEnroll("KY_BA", tranID, "17");
        String id_trans_ref_867_New17 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_867", tranID, "17");
        String id_trans_ref_810_New17 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_810", tranID, "17");
        // cd_purpose = 18: cancel
        String ky_ba_New18 = db.querySpecificInfoFollowingKyEnroll("KY_BA", tranID, "18");
        String id_trans_ref_867_New18 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_867", tranID, "18");
        String id_trans_ref_810_New18 = db.querySpecificInfoFollowingKyEnroll("ID_TRANS_REF_NUM_810", tranID, "18");

        if (rsNewCDPurpose.contains("01") && rsNewCDPurpose.contains("00")) {
            //verify for appearance of 00 and 01
            Assert.assertEquals(rsNewCDPurpose.get(0), "01");
            Assert.assertEquals(rsNewCDPurpose.get(1), "00");

            //verify for content cd_purpose = 00 against processed trans
            Assert.assertEquals(ky_ba_ProcessedTran, ky_ba_New00);
            Assert.assertEquals(id_trans_ref_867_ProcessedTran, id_trans_ref_867_New00);
            Assert.assertEquals(id_trans_ref_810_ProcessedTran, id_trans_ref_810_New00);

            //verify for content cd_purpose = 01 against processed trans
            Assert.assertEquals(ky_ba_ProcessedTran, ky_ba_New01);
            Assert.assertEquals(id_trans_ref_867_ProcessedTran, id_trans_ref_867_New01);
            Assert.assertEquals(id_trans_ref_810_ProcessedTran, id_trans_ref_810_New01);
            // store cancel & original id
            cancelTransId = "01";
            originalTransId = "00";

        } else if (rsNewCDPurpose.contains("17") && rsNewCDPurpose.contains("18")) {
            //verify for appearance of 17 and 18
            Assert.assertEquals(rsNewCDPurpose.get(0), "17");
            Assert.assertEquals(rsNewCDPurpose.get(1), "18");

            //verify for content cd_purpose = 17 against processed trans
            Assert.assertEquals(ky_ba_ProcessedTran, ky_ba_New17);
            Assert.assertEquals(id_trans_ref_867_ProcessedTran, id_trans_ref_867_New17);
            Assert.assertEquals(id_trans_ref_810_ProcessedTran, id_trans_ref_810_New17);

            //verify for content cd_purpose = 18 against processed trans
            Assert.assertEquals(ky_ba_ProcessedTran, ky_ba_New18);
            Assert.assertEquals(id_trans_ref_867_ProcessedTran, id_trans_ref_867_New18);
            Assert.assertEquals(id_trans_ref_810_ProcessedTran, id_trans_ref_810_New18);

            // store cancel & original id
            cancelTransId = "17";
            originalTransId = "18";
        } else {
            Assert.fail("Wrong output of cd_purpose: no valid value !");
        }
//step 4:
        //1. get ky_enroll, ky_supplier
        String queryEnroll = "select KY_ENROLL from custpro.cpm_pnd_tran_hdr where ky_pnd_seq_trans = " + tranID;
        String ky_enroll = db.executeQueryReturnString(queryEnroll).get(0);

        String querySupplier = "select KY_SUPPLIER from custpro.cpm_pnd_tran_hdr where ky_pnd_seq_trans = " + tranID;
        String kySupplier = db.executeQueryReturnString(querySupplier).get(0);
        System.out.println("ky supplier: "+kySupplier);
        //2. log in PORTAL with acc : generic
        String userGeneric = "QAGENERIC";
        String pswGeneric = "QA!generic1";

        LoginPage loginPage = Page.Login().goTo();
        loginPage.logonWithEncodedCredential(userGeneric, pswGeneric);

        //3. Search for ky_enroll
        Page.TopNavigation().clickSearchButton();
        Page.Search().selectSupplierByName("Think Energy2");
//        Page.Search().selectSupplierByKySupplier(kySupplier);
        Page.Search().searchByEnrollmentNumberWithFilter("equals", "4583564");
        Page.Search().clickSearchButton();

        //4. choose billing transaction interface view
        Page.Search().selectViewFromEnrollment("Billing Transaction Interface");


        /** 5. click on cancel and original trans
            6. Click on validate button and verify no error
         **/
        String kyPndSeqCancelTransNumber = db.querySpecificInfoFollowingKyEnroll("KY_PND_SEQ_TRANS", tranID, cancelTransId);
        String kyPndSeqOriginalTransNumber = db.querySpecificInfoFollowingKyEnroll("KY_PND_SEQ_TRANS", tranID, originalTransId);

        Page.Search().clickOnTransText(kyPndSeqCancelTransNumber);
        Page.Search().clickOnValidateButton();
        Assert.assertEquals(Page.Search().getTextErrorWorkList(), "No errors exist.");

        Page.Search().clickOnBackBillingTransList();

        Page.Search().clickOnTransText(kyPndSeqOriginalTransNumber);
        Page.Search().clickOnValidateButton();
        Assert.assertEquals(Page.Search().getTextErrorWorkList(), "No errors exist.");

        Page.Search().clickOnBackBillingTransList();

        String queryToClean = "select ky_pnd_seq_trans from custpro.cpm_pnd_tran_hdr " +
                "where ky_enroll in(select ky_enroll " +
                "from custpro.cpm_pnd_tran_hdr where ky_pnd_seq_trans = " + tranID + ") " +
                "and cd_tran_status = 28";
        List<String> kyPndSeqTrans = db.executeQueryReturnString(queryToClean);
        if(!kyPndSeqTrans.isEmpty()){
            for(String i :kyPndSeqTrans)
            Page.Search().clickOnTransText(i);
            Page.Search().clickOnAbandonButton();
        }
    }
//    @BeforeTest
//    public void cleandb(){
//        String queryToClean = "select ky_pnd_seq_trans from custpro.cpm_pnd_tran_hdr " +
//                "where ky_enroll in(select ky_enroll " +
//                "from custpro.cpm_pnd_tran_hdr where ky_pnd_seq_trans = " + tranID + ") " +
//                "and cd_tran_status = 28";
//        List<String> kyPndSeqTrans = db.executeQueryReturnString(queryToClean);
//        if(!kyPndSeqTrans.isEmpty()){
//            for(String i :kyPndSeqTrans){
//                Page.Search().clickOnTransText(i);
//                System.out.println("trans: "+i);
//                Page.Search().clickOnAbandonButton();
//                Page.Search().handlingCommentBox();
//                Page.Search().clickOnBackBillingTransList();
//            }
//
//        }
//    }
}
