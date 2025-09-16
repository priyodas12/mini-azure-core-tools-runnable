package lab.azure.function.model;

public class ProductResponse {

	private ProductInvoice product;

	public ProductResponse(ProductInvoice product) {
		this.product = product;
	}

	public ProductResponse() {
	}

	public ProductInvoice getProduct() {
		return product;
	}

	public void setProduct(ProductInvoice product) {
		this.product = product;
	}
}
