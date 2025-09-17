package my.foodon.pizzamania.models;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    
    private String message;
    private int type;
    private long timestamp;
    private boolean isLoading;
    
    public ChatMessage() {}
    
    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
    }
    
    public ChatMessage(String message, int type, boolean isLoading) {
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = isLoading;
    }
    
    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { this.isLoading = loading; }
}
