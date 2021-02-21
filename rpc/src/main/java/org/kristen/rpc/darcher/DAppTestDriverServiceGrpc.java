package org.kristen.rpc.darcher;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.30.2)",
    comments = "Source: dapp_test_service.proto")
public final class DAppTestDriverServiceGrpc {

  private DAppTestDriverServiceGrpc() {}

  public static final String SERVICE_NAME = "darcher.DAppTestDriverService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestStartMsg,
      com.google.protobuf.Empty> getNotifyTestStartMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "notifyTestStart",
      requestType = org.kristen.rpc.darcher.DappTestService.TestStartMsg.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestStartMsg,
      com.google.protobuf.Empty> getNotifyTestStartMethod() {
    io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestStartMsg, com.google.protobuf.Empty> getNotifyTestStartMethod;
    if ((getNotifyTestStartMethod = DAppTestDriverServiceGrpc.getNotifyTestStartMethod) == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        if ((getNotifyTestStartMethod = DAppTestDriverServiceGrpc.getNotifyTestStartMethod) == null) {
          DAppTestDriverServiceGrpc.getNotifyTestStartMethod = getNotifyTestStartMethod =
              io.grpc.MethodDescriptor.<org.kristen.rpc.darcher.DappTestService.TestStartMsg, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "notifyTestStart"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.TestStartMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DAppTestDriverServiceMethodDescriptorSupplier("notifyTestStart"))
              .build();
        }
      }
    }
    return getNotifyTestStartMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestEndMsg,
      com.google.protobuf.Empty> getNotifyTestEndMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "notifyTestEnd",
      requestType = org.kristen.rpc.darcher.DappTestService.TestEndMsg.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestEndMsg,
      com.google.protobuf.Empty> getNotifyTestEndMethod() {
    io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TestEndMsg, com.google.protobuf.Empty> getNotifyTestEndMethod;
    if ((getNotifyTestEndMethod = DAppTestDriverServiceGrpc.getNotifyTestEndMethod) == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        if ((getNotifyTestEndMethod = DAppTestDriverServiceGrpc.getNotifyTestEndMethod) == null) {
          DAppTestDriverServiceGrpc.getNotifyTestEndMethod = getNotifyTestEndMethod =
              io.grpc.MethodDescriptor.<org.kristen.rpc.darcher.DappTestService.TestEndMsg, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "notifyTestEnd"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.TestEndMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DAppTestDriverServiceMethodDescriptorSupplier("notifyTestEnd"))
              .build();
        }
      }
    }
    return getNotifyTestEndMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TxMsg,
      com.google.protobuf.Empty> getWaitForTxProcessMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "waitForTxProcess",
      requestType = org.kristen.rpc.darcher.DappTestService.TxMsg.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TxMsg,
      com.google.protobuf.Empty> getWaitForTxProcessMethod() {
    io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.TxMsg, com.google.protobuf.Empty> getWaitForTxProcessMethod;
    if ((getWaitForTxProcessMethod = DAppTestDriverServiceGrpc.getWaitForTxProcessMethod) == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        if ((getWaitForTxProcessMethod = DAppTestDriverServiceGrpc.getWaitForTxProcessMethod) == null) {
          DAppTestDriverServiceGrpc.getWaitForTxProcessMethod = getWaitForTxProcessMethod =
              io.grpc.MethodDescriptor.<org.kristen.rpc.darcher.DappTestService.TxMsg, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "waitForTxProcess"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.TxMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DAppTestDriverServiceMethodDescriptorSupplier("waitForTxProcess"))
              .build();
        }
      }
    }
    return getWaitForTxProcessMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg,
      org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> getDappDriverControlMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "dappDriverControl",
      requestType = org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg.class,
      responseType = org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg,
      org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> getDappDriverControlMethod() {
    io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg, org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> getDappDriverControlMethod;
    if ((getDappDriverControlMethod = DAppTestDriverServiceGrpc.getDappDriverControlMethod) == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        if ((getDappDriverControlMethod = DAppTestDriverServiceGrpc.getDappDriverControlMethod) == null) {
          DAppTestDriverServiceGrpc.getDappDriverControlMethod = getDappDriverControlMethod =
              io.grpc.MethodDescriptor.<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg, org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "dappDriverControl"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg.getDefaultInstance()))
              .setSchemaDescriptor(new DAppTestDriverServiceMethodDescriptorSupplier("dappDriverControl"))
              .build();
        }
      }
    }
    return getDappDriverControlMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg,
      com.google.protobuf.Empty> getNotifyConsoleErrorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "notifyConsoleError",
      requestType = org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg,
      com.google.protobuf.Empty> getNotifyConsoleErrorMethod() {
    io.grpc.MethodDescriptor<org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg, com.google.protobuf.Empty> getNotifyConsoleErrorMethod;
    if ((getNotifyConsoleErrorMethod = DAppTestDriverServiceGrpc.getNotifyConsoleErrorMethod) == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        if ((getNotifyConsoleErrorMethod = DAppTestDriverServiceGrpc.getNotifyConsoleErrorMethod) == null) {
          DAppTestDriverServiceGrpc.getNotifyConsoleErrorMethod = getNotifyConsoleErrorMethod =
              io.grpc.MethodDescriptor.<org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "notifyConsoleError"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DAppTestDriverServiceMethodDescriptorSupplier("notifyConsoleError"))
              .build();
        }
      }
    }
    return getNotifyConsoleErrorMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DAppTestDriverServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceStub>() {
        @java.lang.Override
        public DAppTestDriverServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DAppTestDriverServiceStub(channel, callOptions);
        }
      };
    return DAppTestDriverServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DAppTestDriverServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceBlockingStub>() {
        @java.lang.Override
        public DAppTestDriverServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DAppTestDriverServiceBlockingStub(channel, callOptions);
        }
      };
    return DAppTestDriverServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DAppTestDriverServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DAppTestDriverServiceFutureStub>() {
        @java.lang.Override
        public DAppTestDriverServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DAppTestDriverServiceFutureStub(channel, callOptions);
        }
      };
    return DAppTestDriverServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class DAppTestDriverServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     **
     *DApp driver should call notifyTestStart() rpc once when test starts
     * </pre>
     */
    public void notifyTestStart(org.kristen.rpc.darcher.DappTestService.TestStartMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyTestStartMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestEnd() rpc once when test ends
     * </pre>
     */
    public void notifyTestEnd(org.kristen.rpc.darcher.DappTestService.TestEndMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyTestEndMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     *Each time DApp driver performs a `send transaction` operation, it should call waitForTxProcess() rpc immediately after transaction is send.
     *This rpc call may block for arbitrary amount of time. DApp driver must wait for this rpc call to return.
     * </pre>
     */
    public void waitForTxProcess(org.kristen.rpc.darcher.DappTestService.TxMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getWaitForTxProcessMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     *Reverse rpc to let darcher control dapp driver.
     *Reverse rpc is implemented with bidirectional stream grpc, in order to make it possible to let server send rpc call to client.
     *Client should first send an initial DAppDriverControlMsg to server and maintain the input/output stream and act as a logical rpc server.
     *During the connection, server may send a DAppDriverControlMsg as request to client and client should respond with the same (role, id, dapp_name, instance_id)
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> dappDriverControl(
        io.grpc.stub.StreamObserver<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> responseObserver) {
      return asyncUnimplementedStreamingCall(getDappDriverControlMethod(), responseObserver);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyConsoleError when there is an error in dapp console
     * </pre>
     */
    public void notifyConsoleError(org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getNotifyConsoleErrorMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getNotifyTestStartMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.kristen.rpc.darcher.DappTestService.TestStartMsg,
                com.google.protobuf.Empty>(
                  this, METHODID_NOTIFY_TEST_START)))
          .addMethod(
            getNotifyTestEndMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.kristen.rpc.darcher.DappTestService.TestEndMsg,
                com.google.protobuf.Empty>(
                  this, METHODID_NOTIFY_TEST_END)))
          .addMethod(
            getWaitForTxProcessMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.kristen.rpc.darcher.DappTestService.TxMsg,
                com.google.protobuf.Empty>(
                  this, METHODID_WAIT_FOR_TX_PROCESS)))
          .addMethod(
            getDappDriverControlMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg,
                org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg>(
                  this, METHODID_DAPP_DRIVER_CONTROL)))
          .addMethod(
            getNotifyConsoleErrorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg,
                com.google.protobuf.Empty>(
                  this, METHODID_NOTIFY_CONSOLE_ERROR)))
          .build();
    }
  }

  /**
   */
  public static final class DAppTestDriverServiceStub extends io.grpc.stub.AbstractAsyncStub<DAppTestDriverServiceStub> {
    private DAppTestDriverServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DAppTestDriverServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DAppTestDriverServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestStart() rpc once when test starts
     * </pre>
     */
    public void notifyTestStart(org.kristen.rpc.darcher.DappTestService.TestStartMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyTestStartMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestEnd() rpc once when test ends
     * </pre>
     */
    public void notifyTestEnd(org.kristen.rpc.darcher.DappTestService.TestEndMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyTestEndMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     *Each time DApp driver performs a `send transaction` operation, it should call waitForTxProcess() rpc immediately after transaction is send.
     *This rpc call may block for arbitrary amount of time. DApp driver must wait for this rpc call to return.
     * </pre>
     */
    public void waitForTxProcess(org.kristen.rpc.darcher.DappTestService.TxMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getWaitForTxProcessMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     **
     *Reverse rpc to let darcher control dapp driver.
     *Reverse rpc is implemented with bidirectional stream grpc, in order to make it possible to let server send rpc call to client.
     *Client should first send an initial DAppDriverControlMsg to server and maintain the input/output stream and act as a logical rpc server.
     *During the connection, server may send a DAppDriverControlMsg as request to client and client should respond with the same (role, id, dapp_name, instance_id)
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> dappDriverControl(
        io.grpc.stub.StreamObserver<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getDappDriverControlMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyConsoleError when there is an error in dapp console
     * </pre>
     */
    public void notifyConsoleError(org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getNotifyConsoleErrorMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DAppTestDriverServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<DAppTestDriverServiceBlockingStub> {
    private DAppTestDriverServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DAppTestDriverServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DAppTestDriverServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestStart() rpc once when test starts
     * </pre>
     */
    public com.google.protobuf.Empty notifyTestStart(org.kristen.rpc.darcher.DappTestService.TestStartMsg request) {
      return blockingUnaryCall(
          getChannel(), getNotifyTestStartMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestEnd() rpc once when test ends
     * </pre>
     */
    public com.google.protobuf.Empty notifyTestEnd(org.kristen.rpc.darcher.DappTestService.TestEndMsg request) {
      return blockingUnaryCall(
          getChannel(), getNotifyTestEndMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     *Each time DApp driver performs a `send transaction` operation, it should call waitForTxProcess() rpc immediately after transaction is send.
     *This rpc call may block for arbitrary amount of time. DApp driver must wait for this rpc call to return.
     * </pre>
     */
    public com.google.protobuf.Empty waitForTxProcess(org.kristen.rpc.darcher.DappTestService.TxMsg request) {
      return blockingUnaryCall(
          getChannel(), getWaitForTxProcessMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyConsoleError when there is an error in dapp console
     * </pre>
     */
    public com.google.protobuf.Empty notifyConsoleError(org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg request) {
      return blockingUnaryCall(
          getChannel(), getNotifyConsoleErrorMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DAppTestDriverServiceFutureStub extends io.grpc.stub.AbstractFutureStub<DAppTestDriverServiceFutureStub> {
    private DAppTestDriverServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DAppTestDriverServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DAppTestDriverServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestStart() rpc once when test starts
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> notifyTestStart(
        org.kristen.rpc.darcher.DappTestService.TestStartMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyTestStartMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyTestEnd() rpc once when test ends
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> notifyTestEnd(
        org.kristen.rpc.darcher.DappTestService.TestEndMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyTestEndMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     *Each time DApp driver performs a `send transaction` operation, it should call waitForTxProcess() rpc immediately after transaction is send.
     *This rpc call may block for arbitrary amount of time. DApp driver must wait for this rpc call to return.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> waitForTxProcess(
        org.kristen.rpc.darcher.DappTestService.TxMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getWaitForTxProcessMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     **
     *DApp driver should call notifyConsoleError when there is an error in dapp console
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> notifyConsoleError(
        org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getNotifyConsoleErrorMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_NOTIFY_TEST_START = 0;
  private static final int METHODID_NOTIFY_TEST_END = 1;
  private static final int METHODID_WAIT_FOR_TX_PROCESS = 2;
  private static final int METHODID_NOTIFY_CONSOLE_ERROR = 3;
  private static final int METHODID_DAPP_DRIVER_CONTROL = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DAppTestDriverServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DAppTestDriverServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_NOTIFY_TEST_START:
          serviceImpl.notifyTestStart((org.kristen.rpc.darcher.DappTestService.TestStartMsg) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_NOTIFY_TEST_END:
          serviceImpl.notifyTestEnd((org.kristen.rpc.darcher.DappTestService.TestEndMsg) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_WAIT_FOR_TX_PROCESS:
          serviceImpl.waitForTxProcess((org.kristen.rpc.darcher.DappTestService.TxMsg) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_NOTIFY_CONSOLE_ERROR:
          serviceImpl.notifyConsoleError((org.kristen.rpc.darcher.DappTestService.ConsoleErrorMsg) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DAPP_DRIVER_CONTROL:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.dappDriverControl(
              (io.grpc.stub.StreamObserver<org.kristen.rpc.darcher.DappTestService.DAppDriverControlMsg>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DAppTestDriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DAppTestDriverServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.kristen.rpc.darcher.DappTestService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DAppTestDriverService");
    }
  }

  private static final class DAppTestDriverServiceFileDescriptorSupplier
      extends DAppTestDriverServiceBaseDescriptorSupplier {
    DAppTestDriverServiceFileDescriptorSupplier() {}
  }

  private static final class DAppTestDriverServiceMethodDescriptorSupplier
      extends DAppTestDriverServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DAppTestDriverServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DAppTestDriverServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DAppTestDriverServiceFileDescriptorSupplier())
              .addMethod(getNotifyTestStartMethod())
              .addMethod(getNotifyTestEndMethod())
              .addMethod(getWaitForTxProcessMethod())
              .addMethod(getDappDriverControlMethod())
              .addMethod(getNotifyConsoleErrorMethod())
              .build();
        }
      }
    }
    return result;
  }
}
