package com.example.user.socket;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URISyntaxException;

import static android.content.Context.*;

public class MainActivity extends AppCompatActivity {
    private Socket mSocket; //소켓 연결
    {
        try {
            mSocket = IO.socket("http://220.230.119.61:3000"); // 소켓 서버 주소
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }



    private TextView mFlag; //실험용 플래그
    private TextView mTextView; // 메세지 수신확인 텍스트 뷰
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFlag = (TextView)findViewById(R.id.flag);
        mTextView = (TextView)findViewById(R.id.text);

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE); // 내 ip 주소 받아오는부분
        DhcpInfo dhcpInfo = wm.getDhcpInfo() ;
        int serverIp = dhcpInfo.gateway;

        String ipAddress = String.format(
                "%d.%d.%d.%d",
                (serverIp & 0xff),
                (serverIp >> 8 & 0xff),
                (serverIp >> 16 & 0xff),
                (serverIp >> 24 & 0xff));

        mSocket.emit("join",ipAddress); // 소켓 서버에 조인 메세지 보내기
        mSocket.on("msg", onNewMessage); // 소켓 서버에서 메세지 수신
        mSocket.connect(); // 소켓 연결
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() { //소켓 리스너 설정
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    Log.d("data", data.toString());
                    try {
                        username = data.getString("nickname");
                        message = data.getString("msg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    showMessage(username, message);
                }
            });

        }
    };

    private void showMessage(String username, String message){ //메세지 표시 : 이부분을 가지고 장난치면됨
        mTextView.setText("IP: "+ username + "msg : " + message);
        if(message.equals("start"))
            mFlag.setText("start");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }

}
