package my.foodon.pizzamania.models;

public class Pizza {
    private String name;
    private String description;
    private double smallPrice;
    private double mediumPrice;
    private double largePrice;
    private int imageResource;

    // Enum for pizza sizes
    public enum PizzaSize {
        SMALL("Small", 0.8),
        MEDIUM("Medium", 1.0),
        LARGE("Large", 1.3);

        private final String displayName;
        private final double priceMultiplier;

        PizzaSize(String displayName, double priceMultiplier) {
            this.displayName = displayName;
            this.priceMultiplier = priceMultiplier;
        }

        public String getDisplayName() { return displayName; }
        public double getPriceMultiplier() { return priceMultiplier; }
    }

    //Constructor
    public Pizza(String name, String description, double basePrice, int imageResource) {
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;

        // Calculate prices for different sizes based on base price (medium)
        this.mediumPrice = basePrice;
        this.smallPrice = basePrice * PizzaSize.SMALL.getPriceMultiplier();
        this.largePrice = basePrice * PizzaSize.LARGE.getPriceMultiplier();
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageResource() { return imageResource; }

    //Size-based price methods
    public double getPrice(PizzaSize size) {
        switch (size) {
            case SMALL: return smallPrice;
            case MEDIUM: return mediumPrice;
            case LARGE: return largePrice;
            default: return mediumPrice;
        }
    }

    public double getSmallPrice() { return smallPrice; }
    public double getMediumPrice() { return mediumPrice; }
    public double getLargePrice() { return largePrice; }

    // For backward compatibility
    public double getPrice() { return mediumPrice; }
}
