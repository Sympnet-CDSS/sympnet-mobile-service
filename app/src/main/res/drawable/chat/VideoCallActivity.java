package com.sympnet.ui.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import com.sympnet.R;
import com.sympnet.models.Conversation;
import com.sympnet.models.VideoCallSession;
import com.sympnet.network.WebSocketManager;
import org.webrtc.*;
import java.util.ArrayList;
import java.util.List;

public class VideoCallActivity extends AppCompatActivity implements WebSocketManager.ChatListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String EXTRA_CALL_ID = "call_id";
    private static final String EXTRA_CALLER_ID = "caller_id";
    private static final String EXTRA_CALLER_NAME = "caller_name";
    private static final String EXTRA_RECEIVER_ID = "receiver_id";
    private static final String EXTRA_RECEIVER_NAME = "receiver_name";
    private static final String EXTRA_IS_CALLER = "is_caller";
    private static final String EXTRA_CALL_TYPE = "call_type";

    private TextView tvStatus, tvCallerName;
    private ImageButton btnToggleMic, btnToggleCamera, btnEndCall, btnSwitchCamera;
    private MaterialCardView cardControls;
    
    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;
    
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private VideoCapturer videoCapturer;
    
    private String callId;
    private int callerId;
    private String callerName;
    private int receiverId;
    private String receiverName;
    private boolean isCaller;
    private String callType;
    private WebSocketManager webSocketManager;
    private boolean isMicMuted = false;
    private boolean isCameraMuted = false;

    public static void startAsCaller(AppCompatActivity activity, Conversation conversation) {
        Intent intent = new Intent(activity, VideoCallActivity.class);
        intent.putExtra(EXTRA_CALL_ID, "call_" + System.currentTimeMillis());
        intent.putExtra(EXTRA_RECEIVER_ID, conversation.getOtherUserId());
        intent.putExtra(EXTRA_RECEIVER_NAME, conversation.getOtherUserName());
        intent.putExtra(EXTRA_IS_CALLER, true);
        intent.putExtra(EXTRA_CALL_TYPE, "video");
        activity.startActivity(intent);
    }

    public static void startAsReceiver(AppCompatActivity activity, VideoCallSession call) {
        Intent intent = new Intent(activity, VideoCallActivity.class);
        intent.putExtra(EXTRA_CALL_ID, call.getCallId());
        intent.putExtra(EXTRA_CALLER_ID, call.getCallerId());
        intent.putExtra(EXTRA_CALLER_NAME, call.getCallerName());
        intent.putExtra(EXTRA_RECEIVER_ID, call.getReceiverId());
        intent.putExtra(EXTRA_IS_CALLER, false);
        intent.putExtra(EXTRA_CALL_TYPE, call.getCallType());
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // Get extras
        callId = getIntent().getStringExtra(EXTRA_CALL_ID);
        callerId = getIntent().getIntExtra(EXTRA_CALLER_ID, -1);
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        receiverId = getIntent().getIntExtra(EXTRA_RECEIVER_ID, -1);
        receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);
        isCaller = getIntent().getBooleanExtra(EXTRA_IS_CALLER, true);
        callType = getIntent().getStringExtra(EXTRA_CALL_TYPE);

        initViews();
        checkPermissions();
        initWebRTC();
        setupWebSocket();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvCallerName = findViewById(R.id.tvCallerName);
        btnToggleMic = findViewById(R.id.btnToggleMic);
        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnEndCall = findViewById(R.id.btnEndCall);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        cardControls = findViewById(R.id.cardControls);
        localRenderer = findViewById(R.id.localRenderer);
        remoteRenderer = findViewById(R.id.remoteRenderer);

        tvCallerName.setText(isCaller ? receiverName : callerName);

        btnToggleMic.setOnClickListener(v -> toggleMic());
        btnToggleCamera.setOnClickListener(v -> toggleCamera());
        btnEndCall.setOnClickListener(v -> endCall());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
    }

    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        };
        
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                missingPermissions.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        } else {
            startVideoCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startVideoCall();
            } else {
                Toast.makeText(this, "Permissions nécessaires pour l'appel", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initWebRTC() {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions());
        
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory();
        
        // Create video capturer
        videoCapturer = createVideoCapturer();
        
        // Video source
        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        localVideoTrack = peerConnectionFactory.createVideoTrack("video_track", videoSource);
        
        if (videoCapturer != null) {
            videoCapturer.initialize(new SurfaceTextureHelper("Thread", new Handler()), 
                this, videoSource.getCapturerObserver());
            videoCapturer.startCapture(1280, 720, 30);
        }
        
        // Audio source
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("audio_track", audioSource);
        
        // Renderers
        localRenderer.init(localRenderer.getContext(), null);
        localVideoTrack.addSink(localRenderer);
        
        remoteRenderer.init(remoteRenderer.getContext(), null);
        
        // Set initial visibility
        localRenderer.setZOrderMediaOverlay(true);
        localRenderer.setMirror(true);
    }

    private VideoCapturer createVideoCapturer() {
        return new Camera2Enumerator(this).createCapturer(
            Camera2Enumerator.getDeviceNames(this)[0],
            null,
            new CameraVideoCapturer.CameraEventsHandler() {
                @Override public void onCameraError(String error) {}
                @Override public void onCameraDisconnected() {}
                @Override public void onCameraFreezed(String error) {}
                @Override public void onCameraOpening(String info) {}
                @Override public void onFirstFrameAvailable() {}
                @Override public void onCameraClosed() {}
            }
        );
    }

    private void createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(
            List.of(new PeerConnection.IceServer("stun:stun.l.google.com:19302"))
        );
        
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                webSocketManager.sendIceCandidate(callId, candidate.sdp, candidate.sdpMLineIndex, candidate.sdpMid);
            }
            
            @Override
            public void onAddStream(MediaStream mediaStream) {
                if (mediaStream.videoTracks.size() > 0) {
                    mediaStream.videoTracks.get(0).addSink(remoteRenderer);
                }
            }
            
            @Override public void onSignalingChange(PeerConnection.SignalingState newState) {}
            @Override public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {}
            @Override public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {}
            @Override public void onIceCandidatesRemoved(IceCandidate[] candidates) {}
            @Override public void onRemoveStream(MediaStream mediaStream) {}
            @Override public void onDataChannel(DataChannel dataChannel) {}
            @Override public void onRenegotiationNeeded() {}
            @Override public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {}
        });
        
        // Add tracks
        peerConnection.addTrack(localVideoTrack);
        peerConnection.addTrack(localAudioTrack);
        
        if (isCaller) {
            createOffer();
        }
    }

    private void createOffer() {
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override public void onSetSuccess() {
                        webSocketManager.sendOffer(callId, sdp.description);
                    }
                    @Override public void onCreateSuccess(SessionDescription sdp) {}
                    @Override public void onSetFailure(String error) {}
                    @Override public void onCreateFailure(String error) {}
                }, sdp);
            }
            @Override public void onCreateFailure(String error) {}
            @Override public void onSetSuccess() {}
            @Override public void onSetFailure(String error) {}
        }, new MediaConstraints());
    }

    private void setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.setChatListener(this);
        
        if (isCaller) {
            webSocketManager.startCall(receiverId, callType);
            createPeerConnection();
        }
    }

    private void startVideoCall() {
        createPeerConnection();
        tvStatus.setText("Connexion en cours...");
    }

    private void toggleMic() {
        isMicMuted = !isMicMuted;
        localAudioTrack.setEnabled(!isMicMuted);
        btnToggleMic.setImageResource(isMicMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
    }

    private void toggleCamera() {
        isCameraMuted = !isCameraMuted;
        localVideoTrack.setEnabled(!isCameraMuted);
        btnToggleCamera.setImageResource(isCameraMuted ? R.drawable.ic_videocam_off : R.drawable.ic_videocam);
    }

    private void switchCamera() {
        if (videoCapturer instanceof CameraVideoCapturer) {
            ((CameraVideoCapturer) videoCapturer).switchCamera(null);
        }
    }

    private void endCall() {
        if (peerConnection != null) {
            peerConnection.close();
        }
        webSocketManager.endCall(callId);
        finish();
    }

    @Override
    public void onConnected() {}

    @Override
    public void onDisconnected() {}

    @Override
    public void onNewMessage(Message message) {}

    @Override
    public void onMessageDelivered(int messageId) {}

    @Override
    public void onMessageRead(int messageId) {}

    @Override
    public void onTyping(int userId, String userName, boolean isTyping) {}

    @Override
    public void onIncomingCall(VideoCallSession call) {}

    @Override
    public void onCallAccepted(String callId) {
        if (callId.equals(this.callId)) {
            runOnUiThread(() -> tvStatus.setText("Appel en cours..."));
        }
    }

    @Override
    public void onCallRejected(String callId) {
        if (callId.equals(this.callId)) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Appel refusé", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    @Override
    public void onCallEnded(String callId) {
        if (callId.equals(this.callId)) {
            runOnUiThread(() -> finish());
        }
    }

    @Override
    public void onOffer(String callId, String sdp) {
        if (callId.equals(this.callId) && !isCaller) {
            SessionDescription offer = new SessionDescription(SessionDescription.Type.OFFER, sdp);
            peerConnection.setRemoteDescription(new SdpObserver() {
                @Override public void onSetSuccess() {
                    createAnswer();
                }
                @Override public void onSetFailure(String error) {}
                @Override public void onCreateSuccess(SessionDescription sdp) {}
                @Override public void onCreateFailure(String error) {}
            }, offer);
        }
    }

    private void createAnswer() {
        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override public void onSetSuccess() {
                        webSocketManager.sendAnswer(callId, sdp.description);
                    }
                    @Override public void onCreateSuccess(SessionDescription sdp) {}
                    @Override public void onSetFailure(String error) {}
                    @Override public void onCreateFailure(String error) {}
                }, sdp);
            }
            @Override public void onCreateFailure(String error) {}
            @Override public void onSetSuccess() {}
            @Override public void onSetFailure(String error) {}
        }, new MediaConstraints());
    }

    @Override
    public void onAnswer(String callId, String sdp) {
        if (callId.equals(this.callId) && isCaller) {
            SessionDescription answer = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
            peerConnection.setRemoteDescription(new SdpObserver() {
                @Override public void onSetSuccess() {}
                @Override public void onSetFailure(String error) {}
                @Override public void onCreateSuccess(SessionDescription sdp) {}
                @Override public void onCreateFailure(String error) {}
            }, answer);
        }
    }

    @Override
    public void onIceCandidate(String callId, String candidate, int sdpMLineIndex, String sdpMid) {
        if (callId.equals(this.callId)) {
            peerConnection.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, candidate));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (peerConnection != null) {
            peerConnection.dispose();
        }
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
        }
    }
}