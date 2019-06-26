package external;

public class ExternalAPIFactory {
	// update manually if necessary
	private static final String DEFAULT_PIPELINE = "yelp";

	// Start different APIs based on the pipeline.
	public static ExternalAPI getExternalAPI(String pipeline) {
		switch (pipeline) {
		case "ticketmaster":
			return new TicketMasterAPI();
		case "yelp":
			return new YelpAPI();
		default:
			throw new IllegalArgumentException("Invalid pipeline " + pipeline);
		}
	}

	public static ExternalAPI getExternalAPI() {
		return getExternalAPI(DEFAULT_PIPELINE);
	}
}