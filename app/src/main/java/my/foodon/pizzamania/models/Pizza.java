package my.foodon.pizzamania.models;


public class Pizza {

    // Core fields (public getters/setters required for Firebase)
    private String id;
    private String name;
    private String description;
    private String category;

    // Prices as String to tolerate String or numeric values coming from DB
    private String smallPrice;
    private String mediumPrice;
    private String largePrice;

    private boolean inStock;
    private String imageUrl;

    // Optional local drawable (not used by Firebase mapping)
    private int imageResource;

    // Required empty constructor for Firebase
    public Pizza() {}

    // Convenience constructor for local usage (optional)
    public Pizza(String id,
                 String name,
                 String description,
                 String category,
                 String smallPrice,
                 String mediumPrice,
                 String largePrice,
                 boolean inStock,
                 String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.smallPrice = smallPrice;
        this.mediumPrice = mediumPrice;
        this.largePrice = largePrice;
        this.inStock = inStock;
        this.imageUrl = imageUrl;
    }

    // If building locally from a base price (kept for compatibility)
    public Pizza(String name, String description, double basePrice, int imageResource, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.imageResource = imageResource;
        // derive prices into string form
        double sp = basePrice * PizzaSize.SMALL.getPriceMultiplier();
        double mp = basePrice * PizzaSize.MEDIUM.getPriceMultiplier();
        double lp = basePrice * PizzaSize.LARGE.getPriceMultiplier();
        this.smallPrice = format(sp);
        this.mediumPrice = format(mp);
        this.largePrice = format(lp);
        this.inStock = true;
    }

    // Price enum and helpers
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

    // Safe parsing (handles null/empty/non-numeric)
    private static double parse(String s) {
        try {
            if (s == null) return 0.0;
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static String format(double v) {
        // Keep raw string; adapter can format for display
        return String.valueOf(v);
    }

    // Public getters/setters (needed by Firebase)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSmallPrice() { return smallPrice; }
    public void setSmallPrice(String smallPrice) { this.smallPrice = smallPrice; }

    public String getMediumPrice() { return mediumPrice; }
    public void setMediumPrice(String mediumPrice) { this.mediumPrice = mediumPrice; }

    public String getLargePrice() { return largePrice; }
    public void setLargePrice(String largePrice) { this.largePrice = largePrice; }

    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }

    // Numeric accessors for UI math
    public double small()  { return parse(smallPrice); }
    public double medium() { return parse(mediumPrice); }
    public double large()  { return parse(largePrice); }

    // Unified price API used by adapters
    public double getPrice(PizzaSize size) {
        switch (size) {
            case SMALL:  return small();
            case MEDIUM: return medium();
            case LARGE:  return large();
            default:     return medium();
        }
    }

    // Backward compatibility for callers using getPrice()
    public double getPrice() { return medium(); }
}
