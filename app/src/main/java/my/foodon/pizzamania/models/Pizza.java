package my.foodon.pizzamania.models;

public class Pizza {
    private String name;
    private String description;
    private String category;
    private double smallPrice;
    private double mediumPrice;
    private double largePrice;
    private int imageResource;

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

    public Pizza(String name, String description, double basePrice, int imageResource, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageResource = imageResource;
        this.mediumPrice = basePrice;
        this.smallPrice = basePrice * PizzaSize.SMALL.getPriceMultiplier();
        this.largePrice = basePrice * PizzaSize.LARGE.getPriceMultiplier();
    }

    public Pizza(String name, String description, double basePrice, int imageResource) {
        this(name, description, basePrice, imageResource, "Classic");
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getImageResource() { return imageResource; }
    public String getCategory() { return category; }

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
    public double getPrice() { return mediumPrice; }
}
