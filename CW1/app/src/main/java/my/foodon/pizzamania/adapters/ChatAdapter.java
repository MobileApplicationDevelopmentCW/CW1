package my.foodon.pizzamania.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    
    private List<ChatMessage> messages;
    
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (message.getType() == ChatMessage.TYPE_USER) {
            holder.layoutUserMessage.setVisibility(View.VISIBLE);
            holder.layoutBotMessage.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getMessage());
        } else {
            holder.layoutUserMessage.setVisibility(View.GONE);
            holder.layoutBotMessage.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(message.getMessage());
            
            if (message.isLoading()) {
                holder.progressBot.setVisibility(View.VISIBLE);
                holder.tvBotMessage.setText("Thinking...");
            } else {
                holder.progressBot.setVisibility(View.GONE);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateLastMessage(String message) {
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            lastMessage.setMessage(message);
            lastMessage.setLoading(false);
            notifyItemChanged(messages.size() - 1);
        }
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUserMessage, layoutBotMessage;
        TextView tvUserMessage, tvBotMessage;
        ProgressBar progressBot;
        
        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUserMessage = itemView.findViewById(R.id.layoutUserMessage);
            layoutBotMessage = itemView.findViewById(R.id.layoutBotMessage);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
            progressBot = itemView.findViewById(R.id.progressBot);
        }
    }
}
