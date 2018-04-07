package socket.io.com.chat.activity;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import socket.io.com.chat.adapter.MessageAdapter;
import socket.io.com.chat.model.Message;
import socket.io.com.chat.R;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "ChatActiviry";
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://192.168.0.163:4000");
        } catch (URISyntaxException e) {
        }
    }

    private List<Message> listMessages = new ArrayList<Message>();
    private ImageButton btnSend;
    private EditText edtMessage;
    private RecyclerView.Adapter messagesAdapter;
    private RecyclerView recyclerMessagesView;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mSocket.connect();
        mSocket.on(getString(R.string.new_message), onNewMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        recyclerMessagesView = (RecyclerView) findViewById(R.id.listMessages);
        recyclerMessagesView.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessagesView.setAdapter(messagesAdapter);

        messagesAdapter = new MessageAdapter(this, listMessages);
        recyclerMessagesView.setAdapter(messagesAdapter);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtMessage.getText() + "")) {
                    return;
                }
                if (!mSocket.connected()) {
                    snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_connect), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                edtMessage.setText("");
                mSocket.emit(getString(R.string.new_message), edtMessage.getText() + "");
            }
        });
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString(getString(R.string.user_name));
                        message = data.getString(getString(R.string.message));
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addMessage(username, message);
                }
            });
        }
    };

    private void addMessage(String username, String message) {
        listMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        messagesAdapter.notifyItemInserted(listMessages.size() - 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(getString(R.string.new_message), onNewMessage);
    }

}
