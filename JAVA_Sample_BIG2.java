package net.authorize.sample.SampleCodeTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.text.DecimalFormat;

import org.junit.Test;

import junit.framework.Assert;
import net.authorize.api.contract.v1.ANetApiResponse;
import net.authorize.api.contract.v1.ARBCreateSubscriptionResponse;
import net.authorize.api.contract.v1.ARBGetSubscriptionResponse;
import net.authorize.api.contract.v1.ARBGetSubscriptionStatusResponse;
import net.authorize.api.contract.v1.ARBUpdateSubscriptionResponse;
import net.authorize.api.contract.v1.CreateCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.CreateCustomerProfileResponse;
import net.authorize.api.contract.v1.CreateCustomerShippingAddressResponse;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.DeleteCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.DeleteCustomerShippingAddressResponse;
import net.authorize.api.contract.v1.GetCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.GetCustomerProfileResponse;
import net.authorize.api.contract.v1.GetCustomerShippingAddressResponse;
import net.authorize.api.contract.v1.GetHostedProfilePageResponse;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.UpdateCustomerPaymentProfileResponse;
import net.authorize.api.contract.v1.UpdateCustomerProfileResponse;
import net.authorize.api.contract.v1.UpdateCustomerShippingAddressResponse;
import net.authorize.api.contract.v1.ValidateCustomerPaymentProfileResponse;
import net.authorize.sample.AcceptSuite.GetAcceptCustomerProfilePage;
import net.authorize.sample.AcceptSuite.GetAnAcceptPaymentPage;
import net.authorize.sample.CustomerProfiles.CreateCustomerPaymentProfile;
import net.authorize.sample.CustomerProfiles.CreateCustomerProfile;
import net.authorize.sample.CustomerProfiles.CreateCustomerProfileFromTransaction;
import net.authorize.sample.CustomerProfiles.CreateCustomerShippingAddress;
import net.authorize.sample.CustomerProfiles.DeleteCustomerPaymentProfile;
import net.authorize.sample.CustomerProfiles.DeleteCustomerProfile;
import net.authorize.sample.CustomerProfiles.DeleteCustomerShippingAddress;
import net.authorize.sample.CustomerProfiles.GetCustomerPaymentProfile;
import net.authorize.sample.CustomerProfiles.GetCustomerProfile;
import net.authorize.sample.CustomerProfiles.GetCustomerShippingAddress;
import net.authorize.sample.CustomerProfiles.UpdateCustomerPaymentProfile;
import net.authorize.sample.CustomerProfiles.UpdateCustomerProfile;
import net.authorize.sample.CustomerProfiles.UpdateCustomerShippingAddress;
import net.authorize.sample.CustomerProfiles.ValidateCustomerPaymentProfile;
import net.authorize.sample.PaymentTransactions.AuthorizeCreditCard;
import net.authorize.sample.PaymentTransactions.CaptureFundsAuthorizedThroughAnotherChannel;
import net.authorize.sample.PaymentTransactions.CapturePreviouslyAuthorizedAmount;
import net.authorize.sample.PaymentTransactions.ChargeCreditCard;
import net.authorize.sample.PaymentTransactions.ChargeCustomerProfile;
import net.authorize.sample.PaymentTransactions.ChargeTokenizedCreditCard;
import net.authorize.sample.PaymentTransactions.CreditBankAccount;
import net.authorize.sample.PaymentTransactions.DebitBankAccount;
import net.authorize.sample.PaymentTransactions.RefundTransaction;
import net.authorize.sample.PaymentTransactions.VoidTransaction;
import net.authorize.sample.PayPalExpressCheckout.AuthorizationAndCapture;
import net.authorize.sample.PayPalExpressCheckout.AuthorizationAndCaptureContinued;
import net.authorize.sample.PayPalExpressCheckout.AuthorizationOnly;
import net.authorize.sample.PayPalExpressCheckout.AuthorizationOnlyContinued;
import net.authorize.sample.PayPalExpressCheckout.Credit;
import net.authorize.sample.PayPalExpressCheckout.GetDetails;
import net.authorize.sample.PayPalExpressCheckout.PriorAuthorizationCapture;
import net.authorize.sample.RecurringBilling.CancelSubscription;
import net.authorize.sample.RecurringBilling.CreateSubscription;
import net.authorize.sample.RecurringBilling.CreateSubscriptionFromCustomerProfile;
import net.authorize.sample.RecurringBilling.GetSubscription;
import net.authorize.sample.RecurringBilling.GetSubscriptionStatus;
import net.authorize.sample.RecurringBilling.UpdateSubscription;
import net.authorize.sample.TransactionReporting.GetTransactionDetails;

public class TestRunner {

	String apiLoginId = Constants.API_LOGIN_ID;
	String transactionKey = Constants.TRANSACTION_KEY;
	String TransactionID = Constants.TRANSACTION_ID;
	String payerID = Constants.PAYER_ID;

	static SecureRandom rgenerator = new SecureRandom();

	private static String getEmail()
	{
		return rgenerator.nextInt(1000000) + "@test.com";
	}

	private static Double getAmount()
	{
		double d = (double)(1.05 + (450.0 * rgenerator.nextDouble()));
		DecimalFormat df = new DecimalFormat("#.##");      
		d = Double.valueOf(df.format(d));
		return d;
	}

	private static short getDays()
	{
		return (short) (rgenerator.nextInt(358) + 7);
	}

	@Test
	public void TestAllSampleCodes()
	{
		String fileName = Constants.CONFIG_FILE;

		int numRetries = 3;

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		TestRunner tr = new TestRunner();
		int cnt = 0;
		String line;
		try {

			while ((line = reader.readLine()) != null)
			{
				String[] items = line.split("\t");

				String apiName = items[0];
				String isDependent = items[1];
				String shouldApiRun = items[2];

				System.out.println(apiName);

				if (!shouldApiRun.equals("1"))
					continue;
				
				
				System.out.println("-------------------");
				System.out.println("Running test case for :: " + apiName);
				System.out.println("-------------------");
				 
				ANetApiResponse response = null;

				cnt++;
				
				for (int i = 0;i<numRetries;++i)
				{
					try
					{
						if (isDependent.equals("0"))
						{
							response = InvokeRunMethod(apiName); 
						}
						else
						{
							String[] namespace = apiName.split("\\.");
							
							String className = namespace[1];
							Class classType = this.getClass();
							response = (ANetApiResponse)classType.getMethod("Test" + className).invoke(tr);
						}

						if ((response != null) && (response.getMessages().getResultCode() == MessageTypeEnum.OK))
							break;
					}
					catch (Exception e)
					{
						System.out.println("Exception in " + apiName + " " + e.toString());
						System.out.println(e.getMessage());
					}
				}
				Assert.assertNotNull(response);
				Assert.assertEquals(response.getMessages().getResultCode(), MessageTypeEnum.OK);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total Sample Runs: " + cnt);
	}

	public ANetApiResponse InvokeRunMethod(String className)
	{
		String fqClassName = "net.authorize.sample." + className;

		Class classType = null;
		try {
			classType = Class.forName(fqClassName);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Method runMethod = null;
		try {
			runMethod = classType.getMethod("run",String.class, String.class);
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			return (ANetApiResponse)runMethod.invoke(null, Constants.API_LOGIN_ID, Constants.TRANSACTION_KEY);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ANetApiResponse TestValidateCustomerPaymentProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse) CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse customerPaymentProfile = (CreateCustomerPaymentProfileResponse)CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		System.out.println(response.getCustomerProfileId());
		System.out.println(customerPaymentProfile.getCustomerPaymentProfileId());
		ValidateCustomerPaymentProfileResponse validateResponse = (ValidateCustomerPaymentProfileResponse) ValidateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId(), customerPaymentProfile.getCustomerPaymentProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return validateResponse;
	}


	public ANetApiResponse TestUpdateCustomerShippingAddress()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerShippingAddressResponse shippingResponse = (CreateCustomerShippingAddressResponse)CreateCustomerShippingAddress.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		UpdateCustomerShippingAddressResponse updateResponse = (UpdateCustomerShippingAddressResponse) UpdateCustomerShippingAddress.run(apiLoginId, transactionKey, response.getCustomerProfileId(), shippingResponse.getCustomerAddressId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return updateResponse;
	}

	public ANetApiResponse TestUpdateCustomerProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		UpdateCustomerProfileResponse updateResponse = (UpdateCustomerProfileResponse) UpdateCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return updateResponse;
	}

	public ANetApiResponse TestUpdateCustomerPaymentProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse paymentProfileResponse = (CreateCustomerPaymentProfileResponse)CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		UpdateCustomerPaymentProfileResponse updateResponse = (UpdateCustomerPaymentProfileResponse) UpdateCustomerPaymentProfile.run(apiLoginId, transactionKey,
				response.getCustomerProfileId(), paymentProfileResponse.getCustomerPaymentProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return updateResponse;
	}

	public ANetApiResponse TestGetCustomerShippingAddress()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerShippingAddressResponse shippingResponse = (CreateCustomerShippingAddressResponse)CreateCustomerShippingAddress.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		GetCustomerShippingAddressResponse getResponse = (GetCustomerShippingAddressResponse) GetCustomerShippingAddress.run(apiLoginId, transactionKey, 
				response.getCustomerProfileId(), shippingResponse.getCustomerAddressId());

		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		return getResponse;
	}

	public ANetApiResponse TestGetCustomerProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		GetCustomerProfileResponse profileResponse = (GetCustomerProfileResponse) GetCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return profileResponse;
	}

	public ANetApiResponse TestGetAcceptCustomerProfilePage()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		GetHostedProfilePageResponse profileResponse = (GetHostedProfilePageResponse) GetAcceptCustomerProfilePage.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return profileResponse;
	}

	public ANetApiResponse TestGetCustomerPaymentProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse paymentProfileResponse = (CreateCustomerPaymentProfileResponse)CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		GetCustomerPaymentProfileResponse getResponse = (GetCustomerPaymentProfileResponse) GetCustomerPaymentProfile.run(apiLoginId, transactionKey,
				response.getCustomerProfileId(), paymentProfileResponse.getCustomerPaymentProfileId());

		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		return getResponse;
	}

	public ANetApiResponse TestDeleteCustomerShippingAddress()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerShippingAddressResponse shippingResponse = (CreateCustomerShippingAddressResponse)CreateCustomerShippingAddress.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerShippingAddressResponse deleteResponse = (DeleteCustomerShippingAddressResponse) DeleteCustomerShippingAddress.run(apiLoginId, transactionKey,
				response.getCustomerProfileId(), shippingResponse.getCustomerAddressId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return deleteResponse;
	}

	public ANetApiResponse TestDeleteCustomerProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		return DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
	}

	public ANetApiResponse TestDeleteCustomerPaymentProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse paymentProfileResponse = (CreateCustomerPaymentProfileResponse)CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerPaymentProfileResponse deleteResponse = (DeleteCustomerPaymentProfileResponse) DeleteCustomerPaymentProfile.run(apiLoginId, transactionKey,
				response.getCustomerProfileId(), paymentProfileResponse.getCustomerPaymentProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return deleteResponse;
	}

	public ANetApiResponse TestCreateCustomerShippingAddress()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerShippingAddressResponse shippingResponse = (CreateCustomerShippingAddressResponse)CreateCustomerShippingAddress.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return shippingResponse;
	}

	public ANetApiResponse TestAuthorizeCreditCard()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizeCreditCard.run(apiLoginId, transactionKey, getAmount());
		return response;
	}
	
	public ANetApiResponse TestDebitBankAccount()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)DebitBankAccount.run(apiLoginId, transactionKey, getAmount());
		return response;
	}
	
	public ANetApiResponse TestChargeTokenizedCreditCard()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)ChargeTokenizedCreditCard.run(apiLoginId, transactionKey, getAmount());
		return response;
	}

	public ANetApiResponse TestGetTransactionDetails()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizeCreditCard.run(apiLoginId, transactionKey, getAmount());
		return GetTransactionDetails.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
	}

	public ANetApiResponse TestChargeCreditCard()
	{
		return ChargeCreditCard.run(apiLoginId, transactionKey, getAmount());
	}
	
	public ANetApiResponse TestCreateCustomerProfileFromTransaction()
	{
		return CreateCustomerProfileFromTransaction.run(apiLoginId, transactionKey, getAmount(), getEmail());
	}

	public ANetApiResponse TestCapturePreviouslyAuthorizedAmount()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizeCreditCard.run(apiLoginId, transactionKey, getAmount());
		return CapturePreviouslyAuthorizedAmount.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
	}

	public ANetApiResponse TestRefundTransaction()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizeCreditCard.run(apiLoginId, transactionKey, getAmount());
		response = (CreateTransactionResponse)CapturePreviouslyAuthorizedAmount.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
		return RefundTransaction.run(apiLoginId, transactionKey, getAmount(), response.getTransactionResponse().getTransId());
	}

	public ANetApiResponse TestVoidTransaction()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizeCreditCard.run(apiLoginId, transactionKey, getAmount());
		return VoidTransaction.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
	}

	public ANetApiResponse TestCreditBankAccount()
	{
		return CreditBankAccount.run(apiLoginId, transactionKey, TransactionID, getAmount());
	}

	public ANetApiResponse TestChargeCustomerProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse paymentProfileResponse = (CreateCustomerPaymentProfileResponse)CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
		CreateTransactionResponse chargeResponse = (CreateTransactionResponse) ChargeCustomerProfile.run(apiLoginId, transactionKey,
				response.getCustomerProfileId(), paymentProfileResponse.getCustomerPaymentProfileId(), getAmount());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());

		return chargeResponse;
	}

	public ANetApiResponse TestPayPalVoid()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
		return net.authorize.sample.PayPalExpressCheckout.Void.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
	}  

	public ANetApiResponse TestPayPalAuthorizationAndCapture()
	{
		return AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
	}

	public ANetApiResponse TestPayPalAuthorizationAndCaptureContinued()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
		return AuthorizationAndCaptureContinued.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId(), payerID, getAmount());
	}
	
	public ANetApiResponse TestPayPalAuthorizationOnly()
	{
		return AuthorizationOnly.run(apiLoginId, transactionKey, getAmount());
	}

	public ANetApiResponse TestPayPalAuthorizationOnlyContinued()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
		return AuthorizationOnlyContinued.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId(), payerID, getAmount());
	}

	public ANetApiResponse TestPayPalCredit()
	{
		return Credit.run(apiLoginId, transactionKey, TransactionID);
	}

	public ANetApiResponse TestPayPalGetDetails()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
		return GetDetails.run(apiLoginId, transactionKey, response.getTransactionResponse()
				.getTransId());
	}

	public ANetApiResponse TestPayPalPriorAuthorizationCapture()
	{
		CreateTransactionResponse response = (CreateTransactionResponse)AuthorizationAndCapture.run(apiLoginId, transactionKey, getAmount());
		return PriorAuthorizationCapture.run(apiLoginId, transactionKey, response.getTransactionResponse().getTransId());
	}

	public ANetApiResponse TestCancelSubscription()
	{
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse)CreateSubscription.run(apiLoginId, transactionKey, getDays(), getAmount());
		return CancelSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());
	}

	public ANetApiResponse TestCreateSubscription()
	{
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse)CreateSubscription.run(apiLoginId, transactionKey,  getDays(), getAmount());
		CancelSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());

		return response;
	}
	
	public ANetApiResponse TestCreateSubscriptionFromCustomerProfile()
	{
		CreateCustomerProfileResponse profileResponse = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		CreateCustomerPaymentProfileResponse paymentResponse = (CreateCustomerPaymentProfileResponse) CreateCustomerPaymentProfile.
				run(apiLoginId, transactionKey, profileResponse.getCustomerProfileId());

		CreateCustomerShippingAddressResponse shippingResponse = (CreateCustomerShippingAddressResponse)CreateCustomerShippingAddress.
				run(apiLoginId, transactionKey, profileResponse.getCustomerProfileId());
		
		try
		{
			Thread.sleep(10000);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse) CreateSubscriptionFromCustomerProfile.run(apiLoginId, transactionKey, getDays(), getAmount(), profileResponse.getCustomerProfileId(), 
				paymentResponse.getCustomerPaymentProfileId(), shippingResponse.getCustomerAddressId());

		CancelSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());
		DeleteCustomerProfile.run(apiLoginId, transactionKey, profileResponse.getCustomerProfileId());
		
		return response;
	}

	public ANetApiResponse TestGetSubscriptionStatus()
	{
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse)CreateSubscription.run(apiLoginId, transactionKey, getDays(), getAmount());
		ARBGetSubscriptionStatusResponse subscriptionResponse = (ARBGetSubscriptionStatusResponse) GetSubscriptionStatus.run(apiLoginId, transactionKey, response.getSubscriptionId());


		return subscriptionResponse;
	}

	public ANetApiResponse TestGetSubscription()
	{
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse)CreateSubscription.run(apiLoginId, transactionKey, getDays(), getAmount());
		ARBGetSubscriptionResponse getResponse = (ARBGetSubscriptionResponse) GetSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());
		CancelSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());
		return getResponse;
	}

	public ANetApiResponse TestUpdateSubscription()
	{
		ARBCreateSubscriptionResponse response = (ARBCreateSubscriptionResponse)CreateSubscription.run(apiLoginId, transactionKey, getDays(), getAmount());
		ARBUpdateSubscriptionResponse updateResponse = (ARBUpdateSubscriptionResponse) UpdateSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());
		CancelSubscription.run(apiLoginId, transactionKey, response.getSubscriptionId());

		return updateResponse;
	}

	public ANetApiResponse TestCreateCustomerProfile()
	{
		return CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
	}
	
	public ANetApiResponse TestCaptureFundsAuthorizedThroughAnotherChannel()
	{
		return CaptureFundsAuthorizedThroughAnotherChannel.run(apiLoginId, transactionKey, getAmount());
	}

	public ANetApiResponse TestCreateCustomerPaymentProfile()
	{
		CreateCustomerProfileResponse response = (CreateCustomerProfileResponse)CreateCustomerProfile.run(apiLoginId, transactionKey, getEmail());
		return CreateCustomerPaymentProfile.run(apiLoginId, transactionKey, response.getCustomerProfileId());
	}
	
	public ANetApiResponse TestGetAnAcceptPaymentPage()
	{
		return GetAnAcceptPaymentPage.run(apiLoginId, transactionKey, getAmount());
	}
}


package servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import constants.IOnlineBookStoreConstants;
import sql.IBookConstants;

public class AddBookServlet extends GenericServlet{
	public void service(ServletRequest req,ServletResponse res) throws IOException,ServletException
	{
		PrintWriter pw = res.getWriter();
		
		res.setContentType(IOnlineBookStoreConstants.CONTENT_TYPE_TEXT_HTML);
		
		String bCode = req.getParameter(IBookConstants.COLUMN_BARCODE);
		String bName = req.getParameter(IBookConstants.COLUMN_NAME);
		String bAuthor = req.getParameter(IBookConstants.COLUMN_AUTHOR);
		int bPrice =Integer.parseInt(req.getParameter(IBookConstants.COLUMN_PRICE));
		int bQty = Integer.parseInt(req.getParameter(IBookConstants.COLUMN_QUANTITY));
		
		try {
			Connection con = DBConnection.getCon();
			PreparedStatement ps = con.prepareStatement("insert into " + IBookConstants.TABLE_BOOK + "  values(?,?,?,?,?)");
			ps.setString(1, bCode);
			ps.setString(2, bName);
			ps.setString(3, bAuthor);
			ps.setInt(4, bPrice);
			ps.setInt(5, bQty);
			int k = ps.executeUpdate();
			if(k==1)
			{
				RequestDispatcher rd = req.getRequestDispatcher("AddBook.html");
				rd.include(req, res);
				pw.println("<div class=\"tab\">Book Detail Updated Successfully!<br/>Add More Books</div>");
			}
			else
			{
				RequestDispatcher rd = req.getRequestDispatcher("AddBook.html");
				pw.println("<div class=\"tab\">Failed to Add Books! Fill up CareFully</div>");
				rd.include(req, res);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}


package servlets;
import java.io.*;
import java.sql.*;
import javax.servlet.*;

import constants.IOnlineBookStoreConstants;
import sql.IBookConstants;
import sql.IUserContants;
public class BuyBooksServlet extends GenericServlet{
	public void service(ServletRequest req,ServletResponse res) throws IOException,ServletException
	{
		PrintWriter pw = res.getWriter();
		res.setContentType(IOnlineBookStoreConstants.CONTENT_TYPE_TEXT_HTML);
		try {
			Connection con = DBConnection.getCon();
			//ArrayList<Books> al = new ArrayList<Books>();
			PreparedStatement ps = con.prepareStatement("Select * from " + IBookConstants.TABLE_BOOK);
			ResultSet rs = ps.executeQuery();
			RequestDispatcher rd = req.getRequestDispatcher("ViewBooks.html");
			rd.include(req, res);
			pw.println("<div class=\"tab hd brown \">Books Available In Our Store</div>");
			pw.println("<div class=\"tab\"><form action=\"buys\" method=\"post\">");
			pw.println("<table>\r\n" + 
					"			<tr>\r\n" + 
					"				<th>Books</th>\r\n" + 
					"				<th>Code</th>\r\n" + 
					"				<th>Name</th>\r\n" + 
					"				<th>Author</th>\r\n" + 
					"				<th>Price</th>\r\n" + 
					"				<th>Avail</th>\r\n" + 
					"				<th>Qty</th>\r\n" + 
					"			</tr>");
			int i=0;
			while(rs.next())
			{
				String bCode = rs.getString(1);
				String bName = rs.getString(2);
				String bAuthor = rs.getString(3);
				int bPrice = rs.getInt(4);
				int bAvl = rs.getInt(5);
				i=i+1;
				String n = "checked"+ Integer.toString(i);
				String q = "qty"+Integer.toString(i);
				pw.println("<tr>\r\n" + 
						"				<td>\r\n" + 
						"					<input type=\"checkbox\" name="+n+" value=\"pay\">\r\n" + //Value is made equal to bcode
						"				</td>");
				pw.println("<td>"+bCode+"</td>");
				pw.println("<td>"+bName+"</td>");
				pw.println("<td>"+bAuthor+"</td>");
				pw.println("<td>"+bPrice+"</td>");
				pw.println("<td>"+bAvl+"</td>");
				pw.println("<td><input type=\"text\" name="+q+" value=\"0\" text-align=\"center\"></td></tr>");
				
			}
			pw.println("</table>\r\n" + "<input type=\"submit\" value=\" PAY NOW \">"+"<br/>"+
					"	</form>\r\n" + 
					"	</div>");
			//pw.println("<div class=\"tab\"><a href=\"AddBook.html\">Add More Books</a></div>");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

class Food implements Serializable
{
    int itemno;
    int quantity;   
    float price;
    
    Food(int itemno,int quantity)
    {
        this.itemno=itemno;
        this.quantity=quantity;
        switch(itemno)
        {
            case 1:price=quantity*50;
                break;
            case 2:price=quantity*60;
                break;
            case 3:price=quantity*70;
                break;
            case 4:price=quantity*30;
                break;
        }
    }
}
class Singleroom implements Serializable
{
    String name;
    String contact;
    String gender;   
    ArrayList<Food> food =new ArrayList<>();

   
    Singleroom()
    {
        this.name="";
    }
    Singleroom(String name,String contact,String gender)
    {
        this.name=name;
        this.contact=contact;
        this.gender=gender;
    }
}
class Doubleroom extends Singleroom implements Serializable
{ 
    String name2;
    String contact2;
    String gender2;  
    
    Doubleroom()
    {
        this.name="";
        this.name2="";
    }
    Doubleroom(String name,String contact,String gender,String name2,String contact2,String gender2)
    {
        this.name=name;
        this.contact=contact;
        this.gender=gender;
        this.name2=name2;
        this.contact2=contact2;
        this.gender2=gender2;
    }
}
class NotAvailable extends Exception
{
    @Override
    public String toString()
    {
        return "Not Available !";
    }
}

class holder implements Serializable
{
    Doubleroom luxury_doublerrom[]=new Doubleroom[10]; //Luxury
    Doubleroom deluxe_doublerrom[]=new Doubleroom[20]; //Deluxe
    Singleroom luxury_singleerrom[]=new Singleroom[10]; //Luxury
    Singleroom deluxe_singleerrom[]=new Singleroom[20]; //Deluxe
}

class Hotel
{
    static holder hotel_ob=new holder();
    static Scanner sc = new Scanner(System.in);
    static void CustDetails(int i,int rn)
    {
        String name, contact, gender;
        String name2 = null, contact2 = null; 
        String gender2="";
        System.out.print("\nEnter customer name: ");
        name = sc.next();
        System.out.print("Enter contact number: ");
        contact=sc.next();
        System.out.print("Enter gender: ");
        gender = sc.next();
        if(i<3)
        {
        System.out.print("Enter second customer name: ");
        name2 = sc.next();
        System.out.print("Enter contact number: ");
        contact2=sc.next();
        System.out.print("Enter gender: ");
        gender2 = sc.next();
        }      
        
          switch (i) {
            case 1:hotel_ob.luxury_doublerrom[rn]=new Doubleroom(name,contact,gender,name2,contact2,gender2);
                break;
            case 2:hotel_ob.deluxe_doublerrom[rn]=new Doubleroom(name,contact,gender,name2,contact2,gender2);
                break;
            case 3:hotel_ob.luxury_singleerrom[rn]=new Singleroom(name,contact,gender);
                break;
            case 4:hotel_ob.deluxe_singleerrom[rn]=new Singleroom(name,contact,gender);
                break;
            default:System.out.println("Wrong option");
                break;
        }
    }
    
    static void bookroom(int i)
    {
        int j;
        int rn;
        System.out.println("\nChoose room number from : ");
        switch (i) {
            case 1:
                for(j=0;j<hotel_ob.luxury_doublerrom.length;j++)
                {
                    if(hotel_ob.luxury_doublerrom[j]==null)
                    {
                        System.out.print(j+1+",");
                    }
                }
                System.out.print("\nEnter room number: ");
                try{
                rn=sc.nextInt();
                rn--;
                if(hotel_ob.luxury_doublerrom[rn]!=null)
                    throw new NotAvailable();
                CustDetails(i,rn);
                }
                catch(Exception e)
                {
                    System.out.println("Invalid Option");
                    return;
                }
                break;
            case 2:
                 for(j=0;j<hotel_ob.deluxe_doublerrom.length;j++)
                {
                    if(hotel_ob.deluxe_doublerrom[j]==null)
                    {
                        System.out.print(j+11+",");
                    }
                }
                System.out.print("\nEnter room number: ");
                try{
                rn=sc.nextInt();
                rn=rn-11;
                if(hotel_ob.deluxe_doublerrom[rn]!=null)
                    throw new NotAvailable();
                CustDetails(i,rn);
                }
                catch(Exception e)
                {
                    System.out.println("Invalid Option");
                    return;
                }
                break;
            case 3:
                  for(j=0;j<hotel_ob.luxury_singleerrom.length;j++)
                {
                    if(hotel_ob.luxury_singleerrom[j]==null)
                    {
                        System.out.print(j+31+",");
                    }
                }
                System.out.print("\nEnter room number: ");
                try{
                rn=sc.nextInt();
                rn=rn-31;
                if(hotel_ob.luxury_singleerrom[rn]!=null)
                    throw new NotAvailable();
                CustDetails(i,rn);
                }
                catch(Exception e)
                {
                    System.out.println("Invalid Option");
                    return;
                }
                break;
            case 4:
                  for(j=0;j<hotel_ob.deluxe_singleerrom.length;j++)
                {
                    if(hotel_ob.deluxe_singleerrom[j]==null)
                    {
                        System.out.print(j+41+",");
                    }
                }
                System.out.print("\nEnter room number: ");
                try{
                rn=sc.nextInt();
                rn=rn-41;
                if(hotel_ob.deluxe_singleerrom[rn]!=null)
                    throw new NotAvailable();
                CustDetails(i,rn);
                }
                catch(Exception e)
                {
                   System.out.println("Invalid Option");
                    return;
                }
                break;
            default:
                System.out.println("Enter valid option");
                break;
        }
        System.out.println("Room Booked");
    }
    
    static void features(int i)
    {
        switch (i) {
            case 1:System.out.println("Number of double beds : 1\nAC : Yes\nFree breakfast : Yes\nCharge per day:4000 ");
                break;
            case 2:System.out.println("Number of double beds : 1\nAC : No\nFree breakfast : Yes\nCharge per day:3000  ");
                break;
            case 3:System.out.println("Number of single beds : 1\nAC : Yes\nFree breakfast : Yes\nCharge per day:2200  ");
                break;
            case 4:System.out.println("Number of single beds : 1\nAC : No\nFree breakfast : Yes\nCharge per day:1200 ");
                break;
            default:
                System.out.println("Enter valid option");
                break;
        }
    }
    
    static void availability(int i)
    {
      int j,count=0;
        switch (i) {
            case 1:
                for(j=0;j<10;j++)
                {
                    if(hotel_ob.luxury_doublerrom[j]==null)
                        count++;
                }
                break;
            case 2:
                for(j=0;j<hotel_ob.deluxe_doublerrom.length;j++)
                {
                    if(hotel_ob.deluxe_doublerrom[j]==null)
                        count++;
                }
                break;
            case 3:
                for(j=0;j<hotel_ob.luxury_singleerrom.length;j++)
                {
                    if(hotel_ob.luxury_singleerrom[j]==null)
                        count++;
                }
                break;
            case 4:
                for(j=0;j<hotel_ob.deluxe_singleerrom.length;j++)
                {
                    if(hotel_ob.deluxe_singleerrom[j]==null)
                        count++;
                }
                break;
            default:
                System.out.println("Enter valid option");
                break;
        }
        System.out.println("Number of rooms available : "+count);
    }
    
    static void bill(int rn,int rtype)
    {
        double amount=0;
        String list[]={"Sandwich","Pasta","Noodles","Coke"};
        System.out.println("\n*******");
        System.out.println(" Bill:-");
        System.out.println("*******");
               
        switch(rtype)
        {
            case 1:
                amount+=4000;
                    System.out.println("\nRoom Charge - "+4000);
                    System.out.println("\n===============");
                    System.out.println("Food Charges:- ");
                    System.out.println("===============");
                     System.out.println("Item   Quantity    Price");
                    System.out.println("-------------------------");
                    for(Food obb:hotel_ob.luxury_doublerrom[rn].food)
                    {
                        amount+=obb.price;
                        String format = "%-10s%-10s%-10s%n";
                        System.out.printf(format,list[obb.itemno-1],obb.quantity,obb.price );
                    }
                    
                break;
            case 2:amount+=3000;
                    System.out.println("Room Charge - "+3000);
                    System.out.println("\nFood Charges:- ");
                    System.out.println("===============");
                     System.out.println("Item   Quantity    Price");
                    System.out.println("-------------------------");
                    for(Food obb:hotel_ob.deluxe_doublerrom[rn].food)
                    {
                        amount+=obb.price;
                        String format = "%-10s%-10s%-10s%n";
                        System.out.printf(format,list[obb.itemno-1],obb.quantity,obb.price );
                    }
                break;
            case 3:amount+=2200;
                    System.out.println("Room Charge - "+2200);
                    System.out.println("\nFood Charges:- ");
                    System.out.println("===============");
                    System.out.println("Item   Quantity    Price");
                    System.out.println("-------------------------");
                    for(Food obb:hotel_ob.luxury_singleerrom[rn].food)
                    {
                        amount+=obb.price;
                        String format = "%-10s%-10s%-10s%n";
                        System.out.printf(format,list[obb.itemno-1],obb.quantity,obb.price );
                    }
                break;
            case 4:amount+=1200;
                    System.out.println("Room Charge - "+1200);
                    System.out.println("\nFood Charges:- ");
                    System.out.println("===============");
                    System.out.println("Item   Quantity    Price");
                    System.out.println("-------------------------");
                    for(Food obb: hotel_ob.deluxe_singleerrom[rn].food)
                    {
                        amount+=obb.price;
                        String format = "%-10s%-10s%-10s%n";
                        System.out.printf(format,list[obb.itemno-1],obb.quantity,obb.price );
                    }
                break;
            default:
                System.out.println("Not valid");
        }
        System.out.println("\nTotal Amount- "+amount);
    }
    
    static void deallocate(int rn,int rtype)
    {
        int j;
        char w;
        switch (rtype) {
            case 1:               
                if(hotel_ob.luxury_doublerrom[rn]!=null)
                    System.out.println("Room used by "+hotel_ob.luxury_doublerrom[rn].name);                
                else 
                {    
                    System.out.println("Empty Already");
                        return;
                }
                System.out.println("Do you want to checkout ?(y/n)");
                 w=sc.next().charAt(0);
                if(w=='y'||w=='Y')
                {
                    bill(rn,rtype);
                    hotel_ob.luxury_doublerrom[rn]=null;
                    System.out.println("Deallocated succesfully");
                }
                
                break;
            case 2:
                if(hotel_ob.deluxe_doublerrom[rn]!=null)
                    System.out.println("Room used by "+hotel_ob.deluxe_doublerrom[rn].name);                
                else 
                {    
                    System.out.println("Empty Already");
                        return;
                }
                System.out.println(" Do you want to checkout ?(y/n)");
                 w=sc.next().charAt(0);
                if(w=='y'||w=='Y')
                {
                    bill(rn,rtype);
                    hotel_ob.deluxe_doublerrom[rn]=null;
                    System.out.println("Deallocated succesfully");
                }
                 
                break;
            case 3:
                if(hotel_ob.luxury_singleerrom[rn]!=null)
                    System.out.println("Room used by "+hotel_ob.luxury_singleerrom[rn].name);                
                else 
                 {    
                    System.out.println("Empty Already");
                        return;
                }
                System.out.println(" Do you want to checkout ? (y/n)");
                w=sc.next().charAt(0);
                if(w=='y'||w=='Y')
                {
                    bill(rn,rtype);
                    hotel_ob.luxury_singleerrom[rn]=null;
                    System.out.println("Deallocated succesfully");
                }
                
                break;
            case 4:
                if(hotel_ob.deluxe_singleerrom[rn]!=null)
                    System.out.println("Room used by "+hotel_ob.deluxe_singleerrom[rn].name);                
                else 
                 {    
                    System.out.println("Empty Already");
                        return;
                }
                System.out.println(" Do you want to checkout ? (y/n)");
                 w=sc.next().charAt(0);
                if(w=='y'||w=='Y')
                {
                    bill(rn,rtype);
                    hotel_ob.deluxe_singleerrom[rn]=null;
                    System.out.println("Deallocated succesfully");
                }
                break;
            default:
                System.out.println("\nEnter valid option : ");
                break;
        }
    }
    
    static void order(int rn,int rtype)
    {
        int i,q;
        char wish;
         try{
             System.out.println("\n==========\n   Menu:  \n==========\n\n1.Sandwich\tRs.50\n2.Pasta\t\tRs.60\n3.Noodles\tRs.70\n4.Coke\t\tRs.30\n");
        do
        {
            i = sc.nextInt();
            System.out.print("Quantity- ");
            q=sc.nextInt();
           
              switch(rtype){
            case 1: hotel_ob.luxury_doublerrom[rn].food.add(new Food(i,q));
                break;
            case 2: hotel_ob.deluxe_doublerrom[rn].food.add(new Food(i,q));
                break;
            case 3: hotel_ob.luxury_singleerrom[rn].food.add(new Food(i,q));
                break;
            case 4: hotel_ob.deluxe_singleerrom[rn].food.add(new Food(i,q));
                break;                                                 
        }
              System.out.println("Do you want to order anything else ? (y/n)");
              wish=sc.next().charAt(0); 
        }while(wish=='y'||wish=='Y');  
        }
         catch(NullPointerException e)
            {
                System.out.println("\nRoom not booked");
            }
         catch(Exception e)
         {
             System.out.println("Cannot be done");
         }
    }
}


class write implements Runnable
{
    holder hotel_ob;
    write(holder hotel_ob)
    {
        this.hotel_ob=hotel_ob;
    }
    @Override
    public void run() {
          try{
        FileOutputStream fout=new FileOutputStream("backup");
        ObjectOutputStream oos=new ObjectOutputStream(fout);
        oos.writeObject(hotel_ob);
        }
        catch(Exception e)
        {
            System.out.println("Error in writing "+e);
        }         
        
    }
    
}

public class Main {
    public static void main(String[] args){
        
        try
        {           
        File f = new File("backup");
        if(f.exists())
        {
            FileInputStream fin=new FileInputStream(f);
            ObjectInputStream ois=new ObjectInputStream(fin);
            Hotel.hotel_ob=(holder)ois.readObject();
        }
        Scanner sc = new Scanner(System.in);
        int ch,ch2;
        char wish;
        x:
        do{

        System.out.println("\nEnter your choice :\n1.Display room details\n2.Display room availability \n3.Book\n4.Order food\n5.Checkout\n6.Exit\n");
        ch = sc.nextInt();
        switch(ch){
            case 1: System.out.println("\nChoose room type :\n1.Luxury Double Room \n2.Deluxe Double Room \n3.Luxury Single Room \n4.Deluxe Single Room\n");
                    ch2 = sc.nextInt();
                    Hotel.features(ch2);
                break;
            case 2:System.out.println("\nChoose room type :\n1.Luxury Double Room \n2.Deluxe Double Room \n3.Luxury Single Room\n4.Deluxe Single Room\n");
                     ch2 = sc.nextInt();
                     Hotel.availability(ch2);
                break;
            case 3:System.out.println("\nChoose room type :\n1.Luxury Double Room \n2.Deluxe Double Room \n3.Luxury Single Room\n4.Deluxe Single Room\n");
                     ch2 = sc.nextInt();
                     Hotel.bookroom(ch2);                     
                break;
            case 4:
                 System.out.print("Room Number -");
                     ch2 = sc.nextInt();
                     if(ch2>60)
                         System.out.println("Room doesn't exist");
                     else if(ch2>40)
                         Hotel.order(ch2-41,4);
                     else if(ch2>30)
                         Hotel.order(ch2-31,3);
                     else if(ch2>10)
                         Hotel.order(ch2-11,2);
                     else if(ch2>0)
                         Hotel.order(ch2-1,1);
                     else
                         System.out.println("Room doesn't exist");
                     break;
            case 5:                 
                System.out.print("Room Number -");
                     ch2 = sc.nextInt();
                     if(ch2>60)
                         System.out.println("Room doesn't exist");
                     else if(ch2>40)
                         Hotel.deallocate(ch2-41,4);
                     else if(ch2>30)
                         Hotel.deallocate(ch2-31,3);
                     else if(ch2>10)
                         Hotel.deallocate(ch2-11,2);
                     else if(ch2>0)
                         Hotel.deallocate(ch2-1,1);
                     else
                         System.out.println("Room doesn't exist");
                     break;
            case 6:break x;
                
        }
           
            System.out.println("\nContinue : (y/n)");
            wish=sc.next().charAt(0); 
            if(!(wish=='y'||wish=='Y'||wish=='n'||wish=='N'))
            {
                System.out.println("Invalid Option");
                System.out.println("\nContinue : (y/n)");
                wish=sc.next().charAt(0); 
            }
            
        }while(wish=='y'||wish=='Y');    
        
        Thread t=new Thread(new write(Hotel.hotel_ob));
        t.start();
        }        
            catch(Exception e)
            {
                System.out.println("Not a valid input");
            }
    }
}

