package lab.azure.function;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import lab.azure.function.model.ProductInvoice;
import lab.azure.function.model.ProductResponse;


public class ProductProcessingFunction {

	String url = System.getenv("DB_URL");
	String user = System.getenv("DB_USER");
	String password = System.getenv("DB_PASSWORD");
	String productApiUrl = System.getenv("PRODUCT_API_URL");
	ObjectMapper mapper = new ObjectMapper();

	@FunctionName("timerFunction")
	public void run(
			@TimerTrigger(name = "timer", schedule = "*/15 * * * * *") String timer,
			final ExecutionContext context) {
		String invocationId = context.getInvocationId();
		context.getLogger().info(
				"\n[" + invocationId + " : timerFunction ] started - "
						+ DateTimeFormatter.ISO_INSTANT.format(
						Instant.now()));

		String productApiResp = callProductAPI(context);
		if (productApiResp != null) {
			ProductInvoice productInvoice = mapToProductInvoice(productApiResp, context);
			assert productInvoice != null;
			saveProductInvoice(context, productInvoice);
		}

		context.getLogger().info(
				"\n[" + invocationId + " : timerFunction ] completed - "
						+ DateTimeFormatter.ISO_INSTANT.format(Instant.now())
						+ "\n");
	}


	private String callProductAPI(ExecutionContext context) {
		context.getLogger().info(
				"\n[ callProductAPI ] started - " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(productApiUrl + randomProductId()))
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			context.getLogger().info("\nProduct API Response: \n\n" + response.body());
			return response.body();
		} catch (Exception e) {
			context.getLogger().severe("\nError calling Product API: " + e.getMessage());
		} finally {
			context.getLogger().info(
					"\n[ callProductAPI ] completed - " + DateTimeFormatter.ISO_INSTANT.format(
							Instant.now()));
		}
		return null;
	}

	private ProductInvoice mapToProductInvoice(String productApiResponse, ExecutionContext context) {
		context.getLogger().info(
				"\n[ mapToProductInvoice ] started - " + DateTimeFormatter.ISO_INSTANT.format(
						Instant.now()));
		try {
			ProductResponse productResponse = mapper.readValue(productApiResponse, ProductResponse.class);
			return productResponse.getProduct();
		} catch (JsonProcessingException e) {
			context.getLogger().severe("\nError while mapping productInvoice: " + e.getMessage());
		} finally {
			context.getLogger().info(
					"\n[ mapToProductInvoice ] completed - " + DateTimeFormatter.ISO_INSTANT.format(
							Instant.now()));
		}
		return null;
	}

	private void saveProductInvoice(ExecutionContext context, ProductInvoice productInvoice) {

		context.getLogger().info(
				"\n[ saveProductInvoice ] started - " + DateTimeFormatter.ISO_INSTANT.format(
						Instant.now()));
		String sql = "CALL save_product_invoice(?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(url, user, password);
				CallableStatement stmt = conn.prepareCall(sql)) {

			stmt.setLong(1, productInvoice.getId());
			stmt.setString(2, productInvoice.getProductName());
			stmt.setString(3, productInvoice.getProductType());
			stmt.setString(4, productInvoice.getProductDescription());
			stmt.setBigDecimal(5, productInvoice.getProductPrice());

			stmt.execute();
			context.getLogger().info(
					"\nProductInvoice saved successfully.ProductId: " + productInvoice.getId());

		} catch (SQLException e) {
			context.getLogger().severe("\nError while saving product_invoice : " + e.getMessage());
		} finally {
			context.getLogger().info(
					"\n[ saveProductInvoice ] completed - " + DateTimeFormatter.ISO_INSTANT.format(
							Instant.now()));
		}
	}

	private String randomProductId() {
		Random rand = new Random();
		int randomNum = rand.nextInt(99999);
		return String.valueOf(randomNum);
	}


}
