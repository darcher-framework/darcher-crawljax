//package org.kristen.crawljax.plugins.grpc;
//
//import com.crawljax.browser.EmbeddedBrowser;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.grpc.stub.StreamObserver;
//import Common;
//import DAppTestDriverServiceGrpc;
//import DappTestService;
//import org.openqa.selenium.WebDriver;
//
//public class ControlMsgHandlerThread implements Runnable {
//    private final String INIT_CONTROL_MSG_ID = "0";
//    private String dappName;
//    private int instanceId;
//    private String SERVER_HOST = "localhost";
//    private int SERVER_PORT = 1234;
//    private ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
//            .usePlaintext()
//            .build();
//    private DAppTestDriverServiceGrpc.DAppTestDriverServiceBlockingStub blockingStub;
//    private DAppTestDriverServiceGrpc.DAppTestDriverServiceStub asyncStub;
////    private WebDriver driver;
//    private EmbeddedBrowser browser;
//
//    private StreamObserver<DappTestService.DAppDriverControlMsg> requestObserver;
//    private StreamObserver<DappTestService.DAppDriverControlMsg> responseObserver =
//            DAppTestDriverServiceGrpc.newStub(channel).dappDriverControl(new StreamObserver<DappTestService.DAppDriverControlMsg>() {
//                @Override
//                public void onNext(DappTestService.DAppDriverControlMsg dAppDriverControlMsg) {
//                    System.out.println("Receive from stream: " + dAppDriverControlMsg.getAllFields());
//                    handleControlMsg(dAppDriverControlMsg);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    System.out.println("Error from stream");
//                }
//
//                @Override
//                public void onCompleted() {
//                    System.out.println("Stream completed");
//                }
//            });
//
//    public ControlMsgHandlerThread(String host, int port, ManagedChannel channel,
//                                   DAppTestDriverServiceGrpc.DAppTestDriverServiceBlockingStub blockingStub,
//                                   DAppTestDriverServiceGrpc.DAppTestDriverServiceStub asyncStub,
//                                   String dappName, int instanceId) {
//        this.SERVER_HOST = host;
//        this.SERVER_PORT = port;
//        this.channel = channel;
//        this.blockingStub = blockingStub;
//        this.asyncStub = asyncStub;
//        this.dappName = dappName;
//        this.instanceId = instanceId;
//    }
//
//    @Override
//    public void run() {
//        setupConnection();
//        while (true) {
//            // pass
//        }
//    }
//
//    public void setBrowser(EmbeddedBrowser browser) {
//        this.browser = browser;
//    }
//
//    private void setupConnection() {
//        DappTestService.DAppDriverControlMsg dAppDriverControlMsg = DappTestService.DAppDriverControlMsg
//                .newBuilder()
//                .setRole(Common.Role.DAPP)
//                .setId(INIT_CONTROL_MSG_ID)
//                .setDappName(this.dappName)
//                .setInstanceId(Integer.toString(this.instanceId))
//                .setControlType(DappTestService.DAppDriverControlType.NilType)
//                .build();
//        this.requestObserver = asyncStub.dappDriverControl(responseObserver);
//        System.out.println("Stream connection have setup.");
//    }
//
//    private void handleControlMsg(DappTestService.DAppDriverControlMsg dAppDriverControlMsg) {
//        DappTestService.DAppDriverControlType controlType = dAppDriverControlMsg.getControlType();
//        switch (controlType) {
//            case Refresh:
//                WebDriver driver = this.browser.getWebDriver();
//                driver.navigate().refresh();
//                break;
//            case NilType:
//            case UNRECOGNIZED:
//                System.out.println("Unrecognized control type.");
//                break;
//            default:
//                break;
//        }
//    }
//}
