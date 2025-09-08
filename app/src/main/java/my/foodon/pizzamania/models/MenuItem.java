package my.foodon.pizzamania.models;

public class MenuItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PIZZA = 1;

    private int type;
    private String title;  // For headers
    private Pizza pizza;   // For pizzas

    // Constructor for header
    public MenuItem(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
    }

    // Constructor for pizza
    public MenuItem(Pizza pizza) {
        this.type = TYPE_PIZZA;
        this.pizza = pizza;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public Pizza getPizza() { return pizza; }
}
