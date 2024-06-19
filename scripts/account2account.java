

package com.cbe.Plugin;

import com.mcommerce.BankTransaction;
import com.mcommerce.IBankTransaction;
import com.mcommerce.ProductSpec;
import com.mcommerce.mobile.IProductLookup;
import com.mcommerce.mobile.MobileItem;
import com.mcommerce.repository.common.RepositoryException;
import com.mcommerce.server.BillRejection;
import com.mcommerce.server.Enquiry;
import com.mcommerce.server.EnquiryResponse;
import com.mcommerce.server.ExternalUserInfo;
import com.mcommerce.server.FulfillmentRequest;
import com.mcommerce.server.FulfillmentResponse;
import com.mcommerce.server.IBankPaymentMethod;
import com.mcommerce.server.ICustomer;
import com.mcommerce.server.ICustomerBankingDirectFunctions;
import com.mcommerce.server.ProductFulfillmentInfo;
import com.mcommerce.server.ProductSet;
import com.mcommerce.server.Version;
import com.mcommerce.server.VersionResponse;
import com.temenos.plugin.attributes.IT24CustomerAttributes;
import com.temenos.plugins.PluginUtils;
import com.widefield.repository.merchant.adaptor.AbstractBankingMerchantAdaptor;
import com.widefield.util.entry.AmountEntryHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author Robel Version 1
 */
public class R17Person2PersonWithEALMerchant
  extends AbstractBankingMerchantAdaptor
{
  PluginUtils utils = new PluginUtils();
  private Locale locale;
  private AmountEntryHelper amountEntryHelper = new AmountEntryHelper();
 
  public boolean isDirectConnectorUsePossible()
  {
    return true;
  }
 
  public FulfillmentResponse billRejected(BillRejection rejection)
    throws Exception
  {
    return null;
  }
 
  public FulfillmentResponse fulfillProductDelivery(FulfillmentRequest request)
    throws Exception
  {
      IBankTransaction tx = null;
        try {  
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    this.utils.setCoreBankCustomerRef(getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    System.out.println(getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    String theId = getFoundByVendorId();
    String fromPmId = getHelper().removePrefix(theId);
    this.utils.setCoreBankCustomerRef(getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    System.out.println("Core: " + getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    IBankPaymentMethod fromPm = this.utils.getBankingPaymentMethodByIds(getCustomer(), fromPmId.substring(fromPmId.indexOf("-") + 1), getProcessLayer().getBankingCoordinatorProcess(), false);
    if (fromPm == null) {
      throw new RepositoryException("repository.paymentMethodDoesNotExist", fromPmId);
    }
    
    ProductFulfillmentInfo info = request.getLookups()[0];
    theId = info.getProductId();
    String[] accrec = (String[])null;
    accrec = getBeneficiaryById2(theId, getCustomer(), getProcessLayer().getBankingCoordinatorProcess());
    IBankPaymentMethod toeto = this.utils.getBankingPaymentMethodByIds(getCustomer(), theId, getProcessLayer().getBankingCoordinatorProcess(), false);
   
    String selectedOptions = info.getSelectedOptions();
    if (!MobileItem.isExtraOptionsConfigured(selectedOptions))
    {
      getProcessLayer().getLogger().logMessage(4, "BankPaymentMerchant: options missing [" + selectedOptions + "] for getCustomer() [" + super.getCustomer().getObjectId() + "]");
      
      throw new RepositoryException("repository.extraOptionsRequired");
    }
    if (MobileItem.getNumberOfOptions(selectedOptions) < 2)
    {
      getProcessLayer().getLogger().logMessage(4, "BankPaymentMerchant: options not correct [" + selectedOptions + "] for getCustomer() [" + super.getCustomer().getObjectId() + "]");
      
      throw new RepositoryException("repository.extraOptionsRequired");
    }
    IT24CustomerAttributes t24CustomerAttribute = getProcessLayer().getBankingCoordinatorProcess().t24CustomerAttributes(getCustomer());
    
    String[] amountDetails = MobileItem.getOptionsSet(selectedOptions, 0);
    String[] reasonDetails = MobileItem.getOptionsSet(selectedOptions, 1);
    String amountStr = amountDetails[1];
    String reason = reasonDetails[1];
    matcher(reason, reasonDetails[0]);
    reason = reason + " done via ";
    if (t24CustomerAttribute != null)
    {
      if ((t24CustomerAttribute.getMobileChannel() == null) || (t24CustomerAttribute.getMobileChannel().equals(""))) {
        reason = reason + "Mobile";
      } else {
        reason = reason + t24CustomerAttribute.getMobileChannel();
      }
    }
    else {
      reason = reason + "Mobile";
    }
    if (reason.length() > 140) {
      throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.maxCharEntered", reasonDetails));
    }
    Date transDate = new Date();
    long amount = this.amountEntryHelper.getAmountInPennies(amountStr, fromPm.getCurrency(), false);
    
    System.out.println("Inputed Amount" + amount);
    if (0L == amount) {
      throw new RepositoryException("Invalid Amount");
    }
    if ((toeto.getObjectId().equals("1000008967249") | toeto.getObjectId().equals("8881"))) {
        throw new RepositoryException("Please use the DStv menu to pay for DStv package.");
    }
    
    if ((toeto.getObjectId().equals("1000140737686") | toeto.getObjectId().equals("777"))) {
        
        throw new RepositoryException("Please use the ET-Airlines menu to pay for ET-Airlines Ticket.");
    }
         
            tx = makeFundsTransfer(getCustomer(), request.getBasketId(), fromPm.getObjectId(), fromPm.getCurrency(), amount, toeto.getCurrency(), fromPm.getDescription(), accrec[1] + "-" + accrec[2] + "-" + accrec[0].substring(9, accrec[0].length()), toeto.getObjectId(), reason, transDate, getProcessLayer().getBankingCoordinatorProcess());
            formatter.applyPattern("dd-MMM-yyyy");
            FulfillmentResponse response = new FulfillmentResponse(request);
        
            String mobileChannel = "";
            if (t24CustomerAttribute != null) {
                if ((t24CustomerAttribute.getMobileChannel() == null) || (t24CustomerAttribute.getMobileChannel().equals(""))) {
                    mobileChannel = request.getMobileAppId();
                } else {
                    mobileChannel = t24CustomerAttribute.getMobileChannel();
                }
            } else {
                mobileChannel = request.getMobileAppId();
            }
            String sExchangeRate = "";
            if ((tx.getExtraInfo() != null) && (!tx.getExtraInfo().equals(""))) {
                Object[] exch = new Object[1];
                exch[0] = tx.getExtraInfo();
                System.out.println("Exchange Rate 2" + Arrays.toString(exch));
                sExchangeRate = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.Exch", exch);
            }
            Object[] params = new Object[9];
            params[0] = fromPm.getCurrency();
            params[1] = tx.getPresentableAmount();
            params[2] = tx.getPaymentMethodDescription();
            params[3] = tx.getRecipientName();
            params[4] = formatter.format(transDate);
            params[5] = tx.getTransactionId();
            params[6] = mobileChannel;
            params[7] = sExchangeRate;
            params[8] = reason;
            
            String msg = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.Done", params);
            info.setProductName(msg);
            response.setLookups(request.getLookups());
            processFulfillmentResponse(response,tx,fromPm,toeto);
            this.utils.setCoreBankCustomerRef("");
            return response;
        } catch (RepositoryException e) {
            if (tx != null && e.getMessage().contains("repository.") && tx.getTransactionId() != null) {
                throw new RepositoryException("..ETB "+tx.getPresentableAmount()+"debited from "+tx.getPaymentMethodDescription()+"for "+tx.getRecipientName()+"on "+tx.getProcessedDate()+"with transaction ID:"+tx.getTransactionId());
            } else if (e.getMessage().contains("repository.") && tx == null) {
                throw new RepositoryException("System Error, Please try again. for more info please call 951");
            } else {
                throw e;
            }
        }
    }

  public void processFulfillmentResponse(FulfillmentResponse response, IBankTransaction tx, IBankPaymentMethod fromPm, IBankPaymentMethod toeto) {
        try {
        if (tx != null) {
           
            String CreditAcc = MaskAccountNo(toeto.getObjectId());
            String manipulatedObjectId = MaskAccountNo(fromPm.getObjectId());
            String[] Named = fromPm.getFullTitle().split("\\s");
            String Firstnamed0 = Named[0].replaceAll("[^a-zA-Z]","");
            String DebitName = Firstnamed0.substring(0, 1).toUpperCase() + Firstnamed0.substring(1).toLowerCase();
          
            String[] LockedAmountinfo = this.getLockedAmountinfo(fromPm.getObjectId(), this.getCustomer(), (Object) this.getProcessLayer().getBankingCoordinatorProcess());
            String[] CredAmountinfo = this.getLockedAmountinfo(toeto.getObjectId(), this.getCustomer(), (Object) this.getProcessLayer().getBankingCoordinatorProcess());
            String iDd = fromPm.getObjectId().substring(5);
            String iDc = toeto.getObjectId().substring(5);
            String IDc = tx.getTransactionId() + iDc;
            String IDd = tx.getTransactionId() + iDd;
            try {
                //String ResDebit = sendGetRequestTransferToanyCbe("http://172.30.226.15:7790/message?from=8986&to=" + this.getCustomer().getMobilePhoneNumber() + "&text=Dear%20" + FinalNamed0 + "%20your%20Account%20" + manipulatedObjectId + "%20has%20been%20debited%20with%20" + tx.getCurrency() + "%20" + tx.getPresentableAmount() + "%20for%20" + Creditname+ ",%20Your%20Current%20Balance%20is%20" + tx.getCurrency() + "%20" + LockedAmountinfo[7] + "%20Do%20not%20share%20this%20message%20for%20others.%20Thank%20you%20for%20Banking%20with%20CBE!%20https://apps.cbe.com.et:100/?id=" + IDd + "&user=CBEMB&pass=Cbemb@123&id&dlrreq=0");
                String ResDebit = sendGetRequestTransferToanyCbe("http://172.30.226.15:889/message?from=8986&to="+this.getCustomer().getMobilePhoneNumber()+"&text=Dear%20"+DebitName+"%20your%20Account%20"+manipulatedObjectId+"%20has%20been%20debited%20with%20"+tx.getCurrency()+"%20"+tx.getPresentableAmount()+"%20Your%20Current%20Balance%20is%20"+tx.getCurrency()+"%20"+LockedAmountinfo[7]+".%20Thank%20you%20for%20Banking%20with%20CBE!%20https://apps.cbe.com.et:100/?id="+IDd+"&user=CBEMB&pass=Cbemb@123&id&dlrreq=0");
            } catch (Exception ex) {
            }
          String[] PhoneNuminfo = this.getphoneandNameinfo(toeto.getObjectId(), this.getCustomer(), (Object) this.getProcessLayer().getBankingCoordinatorProcess());
            if (PhoneNuminfo.length == 2) {
                 String CreditPhone = PhoneNuminfo[1];
                try {
              String[] Namec = PhoneNuminfo[0].split("\\s");
            String Firstnamec0 = Namec[0].replaceAll("[^a-zA-Z]","");
            String CreditName = Firstnamec0.substring(0, 1).toUpperCase() + Firstnamec0.substring(1).toLowerCase();
                 //String Rescredit = sendGetRequestTransferToanyCbe("http://172.30.226.15:7790/message?from=8986&to=" + CreditPhone + "&text=Dear%20" + FinalNamec0+ "%20your%20Account%20" + CreditAcc + "%20has%20been%20Credited%20with%20" + tx.getCurrency() + "%20" + tx.getPresentableAmount() + ".%20from%20" + Debitname + ",%20Your%20Current%20Balance%20is%20" + tx.getCurrency() + "%20" + CredAmountinfo[7] + "%20Do%20not%20share%20this%20message%20for%20others.%20Thank%20you%20for%20Banking%20with%20CBE!%20https://apps.cbe.com.et:100/?id=" + IDc + "&user=CBEMB&pass=Cbemb@123&id&dlrreq=0");
                String Rescredit = sendGetRequestTransferToanyCbe("http://172.30.226.15:889/message?from=8986&to=" + CreditPhone + "&text=Dear%20"+CreditName+"%20your%20Account%20" + CreditAcc + "%20has%20been%20Credited%20with%20" + tx.getCurrency() + "%20" + tx.getPresentableAmount() + ".%20Your%20Current%20Balance%20is%20" + tx.getCurrency() + "%20" + CredAmountinfo[7] + "%20Thank%20you%20for%20Banking%20with%20CBE!%20https://apps.cbe.com.et:100/?id=" + IDc + "&user=CBEMB&pass=Cbemb@123&id&dlrreq=0");
                } catch (Exception ex) {
                }
            }           
        } else {
        }
    } catch (Exception ex) {
            }
    }   

  private static String MaskAccountNo(String originalObjectId) {
        if (originalObjectId != null && originalObjectId.length() == 13) {        
            return "1*********" + originalObjectId.substring(9);
        } else {  
            return "Invalid Object account no";
        }
    }
 
  public void matcher(String s, String field)
  {
    try
    {
      Pattern p = Pattern.compile("[\\p{Alpha}]*[\\p{Punct}][\\p{Alpha}]*");
      Matcher m = p.matcher(s);
      
      boolean b = m.find();
      if ((s.indexOf("\b") != -1) || (s.indexOf("\t") != -1) || (s.indexOf("\n") != -1) || (s.indexOf("\f") != -1) || (s.indexOf("\r") != -1) || (s.indexOf("\"") != -1) || (s.indexOf("'") != -1) || (s.indexOf("\\") != -1)) {
        b = true;
      }
      if (b) {
        throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "error.SpecialChar", field));
      }
    }
    catch (Exception e)
    {
      throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "error.SpecialChar", field));
    }
  }
 
  public ProductSet findProduct(IProductLookup lookup)
    throws Exception
  {
    String productGroupId = lookup.getProductGroupId();
    String productId = lookup.getProductId();
    return findProduct(productGroupId, productId);
  }
 
  public ProductSet findProduct(String productGroupId, String productId)
    throws Exception
  {
    String s = "";
    ProductSet all = getProducts(productGroupId, s, productId);
    return new ProductSet(all.findProduct(productId));
  }
 
  public static boolean isNumeric(String str)
  {
    try
    {
        double d = Double.parseDouble(str);
    }
    catch (NumberFormatException ex)
    {
      double d;
      System.out.println("Invalid Account number");
      return false;
    }
    return true;
  }
 
  public ProductSet getProducts(String productGroupId, String extraOptions)
    throws Exception
  {
    ProductSet rtn = new ProductSet();
    String theId = getFoundByVendorId();
    String vendorId = "";
    String accNo;
//    String accNo;
    if (!extraOptions.equals(""))
    {
      String[] nickNameDetails = MobileItem.getOptionsSet(extraOptions, 0);
      accNo = nickNameDetails[1];
    }
    else
    {
      accNo = theId.substring(theId.indexOf("-"));
    }
    boolean n = isNumeric(accNo);
    if (!n) {
      throw new RepositoryException("Invalid account number");
    }
    String[] accrec = (String[])null;
    try
    {
      accrec = getBeneficiaryById2(accNo, getCustomer(), getProcessLayer().getBankingCoordinatorProcess());
      if (accrec[2].equals("")) {
        throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.accNotFound"));
      }
    }
    catch (Exception ex)
    {
      throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.accNotFound"));
    }
    vendorId = getFoundByVendorId();
    String options = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.MerchantExtraOpt");
    
    this.utils.setCoreBankCustomerRef(getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    String pmId = getHelper().removePrefix(getFoundByVendorId());
    IBankPaymentMethod frompm = this.utils.getBankingPaymentMethodByIds(getCustomer(), pmId, getProcessLayer().getBankingCoordinatorProcess(), false);
    String title = accrec[1] + "-" + accrec[2] + "-" + accrec[0].substring(9, accrec[0].length());
    
    Object[] param = new Object[2];
    param[0] = (accrec[1] + "-" + accrec[2] + "-" + accrec[0].substring(9, accrec[0].length()));
    param[1] = frompm.getFullTitle();
    String description = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.MerchantPrdDes", param);
    
    ProductSpec product = getAsProduct(vendorId, title, description, accrec[0], "payment", options, "");
    product.setGroups("bullet");
    String extrapgTitle = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.extraPageTitle");
    product.setExtraInfo("~" + extrapgTitle + "~" + accrec[1]);
    
    rtn.addProduct(product);
    return rtn;
  }
 
  public ProductSet getProducts(String productGroupId, String extraOptions, String productId)
    throws Exception
  {
    ProductSet rtn = new ProductSet();
    String theId = getFoundByVendorId();
    String pmId = getHelper().removePrefix(theId);
    String vendorId = "";
    String accNo;
//    String accNo;
    if (!extraOptions.equals(""))
    {
      String[] nickNameDetails = MobileItem.getOptionsSet(extraOptions, 0);
      accNo = nickNameDetails[1];
    }
    else
    {
      accNo = productId;
    }
    this.utils.setCoreBankCustomerRef(getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    System.out.println("Core: " + getProcessLayer().getCoreBankingRepositories()[0].findCoreBankingCustomerReference(getCustomer().getObjectId()));
    IBankPaymentMethod frompm = this.utils.getBankingPaymentMethodByIds(getCustomer(), pmId, getProcessLayer().getBankingCoordinatorProcess(), false);
    if (frompm == null) {
      throw new RepositoryException("repository.paymentMethodDoesNotExist");
    }
    String[] accrec = (String[])null;
    accrec = getBeneficiaryById2(accNo, getCustomer(), getProcessLayer().getBankingCoordinatorProcess());
    vendorId = getFoundByVendorId();
    Object[] param = new Object[2];
    param[0] = (accrec[1] + "-" + accrec[2] + "-" + accrec[0].substring(9, accrec[0].length()));
    param[1] = frompm.getFullTitle();
    
    String description = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.MerchantPrdDes", param);
    String options;
//    String options;
    if ((accNo.equals("1000140737686") | accNo.equals("777"))) {
      options = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "flc.MerchantExtraOpt");
    } else {
      options = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.MerchantExtraOpt");
    }
    ProductSpec product = getAsProduct(vendorId, accrec[1], description, accrec[0], "bullet", options, "");
    product.setGroups("bullet");
    String extrapgTitle = getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.extraPageTitle");
    product.setExtraInfo("~" + extrapgTitle + "~" + accrec[0]);
    rtn.addProduct(product);
    return rtn;
  }
 
  public boolean isFulfillmentPossible()
  {
    return true;
  }
 
  public String[] getBeneficiaryPayList(ICustomer customer, String accNo, Object bankingCoordinator)
  {
    String[] beneficiariesArray = (String[])null;
    int benArrayRow = 0;
    int benArrayColumn = 0;
    Enquiry enquiry = new Enquiry();
    enquiry.setRequestType("OFS.ENQUIRY");
    enquiry.setApplicationName("ENQ");
    enquiry.setOptions("", "ENQUIRY.SELECT", "CBE.ETAL.ENQ");
    enquiry.addEnquiryField("@ID", "EQ", accNo, "");
    String string = null;
    try
    {
      EnquiryResponse response = ((ICustomerBankingDirectFunctions)bankingCoordinator).invokeEnquiry("1-", customer, enquiry);
      StringBuffer rowToDisp = new StringBuffer();
      Iterator<List<String>> iterator;
      if (response.getRowDatas() != null)
      {
        List<List<String>> rowList = response.getRowDatas();
        if (rowList == null)
        {
          rowToDisp.append("No Values to append");
          return null;
        }
        beneficiariesArray = new String[10];
        for (iterator = rowList.iterator(); iterator.hasNext();)
        {
          List<String> list = (List)iterator.next();
          benArrayColumn = 0;
          for (Iterator<String> iterator2 = list.iterator(); iterator2.hasNext(); benArrayColumn++)
          {
            string = (String)iterator2.next();
            beneficiariesArray[benArrayColumn] = string;
          }
          benArrayRow++;
        }
      }
      return beneficiariesArray;
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
      throw new RepositoryException("Enter valid Id");
    }
  }
 
  public BankTransaction makeFundsTransfer(ICustomer customer, String basketId, String sDebitAccount, String sDebitCurrency, long nDebitAmount, String crdCcy, String fromPaymentMethodDescription, String toPaymentMethodDescription, String sCreditAccount, String sPaymentDetails, Date sDate, Object bankingCoordinator)
  {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    double dDebitAmount = 0.0D;
    DecimalFormat df = null;
    if (this.amountEntryHelper.isCurrencyZeroDigitsAfter(sDebitCurrency))
    {
      dDebitAmount = nDebitAmount;
      df = new DecimalFormat("#########");
    }
    else if (this.amountEntryHelper.isCurrencyThreeDigitsAfter(sDebitCurrency))
    {
      dDebitAmount = nDebitAmount / 1000.0D;
      df = new DecimalFormat("#########.###");
    }
    else
    {
      dDebitAmount = nDebitAmount / 100.0D;
      df = new DecimalFormat("#########.##");
    }
        final BigDecimal bDDebitAmount = new BigDecimal(dDebitAmount);
        final Version version = new Version();
        final ExternalUserInfo userInfo = new ExternalUserInfo();
        version.setUserInfo(userInfo);
        version.setRequestType("OFS.APPLICATION");
        version.setApplicationName("FUNDS.TRANSFER");
        version.setOptions("", "PROCESS", this.getProcessLayer().getResourceManager().getResource(this.getCustomersLocale(), "p2p.versionNameEbirwallet"));
        version.addVersionField("DEBIT.ACCT.NO", sDebitAccount , "1", "1");
        version.addVersionField("DEBIT.CURRENCY", sDebitCurrency, "1", "1");
        version.addVersionField("DEBIT.AMOUNT", df.format(bDDebitAmount), "1", "1");
        version.addVersionField("CREDIT.ACCT.NO", sCreditAccount, "1", "1");
        version.addVersionField("TRANSACTION.TYPE", "ACMB", "1", "1");

        System.out.println("DEBIT.ACCT.NO:" + sDebitAccount);
        System.out.println("DEBIT.CURRENCY" + sDebitCurrency);
        System.out.println("DEBIT.AMOUNT" + df.format(bDDebitAmount));
        System.out.println("CREDIT.ACCT.NO" + sCreditAccount);
    if ((sPaymentDetails.length() > 35) || (sPaymentDetails.equals("")) || (sPaymentDetails == null))
    {
      int j = 1;
      while (!sPaymentDetails.equals(""))
      {
        if (j > 4) {
          throw new RepositoryException("You have entered more than 140 characters", "Error");
        }
        try
        {
          int index = 35;
          String temp = sPaymentDetails.substring(0, index);
          version.addVersionField("PAYMENT.DETAILS", temp, String.valueOf(j), "1");
          sPaymentDetails = sPaymentDetails.substring(index);
          j++;
        }
        catch (Exception e)
        {
          version.addVersionField("PAYMENT.DETAILS", sPaymentDetails, String.valueOf(j), "1");
          sPaymentDetails = "";
        }
      }
    }
    else
    {
      version.addVersionField("PAYMENT.DETAILS", sPaymentDetails, "1", "1");
    }
    VersionResponse response = ((ICustomerBankingDirectFunctions)bankingCoordinator).invokeVersion("1-", customer, version);
    String sId = null;
    String sExchangeRate = "";
    Date sProcessDate = null;
    String sProcessDateString = "";
    String sTxnStatus = response.getAccepted();
    if (!sTxnStatus.equalsIgnoreCase("NO"))
    {
      sId = response.getRecordId();
      List<VersionResponse.VersionField> lVFields = response.getVersionFields();
      Iterator<VersionResponse.VersionField> itr = lVFields.iterator();
      while (itr.hasNext())
      {
        VersionResponse.VersionField versionFields = (VersionResponse.VersionField)itr.next();
        if (versionFields.getFieldName().equalsIgnoreCase("CUSTOMER.RATE")) {
          sExchangeRate = versionFields.getSubValueNumber();
        }
        if (versionFields.getFieldName().equalsIgnoreCase("PROCESSING.DATE"))
        {
          sProcessDateString = versionFields.getSubValueNumber();
          try
          {
            sProcessDate = formatter.parse(sProcessDateString);
          }
          catch (Exception e)
          {
            sProcessDate = new Date();
          }
        }
      }
    }
    else
    {
      List<VersionResponse.VersionField> lVFields = response.getVersionFields();
      Iterator<VersionResponse.VersionField> itr = lVFields.iterator();
      if (itr.hasNext())
      {
        VersionResponse.VersionField versionFields = (VersionResponse.VersionField)itr.next();
        throw new RepositoryException(versionFields.getSubValueNumber(), response.getMessageId());
      }
    }
    BankTransaction rtn = new BankTransaction();
    rtn.setAmount(nDebitAmount);
    rtn.setAndFunction(false);
    rtn.setAuthCodeRequired(false);
    rtn.setBasketId(basketId);
    rtn.setChannelName("-");
    rtn.setCurrency(sDebitCurrency);
    rtn.setCustomerId(customer.getObjectId());
    rtn.setCustomerIdentityRequiredBeforePayment(false);
    rtn.setMobileAppId(customer.getMobileAppId());
    rtn.setObjectId(sId);
    rtn.setPaymentMethodDescription(fromPaymentMethodDescription);
    rtn.setPresentableIdentity("");
    rtn.setProcessedDate(sProcessDate);
    rtn.setProductName(sPaymentDetails);
    rtn.setRecipientName(toPaymentMethodDescription);
    rtn.setStatusCode("PAID");
    rtn.setTransactionType("DEBIT");
    rtn.setVerb((short)0);
    rtn.setExtraInfo(sExchangeRate);
    return rtn;
  }
 
 
 
  public ProductSpec getAsProduct(String vendorId, String title, String description, String productId, String type, String extraOptions, String beneficiaryId)
  {
    ProductSpec p = new ProductSpec();
    p.setAddressDeliverySupported(false);
    p.setAddressRequired(false);
    p.setAuthCodeRequiredBeforePayment(false);
    p.setTransactionValidityTime(3600000L);
    p.setCurrency("GBP");
    p.setCustomerMask(0L);
    p.setDescription(description);
    p.setEmailDeliverySupported(false);
    p.setEmailRequired(false);
    p.setExtraOptions(extraOptions);
    p.setFulfilmentRequiredBeforePayment(false);
    p.setMobileDeliverySupported(false);
    p.setName(title);
    p.setObjectId(productId);
    p.setPrice(0L);
    p.setPriceOverrideAvailable(false);
    p.setPurchasePossible(true);
    p.setMinPrice(0L);
    p.setSalesTaxCode("ZERO");
    p.setProductMask(0L);
    p.setType(type);
    p.setVendorId(vendorId);
    p.setVerb((short)3);
    return p;
  }
 
  public String[] getBeneficiaryById2(String id, ICustomer customer, Object bankingCoordinator)
  {
    String[] beneficiariesArray = (String[])null;
    int benArrayColumn = 0;
    int benArrayRow = 0;
    Enquiry enquiry = new Enquiry();
    enquiry.setRequestType("OFS.ENQUIRY");
    enquiry.setApplicationName("ENQ");
    enquiry.setOptions("", "ENQUIRY.SELECT", "ARC.MO.ACCT.LIST");
    enquiry.addEnquiryField("@ID", "EQ", id, "");
    String string = null;
    EnquiryResponse response = ((ICustomerBankingDirectFunctions)bankingCoordinator).invokeEnquiry("1-", customer, enquiry);
    StringBuffer rowToDisp = new StringBuffer();
    Iterator<List<String>> iterator;
    if (response.getRowDatas() != null)
    {
      List<List<String>> rowList = response.getRowDatas();
      if (rowList == null)
      {
        rowToDisp.append("No Values to append");
        return null;
      }
      beneficiariesArray = new String[8];
      for (iterator = rowList.iterator(); iterator.hasNext();)
      {
        List<String> list = (List)iterator.next();
        
        benArrayColumn = 0;
        for (Iterator<String> iterator2 = list.iterator(); iterator2.hasNext(); benArrayColumn++)
        {
          string = (String)iterator2.next();
          beneficiariesArray[benArrayColumn] = string;
        }
        benArrayRow++;
      }
    }
    System.out.println(beneficiariesArray[2]);
    if ((beneficiariesArray[2].equals("")) || (beneficiariesArray[2] == null)) {
      throw new RepositoryException(getProcessLayer().getResourceManager().getResource(getCustomersLocale(), "p2p.accNotFound"));
    }
    return beneficiariesArray;
  }
 
 
   public String[] getLockedAmountinfo(String Account, ICustomer customer, Object bankingCoordinator) {
        String[] LockedAmountinfo = new String[0];
        Enquiry enquiry = new Enquiry();
        enquiry.setRequestType("OFS.ENQUIRY");
        enquiry.setApplicationName("ENQ");
        enquiry.setOptions("", "ENQUIRY.SELECT", "AI.ACCT.BAL.TODAYFORMB");
        enquiry.addEnquiryField("@ID", "EQ", Account, "");
        String string = null;
        EnquiryResponse response = ((ICustomerBankingDirectFunctions) bankingCoordinator).invokeEnquiry("1-", customer, enquiry);
        if (response.getRowDatas() != null) {
            List<List<String>> rowList = response.getRowDatas();
            if (rowList == null) {
                throw new RepositoryException("No Record Found");
            }
            List<String> beneficiaryList = new ArrayList<>();
            for (int i = 0; i < rowList.get(0).size(); i++) {
                for (int j = 0; j < rowList.size(); j++) {
                    String beneficiary = rowList.get(j).get(i);
                    if (beneficiary != null && !beneficiary.isEmpty()) {
                        beneficiaryList.add(beneficiary);
                    }
                }
            }
            LockedAmountinfo = beneficiaryList.toArray(new String[beneficiaryList.size()]);
        }
        int[] indicesToKeep = {0, 1, 2, 3, 4, LockedAmountinfo.length - 1, LockedAmountinfo.length - 2, LockedAmountinfo.length - 3, LockedAmountinfo.length - 4};
        LockedAmountinfo = removeIndicesExcept(LockedAmountinfo, indicesToKeep);
        return LockedAmountinfo;
    }
    public String[] removeIndicesExcept(String[] array, int[] indicesToKeep) {
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            boolean shouldKeep = false;
            for (int index : indicesToKeep) {
                if (i == index) {
                    shouldKeep = true;
                    break;
                }
            }
            if (shouldKeep) {
                resultList.add(array[i]);
            }
        }
        return resultList.toArray(new String[resultList.size()]);
    }
    public static String sendGetRequestTransferToanyCbe(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);        
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        connection.disconnect();
        return response.toString();
    }
    
  public String[] getphoneandNameinfo(String Account, ICustomer customer, Object bankingCoordinator) {
        String[] getphoneinfo = new String[0];
        Enquiry enquiry = new Enquiry();
        enquiry.setRequestType("OFS.ENQUIRY");
        enquiry.setApplicationName("ENQ");
        enquiry.setOptions("", "ENQUIRY.SELECT", "GETPHONE.FROM.ACC,FORMB");
        enquiry.addEnquiryField("ACCOUNT.NUMBER", "EQ", Account, "");
        String string = null;
        EnquiryResponse response = ((ICustomerBankingDirectFunctions) bankingCoordinator).invokeEnquiry("1-", customer, enquiry);
        if (response.getRowDatas() != null) {
            List<List<String>> rowList = response.getRowDatas();
            if (rowList == null) {
                throw new RepositoryException("No Record Found");
            }
            List<String> informationList = new ArrayList<>();
            for (int i = 0; i < rowList.get(0).size(); i++) {
                for (int j = 0; j < rowList.size(); j++) {
                    String information = rowList.get(j).get(i);
                    if (information != null && !information.isEmpty()) {
                        informationList.add(information);
                    }
                }
            }
            getphoneinfo = informationList.toArray(new String[informationList.size()]);
        }
        
        for (int j = 0; j < getphoneinfo.length; j++) {
                System.out.println("Value at index " + j + ": " + getphoneinfo[j]);
            }
        
        return getphoneinfo;
    }
 
      public void makereverseTransfer(final ICustomer customer, final String transactionID, final Object bankingCoordinator) {
        final Version version = new Version();
        final ExternalUserInfo userInfo = new ExternalUserInfo();
        version.setUserInfo(userInfo);
        version.setRequestType("OFS.APPLICATION");
        version.setApplicationName("FUNDS.TRANSFER");
        version.setOptions("R", "PROCESS",  "ARCMOBILE.TXN.NG", transactionID);
        final VersionResponse response = ((ICustomerBankingDirectFunctions) bankingCoordinator).invokeVersion("1-", customer, version);
        System.out.println("Reverse SuccessFail " + response.getSuccessFail());
        System.out.println("Reversal is done for Transaction id:"+ transactionID+" "+response.getMessageId());
    }
  public Locale getLocale()
  {
    return this.locale != null ? this.locale : Locale.ENGLISH;
  }
 
  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }
}


