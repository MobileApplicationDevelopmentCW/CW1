package my.foodon.pizzamania.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import my.foodon.pizzamania.R;
import my.foodon.pizzamania.adapters.ChatAdapter;
import my.foodon.pizzamania.models.ChatMessage;
import my.foodon.pizzamania.services.ChatbotService;

public class ChatbotFragment extends Fragment {
    
    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ImageView btnClearChat;
    private Chip chipMenu, chipOrders, chipAbout;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    
    private ChatbotService chatbotService;
    private FirebaseAuth mAuth;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);
        
        initViews(view);
        setupFirebase();
        setupRecyclerView();
        setupClickListeners();
        
        // Add welcome message
        addWelcomeMessage();
        
        return view;
    }
    
    private void initViews(View view) {
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnClearChat = view.findViewById(R.id.btnClearChat);
        chipMenu = view.findViewById(R.id.chipMenu);
        chipOrders = view.findViewById(R.id.chipOrders);
        chipAbout = view.findViewById(R.id.chipAbout);
    }
    
    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize chatbot service with context for branch path
        String userId = mAuth.getCurrentUser() != null ? mAuth.getUid() : "anonymous";
        chatbotService = new ChatbotService(userId, requireContext());
    }
    
    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        
        btnClearChat.setOnClickListener(v -> clearChat());
        
        chipMenu.setOnClickListener(v -> {
            etMessage.setText("What items are on the menu?");
            sendMessage();
        });
        
        chipOrders.setOnClickListener(v -> {
            etMessage.setText("Show me my orders");
            sendMessage();
        });
        
        chipAbout.setOnClickListener(v -> {
            // Add user message
            ChatMessage userMessage = new ChatMessage("Tell me about Pizza Mania", ChatMessage.TYPE_USER);
            chatAdapter.addMessage(userMessage);
            
            // Add loading message
            ChatMessage loadingMessage = new ChatMessage("", ChatMessage.TYPE_BOT, true);
            chatAdapter.addMessage(loadingMessage);
            
            // Directly call about response
            chatbotService.getAboutResponse(new ChatbotService.ChatbotCallback() {
                @Override
                public void onResponse(String response) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(response);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(error);
                        });
                    }
                }
            });
        });
    }
    
    private void addWelcomeMessage() {
        String welcomeMessage = "Hi! I'm your Pizza Mania assistant. I can help you with:\n\n" +
                "🍕 Menu items and prices\n" +
                "📦 Your order status\n" +
                "🏪 About our restaurant\n" +
                "❓ General questions\n\n" +
                "What would you like to know?";
        
        ChatMessage welcome = new ChatMessage(welcomeMessage, ChatMessage.TYPE_BOT);
        chatAdapter.addMessage(welcome);
    }
    
    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        
        // Add user message
        ChatMessage userMessage = new ChatMessage(message, ChatMessage.TYPE_USER);
        chatAdapter.addMessage(userMessage);
        
        // Clear input
        etMessage.setText("");
        
        // Add loading message
        ChatMessage loadingMessage = new ChatMessage("", ChatMessage.TYPE_BOT, true);
        chatAdapter.addMessage(loadingMessage);
        
        // Process the message
        processUserMessage(message);
    }
    
    private void processUserMessage(String message) {
        String lowerMessage = message.toLowerCase();
        
        // Handle test database command (for debugging)
        if (lowerMessage.contains("test database") || lowerMessage.contains("debug")) {
            chatbotService.testDatabaseConnection(new ChatbotService.ChatbotCallback() {
                @Override
                public void onResponse(String response) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(response);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(error);
                        });
                    }
                }
            });
        } else {
            // Handle normal messages
            chatbotService.processMessage(message, new ChatbotService.ChatbotCallback() {
                @Override
                public void onResponse(String response) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(response);
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            chatAdapter.updateLastMessage(error);
                        });
                    }
                }
            });
        }
    }
    
    
    private void clearChat() {
        chatAdapter.clearMessages();
        addWelcomeMessage();
    }
}
