package my.foodon.pizzamania.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.foodon.pizzamania.BranchSession;
import my.foodon.pizzamania.models.Pizza;
import my.foodon.pizzamania.models.Order;

public class ChatbotService {
    
    private DatabaseReference menuRef;
    private DatabaseReference ordersRef;
    private String userId;
    private Context context;
    
    public ChatbotService(String userId, Context context) {
        this.userId = userId;
        this.context = context;
        
        // Use dynamic branch path based on user's location
        String menuPath = BranchSession.branchPath(context, "menuitems");
        String ordersPath = BranchSession.branchPath(context, "orders/" + userId);
        
        // Debug logging
        android.util.Log.d("ChatbotService", "Menu path: " + menuPath);
        android.util.Log.d("ChatbotService", "Orders path: " + ordersPath);
        android.util.Log.d("ChatbotService", "Current branch: " + BranchSession.getBranch(context));
        
        this.menuRef = FirebaseDatabase.getInstance().getReference(menuPath);
        this.ordersRef = FirebaseDatabase.getInstance().getReference(ordersPath);
    }
    
    public interface ChatbotCallback {
        void onResponse(String response);
        void onError(String error);
    }
    
    // Debug method to test database connection
    public void testDatabaseConnection(ChatbotCallback callback) {
        android.util.Log.d("ChatbotService", "Testing database connection...");
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentBranch = BranchSession.getBranch(context);
                String branchDisplayName = currentBranch.equals(BranchSession.BRANCH_COLOMBO) ? "Colombo" : "Galle";
                
                String response = "🔍 **Database Connection Test (" + branchDisplayName + " Branch):**\n\n" +
                        "✅ Connected to Firebase successfully!\n" +
                        "📍 Branch: " + branchDisplayName + "\n" +
                        "🗂️ Path: " + menuRef.toString() + "\n" +
                        "📊 Data exists: " + (snapshot.exists() ? "Yes" : "No") + "\n" +
                        "📈 Items count: " + snapshot.getChildrenCount() + "\n\n";
                
                if (snapshot.exists()) {
                    response += "📋 Sample data structure:\n";
                    int count = 0;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (count >= 3) break; // Show only first 3 items
                        response += "• " + child.getKey() + "\n";
                        count++;
                    }
                    if (snapshot.getChildrenCount() > 3) {
                        response += "... and " + (snapshot.getChildrenCount() - 3) + " more items\n";
                    }
                }
                
                callback.onResponse(response);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("❌ Database connection failed: " + error.getMessage());
            }
        });
    }
    
    public void processMessage(String message, ChatbotCallback callback) {
        String lowerMessage = message.toLowerCase();
        
        android.util.Log.d("ChatbotService", "Processing message: " + message);
        android.util.Log.d("ChatbotService", "Lower message: " + lowerMessage);
        
        if (lowerMessage.contains("menu") || lowerMessage.contains("items") || lowerMessage.contains("pizza") || lowerMessage.contains("food")) {
            android.util.Log.d("ChatbotService", "Triggering menu query");
            if (lowerMessage.contains("price") || lowerMessage.contains("cost") || lowerMessage.contains("how much")) {
                getMenuWithPrices(callback);
            } else if (lowerMessage.contains("category") || lowerMessage.contains("type")) {
                getMenuCategories(callback);
            } else {
                getMenuItems(callback);
            }
        } else if (lowerMessage.contains("order") || lowerMessage.contains("my orders") || lowerMessage.contains("status")) {
            android.util.Log.d("ChatbotService", "Triggering order query");
            if (lowerMessage.contains("current") || lowerMessage.contains("active")) {
                getCurrentOrders(callback);
            } else if (lowerMessage.contains("history") || lowerMessage.contains("past")) {
                getOrderHistory(callback);
            } else {
                getAllOrders(callback);
            }
        } else if (lowerMessage.contains("about") || lowerMessage.contains("pizza mania") || lowerMessage.contains("shop") || lowerMessage.contains("restaurant")) {
            android.util.Log.d("ChatbotService", "Triggering about response");
            getAboutResponse(callback);
        } else if (lowerMessage.contains("help") || lowerMessage.contains("how") || lowerMessage.contains("what")) {
            android.util.Log.d("ChatbotService", "Triggering help response");
            getHelpResponse(callback);
        } else if (lowerMessage.contains("delivery") || lowerMessage.contains("time") || lowerMessage.contains("when")) {
            android.util.Log.d("ChatbotService", "Triggering delivery info");
            getDeliveryInfo(callback);
        } else if (lowerMessage.contains("contact") || lowerMessage.contains("phone") || lowerMessage.contains("address")) {
            android.util.Log.d("ChatbotService", "Triggering contact info");
            getContactInfo(callback);
        } else {
            android.util.Log.d("ChatbotService", "Triggering general response");
            getGeneralResponse(message, callback);
        }
    }
    
    private void getMenuItems(ChatbotCallback callback) {
        android.util.Log.d("ChatbotService", "Getting menu items from: " + menuRef.toString());
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentBranch = BranchSession.getBranch(context);
                String branchDisplayName = currentBranch.equals(BranchSession.BRANCH_COLOMBO) ? "Colombo" : "Galle";
                
                android.util.Log.d("ChatbotService", "Snapshot exists: " + snapshot.exists());
                android.util.Log.d("ChatbotService", "Children count: " + snapshot.getChildrenCount());
                
                StringBuilder response = new StringBuilder("🍕 **Our Menu Items (").append(branchDisplayName).append(" Branch):**\n\n");
                
                if (snapshot.exists()) {
                    List<Pizza> items = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Pizza item = itemSnapshot.getValue(Pizza.class);
                        if (item != null && item.isInStock()) {
                            items.add(item);
                        }
                    }
                    
                    if (items.isEmpty()) {
                        response.append("Sorry, no items are currently available at the ").append(branchDisplayName).append(" branch.");
                    } else {
                        for (Pizza item : items) {
                            response.append("• ").append(item.getName()).append("\n");
                            response.append("  ").append(item.getDescription()).append("\n\n");
                        }
                    }
                } else {
                    response.append("Sorry, I couldn't find any menu items at the ").append(branchDisplayName).append(" branch at the moment.");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve the menu. Please try again later.");
            }
        });
    }
    
    private void getMenuWithPrices(ChatbotCallback callback) {
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentBranch = BranchSession.getBranch(context);
                String branchDisplayName = currentBranch.equals(BranchSession.BRANCH_COLOMBO) ? "Colombo" : "Galle";
                
                StringBuilder response = new StringBuilder("💰 **Menu with Prices (").append(branchDisplayName).append(" Branch):**\n\n");
                
                if (snapshot.exists()) {
                    List<Pizza> items = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Pizza item = itemSnapshot.getValue(Pizza.class);
                        if (item != null && item.isInStock()) {
                            items.add(item);
                        }
                    }
                    
                    if (items.isEmpty()) {
                        response.append("Sorry, no items are currently available at the ").append(branchDisplayName).append(" branch.");
                    } else {
                        for (Pizza item : items) {
                            response.append("🍕 **").append(item.getName()).append("**\n");
                            response.append("   ").append(item.getDescription()).append("\n");
                            response.append("   Small: $").append(String.format(Locale.US, "%.2f", item.small())).append("\n");
                            response.append("   Medium: $").append(String.format(Locale.US, "%.2f", item.medium())).append("\n");
                            response.append("   Large: $").append(String.format(Locale.US, "%.2f", item.large())).append("\n\n");
                        }
                    }
                } else {
                    response.append("Sorry, I couldn't find any menu items at the ").append(branchDisplayName).append(" branch at the moment.");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve the menu. Please try again later.");
            }
        });
    }
    
    private void getMenuCategories(ChatbotCallback callback) {
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentBranch = BranchSession.getBranch(context);
                String branchDisplayName = currentBranch.equals(BranchSession.BRANCH_COLOMBO) ? "Colombo" : "Galle";
                
                StringBuilder response = new StringBuilder("📂 **Menu Categories (").append(branchDisplayName).append(" Branch):**\n\n");
                
                if (snapshot.exists()) {
                    List<String> categories = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Pizza item = itemSnapshot.getValue(Pizza.class);
                        if (item != null && item.isInStock() && !categories.contains(item.getCategory())) {
                            categories.add(item.getCategory());
                        }
                    }
                    
                    if (categories.isEmpty()) {
                        response.append("Sorry, no categories are currently available at the ").append(branchDisplayName).append(" branch.");
                    } else {
                        for (String category : categories) {
                            response.append("• ").append(category).append("\n");
                        }
                    }
                } else {
                    response.append("Sorry, I couldn't find any menu categories at the ").append(branchDisplayName).append(" branch at the moment.");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve the menu categories. Please try again later.");
            }
        });
    }
    
    private void getAllOrders(ChatbotCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder response = new StringBuilder("📦 **Your Orders:**\n\n");
                
                if (snapshot.exists()) {
                    List<Order> orders = new ArrayList<>();
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null) {
                            orders.add(order);
                        }
                    }
                    
                    if (orders.isEmpty()) {
                        response.append("You don't have any orders yet. Start by browsing our menu!");
                    } else {
                        // Sort by creation date (newest first)
                        orders.sort((o1, o2) -> Long.compare(o2.createdAt, o1.createdAt));
                        
                        for (Order order : orders) {
                            response.append("📦 **Order #").append(order.orderId.substring(0, 8)).append("...**\n");
                            response.append("   Status: ").append(getStatusEmoji(order.status)).append(" ").append(order.status.toUpperCase()).append("\n");
                            response.append("   Amount: $").append(String.format(Locale.US, "%.2f", order.amount)).append("\n");
                            response.append("   Date: ").append(getFormattedDate(order.createdAt)).append("\n");
                            response.append("   Payment: ").append(order.paymentMethod).append("\n\n");
                        }
                    }
                } else {
                    response.append("You don't have any orders yet. Start by browsing our menu!");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve your orders. Please try again later.");
            }
        });
    }
    
    private void getCurrentOrders(ChatbotCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder response = new StringBuilder("🚚 **Your Current Orders:**\n\n");
                
                if (snapshot.exists()) {
                    List<Order> currentOrders = new ArrayList<>();
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && !order.status.equals("delivered") && !order.status.equals("cancelled")) {
                            currentOrders.add(order);
                        }
                    }
                    
                    if (currentOrders.isEmpty()) {
                        response.append("You don't have any active orders at the moment.");
                    } else {
                        for (Order order : currentOrders) {
                            response.append("📦 **Order #").append(order.orderId.substring(0, 8)).append("...**\n");
                            response.append("   Status: ").append(getStatusEmoji(order.status)).append(" ").append(order.status.toUpperCase()).append("\n");
                            response.append("   Amount: $").append(String.format(Locale.US, "%.2f", order.amount)).append("\n");
                            response.append("   Date: ").append(getFormattedDate(order.createdAt)).append("\n\n");
                        }
                    }
                } else {
                    response.append("You don't have any active orders at the moment.");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve your current orders. Please try again later.");
            }
        });
    }
    
    private void getOrderHistory(ChatbotCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder response = new StringBuilder("📚 **Your Order History:**\n\n");
                
                if (snapshot.exists()) {
                    List<Order> historyOrders = new ArrayList<>();
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null && (order.status.equals("delivered") || order.status.equals("cancelled"))) {
                            historyOrders.add(order);
                        }
                    }
                    
                    if (historyOrders.isEmpty()) {
                        response.append("You don't have any order history yet.");
                    } else {
                        // Sort by creation date (newest first)
                        historyOrders.sort((o1, o2) -> Long.compare(o2.createdAt, o1.createdAt));
                        
                        for (Order order : historyOrders) {
                            response.append("📦 **Order #").append(order.orderId.substring(0, 8)).append("...**\n");
                            response.append("   Status: ").append(getStatusEmoji(order.status)).append(" ").append(order.status.toUpperCase()).append("\n");
                            response.append("   Amount: $").append(String.format(Locale.US, "%.2f", order.amount)).append("\n");
                            response.append("   Date: ").append(getFormattedDate(order.createdAt)).append("\n\n");
                        }
                    }
                } else {
                    response.append("You don't have any order history yet.");
                }
                
                callback.onResponse(response.toString());
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Sorry, I couldn't retrieve your order history. Please try again later.");
            }
        });
    }
    
    public void getAboutResponse(ChatbotCallback callback) {
        android.util.Log.d("ChatbotService", "getAboutResponse called");
        
        String aboutMessage = "🍕 **Welcome to Pizza Mania!**\n\n" +
                "**About Our Restaurant:**\n\n" +
                "Pizza Mania is Sri Lanka's premier pizza destination, serving authentic Italian-style pizzas with a local twist. We've been delighting customers with our fresh, handcrafted pizzas since our establishment.\n\n" +
                "**🏪 Our Locations:**\n" +
                "• **Colombo Branch** - Heart of the capital city\n" +
                "• **Galle Branch** - Historic coastal city\n\n" +
                "**🍕 What Makes Us Special:**\n" +
                "• Fresh, locally sourced ingredients\n" +
                "• Traditional wood-fired oven cooking\n" +
                "• Customizable pizza options\n" +
                "• Fast and reliable delivery service\n" +
                "• Family-friendly atmosphere\n\n" +
                "**📋 Our Menu Features:**\n" +
                "• Classic Italian pizzas (Margherita, Pepperoni)\n" +
                "• Local favorites with Sri Lankan flavors\n" +
                "• Vegetarian and vegan options\n" +
                "• Fresh salads and appetizers\n" +
                "• Refreshing beverages\n\n" +
                "**⭐ Our Commitment:**\n" +
                "We're committed to providing the best pizza experience with quality ingredients, excellent service, and affordable prices. Every pizza is made with love and attention to detail!\n\n" +
                "**🕒 Operating Hours:**\n" +
                "• Monday - Friday: 10:00 AM - 11:00 PM\n" +
                "• Saturday - Sunday: 11:00 AM - 12:00 AM\n\n" +
                "Thank you for choosing Pizza Mania! 🍕✨";
        
        android.util.Log.d("ChatbotService", "About message prepared, calling callback");
        callback.onResponse(aboutMessage);
    }
    
    private void getHelpResponse(ChatbotCallback callback) {
        String helpMessage = "🤖 **I'm your Pizza Mania Assistant!**\n\n" +
                "Here's what I can help you with:\n\n" +
                "🍕 **Menu Information**\n" +
                "• Ask about our pizzas, prices, and ingredients\n" +
                "• Get menu categories and availability\n\n" +
                "📦 **Order Management**\n" +
                "• Check your current orders\n" +
                "• View order history\n" +
                "• Track order status\n\n" +
                "💳 **Payment & Checkout**\n" +
                "• Get help with payment methods\n" +
                "• Information about COD and card payments\n\n" +
                "🚚 **Delivery Information**\n" +
                "• Delivery times and areas\n" +
                "• Order tracking\n\n" +
                "📞 **Contact & Support**\n" +
                "• Get our contact information\n" +
                "• General support questions\n\n" +
                "Just ask me anything in natural language!";
        
        callback.onResponse(helpMessage);
    }
    
    private void getDeliveryInfo(ChatbotCallback callback) {
        String deliveryMessage = "🚚 **Delivery Information:**\n\n" +
                "⏰ **Delivery Times:**\n" +
                "• Monday - Friday: 11:00 AM - 10:00 PM\n" +
                "• Saturday - Sunday: 12:00 PM - 11:00 PM\n\n" +
                "📍 **Delivery Areas:**\n" +
                "• Within 5km radius of our store\n" +
                "• Free delivery on orders over $25\n" +
                "• $3 delivery fee for orders under $25\n\n" +
                "⏱️ **Estimated Delivery Time:**\n" +
                "• 30-45 minutes from order confirmation\n" +
                "• May vary during peak hours\n\n" +
                "📱 **Order Tracking:**\n" +
                "• Real-time updates on your order status\n" +
                "• SMS notifications for order updates";
        
        callback.onResponse(deliveryMessage);
    }
    
    private void getContactInfo(ChatbotCallback callback) {
        String contactMessage = "📞 **Contact Information:**\n\n" +
                "🏪 **Pizza Mania**\n" +
                "📍 123 Main Street, City, State 12345\n\n" +
                "📱 **Phone:** (555) 123-PIZZA\n" +
                "📧 **Email:** info@pizzamania.com\n\n" +
                "🕒 **Store Hours:**\n" +
                "• Monday - Friday: 10:00 AM - 11:00 PM\n" +
                "• Saturday - Sunday: 11:00 AM - 12:00 AM\n\n" +
                "🌐 **Online:**\n" +
                "• Website: www.pizzamania.com\n" +
                "• Social Media: @PizzaManiaOfficial\n\n" +
                "💬 **Need immediate help?**\n" +
                "Call us during store hours for the fastest response!";
        
        callback.onResponse(contactMessage);
    }
    
    private void getGeneralResponse(String message, ChatbotCallback callback) {
        String response = "I understand you're asking about: \"" + message + "\"\n\n" +
                "I can help you with:\n" +
                "• Menu items and prices\n" +
                "• Order status and history\n" +
                "• Delivery information\n" +
                "• Contact details\n" +
                "• General questions about Pizza Mania\n\n" +
                "Could you be more specific about what you'd like to know?";
        
        callback.onResponse(response);
    }
    
    private String getStatusEmoji(String status) {
        switch (status.toLowerCase()) {
            case "placed": return "📝";
            case "paid": return "💳";
            case "preparing": return "👨‍🍳";
            case "on_the_way": return "🚚";
            case "delivered": return "✅";
            case "cancelled": return "❌";
            default: return "📦";
        }
    }
    
    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
        return sdf.format(new Date(timestamp));
    }
}
