package my.foodon.pizzamania.models;

public class MenuItem {
    public String id;
    public String name;
    public String description;
    public String category;
    public double smallPrice;
    public double mediumPrice;
    public double largePrice;
    public boolean inStock;
    public String imageUrl;

    public MenuItem() {}

    public MenuItem(String id, String name, String description, String category,
                    double smallPrice, double mediumPrice, double largePrice,
                    boolean inStock, String imageUrl) {
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
}
