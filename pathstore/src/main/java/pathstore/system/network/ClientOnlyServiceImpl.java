package pathstore.system.network;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import pathstore.client.LocalNodeInfo;
import pathstore.grpc.ClientOnlyServiceGrpc;
import pathstore.grpc.pathStoreProto;
import pathstore.sessions.SessionToken;

/**
 * This Service impl is specifically for endpoints that can be accessed only by a client to a server
 */
public class ClientOnlyServiceImpl extends ClientOnlyServiceGrpc.ClientOnlyServiceImplBase {
  /** Network impl which has logic for each endpoint */
  private final NetworkImpl network = NetworkImpl.getInstance();

  /**
   * Validate session from client to local node
   *
   * @param request request sent
   * @param responseObserver way to respond
   * @see NetworkImpl#validateSession(SessionToken)
   */
  @Override
  public void validateSession(
      final pathStoreProto.ValidateSessionRequest request,
      final StreamObserver<pathStoreProto.ValidateSessionResponse> responseObserver) {

    SessionToken sessionToken = SessionToken.fromGRPCSessionTokenObject(request.getSessionToken());

    boolean response = this.network.validateSession(sessionToken);

    responseObserver.onNext(
        pathStoreProto.ValidateSessionResponse.newBuilder().setResponse(response).build());
    responseObserver.onCompleted();
  }

  /**
   * Get local node id for client side
   *
   * @param request request sent
   * @param responseObserver way to respond
   * @see NetworkImpl#getLocalNodeInfo()
   */
  @Override
  public void getLocalNodeInfo(
      final Empty request,
      final StreamObserver<pathStoreProto.GetLocalNodeResponse> responseObserver) {
    LocalNodeInfo localNodeInfo = this.network.getLocalNodeInfo();

    responseObserver.onNext(
        pathStoreProto
            .GetLocalNodeResponse
            .newBuilder()
            .setInfoPayload(localNodeInfo.toGRPCLocalNodeInfoObject())
            .build());
    responseObserver.onCompleted();
  }

  /**
   * Get application lease information
   *
   * @param request request sent
   * @param responseObserver way to respond
   * @see NetworkImpl#getApplicationLease(String)
   */
  @Override
  public void getApplicationLeaseInformation(
      pathStoreProto.GetApplicationLeaseRequest request,
      StreamObserver<pathStoreProto.GetApplicationLeaseResponse> responseObserver) {
    int clt = this.network.getApplicationLease(request.getApplicationName());

    responseObserver.onNext(
        pathStoreProto.GetApplicationLeaseResponse.newBuilder().setClientLeaseTime(clt).build());
    responseObserver.onCompleted();
  }
}
